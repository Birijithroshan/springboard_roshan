package org.example.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Entity
@Table(name = "test_steps")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "step_number")
    private Integer stepNumber;

    @Column(name = "description")
    private String description;

    @Column(name = "step_type")
    private String stepType; // action, validation, wait, etc.

    @Column(name = "step_data", columnDefinition = "TEXT")
    private String stepDataJson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_case_id")
    private TestCase testCase;

    // Transient field for parsed step data
    @Transient
    private Map<String, Object> stepData;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getStepNumber() { return stepNumber; }
    public void setStepNumber(Integer stepNumber) { this.stepNumber = stepNumber; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStepType() { return stepType; }
    public void setStepType(String stepType) { this.stepType = stepType; }

    public String getStepDataJson() { return stepDataJson; }
    public void setStepDataJson(String stepDataJson) { this.stepDataJson = stepDataJson; }

    public TestCase getTestCase() { return testCase; }
    public void setTestCase(TestCase testCase) { this.testCase = testCase; }

    public Map<String, Object> getStepData() { return stepData; }
    public void setStepData(Map<String, Object> stepData) { this.stepData = stepData; }
}
