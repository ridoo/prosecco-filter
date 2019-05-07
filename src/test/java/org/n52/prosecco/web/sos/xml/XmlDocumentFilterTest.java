
package org.n52.prosecco.web.sos.xml;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xmlunit.assertj.XmlAssert;
import org.xmlunit.builder.Input;
import org.xmlunit.builder.Input.Builder;

public class XmlDocumentFilterTest {

    private static final String XPATH = "/Capabilities/OperationsMetadata/Operation/"
            + "Parameter[@name='procedure']/AllowedValues/Value[text()='file32']";

    private static final String XPATH_NS = "/sos:Capabilities/ows:OperationsMetadata/ows:Operation/"
            + "ows:Parameter[@name='procedure']/ows:AllowedValues/ows:Value[text()='file32']";

//    private File file;
//
//    @Before
//    public void setUp() throws URISyntaxException {
//        URL resource = getClass().getResource("/capabilities.xml");
//        this.file = new File(resource.toURI());
//    }

    //
    // @Test
    // public void test_vgt() throws Exception {
    // VTDGen vg = new VTDGen();
    // AutoPilot ap = new AutoPilot();
    //
    // ap.selectXPath(XPATH);
    // XMLModifier xm = new XMLModifier();
    //
    // assertThat(vg.parseFile(file.getAbsolutePath(), false)).isTrue();
    // VTDNav nav = vg.getNav();
    // ap.bind(nav);
    // xm.bind(nav);
    // int i = 0;
    // while ( (i = ap.evalXPath()) != -1) {
    // xm.remove();
    // }
    // xm.output("/tmp/capabilities_filtered_vgt.xml");
    // }
    //
//    @Test
//    public void test_javax_xpath() throws Exception {
//        XPathFactory xpFactory = XPathFactory.newInstance();
//        XPath xp = xpFactory.newXPath();
//        // XPathExpression xpExpression = xp.compile(XPATH);
//        XPathExpression xpExpression = xp.compile("//*[local-name()='Capabilities']");
//
//        InputSource source = new InputSource(new FileReader(file));
//        NodeList result = (NodeList) xpExpression.evaluate(source, XPathConstants.NODESET);
//        // XPathEvaluationResult< ? > results = xpExpression.evaluateExpression(source);
//        // XPathNodes result = (XPathNodes) results.value();
//
//        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//        factory.setNamespaceAware(true);
//        System.out.println("ns aware: " + factory.isNamespaceAware());
//        Document document = factory.newDocumentBuilder()
//                                   .parse(file);
//        String root = document.getDocumentElement()
//                              .getTagName();
//
//        for (int i = 0; i < result.getLength(); i++) {
//            Node node = result.item(i);
//            System.out.println(node.getTextContent());
//        }
//        // no way to write back to file
//    }
    //
    // @Test
    // public void when_multipleSiblings_when_deleteFirst_then_firstSiblingRemoved() throws Exception {
    // String xml = "<foo><bar><baz a1=\"blah\">test</baz><test>empty</test></bar></foo>";
    //
    // String xpath = "/foo/bar/baz";
    // XMLFilterImpl filter = XPathSAXDeleteFilter.create(xpath);
    // InputSource source = asInputSource(xml);
    // SAXSource src = new SAXSource(filter, source);
    //
    // ByteArrayOutputStream stream = new ByteArrayOutputStream();
    // StreamResult result = new StreamResult(stream);
    //
    // TransformerFactory tFactory = TransformerFactory.newInstance();
    // Transformer transformer = tFactory.newTransformer();
    // transformer.transform(src, result);
    //
    // String actualXML = new String(stream.toByteArray());
    // assertThat(actualXML).isEqualTo("<?xml version=\"1.0\"
    // encoding=\"UTF-8\"?><foo><bar><test>empty</test></bar></foo>");
    // }
    //
    // private InputSource asInputSource(String actualXml) {
    // return new InputSource(new StringReader(actualXml));
    // }

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
