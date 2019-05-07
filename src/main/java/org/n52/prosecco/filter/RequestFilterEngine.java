
package org.n52.prosecco.filter;

import java.util.Set;

import org.n52.prosecco.policy.PolicyConfig;
import org.n52.prosecco.web.request.FilterContext;
import org.n52.prosecco.web.request.FilterContext.FilterContextBuilder;
import org.n52.prosecco.web.request.Timespan;

public final class RequestFilterEngine {

    private final PolicyConfig config;

    public RequestFilterEngine(PolicyConfig config) {
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
        ThematicFilter evaluator = new ThematicFilter("phenomenon", config);
        Set<String> values = !context.hasPhenomena()
                ? context.getServiceParameters().getPhenomena()
                : context.getPhenomena();
        return evaluator.evaluate(values, context);
    }

    private Set<String> evaluateOfferings(FilterContext context) {
        ThematicFilter evaluator = new ThematicFilter("offering", config);
        Set<String> values = !context.hasOfferings()
                ? context.getServiceParameters().getOfferings()
                : context.getOfferings();
        return evaluator.evaluate(values, context);
    }

    private Set<String> evaluateProcedures(FilterContext context) {
        ThematicFilter evaluator = new ThematicFilter("procedure", config);
        Set<String> values = !context.hasProcedures()
                ? context.getServiceParameters().getProcedures()
                : context.getProcedures();
        return evaluator.evaluate(values, context);
    }

    private Set<String> evaluateFeatures(FilterContext context) {
        ThematicFilter evaluator = new ThematicFilter("feature", config);
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
        TimeFloatingFilter evaluator = new TimeFloatingFilter("timespan", config);
        return evaluator.evaluate(timespans, context);
    }

};