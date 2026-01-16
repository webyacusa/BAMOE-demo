package com.example.insurance.listener;

import jakarta.enterprise.context.ApplicationScoped;
import org.kie.dmn.api.core.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

/**
 * Custom DMN Runtime Event Listener
 * 
 * This listener captures all DMN decision evaluation events and can be used to:
 * - Log decision executions for auditing
 * - Emit events to Kafka or other message brokers
 * - Persist execution data to a custom database
 * - Debug decision logic
 * 
 * The @ApplicationScoped annotation ensures Kogito/Quarkus automatically 
 * injects this listener into the DMN runtime without any additional configuration.
 */
@ApplicationScoped
public class InsuranceDecisionEventListener implements DMNRuntimeEventListener {
    
    private static final Logger logger = LoggerFactory.getLogger(InsuranceDecisionEventListener.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    /**
     * Called before the DMN model evaluation begins.
     * Use this to capture the initial input data and generate correlation IDs.
     */
    @Override
    public void beforeEvaluateAll(BeforeEvaluateAllEvent event) {
        String correlationId = UUID.randomUUID().toString().substring(0, 8);
        String timestamp = LocalDateTime.now().format(formatter);
        
        logger.info("╔══════════════════════════════════════════════════════════════════");
        logger.info("║ DMN EVALUATION STARTED");
        logger.info("║ Correlation ID: {}", correlationId);
        logger.info("║ Timestamp: {}", timestamp);
        logger.info("╠══════════════════════════════════════════════════════════════════");
        logger.info("║ INPUT DATA:");
        
        // Log all input context
        Map<String, Object> context = event.getResult().getContext().getAll();
        for (Map.Entry<String, Object> entry : context.entrySet()) {
            logger.info("║   {} = {}", entry.getKey(), entry.getValue());
        }
        logger.info("╚══════════════════════════════════════════════════════════════════");
    }
    
    /**
     * Called after the DMN model evaluation completes.
     * Use this to capture the final results and any messages/errors.
     */
    @Override
    public void afterEvaluateAll(AfterEvaluateAllEvent event) {
        String timestamp = LocalDateTime.now().format(formatter);
        
        logger.info("╔══════════════════════════════════════════════════════════════════");
        logger.info("║ DMN EVALUATION COMPLETED");
        logger.info("║ Timestamp: {}", timestamp);
        logger.info("╠══════════════════════════════════════════════════════════════════");
        logger.info("║ DECISION RESULTS:");
        
        // Log all decision outputs
        event.getResult().getDecisionResults().forEach(dr -> {
            logger.info("║   Decision: {} ", dr.getDecisionName());
            logger.info("║     - Result: {}", dr.getResult());
            logger.info("║     - Status: {}", dr.getEvaluationStatus());
            if (dr.hasErrors()) {
                dr.getMessages().forEach(msg -> 
                    logger.info("║     - Message: {} - {}", msg.getSeverity(), msg.getText())
                );
            }
        });
        
        // Check for overall evaluation errors
        if (event.getResult().hasErrors()) {
            logger.warn("║ EVALUATION ERRORS DETECTED:");
            event.getResult().getMessages().forEach(msg ->
                logger.warn("║   {} - {}", msg.getSeverity(), msg.getText())
            );
        }
        
        logger.info("╚══════════════════════════════════════════════════════════════════");
    }
    
    /**
     * Called before each individual decision node is evaluated.
     * Useful for tracing which decision in the DRD is being processed.
     */
    @Override
    public void beforeEvaluateDecision(BeforeEvaluateDecisionEvent event) {
        logger.info("  ▶ Evaluating Decision: {}", event.getDecision().getName());
    }
    
    /**
     * Called after each individual decision node evaluation completes.
     * Captures the result of each decision including decision table matches.
     */
    @Override
    public void afterEvaluateDecision(AfterEvaluateDecisionEvent event) {
        String decisionName = event.getDecision().getName();
        Object result = event.getResult().getContext().get(decisionName);
        
        logger.info("  ◀ Decision '{}' completed", decisionName);
        logger.info("    └─ Result: {}", result);
        
        // Here you could:
        // 1. Emit to Kafka: kafkaProducer.send("decision-events", decisionEvent);
        // 2. Persist to DB: decisionRepository.save(new DecisionAudit(decisionName, result));
        // 3. Send to monitoring: metricsService.recordDecision(decisionName, result);
    }
    
    /**
     * Called before a Business Knowledge Model (BKM) is invoked.
     * This includes PMML model invocations.
     */
    @Override
    public void beforeEvaluateBKM(BeforeEvaluateBKMEvent event) {
        logger.info("    ▷ Invoking BKM: {}", event.getBusinessKnowledgeModel().getName());
    }
    
    /**
     * Called after a Business Knowledge Model (BKM) evaluation completes.
     */
    @Override
    public void afterEvaluateBKM(AfterEvaluateBKMEvent event) {
        logger.info("    ◁ BKM '{}' completed", event.getBusinessKnowledgeModel().getName());
    }
    
    /**
     * Called before a Decision Table is evaluated.
     * Useful for tracking which specific decision table rules are being evaluated.
     */
    @Override
    public void beforeEvaluateDecisionTable(BeforeEvaluateDecisionTableEvent event) {
        logger.debug("      ⊳ Evaluating Decision Table: {}", event.getNodeName());
    }
    
    /**
     * Called after a Decision Table evaluation completes.
     * This is where you can see WHICH RULES FIRED in the decision table.
     */
    @Override
    public void afterEvaluateDecisionTable(AfterEvaluateDecisionTableEvent event) {
        logger.info("      ⊲ Decision Table '{}' completed", event.getNodeName());
        
        // Log which rules matched (very useful for debugging!)
        if (event.getMatches() != null && !event.getMatches().isEmpty()) {
            logger.info("        └─ Rules Fired: {}", event.getMatches());
            logger.info("        └─ Selected Result: {}", event.getSelected());
        }
    }
    
    /**
     * Called before a Context expression is evaluated.
     */
    @Override  
    public void beforeEvaluateContextEntry(BeforeEvaluateContextEntryEvent event) {
        if (event.getVariableName() != null) {
            logger.debug("        ↳ Evaluating context entry: {}", event.getVariableName());
        }
    }
    
    /**
     * Called after a Context expression evaluation completes.
     */
    @Override
    public void afterEvaluateContextEntry(AfterEvaluateContextEntryEvent event) {
        if (event.getVariableName() != null && logger.isDebugEnabled()) {
            logger.debug("        ↲ Context entry '{}' = {}", 
                event.getVariableName(), 
                event.getExpressionResult());
        }
    }
}
