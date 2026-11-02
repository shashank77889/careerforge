package com.careerforge.jobmatch;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ConceptMatcherTests {
 @Test void paraphraseHasSourceEvidence() {
  var result = ConceptMatcher.compare("Build automated testing for releases.", "Created a regression suite for checkout.");
  assertEquals(1, result.supportedConcepts());
  assertEquals("Created a regression suite for checkout.", result.evidence().getFirst().resumeEvidence());
 }
 @Test void negatedAndAspirationalClaimsAreNotEvidence() {
  for (String text : new String[]{"I have no automated testing experience.", "I plan to build automated tests.", "I am learning automated testing."})
   assertEquals(0, ConceptMatcher.compare("Build automated testing.", text).supportedConcepts());
 }
 @Test void unrelatedAndUnknownRequirementsDoNotCreateMatches() {
  assertEquals(0, ConceptMatcher.compare("Build automated testing.", "Created charts for sales.").supportedConcepts());
  assertEquals(0, ConceptMatcher.compare("Practice aerospace engineering.", "Aerospace engineer.").requiredConcepts());
 }
 @Test void olderSavedResultsRemainReadable() {
  var mapper = tools.jackson.databind.json.JsonMapper.builder().build();
  var old = mapper.readValue("{\"score\":20,\"scoreLabel\":\"Partial\",\"skills\":[],\"keywords\":[],\"requirements\":[],\"suggestions\":[],\"learningRecommendations\":[],\"method\":\"v1\",\"integrityNote\":\"test\"}", JobMatchService.Result.class);
  assertNull(old.concepts());
  assertNull(old.resumePassages());
 }
}
