
package org.n52.prosecco.web.sos.xml;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathNodes;

import org.n52.prosecco.web.FilterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

public final class XmlDocumentFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(XmlDocumentFilter.class);

    private final Document xml;

    public XmlDocumentFilter(HttpEntity<String> entity) throws FilterException {
        Objects.requireNonNull(entity, "entity is null");
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dbBuilder = dbFactory.newDocumentBuilder();
            this.xml = dbBuilder.parse(asInputSource(entity.getBody()));
        } catch (Exception e) {
            throw new FilterException("Could not parse XML", e);
        }
    }

    private InputSource asInputSource(String actualXml) {
        return new InputSource(new StringReader(actualXml));
    }
    
    public String getDocumentName() {
        return xml.getDocumentElement().getTagName();
    }

    public void applyRemove(Set<String> xpaths) {
        Consumer< ? super Node> removeAction = node -> {
            Node parentNode = node.getParentNode();
            Node previousSibling = node.getPreviousSibling();
            if (isEmptyTextNode(previousSibling)) {
                parentNode.removeChild(previousSibling);
            }
            parentNode.removeChild(node);
        };
        xpaths.stream()
              .map(this::toXPath)
              .filter(Optional::isPresent)
              .forEach(xpath -> applyActionOn(xpath.get(), removeAction));
    }

    private boolean isEmptyTextNode(Node previousSibling) {
        return previousSibling != null 
                && previousSibling.getNodeType() == Node.TEXT_NODE
                && previousSibling.getNodeValue().trim().isEmpty();
    }

    private void applyActionOn(XPathExpression xpath, Consumer< ? super Node> action) {
        try {
            xpath.evaluateExpression(xml, XPathNodes.class).forEach(action);
        } catch (XPathExpressionException e) {
            LOGGER.error("Unable to apply action on xpath result " + xpath, e);
        }
    }

    private Optional<XPathExpression> toXPath(String xpath) {
        try {
            XPathFactory xpFactory = XPathFactory.newInstance();
            XPath xp = xpFactory.newXPath();
            return Optional.of(xp.compile(xpath));
        } catch (XPathExpressionException e) {
            // TODO don't filter or validate xpaths
            LOGGER.error("Unable to create xpath from " + xpath, e);
            return Optional.empty();
        }
    }

    public String getFilteredXml() {
        try {
            xml.normalizeDocument();
            Source source = new DOMSource(xml);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);

            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();

            transformer.transform(source, result);
            return writer.getBuffer()
                         .toString();
        } catch (TransformerException e) {
            throw new RuntimeException("Unable to write XML!", e);
        }
    }

}
