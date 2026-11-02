package com.careerforge.interview;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.databind.json.JsonMapper;
import com.careerforge.journey.JourneyService;

@Component
public class McqBank {
    public record Entry(String group,String topic,String prompt,List<String> options,int correctIndex,String explanation) {}
    private final List<Entry> entries;
    public McqBank() throws java.io.IOException {
        try(var input = new ClassPathResource("interview-mcq.json").getInputStream()) {
            entries = Arrays.asList(JsonMapper.builder().build().readValue(input,Entry[].class));
        }
    }
    public List<InterviewQuestions.Question> forRole(String role) {
        if(!JourneyService.ROLES.contains(role)) throw new IllegalArgumentException("Choose a supported role");
        String group = switch(role) {
            case "Data Scientist","Data Analyst","AI/ML Engineer" -> "data";
            case "Frontend Developer" -> "frontend";
            case "SDET" -> "testing";
            case "Cybersecurity Analyst" -> "security";
            case "Python Developer" -> "python";
            default -> "java";
        };
        var questions = new ArrayList<InterviewQuestions.Question>();
        entries.stream().filter(e -> e.group().equals("common") || e.group().equals(group))
            .forEach(e -> questions.add(shuffle(e.topic(),e.prompt(),e.options(),e.correctIndex(),e.explanation())));
        int n=ThreadLocalRandom.current().nextInt(3,20);
        numeric(questions,"Percentages","What is 20% of "+(n*100)+"?",n*20,"Divide by 100, then multiply by 20.");
        numeric(questions,"Averages","What is the mean of "+n+", "+(n+2)+" and "+(n+4)+"?",n+2,"Add the three values and divide by 3.");
        numeric(questions,"Ratios","A total of "+(n*5)+" is split in the ratio 2:3. What is the smaller share?",n*2,"There are five parts. Divide the total by 5, then multiply by 2.");
        numeric(questions,"Speed","A vehicle covers "+(n*60)+" km in "+n+" hours. What is its average speed in km/h?",60,"Average speed is distance divided by time.");
        numeric(questions,"Number series","Find the next number: "+n+", "+(n*2)+", "+(n*4)+", "+(n*8)+".",n*16,"Each term is twice the previous term.");
        numeric(questions,"Simple interest","At 10% simple annual interest, what interest is earned on "+(n*100)+" in 2 years?",n*20,"Simple interest = principal × rate × time / 100.");
        numeric(questions,"Work rates",n+" workers complete a task in 6 days. At equal constant rates, how many days do "+(n*2)+" workers need?",3,"Total work is workers × days. Doubling workers halves the time.");
        numeric(questions,"Discounts","An item costs "+(n*100)+". What is its price after a 10% discount?",n*90,"Subtract 10% of the original price, or multiply by 0.9.");
        questions.add(shuffle("Probability","A fair six-sided die is rolled once. What is the probability of an even number?",List.of("1/2","1/3","1/6","2/3"),0,"There are three even outcomes (2, 4, 6) among six equally likely outcomes: 3/6."));
        questions.add(shuffle("Logical reasoning","All roses are flowers. Some flowers fade quickly. Must some roses fade quickly?",List.of("No; this cannot be concluded","Yes, always","No roses are flowers","All flowers are roses"),0,"The flowers that fade quickly may exclude every rose. Do not assume an overlap that the premises do not establish."));
        Collections.shuffle(questions);
        return questions;
    }
    private void numeric(List<InterviewQuestions.Question> result,String topic,String prompt,int answer,String explanation) {
        result.add(shuffle("Aptitude · "+topic,prompt,List.of(""+answer,""+(answer+1),""+(answer+3),""+(answer+7)),0,explanation+" Answer: "+answer+"."));
    }
    private InterviewQuestions.Question shuffle(String topic,String prompt,List<String> options,int correct,String explanation) {
        var shuffled=new ArrayList<>(options);Collections.shuffle(shuffled);
        return new InterviewQuestions.Question(topic,prompt,List.of(),shuffled,shuffled.indexOf(options.get(correct)),explanation);
    }
}
