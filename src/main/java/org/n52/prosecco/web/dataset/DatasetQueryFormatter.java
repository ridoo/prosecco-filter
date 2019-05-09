
package org.n52.prosecco.web.dataset;

import java.util.Optional;
import java.util.stream.Stream;

import org.n52.prosecco.web.request.FilterContext;
import org.n52.prosecco.web.request.QueryFormatter;
import org.n52.prosecco.web.request.Timespan;

public final class DatasetQueryFormatter extends QueryFormatter {

    @Override
    protected String[] formatFilteredQueryParameters(FilterContext context) {
        Optional<String> timespan = context.getTimespans()
                                           .stream()
                                           .map(Timespan::toString)
                                           .findFirst();
        return Stream.of(formatKVP(DatasetFilterParameter.PHENOMENON.filterName, context.getValues("phenomenon")),
                         formatKVP(DatasetFilterParameter.PROCEDURE.filterName, context.getValues("procedure")),
                         formatKVP(DatasetFilterParameter.OFFERING.filterName, context.getValues("offering")),
                         formatKVP(DatasetFilterParameter.CATEGORY.filterName, context.getValues("category")),
                         formatKVP(DatasetFilterParameter.FEATURE.filterName, context.getValues("feature")),
                         formatKVP(DatasetFilterParameter.TIMESPAN.filterName, timespan.orElse("")))
                     .filter(value -> !value.isEmpty())
                     .toArray(String[]::new);
    }

}
