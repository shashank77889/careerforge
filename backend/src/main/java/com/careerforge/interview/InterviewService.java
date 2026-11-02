package com.careerforge.interview;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.json.JsonMapper;
import java.time.Instant;
import java.util.*;

@Service
public class InterviewService {
    private final InterviewRepository repository;
    private final com.careerforge.repository.UserRepository users;
    private final InterviewQuestions bank;
    private final McqBank mcq;
    private final InterviewPersonalizer personalizer;
    private final JsonMapper json = JsonMapper.builder().build();
    public InterviewService(InterviewRepository repository, InterviewQuestions bank, McqBank mcq, com.careerforge.repository.UserRepository users, InterviewPersonalizer personalizer) { this.personalizer=personalizer; this.users=users; this.repository=repository;this.bank=bank;this.mcq=mcq; }
    public record Session(Long id,String role,String status,int currentQuestion,List<InterviewQuestions.Question> questions,List<String> answers,Instant startedAt,Instant completedAt,Long version,Integer score) {}
    public record Summary(Long id,String role,String status,int currentQuestion,int answered,int total,Instant startedAt) {}
    private Session view(InterviewSession entity) {
        var questions = Arrays.asList(json.readValue(entity.questionsJson,InterviewQuestions.Question[].class));
        var answers = Arrays.asList(json.readValue(entity.answersJson,String[].class));
        Integer score = null;
        if (questions.stream().anyMatch(q -> q.options()!=null && !q.options().isEmpty())) {
            if(entity.status.equals("COMPLETED")) {
                score=0;
                for(int i=0;i<questions.size();i++) if(String.valueOf(questions.get(i).correctIndex()).equals(answers.get(i))) score++;
            } else {
                questions = questions.stream().map(q -> new InterviewQuestions.Question(q.topic(),q.prompt(),List.of(),q.options(),null,null)).toList();
            }
        } else if(!entity.status.equals("COMPLETED")) {
            questions = questions.stream().map(q -> new InterviewQuestions.Question(q.topic(),q.prompt(),List.of())).toList();
        }
        return new Session(entity.id,entity.role,entity.status,entity.currentQuestion,questions,answers,entity.startedAt,entity.completedAt,entity.version,score);
    }
    private InterviewSession own(Long userId,Long id) {
        return repository.findById(id).filter(session -> session.userId.equals(userId)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Interview not found"));
    }
    public List<Summary> list(Long userId) {
        return repository.findTop30ByUserIdOrderByStartedAtDesc(userId).stream().map(entity -> { var session=view(entity); return new Summary(session.id(),session.role(),session.status(),session.currentQuestion(),(int)session.answers().stream().filter(answer -> !answer.isBlank()).count(),session.questions().size(),session.startedAt()); }).toList();
    }
    public Session get(Long userId,Long id) { return view(own(userId,id)); }
    @Transactional public Session start(Long userId,String role) {
        return start(userId, role, 10);
    }
    @Transactional public Session start(Long userId,String role,Integer questionCount) {
        users.lockById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Please sign in again"));
        var recentlyAsked = new HashSet<String>();
        repository.findTop30ByUserIdOrderByStartedAtDesc(userId).stream()
            .filter(previous -> previous.role.equals(role)).limit(3)
            .forEach(previous -> view(previous).questions().forEach(question -> recentlyAsked.add(question.prompt())));
        var questions = Objects.equals(questionCount,30) ? mcq.forRole(role) : bank.forRole(role, questionCount == null ? 10 : questionCount, recentlyAsked);
        if (!Objects.equals(questionCount,30)) questions = personalizer.personalize(userId,role,questions);
        var entity = new InterviewSession();entity.userId=userId;entity.role=role;
        entity.questionsJson=json.writeValueAsString(questions);
        entity.answersJson=json.writeValueAsString(Collections.nCopies(questions.size(),""));
        return view(repository.saveAndFlush(entity));
    }
    @Transactional public Session startGenerated(Long userId,String role,List<InterviewQuestions.Question> questions) {
        users.lockById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Please sign in again"));
        var entity=new InterviewSession();entity.userId=userId;entity.role=role;
        entity.questionsJson=json.writeValueAsString(questions);
        entity.answersJson=json.writeValueAsString(Collections.nCopies(questions.size(),""));
        return view(repository.saveAndFlush(entity));
    }
    @Transactional public Session save(Long userId,Long id,int index,String answer,Long version) {
        users.lockById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Please sign in again"));
        var entity=own(userId,id);
        if (!Objects.equals(entity.version,version)) throw new ResponseStatusException(HttpStatus.CONFLICT,"This interview changed in another tab. Reload the saved session before continuing; your draft is still available in this browser.");
        if (!entity.status.equals("IN_PROGRESS")) throw new IllegalArgumentException("This interview is complete. Start another session to practice again.");
        var session=view(entity);
        if (index < 0 || index >= session.questions().size() || answer == null || answer.isBlank() || answer.length()>5000) throw new IllegalArgumentException("Write an answer of 1–5,000 characters");
        var question=session.questions().get(index);
        if(question.options()!=null && !question.options().isEmpty() && !answer.matches("[0-3]")) throw new IllegalArgumentException("Choose one of the four options");
        var answers=new ArrayList<>(session.answers());answers.set(index,answer.trim());
        entity.answersJson=json.writeValueAsString(answers);entity.currentQuestion=Math.min(index+1,answers.size()-1);
        return view(repository.saveAndFlush(entity));
    }
    @Transactional public Session finish(Long userId,Long id,Long version) {
        users.lockById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Please sign in again"));
        var entity=own(userId,id);
        if (!Objects.equals(entity.version,version)) throw new ResponseStatusException(HttpStatus.CONFLICT,"The interview changed. Reload it before finishing.");
        if (view(entity).answers().stream().anyMatch(String::isBlank)) throw new IllegalArgumentException("Save an answer for each question before finishing");
        entity.status="COMPLETED";entity.completedAt=Instant.now();
        return view(repository.saveAndFlush(entity));
    }
}
