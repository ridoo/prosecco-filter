
package org.n52.prosecco.web.dataset;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.n52.prosecco.web.request.FilterContext;
import org.n52.prosecco.web.request.FilterContext.FilterContextBuilder;
import org.n52.prosecco.web.request.Timespan;

public class DatasetQueryFormatterTest {

    @Test
    public void given_filterContextWithCategory_when_formattingQuery_then_expectCategoryIsPresent() {
        FilterContextBuilder builder = FilterContextBuilder.of(Collections.emptySet());
        FilterContext filterContext = builder.withParameters("category", "cat")
                                             .build();

        DatasetQueryFormatter queryFormatter = new DatasetQueryFormatter();
        String[] queryParameters = queryFormatter.formatFilteredQueryParameters(filterContext);
        assertThat(queryParameters).containsExactly("categories=cat");
    }

    @Test
    public void given_filterContextWithTimespan_when_formattingQuery_then_expectTimespanIsPresent() {
        LocalDate now = LocalDate.now();
        Timespan timespan = Timespan.between(now.minus(1, ChronoUnit.DAYS), now);
        FilterContextBuilder builder = FilterContextBuilder.of(Collections.emptySet());
        FilterContext filterContext = builder.withTimespans(timespan)
                                             .build();

        DatasetQueryFormatter queryFormatter = new DatasetQueryFormatter();
        String[] queryParameters = queryFormatter.formatFilteredQueryParameters(filterContext);
        assertThat(queryParameters).containsExactly("timespan=" + timespan);
    }
    
    @Test
    public void given_filterContextWithRemainingParameters_when_formattingContext_then_expectRemainingIsPresent() {
        Map<String, String[]> remainingQuery = new HashMap<>();
        remainingQuery.put("expanded", new String[] { "true" });
        FilterContextBuilder builder = FilterContextBuilder.of(Collections.emptySet());
        FilterContext filterContext = builder.andRemainingFrom(remainingQuery, e -> true)
                                             .build();

        DatasetQueryFormatter queryFormatter = new DatasetQueryFormatter();
        String queryParameters = queryFormatter.format(filterContext);
        assertThat(queryParameters).isEqualTo("expanded=true");
    }
}
