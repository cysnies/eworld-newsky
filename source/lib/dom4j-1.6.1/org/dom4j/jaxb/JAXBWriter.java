package org.dom4j.jaxb;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import javax.xml.bind.Element;
import javax.xml.bind.JAXBException;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.xml.sax.SAXException;

public class JAXBWriter extends JAXBSupport {
   private XMLWriter xmlWriter;
   private OutputFormat outputFormat;

   public JAXBWriter(String contextPath) {
      super(contextPath);
      this.outputFormat = new OutputFormat();
   }

   public JAXBWriter(String contextPath, OutputFormat outputFormat) {
      super(contextPath);
      this.outputFormat = outputFormat;
   }

   public JAXBWriter(String contextPath, ClassLoader classloader) {
      super(contextPath, classloader);
   }

   public JAXBWriter(String contextPath, ClassLoader classloader, OutputFormat outputFormat) {
      super(contextPath, classloader);
      this.outputFormat = outputFormat;
   }

   public OutputFormat getOutputFormat() {
      return this.outputFormat;
   }

   public void setOutput(File file) throws IOException {
      this.getWriter().setOutputStream(new FileOutputStream(file));
   }

   public void setOutput(OutputStream outputStream) throws IOException {
      this.getWriter().setOutputStream(outputStream);
   }

   public void setOutput(Writer writer) throws IOException {
      this.getWriter().setWriter(writer);
   }

   public void startDocument() throws IOException, SAXException {
      this.getWriter().startDocument();
   }

   public void endDocument() throws IOException, SAXException {
      this.getWriter().endDocument();
   }

   public void write(Element jaxbObject) throws IOException, JAXBException {
      this.getWriter().write(this.marshal(jaxbObject));
   }

   public void writeClose(Element jaxbObject) throws IOException, JAXBException {
      this.getWriter().writeClose(this.marshal(jaxbObject));
   }

   public void writeOpen(Element jaxbObject) throws IOException, JAXBException {
      this.getWriter().writeOpen(this.marshal(jaxbObject));
   }

   public void writeElement(org.dom4j.Element element) throws IOException {
      this.getWriter().write(element);
   }

   public void writeCloseElement(org.dom4j.Element element) throws IOException {
      this.getWriter().writeClose(element);
   }

   public void writeOpenElement(org.dom4j.Element element) throws IOException {
      this.getWriter().writeOpen(element);
   }

   private XMLWriter getWriter() throws IOException {
      if (this.xmlWriter == null) {
         if (this.outputFormat != null) {
            this.xmlWriter = new XMLWriter(this.outputFormat);
         } else {
            this.xmlWriter = new XMLWriter();
         }
      }

      return this.xmlWriter;
   }
}
