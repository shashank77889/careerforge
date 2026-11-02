package com.careerforge;

import com.careerforge.service.ResumeService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ResumeTextTests {
    @Test
    void normalizesLigaturesSoftHyphensAndWrappedWordsWithoutDroppingLetters() {
        String extracted = "Built soft-\nware with proﬁciency in Java.\u00ad\u00a0Reviewed results.";
        assertEquals("Built software with proficiency in Java. Reviewed results.", ResumeService.normalizeExtractedText(extracted));
    }
}
