# BAMOE Insurance Decision Service - Project Summary

## Overview

This project demonstrates IBM Business Automation Manager Open Editions (BAMOE) capabilities for a potential client migrating from Red Hat Process Automation Manager. It showcases:

- **DMN Decision Modeling**: Insurance risk assessment using Decision Model and Notation
- **Custom Event Listeners**: Capturing all rules fired during decision evaluation (addressing the Management Console limitation)
- **Quarkus Runtime**: Cloud-native deployment with hot-reload capabilities

---

## Project Structure

```
insurance-decision-service/
├── pom.xml                                         # Maven configuration (Community Edition)
├── pom-enterprise.xml                              # Maven configuration (IBM BAMOE licensed)
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/insurance/
│   │   │       └── listener/
│   │   │           ├── InsuranceDecisionEventListener.java  # DMN event listener
│   │   │           └── RulesEventListener.java              # DRL agenda listener
│   │   └── resources/
│   │       ├── application.properties              # Quarkus configuration
│   │       └── InsuranceRiskAssessment.dmn         # DMN decision model
│   └── test/
│       └── java/
│           └── com/example/insurance/
│               └── InsuranceDecisionTest.java      # Integration tests
├── .vscode/
│   ├── launch.json                                 # Debug configuration
│   ├── settings.json                               # Editor settings
│   └── extensions.json                             # Recommended extensions
└── README.md                                       # Quick reference guide
```

---

## Files Created

### 1. pom.xml (Maven Configuration)

**Purpose**: Defines project dependencies for Quarkus and Kogito/Drools.

**Key Dependencies**:
- `quarkus-arc` - Dependency injection
- `quarkus-resteasy-reactive-jackson` - REST endpoint generation
- `drools-quarkus-decisions` - DMN support
- `drools-quarkus-rules` - DRL rules support
- `kie-dmn-core` - DMN event listener interfaces
- `quarkus-smallrye-openapi` - Swagger UI for testing

**Versions Used** (Community Edition):
```xml
<quarkus.platform.version>3.8.4</quarkus.platform.version>
<kogito.version>10.0.0</kogito.version>
```

---

### 2. application.properties

**Purpose**: Quarkus application configuration.

```properties
# Application Configuration
quarkus.application.name=insurance-decision-service

# HTTP Server
quarkus.http.port=8080

# Swagger UI - enables testing interface
quarkus.swagger-ui.always-include=true
quarkus.swagger-ui.path=/swagger-ui

# Logging Configuration
quarkus.log.console.enable=true
quarkus.log.console.format=%d{HH:mm:ss} %-5p [%c{2.}] (%t) %s%e%n
quarkus.log.console.level=INFO

# Enable detailed logging for Drools/DMN
quarkus.log.category."org.kie".level=INFO
quarkus.log.category."org.drools".level=INFO

# Custom listener logging
quarkus.log.category."com.example.insurance.listener".level=INFO
```

---

### 3. InsuranceRiskAssessment.dmn

**Purpose**: DMN model that evaluates insurance risk based on driver and vehicle information.

**Inputs**:
- `Driver`: Age, YearsOfExperience, NumberOfViolations
- `Vehicle`: Category (Economy/Standard/Luxury/Sports), Year

**Decisions**:
1. `Driver Risk Score` - Decision table evaluating driver risk (0-100)
2. `Vehicle Risk Factor` - Decision table for vehicle multiplier (0.9-1.5)
3. `Insurance Assessment` - Final assessment combining both factors

**Outputs**:
- `RiskCategory`: Low, Medium, or High
- `BasePremium`: 800, 1500, or 2500
- `RiskScore`: Calculated total risk score
- `Eligible`: Boolean indicating if coverage is offered

---

### 4. InsuranceDecisionEventListener.java

**Purpose**: Custom DMN Runtime Event Listener that captures all decision evaluation events, including which decision table rules fired.

**Key Methods**:
- `beforeEvaluateAll()` - Logs input data at evaluation start
- `afterEvaluateAll()` - Logs final results and any errors
- `beforeEvaluateDecision()` / `afterEvaluateDecision()` - Tracks individual decision nodes
- `afterEvaluateDecisionTable()` - **Logs which rules fired** (the key functionality)

**Usage**: Annotated with `@ApplicationScoped` for automatic injection by Quarkus.

---

### 5. RulesEventListener.java

**Purpose**: Custom Agenda Event Listener for DRL rules (if using Drools Rule Language in addition to DMN).

**Key Methods**:
- `matchCreated()` - Called when a rule's conditions are satisfied
- `matchCancelled()` - Called when a rule activation is cancelled
- `beforeMatchFired()` / `afterMatchFired()` - Tracks rule execution

---

### 6. InsuranceDecisionTest.java

**Purpose**: Integration tests for the decision service.

**Test Cases**:
- High risk young driver with violations
- Low risk experienced driver
- Medium risk scenarios
- Senior driver with luxury vehicle

---

## Running the Application

### Prerequisites
- Java JDK 17+
- Maven 3.9+
- VSCode with BAMOE Developer Tools extension

---
## Testing the Service

### Using Swagger UI
Open: http://localhost:8080/swagger-ui

### Using curl

**Full Assessment**:
```bash
curl -X POST http://localhost:8080/InsuranceRiskAssessment ^
  -H "Content-Type: application/json" ^
  -d "{\"Driver\": {\"Age\": 35, \"YearsOfExperience\": 10, \"NumberOfViolations\": 1}, \"Vehicle\": {\"Category\": \"Standard\", \"Year\": 2020}}"
```

**Expected Response**:
```json
{
  "Driver Risk Score": 40,
  "Vehicle Risk Factor": 1.0,
  "Insurance Assessment": {
    "RiskCategory": "Low",
    "BasePremium": 800,
    "RiskScore": 40.0,
    "Eligible": true
  }
}
```

---

## Test Scenarios

| Driver Age | Experience | Violations | Vehicle   | Expected Risk | Premium | Eligible |
|------------|------------|------------|-----------|---------------|---------|----------|
| 22         | 2          | 3          | Sports    | High          | $2,500  | No       |
| 45         | 20         | 0          | Economy   | Low           | $800    | Yes      |
| 72         | 50         | 0          | Luxury    | Medium        | $1,500  | Yes      |
| 19         | 1          | 0          | Standard  | High          | $2,500  | Yes      |
| 35         | 10         | 1          | Standard  | Low           | $800    | Yes      |

---