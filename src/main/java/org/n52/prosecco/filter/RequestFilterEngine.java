
package org.n52.prosecco.filter;

import java.util.Set;
import java.util.function.Consumer;

import org.n52.prosecco.ConfigurationContainer;
import org.n52.prosecco.policy.PolicyConfig;
import org.n52.prosecco.web.request.FilterContext;
import org.n52.prosecco.web.request.FilterContext.FilterContextBuilder;
import org.n52.prosecco.web.request.Timespan;

public final class RequestFilterEngine {

    private final ConfigurationContainer configuration;

    public RequestFilterEngine(ConfigurationContainer configurationContainer) {
        this.configuration = configurationContainer;
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
        FilterContextBuilder builder = FilterContext.fromContext(context)
                                                    .withTimespans(evaluateTimespans(context));
        Set<String> parameters = context.getThematicParameterNames();
        parameters.forEach(updateThematicContext(builder, context));
        return builder.build();
    }

    private Consumer<String> updateThematicContext(FilterContextBuilder builder, FilterContext context) {
        return parameter -> builder.withParameters(parameter, evaluate(parameter, context));
    }

    private Set<String> evaluate(String parameter, FilterContext context) {
        PolicyConfig config = configuration.getConfig(context.getEndpoint());
        ThematicFilter evaluator = new ThematicFilter(context, config);
        Set<String> values = !context.hasParameter(parameter)
                ? context.getServiceParameterValues(parameter)
                : context.getValues(parameter);
        return evaluator.evaluate(parameter, values);
    }

    private Set<Timespan> evaluateTimespans(FilterContext context) {
        Set<Timespan> timespans = context.getTimespans();
        return evaluateTimeFloating("timespan", timespans, context);
    }

    private Set<Timespan> evaluateTimeFloating(String string, Set<Timespan> timespans, FilterContext context) {
        PolicyConfig config = configuration.getConfig(context.getEndpoint());
        TimeFloatingFilter evaluator = new TimeFloatingFilter(context, config);
        return evaluator.evaluate("timespan", timespans);
    }

};
