package com.careerforge.interview;
import com.careerforge.journey.JourneyService;
import com.careerforge.repository.ResumeRepository;
import org.springframework.stereotype.Component;
import java.util.*;
@Component
public class InterviewPersonalizer {
 private final JourneyService journey;
 private final ResumeRepository resumes;
 public InterviewPersonalizer(JourneyService journey, ResumeRepository resumes) { this.journey=journey; this.resumes=resumes; }
 public List<InterviewQuestions.Question> personalize(Long userId,String role,List<InterviewQuestions.Question> original) {
  var levels=journey.readiness(userId,role).skills();
  String text=resumes.findAllByUserId(userId).stream().max(Comparator.comparing(com.careerforge.model.Resume::getId))
    .map(resume -> Objects.toString(resume.getExtractedText(),"").toLowerCase(Locale.ROOT)).orElse("");
  var additions=new ArrayList<InterviewQuestions.Question>();
  levels.stream().filter(level -> java.util.regex.Pattern.compile("(?<![a-z0-9])"+java.util.regex.Pattern.quote(level.skill().toLowerCase(Locale.ROOT))+"(?![a-z0-9])").matcher(text).find()).findFirst().ifPresent(level ->
    additions.add(new InterviewQuestions.Question("Resume · "+level.skill(),
     "Your uploaded resume mentions "+level.skill()+". Describe one real example where you used it: your contribution, a problem you faced, and how you checked the result. If it was only coursework, say so.",
     List.of("Use a real example; distinguish coursework from professional experience.","Explain your own contribution and a concrete design choice.","Describe a test, outcome, limitation, and what you would improve."))));
  levels.stream().filter(level -> !level.status().equals("MATCHED")).min(Comparator.comparingInt(level -> level.current()-level.required())).ifPresent(level ->
    additions.add(new InterviewQuestions.Question("Practice gap · "+level.skill(),
     "Your saved self-rating suggests "+level.skill()+" needs practice for "+role+". Explain the basics, then outline a small exercise you would use to learn and verify it. Be clear about what you have not tried yet.",
     List.of("State current knowledge honestly; do not invent experience.","Explain the core concept and one relevant use case.","Propose a small task, an edge case, and a way to check the result."))));
  var result=new ArrayList<>(original);
  for(int index=0;index<additions.size() && index<result.size();index++) result.set(result.size()-1-index,additions.get(index));
  return result;
 }
}
