package org.n52.prosecco.web.sos.xml;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class XPathConfig {

    private final Map<String, Set<String>> capabilitiesXPathsByParameter;

    @JsonCreator
    public XPathConfig(@JsonProperty("capabilities") Map<String, Set<String>> capabilitiesXPathsByParameter) {
        this.capabilitiesXPathsByParameter = capabilitiesXPathsByParameter != null
                ? capabilitiesXPathsByParameter
                : Collections.emptyMap();
    }
    

    public Map<String, Set<String>> getCapabilitiesXPathsByParameter() {
        return capabilitiesXPathsByParameter;
    }
    
}
