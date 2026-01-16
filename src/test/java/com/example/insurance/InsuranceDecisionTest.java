package com.example.insurance;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

/**
 * Integration tests for Insurance Risk Assessment DMN Service.
 * 
 * These tests validate the decision logic by sending requests
 * to the REST endpoint and verifying the responses.
 * 
 * Note: JsonPath requires bracket notation for keys with spaces,
 * e.g., "['Insurance Assessment'].RiskCategory"
 */
@QuarkusTest
public class InsuranceDecisionTest {

    private static final String ENDPOINT = "/InsuranceRiskAssessment";

    /**
     * Test Case 1: High Risk - Young driver with multiple violations
     * Expected: Very high risk score, not eligible for coverage
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
            .post(ENDPOINT)
        .then()
            .statusCode(200)
            .body("'Driver Risk Score'", is(100))
            .body("'Vehicle Risk Factor'", is(1.5f))
            .body("'Insurance Assessment'.RiskCategory", is("High"))
            .body("'Insurance Assessment'.BasePremium", is(2500))
            .body("'Insurance Assessment'.Eligible", is(false));
    }

    /**
     * Test Case 2: Low Risk - Experienced driver with no violations
     * Expected: Low risk score, eligible for coverage, lowest premium
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
            .post(ENDPOINT)
        .then()
            .statusCode(200)
            .body("'Driver Risk Score'", is(20))
            .body("'Vehicle Risk Factor'", is(0.9f))
            .body("'Insurance Assessment'.RiskCategory", is("Low"))
            .body("'Insurance Assessment'.BasePremium", is(800))
            .body("'Insurance Assessment'.Eligible", is(true));
    }

    /**
     * Test Case 3: Medium Risk - Senior driver with luxury car
     * Expected: Medium risk due to age factor and vehicle type
     */
    @Test
    public void testMediumRiskSeniorDriver() {
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
            .post(ENDPOINT)
        .then()
            .statusCode(200)
            .body("'Driver Risk Score'", is(60))
            .body("'Vehicle Risk Factor'", is(1.3f))
            .body("'Insurance Assessment'.RiskCategory", is("Medium"))
            .body("'Insurance Assessment'.BasePremium", is(1500))
            .body("'Insurance Assessment'.Eligible", is(true));
    }

    /**
     * Test Case 4: Low Risk - Middle-aged driver with one violation
     * Expected: Low risk, standard premium
     */
    @Test
    public void testLowRiskMiddleAgedDriver() {
        String requestBody = """
            {
                "Driver": {
                    "Age": 35,
                    "YearsOfExperience": 10,
                    "NumberOfViolations": 1
                },
                "Vehicle": {
                    "Category": "Standard",
                    "Year": 2020
                }
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post(ENDPOINT)
        .then()
            .statusCode(200)
            .body("'Driver Risk Score'", is(40))
            .body("'Vehicle Risk Factor'", is(1.0f))
            .body("'Insurance Assessment'.RiskCategory", is("Low"))
            .body("'Insurance Assessment'.BasePremium", is(800))
            .body("'Insurance Assessment'.Eligible", is(true));
    }

    /**
     * Test Case 5: High Risk - Young inexperienced driver
     * Expected: High risk due to age and lack of experience
     */
    @Test
    public void testHighRiskYoungInexperiencedDriver() {
        String requestBody = """
            {
                "Driver": {
                    "Age": 19,
                    "YearsOfExperience": 1,
                    "NumberOfViolations": 0
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
            .post(ENDPOINT)
        .then()
            .statusCode(200)
            .body("'Driver Risk Score'", is(80))
            .body("'Vehicle Risk Factor'", is(1.0f))
            .body("'Insurance Assessment'.RiskCategory", is("High"))
            .body("'Insurance Assessment'.BasePremium", is(2500))
            .body("'Insurance Assessment'.Eligible", is(true));
    }

    /**
     * Test that verifies all expected fields are present in the response
     */
    @Test
    public void testResponseStructure() {
        String requestBody = """
            {
                "Driver": {
                    "Age": 30,
                    "YearsOfExperience": 8,
                    "NumberOfViolations": 0
                },
                "Vehicle": {
                    "Category": "Standard",
                    "Year": 2021
                }
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post(ENDPOINT)
        .then()
            .statusCode(200)
            .body("'Driver Risk Score'", notNullValue())
            .body("'Vehicle Risk Factor'", notNullValue())
            .body("'Insurance Assessment'", notNullValue())
            .body("'Insurance Assessment'.RiskCategory", notNullValue())
            .body("'Insurance Assessment'.BasePremium", notNullValue())
            .body("'Insurance Assessment'.RiskScore", notNullValue())
            .body("'Insurance Assessment'.Eligible", notNullValue());
    }

    /**
     * Test edge case: Driver exactly at age boundary (25)
     */
    @Test
    public void testAgeBoundary25() {
        String requestBody = """
            {
                "Driver": {
                    "Age": 25,
                    "YearsOfExperience": 5,
                    "NumberOfViolations": 0
                },
                "Vehicle": {
                    "Category": "Standard",
                    "Year": 2020
                }
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post(ENDPOINT)
        .then()
            .statusCode(200)
            // Age 25 is in range [25..70), experienced, no violations = Low risk (rule 6)
            .body("'Driver Risk Score'", is(20))
            .body("'Insurance Assessment'.RiskCategory", is("Low"));
    }

    /**
     * Test edge case: Driver exactly at senior boundary (70)
     */
    @Test
    public void testAgeBoundary70() {
        String requestBody = """
            {
                "Driver": {
                    "Age": 70,
                    "YearsOfExperience": 45,
                    "NumberOfViolations": 0
                },
                "Vehicle": {
                    "Category": "Standard",
                    "Year": 2020
                }
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post(ENDPOINT)
        .then()
            .statusCode(200)
            // Age 70 triggers senior rule (rule 4) = 60 risk score
            .body("'Driver Risk Score'", is(60))
            .body("'Insurance Assessment'.RiskCategory", is("Medium"));
    }
}