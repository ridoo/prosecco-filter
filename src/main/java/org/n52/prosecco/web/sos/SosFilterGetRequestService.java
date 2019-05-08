
package org.n52.prosecco.web.sos;

import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import org.n52.prosecco.filter.RequestFilterEngine;
import org.n52.prosecco.web.FilterException;
import org.n52.prosecco.web.request.FilterContext;
import org.n52.prosecco.web.request.Timespan;

public final class SosFilterGetRequestService {

    private final RequestFilterEngine filterEngine;

    public SosFilterGetRequestService(RequestFilterEngine filterEngine) {
        this.filterEngine = filterEngine;
    }

    public String filter(HttpServletRequest request, FilterContext context) throws FilterException {
        String operation = request.getParameter("request");
        return !"GetCapabilities".equalsIgnoreCase(operation)
                ? createQueryString(filterEngine.evaluate(context))
                : createQueryString(context);
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
                                       .map("om:phenomenonTime,"::concat)
                                       .collect(Collectors.toSet());
        return new String[] {
            formatKVP(SosFilterParameter.PHENOMENON.filterName, context.getPhenomena()),
            formatKVP(SosFilterParameter.PROCEDURE.filterName, context.getProcedures()),
            formatKVP(SosFilterParameter.OFFERING.filterName, context.getOfferings()),
            formatKVP(SosFilterParameter.FEATURE.filterName, context.getFeatures()),
            formatKVP(SosFilterParameter.TIMESPAN.filterName, timespans)
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

    private String formatKVP(String parameter, Set<String> values) {
        return formatKVP(parameter, values.stream());
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
