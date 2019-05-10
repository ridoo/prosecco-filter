package org.n52.prosecco.web.dataset;

import static org.n52.prosecco.web.dataset.DatasetFilterParameter.FEATURE;
import static org.n52.prosecco.web.dataset.DatasetFilterParameter.OFFERING;
import static org.n52.prosecco.web.dataset.DatasetFilterParameter.PHENOMENON;
import static org.n52.prosecco.web.dataset.DatasetFilterParameter.PROCEDURE;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.n52.prosecco.AuthenticationContext;
import org.n52.prosecco.filter.RequestFilterEngine;
import org.n52.prosecco.web.FilterException;
import org.n52.prosecco.web.FilterRequestService;
import org.n52.prosecco.web.request.FilterContext;
import org.n52.prosecco.web.request.Timespan;
import org.n52.prosecco.web.request.TimespanParser;
import org.springframework.stereotype.Component;

@Component
public class DatasetFilterRequestService implements FilterRequestService {

    static DatasetFilterRequestService withEmptyAuthenticationContext(RequestFilterEngine filterEngine) {
        AuthenticationContext emptyAuthenticationContext = new AuthenticationContext() {
            @Override
            public Set<String> getRoles() {
                return Collections.emptySet();
            }
        };
        return new DatasetFilterRequestService(filterEngine, emptyAuthenticationContext);
    }

    private final DatasetFilterGetRequestService filterGetService;
    
    private final DatasetFilterPostRequestService filterPostService;
    
    private final AuthenticationContext authContext;
    
    public DatasetFilterRequestService(RequestFilterEngine filterEngine, AuthenticationContext authContext) {
        this.filterGetService = new DatasetFilterGetRequestService(filterEngine);
        this.filterPostService = new DatasetFilterPostRequestService(filterEngine);
        this.authContext = authContext;
    }

    @Override
    public String filterGET(HttpServletRequest request) throws FilterException {
        FilterContext context = createFilterContext(request, authContext.getRoles());
        return filterGetService.filter(request, context);
    }

    @Override
    public String filterPOST(HttpServletRequest request) throws FilterException {
        FilterContext context = createFilterContext(request, authContext.getRoles());
        return filterPostService.filter(request, context);
    }

    private FilterContext createFilterContext(HttpServletRequest request, Set<String> roles)
            throws FilterException {
        String temporalFilter = request.getParameter(DatasetFilterParameter.TIMESPAN.filterName);
        Map<String, String[]> valuesByParameter = request.getParameterMap();
        return FilterContext.create("ds", roles)
                            .withTimespans(parseTimespan(temporalFilter))
                            .withParameters("feature", valuesByParameter.get(FEATURE.filterName))
                            .withParameters("phenomenon", valuesByParameter.get(PHENOMENON.filterName))
                            .withParameters("procedures", valuesByParameter.get(PROCEDURE.filterName))
                            .withParameters("offering", valuesByParameter.get(OFFERING.filterName))
                            // TODO .withAllowedParameters(allowedParameters)
                            .andRemainingFrom(valuesByParameter, e -> !DatasetFilterParameter.isKnown(e.getKey()))
                            .build();
    }

    private Timespan parseTimespan(String temporalFilter) {
        return new TimespanParser().parsePhenomenonTime(temporalFilter);
    }


}
