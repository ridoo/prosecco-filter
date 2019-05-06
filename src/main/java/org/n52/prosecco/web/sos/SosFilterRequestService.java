
package org.n52.prosecco.web.sos;

import static org.n52.prosecco.web.sos.SosFilterParameter.FEATURE;
import static org.n52.prosecco.web.sos.SosFilterParameter.OFFERING;
import static org.n52.prosecco.web.sos.SosFilterParameter.PHENOMENON;
import static org.n52.prosecco.web.sos.SosFilterParameter.PROCEDURE;
import static org.n52.prosecco.web.sos.SosFilterParameter.TIMESPAN;

import java.time.DateTimeException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.n52.prosecco.filter.RequestContextFilter;
import org.n52.prosecco.web.FilterException;
import org.n52.prosecco.web.FilterRequestService;
import org.n52.prosecco.web.request.FilterContext;
import org.n52.prosecco.web.request.Timespan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public final class SosFilterRequestService implements FilterRequestService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SosFilterRequestService.class);

    private final SosFilterGetRequestService filterGetService;

    private final SosFilterPostRequestService filterPostService;

    public SosFilterRequestService(RequestContextFilter requestContextEvaluator) {
        this.filterGetService = new SosFilterGetRequestService(requestContextEvaluator);
        this.filterPostService = new SosFilterPostRequestService(requestContextEvaluator);
    }

    @Override
    public String filterGET(HttpServletRequest request) throws FilterException {
        FilterContext context = createFilterContext(request, getRoles());
        return filterGetService.filter(request, context);
    }

    @Override
    public String filterPOST(HttpServletRequest request) throws FilterException {
        FilterContext context = createFilterContext(request, getRoles());
        return filterPostService.filter(request, context);
    }

    /**
     * Creates a {@link FilterContext} from the given request and roles.
     * 
     * @param request
     *        the actual request
     * @param roles
     *        the roles
     * @return a filter context containing all relevant values
     * @throws FilterException
     *         when request is invalid
     */
    private FilterContext createFilterContext(HttpServletRequest request, Set<String> roles)
            throws FilterException {
        String temporalFilter = request.getParameter(TIMESPAN.filterName);
        Timespan timespan = parseTimespan(temporalFilter);
        Map<String, String[]> valuesByParameter = request.getParameterMap();
        return FilterContext.of(roles)
                            .withTimespans(timespan)
                            .withFeatures(valuesByParameter.get(FEATURE.filterName))
                            .withPhenomena(valuesByParameter.get(PHENOMENON.filterName))
                            .withProcedures(valuesByParameter.get(PROCEDURE.filterName))
                            .withOfferings(valuesByParameter.get(OFFERING.filterName))
                            // TODO .withServiceParameters(serviceParameters)
                            .andRemainingQuery(getRemainingQuery(valuesByParameter))
                            .build();
    }

    private Timespan parseTimespan(String temporalFilter) throws FilterException {
        try {
            return new TimespanParser().parsePhenomenonTime(temporalFilter);
        } catch (IllegalArgumentException | DateTimeException e) {
            LOGGER.error("Could not parse temporal filter: {}", temporalFilter, e);
            throw new FilterException("Invalid temporal filter: " + temporalFilter);
        }
    }

    private Map<String, String[]> getRemainingQuery(Map<String, String[]> valuesByParameter) {
        return valuesByParameter.entrySet()
                                .stream()
                                .filter(e -> !SosFilterParameter.isKnown(e.getKey()))
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

}
