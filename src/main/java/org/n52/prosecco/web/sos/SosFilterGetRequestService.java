
package org.n52.prosecco.web.sos;

import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import org.n52.prosecco.filter.RequestContextFilter;
import org.n52.prosecco.web.request.FilterContext;
import org.n52.prosecco.web.request.Timespan;
import org.springframework.stereotype.Component;

@Component
public final class SosFilterGetRequestService extends SosFilterRequestService {

    private final RequestContextFilter requestContextEvaluator;

    public SosFilterGetRequestService(RequestContextFilter requestContextEvaluator) {
        this.requestContextEvaluator = requestContextEvaluator;
    }

    @Override
    public String filter(HttpServletRequest request) throws FilterRequestException {
        String operation = request.getParameter("request");
        FilterContext requestContext = createFilterContext(getRoles(), request);
        return !"GetCapabilities".equalsIgnoreCase(operation)
                ? createQueryString(requestContextEvaluator.evaluate(requestContext))
                : createQueryString(requestContext);
    }

    private String createQueryString(FilterContext context) {
        return Stream.of(formatFilteredQueryParameters(context),
                         formatRemainingQueryParameters(context))
                     .filter(a -> a.length > 0)
                     .flatMap(Arrays::stream)
                     .filter(kvp -> !kvp.isEmpty())
                     .collect(Collectors.joining("&"));
    }

    private String[] formatFilteredQueryParameters(FilterContext context) {
        Set<String> timespans = context.getTimespans()
                                       .stream()
                                       .map(Timespan::toString)
                                       .collect(Collectors.toSet());
        return new String[] {
            formatKVP(SosFilterParameter.PHENOMENON, context.getPhenomena()),
            formatKVP(SosFilterParameter.PROCEDURE, context.getProcedures()),
            formatKVP(SosFilterParameter.OFFERING, context.getOfferings()),
            formatKVP(SosFilterParameter.FEATURE, context.getFeatures()),
            formatKVP(SosFilterParameter.TIMESPAN, timespans)
        };
    }

    private String[] formatRemainingQueryParameters(FilterContext context) {
        Map<String, String[]> remainingQuery = context.getRemainingQuery();
        Set<Entry<String, String[]>> remainingEntries = remainingQuery.entrySet();
        return remainingEntries.stream()
                               .map(this::formatKVP)
                               .toArray(String[]::new);
    }

    private String formatKVP(Map.Entry<String, String[]> entry) {
        return formatKVP(entry.getKey(), Stream.of(entry.getValue()));
    }

    private String formatKVP(SosFilterParameter parameter, Set<String> values) {
        return formatKVP(parameter.filterName, values.stream());
    }

    private String formatKVP(String parameter, Stream<String> values) {
        String csv = asCsv(values);
        return csv.length() > 0
                ? parameter + "=" + csv
                : "";
    }

    private String asCsv(Stream<String> items) {
        return items != null
                ? items.collect(Collectors.joining(","))
                : "";
    }

}
