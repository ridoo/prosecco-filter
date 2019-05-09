
package org.n52.prosecco.web.sos;

import java.util.Set;
import java.util.stream.Collectors;

import org.n52.prosecco.web.request.FilterContext;
import org.n52.prosecco.web.request.QueryFormatter;
import org.n52.prosecco.web.request.Timespan;

public final class SosQueryFormatter extends QueryFormatter {

    @Override
    protected String[] formatFilteredQueryParameters(FilterContext context) {
        Set<String> timespans = context.getTimespans()
                                       .stream()
                                       .map(Timespan::toString)
                                       .map("om:phenomenonTime,"::concat)
                                       .collect(Collectors.toSet());
        return new String[] {
            formatKVP(SosFilterParameter.PHENOMENON.filterName, context.getValues("phenomenon")),
            formatKVP(SosFilterParameter.PROCEDURE.filterName, context.getValues("procedure")),
            formatKVP(SosFilterParameter.OFFERING.filterName, context.getValues("offering")),
            formatKVP(SosFilterParameter.FEATURE.filterName, context.getValues("feature")),
            formatKVP(SosFilterParameter.TIMESPAN.filterName, timespans)
        };
    }

}
