package com.example.insurance;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

/**
 * Integration tests for the Insurance Risk Assessment DMN service.
 * 
 * These tests demonstrate:
 * 1. How to call the auto-generated REST endpoints
 * 2. Different risk scenarios and expected outcomes
 * 3. The JSON structure for DMN inputs
 */
@QuarkusTest
public class InsuranceDecisionTest {

    /**
     * Test: Young driver with violations = High Risk, Not Eligible
     */
    @Test
    public void testHighRiskYoungDriverWithViolations() {
        String requestBody = """
            {
                "Driver": {
                    "Age": 22,
                    "YearsOfExperience": 2,
                    "NumberOfViolations": 3
                },
                "Vehicle": {
                    "Category": "Sports",
                    "Year": 2023
                }
            }
            """;
        
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/InsuranceRiskAssessment")
        .then()
            .statusCode(200)
            .body("Insurance Assessment.RiskCategory", equalTo("High"))
            .body("Insurance Assessment.Eligible", equalTo(false))
            .body("Insurance Assessment.BasePremium", equalTo(2500));
    }

    /**
     * Test: Experienced driver, clean record, economy car = Low Risk
     */
    @Test
    public void testLowRiskExperiencedDriver() {
        String requestBody = """
            {
                "Driver": {
                    "Age": 45,
                    "YearsOfExperience": 20,
                    "NumberOfViolations": 0
                },
                "Vehicle": {
                    "Category": "Economy",
                    "Year": 2020
                }
            }
            """;
        
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/InsuranceRiskAssessment")
        .then()
            .statusCode(200)
            .body("Insurance Assessment.RiskCategory", equalTo("Low"))
            .body("Insurance Assessment.Eligible", equalTo(true))
            .body("Insurance Assessment.BasePremium", equalTo(800));
    }

    /**
     * Test: Medium risk scenario
     */
    @Test
    public void testMediumRiskDriver() {
        String requestBody = """
            {
                "Driver": {
                    "Age": 35,
                    "YearsOfExperience": 10,
                    "NumberOfViolations": 1
                },
                "Vehicle": {
                    "Category": "Standard",
                    "Year": 2018
                }
            }
            """;
        
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/InsuranceRiskAssessment")
        .then()
            .statusCode(200)
            .body("Insurance Assessment.RiskCategory", equalTo("Low"))
            .body("Insurance Assessment.Eligible", equalTo(true));
    }

    /**
     * Test: Senior driver with luxury vehicle
     */
    @Test
    public void testSeniorDriverLuxuryVehicle() {
        String requestBody = """
            {
                "Driver": {
                    "Age": 72,
                    "YearsOfExperience": 50,
                    "NumberOfViolations": 0
                },
                "Vehicle": {
                    "Category": "Luxury",
                    "Year": 2022
                }
            }
            """;
        
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/InsuranceRiskAssessment")
        .then()
            .statusCode(200)
            .body("Insurance Assessment.Eligible", equalTo(true))
            .body("Insurance Assessment.RiskScore", notNullValue());
    }

    /**
     * Test: Driver Risk Score decision only
     */
    @Test
    public void testDriverRiskScoreOnly() {
        String requestBody = """
            {
                "Driver": {
                    "Age": 19,
                    "YearsOfExperience": 1,
                    "NumberOfViolations": 0
                }
            }
            """;
        
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/InsuranceRiskAssessment/Driver Risk Score")
        .then()
            .statusCode(200)
            .body("Driver Risk Score", equalTo(80));
    }
}
