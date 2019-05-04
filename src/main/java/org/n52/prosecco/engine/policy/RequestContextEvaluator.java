
package org.n52.prosecco.engine.policy;

import java.util.Collection;
import java.util.Set;

import org.n52.prosecco.engine.filter.FilterContext;
import org.n52.prosecco.engine.filter.FilterContext.FilterContextBuilder;
import org.n52.prosecco.engine.filter.Timespan;

public class RequestContextEvaluator {

    private final PolicyConfig config;

    public RequestContextEvaluator(PolicyConfig config) {
        this.config = config;
    }

    /**
     * Evaluates a given filter context against the policy configuration. The result is a new instance of the
     * context manipulated in order to satisfy configured policies.
     * 
     * @param context
     *        the context to evaluate
     * @return the new context matching the policy config.
     */
    public FilterContext evaluate(FilterContext context) {
        return FilterContextBuilder.of(context.getRoles())
                                   .withFeatures(evaluateFeatures(context))
                                   .withTimespans(evaluateTimespans(context))
                                   .withPhenomena(evaluatePhenomena(context))
                                   .withOfferings(evaluateOfferings(context))
                                   .withProcedures(evaluateProcedures(context))
                                   .withServiceParameters(context.getServiceParameters())
                                   .andRemainingQuery(context.getRemainingQuery())
                                   .build();
    }

    private Set<String> evaluatePhenomena(FilterContext context) {
        ThematicEvaluator evaluator = new ThematicEvaluator("phenomenon", config);
        Set<String> values = !context.hasPhenomena()
                ? context.getServiceParameters().getPhenomena()
                : context.getPhenomena();
        return evaluator.evaluate(values, context);
    }

    private Set<String> evaluateOfferings(FilterContext context) {
        ThematicEvaluator evaluator = new ThematicEvaluator("offering", config);
        Set<String> values = !context.hasOfferings()
                ? context.getServiceParameters().getOfferings()
                : context.getOfferings();
        return evaluator.evaluate(values, context);
    }

    private Set<String> evaluateProcedures(FilterContext context) {
        ThematicEvaluator evaluator = new ThematicEvaluator("procedure", config);
        Set<String> values = !context.hasProcedures()
                ? context.getServiceParameters().getProcedures()
                : context.getProcedures();
        return evaluator.evaluate(values, context);
    }

    private Set<String> evaluateFeatures(FilterContext context) {
        ThematicEvaluator evaluator = new ThematicEvaluator("feature", config);
        Set<String> values = !context.hasFeatures()
                ? context.getServiceParameters().getFeatures()
                : context.getFeatures();
        return evaluator.evaluate(values, context);
    }


    private Set<Timespan> evaluateTimespans(FilterContext context) {
        Set<Timespan> timespans = context.getTimespans();
        return evaluateTimeFloating("timespan", timespans, context);
    }

    private Set<Timespan> evaluateTimeFloating(String string, Set<Timespan> timespans, FilterContext context) {
        TimeFloatingEvaluator evaluator = new TimeFloatingEvaluator("timespan", config);
        return evaluator.evaluate(timespans, context);
    }

    private Collection<Rule> getRelevantRules(FilterContext context) {
        return config.getRulesForRole(context.getRoles());
    }

};