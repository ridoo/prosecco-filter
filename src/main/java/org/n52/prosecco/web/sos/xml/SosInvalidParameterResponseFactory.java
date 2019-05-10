
package org.n52.prosecco.web.sos.xml;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Set;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fasterxml.aalto.stax.OutputFactoryImpl;

public class SosInvalidParameterResponseFactory {

    public static ResponseEntity<String> create(String locator, Set<String> values, String version) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("No values to set in ExceptionReport!");
        }
        
        try {
            SosInvalidParameterResponseFactory factory = new SosInvalidParameterResponseFactory();
            String report = factory.createExceptionReport(locator, values, version);
            return new ResponseEntity<>(report, HttpStatus.OK);
        } catch (IOException | XMLStreamException e) {
            throw new RuntimeException("Could not create ExceptionReport!", e);
        }
    }
    
    private String createExceptionReport(String locator, Set<String> values, String version) throws XMLStreamException, IOException {

        try(StringWriter writer = new StringWriter()) {
            XMLStreamWriter xmlStreamWriter = createXmlWriter(writer);
            xmlStreamWriter.writeStartDocument();
            xmlStreamWriter.writeStartElement("ExceptionReport");
            xmlStreamWriter.writeAttribute("version", version);
            xmlStreamWriter.writeDefaultNamespace("http://www.opengis.net/ows/1.1");
            xmlStreamWriter.writeNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
            
            writeExceptionElements(locator, values, xmlStreamWriter);
            
            xmlStreamWriter.writeEndDocument();
            xmlStreamWriter.flush();
            return writer.toString();
        }
    }

    private XMLStreamWriter createXmlWriter(StringWriter writer) throws FactoryConfigurationError, XMLStreamException {
        XMLOutputFactory xmlOutputFactory = new OutputFactoryImpl();
        return xmlOutputFactory.createXMLStreamWriter(writer);
    }

    private void writeExceptionElements(String locator, Set<String> values, XMLStreamWriter writer) throws XMLStreamException {
        for (String value : values) {
            writer.writeStartElement("Exception");
            writer.writeAttribute("exceptionCode", "InvalidParameterValue");
            writer.writeAttribute("locator", locator);
            
            writer.writeStartElement("ExceptionText");
            writer.writeCharacters("Test value '" + value + "' of the parameter '" + locator + "' is invalid"); 
            writer.writeEndElement();
        }
        
    }

}
