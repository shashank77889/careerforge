package com.careerforge.interview;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class AiMcqGeneratorTests {
    @Test void validatesShapeAndPreservesAnswersWhenShuffling() {
        var questions=new InterviewQuestions.Question[30];
        for(int i=0;i<30;i++)questions[i]=new InterviewQuestions.Question(i<10?"Aptitude":"Java","Question "+i,List.of(),List.of("Correct","Wrong A","Wrong B","Wrong C"),0,"Explain the answer.");
        var accepted=AiMcqGenerator.validate(questions);
        assertEquals(30,accepted.size());
        for(var q:accepted)assertEquals("Correct",q.options().get(q.correctIndex()));
        questions[29]=questions[28];
        assertThrows(IllegalArgumentException.class,()->AiMcqGenerator.validate(questions));
        assertThrows(IllegalArgumentException.class,()->AiMcqGenerator.validate(new InterviewQuestions.Question[0]));
    }
}
