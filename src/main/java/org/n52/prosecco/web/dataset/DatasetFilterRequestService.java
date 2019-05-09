package org.n52.prosecco.web.dataset;

import static org.n52.prosecco.web.dataset.DatasetFilterParameter.FEATURE;
import static org.n52.prosecco.web.dataset.DatasetFilterParameter.OFFERING;
import static org.n52.prosecco.web.dataset.DatasetFilterParameter.PHENOMENON;
import static org.n52.prosecco.web.dataset.DatasetFilterParameter.PROCEDURE;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.n52.prosecco.AuthenticationContext;
import org.n52.prosecco.filter.RequestFilterEngine;
import org.n52.prosecco.web.FilterException;
import org.n52.prosecco.web.FilterRequestService;
import org.n52.prosecco.web.request.FilterContext;
import org.n52.prosecco.web.request.Timespan;
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
        return FilterContext.of(roles)
                            .withTimespans(parseTimespan(temporalFilter))
                            .withFeatures(valuesByParameter.get(FEATURE.filterName))
                            .withPhenomena(valuesByParameter.get(PHENOMENON.filterName))
                            .withProcedures(valuesByParameter.get(PROCEDURE.filterName))
                            .withOfferings(valuesByParameter.get(OFFERING.filterName))
                            // TODO .withServiceParameters(serviceParameters)
                            .andRemainingQuery(getRemainingQuery(valuesByParameter))
                            .build();
    }

    private Timespan parseTimespan(String temporalFilter) {
        // TODO Auto-generated method stub
        return null;
    }

    private Map<String, String[]> getRemainingQuery(Map<String, String[]> valuesByParameter) {
        return valuesByParameter.entrySet()
                                .stream()
                                .filter(e -> !DatasetFilterParameter.isKnown(e.getKey()))
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
