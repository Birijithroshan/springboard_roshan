package org.example.engine;

import org.example.model.TestCase;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Component
public class AITestExecutor {

    private static final Logger log = LoggerFactory.getLogger(AITestExecutor.class);

    public TestExecutionEngine.TestExecutionResult execute(Map<String, Object> testData, TestCase testCase) {
        TestExecutionEngine.TestExecutionResult result = new TestExecutionEngine.TestExecutionResult();
        List<String> logs = new ArrayList<>();
        
        try {
            logs.add("Starting AI test execution for: " + testCase.getName());
            
            // AI testing can include:
            // - Model performance testing
            // - Response quality validation
            // - Latency testing
            // - A/B testing different prompts
            // - Output consistency testing
            
            String aiTestType = testData.getOrDefault("aiTestType", "response_quality").toString();
            logs.add("AI Test Type: " + aiTestType);

            switch (aiTestType.toLowerCase()) {
                case "response_quality":
                    result = executeResponseQualityTest(testData, logs);
                    break;
                case "performance":
                    result = executePerformanceTest(testData, logs);
                    break;
                case "consistency":
                    result = executeConsistencyTest(testData, logs);
                    break;
                case "prompt_testing":
                    result = executePromptTest(testData, logs);
                    break;
                default:
                    result = executeGenericAITest(testData, logs);
                    break;
            }

            result.setExecutionLogs(String.join("\n", logs));

        } catch (Exception e) {
            log.error("AI test execution failed", e);
            result.setSuccess(false);
            result.setErrorMessage("AI test execution failed: " + e.getMessage());
            logs.add("ERROR: " + e.getMessage());
            result.setExecutionLogs(String.join("\n", logs));
        }

        return result;
    }

    private TestExecutionEngine.TestExecutionResult executeResponseQualityTest(Map<String, Object> testData, List<String> logs) {
        TestExecutionEngine.TestExecutionResult result = new TestExecutionEngine.TestExecutionResult();
        
        try {
            // Mock AI response quality testing
            String prompt = testData.getOrDefault("prompt", "").toString();
            List<String> expectedKeywords = (List<String>) testData.getOrDefault("expectedKeywords", new ArrayList<>());
            
            logs.add("Testing prompt: " + prompt);
            logs.add("Expected keywords: " + expectedKeywords);

            // Simulate AI API call and response validation
            String mockResponse = generateMockAIResponse(prompt);
            logs.add("AI Response: " + mockResponse);

            // Validate response quality
            boolean qualityCheck = validateResponseQuality(mockResponse, expectedKeywords, logs);
            
            result.setSuccess(qualityCheck);
            if (!qualityCheck) {
                result.setErrorMessage("AI response quality validation failed");
            }

        } catch (Exception e) {
            result.setSuccess(false);
            result.setErrorMessage("Response quality test failed: " + e.getMessage());
        }

        return result;
    }

    private TestExecutionEngine.TestExecutionResult executePerformanceTest(Map<String, Object> testData, List<String> logs) {
        TestExecutionEngine.TestExecutionResult result = new TestExecutionEngine.TestExecutionResult();
        
        try {
            int iterations = Integer.parseInt(testData.getOrDefault("iterations", "5").toString());
            long maxLatency = Long.parseLong(testData.getOrDefault("maxLatency", "5000").toString());
            
            logs.add("Performance test with " + iterations + " iterations");
            logs.add("Max allowed latency: " + maxLatency + " ms");

            long totalTime = 0;
            for (int i = 0; i < iterations; i++) {
                long startTime = System.currentTimeMillis();
                
                // Simulate AI processing time
                Thread.sleep(100 + (long)(Math.random() * 200));
                
                long endTime = System.currentTimeMillis();
                long iterationTime = endTime - startTime;
                totalTime += iterationTime;
                
                logs.add("Iteration " + (i + 1) + " completed in " + iterationTime + " ms");
            }

            long averageLatency = totalTime / iterations;
            logs.add("Average latency: " + averageLatency + " ms");

            if (averageLatency <= maxLatency) {
                result.setSuccess(true);
                logs.add("Performance test passed");
            } else {
                result.setSuccess(false);
                result.setErrorMessage("Performance test failed - average latency exceeded threshold");
            }

        } catch (Exception e) {
            result.setSuccess(false);
            result.setErrorMessage("Performance test failed: " + e.getMessage());
        }

        return result;
    }

