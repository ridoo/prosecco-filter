
package org.n52.prosecco.web.sos;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.n52.prosecco.filter.RequestFilterEngine;
import org.n52.prosecco.web.FilterException;
import org.n52.prosecco.web.request.FilterContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SosRequestPostFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SosRequestPostFilter.class);

    private final RequestFilterEngine filterEngine;

    public SosRequestPostFilter(RequestFilterEngine filterEngine) {
        this.filterEngine = filterEngine;
    }

    public String filter(HttpServletRequest request, FilterContext context) throws FilterException {
        return readRequestBody(request);
    }

    String readRequestBody(HttpServletRequest request) {
        try (BufferedReader reader = request.getReader()) {

            // XmlObject xmlDoc = XmlObject.Factory.parse(reader);

            // TODO Auto-generated method stub

            return reader.lines()
                         .collect(Collectors.joining());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            LOGGER.error("Could not read request payload: {}", e);
            throw new RuntimeException("Error processing the request!");
        }
    }

}
