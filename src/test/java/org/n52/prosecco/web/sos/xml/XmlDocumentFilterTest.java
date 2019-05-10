
package org.n52.prosecco.web.sos.xml;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.xmlunit.assertj.XmlAssert;
import org.xmlunit.builder.Input;

public class XmlDocumentFilterTest {

    @Test
    public void given_xmlWithEmptyRoot_when_createFilter_then_noChangeOnNonFiltered() throws Exception {
        String xml = "<baz:foo xmlns:baz=\"http://baz.net\"/>";
        HttpEntity<String> entity = new HttpEntity<String>(xml);
        XmlDocumentFilter filter = new XmlDocumentFilter(entity);
        String actual = filter.getFilteredXml();
        XmlAssert.assertThat(Input.fromString(actual))
                 .and(Input.fromString(xml))
                 .areSimilar();
    }

    @Test
    public void given_xmlWithRootFoo_when_getDocName_then_returnFoo() throws Exception {
        String xml = "<foo/>";
        HttpEntity<String> entity = new HttpEntity<String>(xml);
        XmlDocumentFilter filter = new XmlDocumentFilter(entity);
        String actual = filter.getDocumentName();
        assertThat(actual).isEqualTo("foo");
    }

    @Test
    public void given_xmlRootHavingElement_when_removeElement_then_emptyRoot() throws Exception {
        String xml = "<foo xmlns=\"http://baz.net\"><toBeRemoved/></foo>";
        XmlDocumentFilter filter = new XmlDocumentFilter(new HttpEntity<String>(xml));
        filter.applyRemove(Collections.singleton("/foo/toBeRemoved"));
        XmlAssert.assertThat(Input.fromString(filter.getFilteredXml()))
                 .and(Input.fromString("<baz:foo xmlns:baz=\"http://baz.net\" />"))
                 .areSimilar();
    }
    
    @Test
    public void given_xml_when_allElementsMatchRemove_then_emptyElement() throws Exception {
        String xml = ""
                + "<foo>"
                + "  <bar>"
                + "    <baz a1=\"procedure\">"
                + "      <test>remove</test>"
                + "      <test>keep</test>"
                + "    </baz>"
                + "    <baz a1=\"phenomenon\">"
                + "      <test>remove</test>"
                + "    </baz>"
                + "  </bar>"
                + "</foo>";
        XmlDocumentFilter filter = new XmlDocumentFilter(new HttpEntity<String>(xml));
        filter.applyRemove(Collections.singleton("/foo/bar/baz[@a1='procedure']/test[text()='remove']"));
        String actual = filter.getFilteredXml();

        String expected = ""
                + "<foo>"
                + "  <bar>"
                + "    <baz a1=\"procedure\">"
                + "      <test>keep</test>"
                + "    </baz>"
                + "    <baz a1=\"phenomenon\">"
                + "      <test>remove</test>"
                + "    </baz>"
                + "  </bar>"
                + "</foo>";
        XmlAssert.assertThat(Input.fromString(actual))
                 .and(Input.fromString(expected))
                 .areSimilar();
    }
}
