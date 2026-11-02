package com.careerforge.controller;
import jakarta.persistence.EntityManager;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.*;
import com.careerforge.repository.UserRepository;
import com.careerforge.service.AttemptLimiter;
import java.time.Instant;
import java.util.*;
@RestController
@RequestMapping("/api/account")
public class AccountController {
 private final EntityManager em;
 private final UserRepository users;
 private final PasswordEncoder passwords;
 private final AttemptLimiter limiter;
 public AccountController(EntityManager em, UserRepository users, PasswordEncoder passwords, AttemptLimiter limiter) {
  this.em=em; this.users=users; this.passwords=passwords; this.limiter=limiter;
 }
 // The predicates are fixed server-side, never supplied by a client.
 private static final Map<String,String> OWNED = Map.ofEntries(
  Map.entry("Skill","e.user.id"), Map.entry("Resume","e.user.id"), Map.entry("Profile","e.user.id"),
  Map.entry("CareerGoal","e.user.id"), Map.entry("JobApplication","e.user.id"),
  Map.entry("CareerJourney","e.userId"), Map.entry("LearningStep","e.userId"), Map.entry("ReadinessSnapshot","e.userId"),
  Map.entry("StudyLog","e.userId"), Map.entry("InterviewSession","e.userId"), Map.entry("JobMatchAnalysis","e.userId"));
 private Long id(Authentication auth) {
  if (auth == null || !(auth.getPrincipal() instanceof Long value)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Please sign in again");
  return value;
 }
 @GetMapping("/export")
 @Transactional(readOnly=true)
 public ResponseEntity<Map<String,Object>> export(Authentication auth) {
  Long userId=id(auth);
  var user=users.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
  var data=new LinkedHashMap<String,Object>();
  data.put("exportedAt",Instant.now());
  data.put("account",Map.of("id",userId,"name",user.getName(),"email",user.getEmail()));
  OWNED.entrySet().stream().filter(entry -> !entry.getKey().equals("InterviewSession")).sorted(Map.Entry.comparingByKey()).forEach(entry ->
   data.put(entry.getKey(),em.createQuery("select e from " + entry.getKey() + " e where " + entry.getValue() + " = :id").setParameter("id",userId).getResultList()));
  var sessions=em.createQuery("select e from InterviewSession e where e.userId = :id", com.careerforge.interview.InterviewSession.class).setParameter("id",userId).getResultList();
  data.put("InterviewSession", sessions.stream().map(session -> Map.of("id",session.id,"role",session.role,"status",session.status,"answers",session.answersJson,"startedAt",session.startedAt)).toList());
  return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(data);
 }
 public record DeleteRequest(String password, String confirmation) {}
 @DeleteMapping
 @Transactional
 public ResponseEntity<Void> delete(Authentication auth, @RequestBody DeleteRequest request) {
  Long userId=id(auth);
  limiter.check("delete:" + userId,5);
  if (request == null || !"DELETE".equals(request.confirmation()) || request.password() == null)
   throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Enter your current password and type DELETE to confirm.");
  var user=users.lockById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
  if (!passwords.matches(request.password(),user.getPassword())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"The current password is incorrect.");
  // Child records first; unrelated job catalogue records are retained.
  for (var entry:OWNED.entrySet()) em.createQuery("delete from " + entry.getKey() + " e where " + entry.getValue() + " = :id").setParameter("id",userId).executeUpdate();
  em.createQuery("delete from PasswordRecovery e where e.userId = :id").setParameter("id",userId).executeUpdate();
  users.delete(user); users.flush();
  return ResponseEntity.noContent().build();
 }
}
