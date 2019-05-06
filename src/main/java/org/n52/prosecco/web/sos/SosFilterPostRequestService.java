
package org.n52.prosecco.web.sos;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.n52.prosecco.filter.RequestContextFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public final class SosFilterPostRequestService extends SosFilterRequestService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SosFilterPostRequestService.class);
    
    private final RequestContextFilter requestContextEvaluator;
    
    public SosFilterPostRequestService(RequestContextFilter requestContextEvaluator) {
        this.requestContextEvaluator = requestContextEvaluator;
    }

    public String filter(HttpServletRequest request) throws FilterRequestException {
        
        return readRequestBody(request);
    }

    String readRequestBody(HttpServletRequest request) {
        try (BufferedReader reader = request.getReader()) {
            
            
            //XmlObject xmlDoc = XmlObject.Factory.parse(reader);
            
            // TODO Auto-generated method stub
            
            return reader.lines().collect(Collectors.joining());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            LOGGER.error("Could not read request payload: {}", e);
            throw new RuntimeException("Error processing the request!");
        }
    }

}
