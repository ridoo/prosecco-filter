
package org.n52.prosecco.web.request;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class QueryFormatter {

    public String format(FilterContext context) {
        return Stream.of(formatFilteredQueryParameters(context),
                         formatRemainingQueryParameters(context))
                     .filter(a -> a.length > 0)
                     .flatMap(Arrays::stream)
                     .filter(kvp -> !kvp.isEmpty())
                     .collect(Collectors.joining("&"));
    }

    protected abstract String[] formatFilteredQueryParameters(FilterContext context);

    private String[] formatRemainingQueryParameters(FilterContext context) {
        Map<String, Set<String>> remainingQuery = context.getRemainingQuery();
        Set<Entry<String, Set<String>>> remainingEntries = remainingQuery.entrySet();
        return remainingEntries.stream()
                               .map(this::formatKVP)
                               .toArray(String[]::new);
    }

    protected String formatKVP(String parameter, String value) {
        return formatKVP(parameter, Collections.singleton(value));
    }

    protected String formatKVP(String parameter, Set<String> values) {
        return formatKVP(parameter, values.stream());
    }

    private String formatKVP(Map.Entry<String, Set<String>> entry) {
        return formatKVP(entry.getKey(), entry.getValue());
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
