
package org.n52.prosecco.filter;

import java.util.Set;

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
     * @throws DroppedQueryConditionException
     *         in cases where filtering would falsify the actual query (e.g. dropping an AND condition)
     */
    public FilterContext evaluate(FilterContext context) throws DroppedQueryConditionException {
        FilterContextBuilder builder = FilterContext.fromContext(context)
                                                    .withTimespans(evaluateTimespans(context));
        for (String parameter : context.getThematicParameterNames()) {
            Set<String> filteredContext = evaluate(parameter, context);
            builder.withParameters(parameter, filteredContext);
        }
        return builder.build();
    }

    private Set<String> evaluate(String parameter, FilterContext context) throws DroppedQueryConditionException {
        boolean hasQueryValues = context.hasParameter(parameter);
        Set<String> values = !hasQueryValues
                ? context.getAllowedValues(parameter)
                : context.getValues(parameter);

        PolicyConfig config = configuration.getConfig(context.getEndpoint());
        ThematicFilter evaluator = new ThematicFilter(context, config);
        Set<String> filteredQueryValues = evaluator.evaluate(parameter, values);
        if (isDroppingQueryCondition(hasQueryValues, filteredQueryValues)) {
            Set<String> queryValues = context.getValues(parameter);
            String message = "Filter drops condition for '" + parameter + "'";
            throw new DroppedQueryConditionException(parameter, queryValues, message);
        }
        return filteredQueryValues;
    }

    private boolean isDroppingQueryCondition(boolean hasQueryValues, Set<String> filteredValues) {
        return hasQueryValues && filteredValues.isEmpty();
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
