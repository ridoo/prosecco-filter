
package org.n52.prosecco.web.sos;

import static org.n52.prosecco.web.sos.SosFilterParameter.FEATURE;
import static org.n52.prosecco.web.sos.SosFilterParameter.OFFERING;
import static org.n52.prosecco.web.sos.SosFilterParameter.PHENOMENON;
import static org.n52.prosecco.web.sos.SosFilterParameter.PROCEDURE;
import static org.n52.prosecco.web.sos.SosFilterParameter.TIMESPAN;

import java.time.DateTimeException;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.n52.prosecco.AuthenticationContext;
import org.n52.prosecco.filter.DroppedQueryConditionException;
import org.n52.prosecco.filter.RequestFilterEngine;
import org.n52.prosecco.web.FilterException;
import org.n52.prosecco.web.FilterRequestService;
import org.n52.prosecco.web.GetRequestFilter;
import org.n52.prosecco.web.request.FilterContext;
import org.n52.prosecco.web.request.Timespan;
import org.n52.prosecco.web.request.TimespanParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public final class SosFilterRequestService implements FilterRequestService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SosFilterRequestService.class);
    
    private final GetRequestFilter filterGetService;

    private final SosRequestPostFilter filterPostService;

    private final AuthenticationContext authContext;
    
    public SosFilterRequestService(RequestFilterEngine filterEngine, AuthenticationContext authContext) {
        this.filterGetService = new SosRequestGetFilter(filterEngine);
        this.filterPostService = new SosRequestPostFilter(filterEngine);
        this.authContext = authContext;
    }

    @Override
    public String filterGET(HttpServletRequest request) throws FilterException, DroppedQueryConditionException {
        FilterContext context = createFilterContext(request, authContext.getRoles());
        return filterGetService.filter(request, context);
    }

    @Override
    public String filterPOST(HttpServletRequest request) throws FilterException {
        FilterContext context = createFilterContext(request, authContext.getRoles());
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
        Map<String, String[]> valuesByParameter = request.getParameterMap();
        return FilterContext.create("sos", roles)
                            .withTimespans(parseTimespan(temporalFilter))
                            .withParameters("feature", valuesByParameter.get(FEATURE.filterName))
                            .withParameters("phenomenon", valuesByParameter.get(PHENOMENON.filterName))
                            .withParameters("procedure", valuesByParameter.get(PROCEDURE.filterName))
                            .withParameters("offering", valuesByParameter.get(OFFERING.filterName))
                            // TODO .withAllowedParameters(allowedParameters)
                            .andRemainingFrom(valuesByParameter, e -> !SosFilterParameter.isKnown(e.getKey()))
                            .build();
    }

    private Timespan parseTimespan(String temporalFilter) throws FilterException {
        try {
            TimespanParser timespanParser = new TimespanParser("om:phenomenontime");
            return timespanParser.parsePhenomenonTime(temporalFilter);
        } catch (IllegalArgumentException | DateTimeException e) {
            LOGGER.error("Could not parse temporal filter: {}", temporalFilter, e);
            throw new FilterException("Invalid temporal filter: " + temporalFilter);
        }
    }

}
