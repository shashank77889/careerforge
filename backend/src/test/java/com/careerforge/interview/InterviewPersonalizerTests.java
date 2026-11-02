package com.careerforge.interview;
import com.careerforge.journey.JourneyService;
import com.careerforge.model.Resume;
import com.careerforge.repository.ResumeRepository;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
class InterviewPersonalizerTests {
 @Test void usesOwnedResumeAndWeakRatingsWithoutChangingQuestionCount() {
  var journey=mock(JourneyService.class);var resumes=mock(ResumeRepository.class);
  when(journey.readiness(7L,"Java Developer")).thenReturn(new JourneyService.Readiness(20,20,0,1,1,
   List.of(new JourneyService.SkillLevel("Java",20,70,"DEVELOPING","RESUME"),new JourneyService.SkillLevel("SQL",0,60,"MISSING","NOT_ADDED")),"test"));
  var resume=new Resume();resume.setId(2L);resume.setExtractedText("Course project built using Java.");
  when(resumes.findAllByUserId(7L)).thenReturn(List.of(resume));
  var original=new InterviewQuestions().forRole("Java Developer",10,Set.of());
  var result=new InterviewPersonalizer(journey,resumes).personalize(7L,"Java Developer",original);
  assertEquals(10,result.size());
  assertTrue(result.stream().anyMatch(q -> q.topic().equals("Resume · Java")));
  assertTrue(result.stream().anyMatch(q -> q.topic().equals("Practice gap · SQL")));
  verify(resumes).findAllByUserId(7L);
 }
 @Test void absentResumeNeverClaimsResumeExperience() {
  var journey=mock(JourneyService.class);var resumes=mock(ResumeRepository.class);
  when(journey.readiness(8L,"Java Developer")).thenReturn(new JourneyService.Readiness(100,100,1,0,0,
   List.of(new JourneyService.SkillLevel("Java",100,70,"MATCHED","MANUAL")),"test"));
  when(resumes.findAllByUserId(8L)).thenReturn(List.of());
  var original=new InterviewQuestions().forRole("Java Developer",5,Set.of());
  assertEquals(original,new InterviewPersonalizer(journey,resumes).personalize(8L,"Java Developer",original));
 }
}
