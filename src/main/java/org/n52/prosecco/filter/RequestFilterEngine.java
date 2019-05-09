
package org.n52.prosecco.filter;

import java.util.Set;
import java.util.function.Consumer;

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
        FilterContextBuilder builder = FilterContextBuilder.of(context.getRoles())
                                                           .withTimespans(evaluateTimespans(context))
                                                           .withServiceParameters(context)
                                                           .andRemainingFrom(context);
        Set<String> parameters = context.getThematicParameterNames();
        parameters.forEach(updateThematicContext(builder, context));
        return builder.build();
    }

    private Consumer<String> updateThematicContext(FilterContextBuilder builder, FilterContext context) {
        return parameter -> builder.withParameters(parameter, evaluate(parameter, context));
    }

    private Set<String> evaluate(String parameter, FilterContext context) {
        ThematicFilter evaluator = new ThematicFilter(parameter, config);
        Set<String> values = !context.hasParameter(parameter)
                ? context.getServiceParameterValues(parameter)
                : context.getValues(parameter);
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
