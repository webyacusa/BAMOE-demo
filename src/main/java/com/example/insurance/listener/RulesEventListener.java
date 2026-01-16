package com.example.insurance.listener;

import jakarta.enterprise.context.ApplicationScoped;
import org.kie.api.event.rule.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Custom Agenda Event Listener for DRL Rules
 * 
 * This listener captures Drools rule engine events and can be used to:
 * - Track which rules fired and which were skipped
 * - Log rule execution sequences
 * - Capture facts that triggered rule activation
 * - Debug rule conflicts and salience issues
 * 
 * The @ApplicationScoped annotation ensures Kogito/Quarkus automatically
 * injects this listener into the Drools runtime.
 * 
 * Note: This listener is primarily for DRL rules. DMN decision tables
 * use the DMNRuntimeEventListener instead.
 */
@ApplicationScoped
public class RulesEventListener implements AgendaEventListener {
    
    private static final Logger logger = LoggerFactory.getLogger(RulesEventListener.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    
    private int rulesFiredCount = 0;
    private int matchesCreatedCount = 0;
    
    /**
     * Called when a rule's conditions are satisfied and an activation is created.
     * This happens BEFORE the rule fires - the rule is "matched" but not yet executed.
     */
    @Override
    public void matchCreated(MatchCreatedEvent event) {
        matchesCreatedCount++;
        String timestamp = LocalDateTime.now().format(formatter);
        String ruleName = event.getMatch().getRule().getName();
        String packageName = event.getMatch().getRule().getPackageName();
        
        logger.info("[{}] ✓ Rule MATCHED: '{}'", timestamp, ruleName);
        logger.info("   └─ Package: {}", packageName);
        logger.info("   └─ Facts: {}", event.getMatch().getObjects());
        
        // Here you could:
        // - Record the match for analytics
        // - Check for specific rule patterns
        // - Trigger alerts for certain rule activations
    }
    
    /**
     * Called when a rule activation is cancelled (removed from agenda).
     * This happens when facts change and the rule's conditions are no longer satisfied.
     */
    @Override
    public void matchCancelled(MatchCancelledEvent event) {
        String timestamp = LocalDateTime.now().format(formatter);
        
        logger.info("[{}] ✗ Rule CANCELLED: '{}'", timestamp, event.getMatch().getRule().getName());
        logger.info("   └─ Cause: {}", event.getCause());
        
        // Useful for debugging why a rule didn't fire
    }
    
    /**
     * Called immediately BEFORE a rule's right-hand side (RHS) is executed.
     * The rule is about to fire.
     */
    @Override
    public void beforeMatchFired(BeforeMatchFiredEvent event) {
        String timestamp = LocalDateTime.now().format(formatter);
        String ruleName = event.getMatch().getRule().getName();
        
        logger.info("[{}] ▶ Rule FIRING: '{}'", timestamp, ruleName);
        logger.debug("   └─ Package: {}", event.getMatch().getRule().getPackageName());
        logger.debug("   └─ Metadata: {}", event.getMatch().getRule().getMetaData());
    }
    
    /**
     * Called immediately AFTER a rule's right-hand side (RHS) has executed.
     * The rule has completed firing.
     */
    @Override
    public void afterMatchFired(AfterMatchFiredEvent event) {
        rulesFiredCount++;
        String timestamp = LocalDateTime.now().format(formatter);
        String ruleName = event.getMatch().getRule().getName();
        
        logger.info("[{}] ◀ Rule COMPLETED: '{}'", timestamp, ruleName);
        logger.info("   └─ Total rules fired so far: {}", rulesFiredCount);
        
        // This is the ideal place to:
        // 1. Record rule execution to audit log
        // 2. Emit Kafka message for rule fired
        // 3. Update metrics/monitoring
        // 4. Trigger downstream processes
        
        // Example: Emit to Kafka
        // kafkaProducer.send("rules-fired", new RuleFiredEvent(ruleName, timestamp, facts));
        
        // Example: Persist to database
        // ruleAuditRepository.save(new RuleAuditRecord(ruleName, facts, timestamp));
    }
    
    /**
     * Called when an agenda group is pushed onto the stack.
     * Agenda groups allow you to partition rules and control execution order.
     */
    @Override
    public void agendaGroupPushed(AgendaGroupPushedEvent event) {
        logger.info("📁 Agenda Group PUSHED: '{}'", event.getAgendaGroup().getName());
    }
    
    /**
     * Called when an agenda group is popped from the stack.
     */
    @Override
    public void agendaGroupPopped(AgendaGroupPoppedEvent event) {
        logger.info("📂 Agenda Group POPPED: '{}'", event.getAgendaGroup().getName());
    }
    
    /**
     * Called before all rules are evaluated.
     */
    @Override
    public void beforeRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event) {
        logger.info("🔄 RuleFlow Group ACTIVATED: '{}'", event.getRuleFlowGroup().getName());
    }
    
    /**
     * Called after rule flow group deactivation.
     */
    @Override
    public void afterRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event) {
        // No-op for this implementation
    }
    
    /**
     * Called before rule flow group deactivation.
     */
    @Override
    public void beforeRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event) {
        logger.info("⏹ RuleFlow Group DEACTIVATING: '{}'", event.getRuleFlowGroup().getName());
    }
    
    /**
     * Called after rule flow group deactivation.
     */
    @Override
    public void afterRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event) {
        logger.info("⏹ RuleFlow Group DEACTIVATED: '{}'", event.getRuleFlowGroup().getName());
    }
    
    // Utility methods for statistics
    
    public int getRulesFiredCount() {
        return rulesFiredCount;
    }
    
    public int getMatchesCreatedCount() {
        return matchesCreatedCount;
    }
    
    public void resetCounters() {
        rulesFiredCount = 0;
        matchesCreatedCount = 0;
    }
    
    /**
     * Generate a summary of rule execution statistics.
     * Call this after a session to get a report.
     */
    public String getExecutionSummary() {
        return String.format(
            "Rules Execution Summary:\n" +
            "  - Rules Matched: %d\n" +
            "  - Rules Fired: %d\n" +
            "  - Rules Skipped: %d",
            matchesCreatedCount,
            rulesFiredCount,
            matchesCreatedCount - rulesFiredCount
        );
    }
}
