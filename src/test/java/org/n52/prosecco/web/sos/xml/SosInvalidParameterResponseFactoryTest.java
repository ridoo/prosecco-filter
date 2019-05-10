
package org.n52.prosecco.web.sos.xml;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.w3c.dom.Node;
import org.xmlunit.assertj.XmlAssert;

public class SosInvalidParameterResponseFactoryTest {

    private static final HashMap<String, String> NS_MAPPING = new HashMap<String, String>();

    @Before
    public void setUp() {
        NS_MAPPING.put("ows", "http://www.opengis.net/ows/1.1");
        NS_MAPPING.put("xsi", "http://www.w3.org/2001/XMLSchema-instance");
    }

    @Test(expected = IllegalArgumentException.class)
    public void given_noParameters_when_createExceptionReport_then_exception() {
        createExceptionReport("offering", "2.0.0", new String[0]);
    }

    @Test
    public void given_invalidParameters_when_createExceptionReport_then_owsNamespaceIsSet() {
        ResponseEntity<String> entity = createExceptionReport("offering", "2.0.0", "off1");
        XmlAssert.assertThat(entity.getBody())
                 .withNamespaceContext(NS_MAPPING)
                 .nodesByXPath("//ows:ExceptionReport")
                 .exist();
    }

    @Test
    public void given_invalidParameters_when_createExceptionReport_then_versionAttributeIsSet() {
        ResponseEntity<String> entity = createExceptionReport("offering", "2.0.0", "off1");
        XmlAssert.assertThat(entity.getBody())
                 .withNamespaceContext(NS_MAPPING)
                 .nodesByXPath("//ows:ExceptionReport")
                 .allSatisfy(n -> {
                     assertThat(getAttributeValue(n, "version")).isEqualTo("2.0.0");
                 })
                 .exist();
    }

    @Test
    public void given_invalidParameter_when_createExceptionReport_then_exceptionEntryHasProperAttributes() {
        ResponseEntity<String> entity = createExceptionReport("offering", "2.0.0", "off1");
        XmlAssert.assertThat(entity.getBody())
                 .withNamespaceContext(NS_MAPPING)
                 .nodesByXPath("//ows:ExceptionReport/ows:Exception")
                 .allSatisfy(n -> {
                     assertThat(getAttributeValue(n, "exceptionCode")).isEqualTo("InvalidParameterValue");
                     assertThat(getAttributeValue(n, "locator")).isEqualTo("offering");
                 })
                 .exist();
    }

//    @Test
//    public void given_invalidParameters_when_createExceptionReport_then_exceptionEntries() {
//        ResponseEntity<String> entity = createExceptionReport("offering", "2.0.0", "off1", "off2");
//        XmlAssert.assertThat(entity.getBody())
//                 .withNamespaceContext(NS_MAPPING)
//                 .nodesByXPath("//ows:ExceptionReport/ows:Exception[@locator='offering']/ows:ExceptionText")
//                 .anySatisfy(n -> {
//                     assertThat(n.getTextContent()).isEqualTo("off1");
//                     assertThat(n.getNodeValue()).isEqualTo("off2");
//                 })
//                 .exist();
//    }

    private String getAttributeValue(Node n, String version) {
        return n.getAttributes()
                .getNamedItem(version)
                .getNodeValue();
    }

    private ResponseEntity<String> createExceptionReport(String parameter, String version, String... values) {
        return SosInvalidParameterResponseFactory.create(parameter, toSet(values), version);
    }

    private static Set<String> toSet(String... values) {
        return values == null
                ? Collections.emptySet()
                : new HashSet<>(Arrays.asList(values));
    }
}