    private TestExecutionEngine.TestExecutionResult executeConsistencyTest(Map<String, Object> testData, List<String> logs) {
        TestExecutionEngine.TestExecutionResult result = new TestExecutionEngine.TestExecutionResult();
        
        try {
            String prompt = testData.getOrDefault("prompt", "").toString();
            int runs = Integer.parseInt(testData.getOrDefault("consistencyRuns", "3").toString());
            double minSimilarity = Double.parseDouble(testData.getOrDefault("minSimilarity", "0.8").toString());

            logs.add("Consistency test with " + runs + " runs");
            logs.add("Minimum similarity threshold: " + minSimilarity);

            List<String> responses = new ArrayList<>();
            for (int i = 0; i < runs; i++) {
                String response = generateMockAIResponse(prompt);
                responses.add(response);
                logs.add("Run " + (i + 1) + " response: " + response);
            }

            // Calculate similarity between responses (simplified)
            double similarity = calculateResponseSimilarity(responses);
            logs.add("Calculated similarity: " + similarity);

            if (similarity >= minSimilarity) {
                result.setSuccess(true);
                logs.add("Consistency test passed");
            } else {
                result.setSuccess(false);
                result.setErrorMessage("Consistency test failed - responses too different");
            }

        } catch (Exception e) {
            result.setSuccess(false);
            result.setErrorMessage("Consistency test failed: " + e.getMessage());
        }

        return result;
    }

    private TestExecutionEngine.TestExecutionResult executePromptTest(Map<String, Object> testData, List<String> logs) {
        TestExecutionEngine.TestExecutionResult result = new TestExecutionEngine.TestExecutionResult();
        
        try {
            @SuppressWarnings("unchecked")
            List<String> prompts = (List<String>) testData.get("prompts");
            String expectedOutcome = testData.getOrDefault("expectedOutcome", "").toString();

            logs.add("Testing " + prompts.size() + " different prompts");
            logs.add("Expected outcome: " + expectedOutcome);

            boolean anyPromptSucceeded = false;
            for (int i = 0; i < prompts.size(); i++) {
                String prompt = prompts.get(i);
                String response = generateMockAIResponse(prompt);
                
                logs.add("Prompt " + (i + 1) + ": " + prompt);
                logs.add("Response " + (i + 1) + ": " + response);

                if (response.toLowerCase().contains(expectedOutcome.toLowerCase())) {
                    anyPromptSucceeded = true;
                    logs.add("Prompt " + (i + 1) + " achieved expected outcome");
                }
            }

            result.setSuccess(anyPromptSucceeded);
            if (!anyPromptSucceeded) {
                result.setErrorMessage("No prompt achieved the expected outcome");
            }

        } catch (Exception e) {
            result.setSuccess(false);
            result.setErrorMessage("Prompt test failed: " + e.getMessage());
        }

        return result;
    }

    private TestExecutionEngine.TestExecutionResult executeGenericAITest(Map<String, Object> testData, List<String> logs) {
        TestExecutionEngine.TestExecutionResult result = new TestExecutionEngine.TestExecutionResult();
        
        // Generic AI test - can be customized based on requirements
        result.setSuccess(true);
        logs.add("Generic AI test executed successfully");
        
        return result;
    }

    private String generateMockAIResponse(String prompt) {
        // Mock AI response generation
        String[] mockResponses = {
            "This is a simulated AI response for prompt: " + prompt,
            "AI processing complete. Response generated successfully.",
            "Mock AI system responded with relevant information.",
            "Simulated response demonstrates AI functionality.",
            "Test response from mock AI service."
        };
        
        return mockResponses[(int) (Math.random() * mockResponses.length)];
    }

    private boolean validateResponseQuality(String response, List<String> expectedKeywords, List<String> logs) {
        if (expectedKeywords.isEmpty()) {
            logs.add("No keywords to validate - assuming quality check passed");
            return true;
        }

        int foundKeywords = 0;
        for (String keyword : expectedKeywords) {
            if (response.toLowerCase().contains(keyword.toLowerCase())) {
                foundKeywords++;
                logs.add("Found expected keyword: " + keyword);
            } else {
                logs.add("Missing expected keyword: " + keyword);
            }
        }

        double keywordScore = (double) foundKeywords / expectedKeywords.size();
        logs.add("Keyword score: " + String.format("%.2f", keywordScore));

        // Consider quality good if at least 70% of keywords are found
        return keywordScore >= 0.7;
    }

    private double calculateResponseSimilarity(List<String> responses) {
        if (responses.size() < 2) return 1.0;

        // Simplified similarity calculation - in practice, use more sophisticated algorithms
        // like cosine similarity, Jaccard similarity, etc.
        
        int totalComparisons = 0;
        double totalSimilarity = 0.0;

        for (int i = 0; i < responses.size(); i++) {
            for (int j = i + 1; j < responses.size(); j++) {
                double similarity = calculateSimpleSimilarity(responses.get(i), responses.get(j));
                totalSimilarity += similarity;
                totalComparisons++;
            }
        }

        return totalComparisons > 0 ? totalSimilarity / totalComparisons : 0.0;
    }

    private double calculateSimpleSimilarity(String text1, String text2) {
        // Very simple similarity based on common words
        Set<String> words1 = new HashSet<>(Arrays.asList(text1.toLowerCase().split("\\s+")));
        Set<String> words2 = new HashSet<>(Arrays.asList(text2.toLowerCase().split("\\s+")));

        Set<String> intersection = new HashSet<>(words1);
        intersection.retainAll(words2);

        Set<String> union = new HashSet<>(words1);
        union.addAll(words2);

        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }
}
