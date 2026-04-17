package org.dom4j.util;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

public class XMLErrorHandler implements ErrorHandler {
   protected static final QName ERROR_QNAME = QName.get("error");
   protected static final QName FATALERROR_QNAME = QName.get("fatalError");
   protected static final QName WARNING_QNAME = QName.get("warning");
   private Element errors;
   private QName errorQName;
   private QName fatalErrorQName;
   private QName warningQName;

   public XMLErrorHandler() {
      this.errorQName = ERROR_QNAME;
      this.fatalErrorQName = FATALERROR_QNAME;
      this.warningQName = WARNING_QNAME;
      this.errors = DocumentHelper.createElement("errors");
   }

   public XMLErrorHandler(Element errors) {
      this.errorQName = ERROR_QNAME;
      this.fatalErrorQName = FATALERROR_QNAME;
      this.warningQName = WARNING_QNAME;
      this.errors = errors;
   }

   public void error(SAXParseException e) {
      Element element = this.errors.addElement(this.errorQName);
      this.addException(element, e);
   }

   public void fatalError(SAXParseException e) {
      Element element = this.errors.addElement(this.fatalErrorQName);
      this.addException(element, e);
   }

   public void warning(SAXParseException e) {
      Element element = this.errors.addElement(this.warningQName);
      this.addException(element, e);
   }

   public Element getErrors() {
      return this.errors;
   }

   public void setErrors(Element errors) {
      this.errors = errors;
   }

   public QName getErrorQName() {
      return this.errorQName;
   }

   public void setErrorQName(QName errorQName) {
      this.errorQName = errorQName;
   }

   public QName getFatalErrorQName() {
      return this.fatalErrorQName;
   }

   public void setFatalErrorQName(QName fatalErrorQName) {
      this.fatalErrorQName = fatalErrorQName;
   }

   public QName getWarningQName() {
      return this.warningQName;
   }

   public void setWarningQName(QName warningQName) {
      this.warningQName = warningQName;
   }

   protected void addException(Element element, SAXParseException e) {
      element.addAttribute("column", Integer.toString(e.getColumnNumber()));
      element.addAttribute("line", Integer.toString(e.getLineNumber()));
      String publicID = e.getPublicId();
      if (publicID != null && publicID.length() > 0) {
         element.addAttribute("publicID", publicID);
      }

      String systemID = e.getSystemId();
      if (systemID != null && systemID.length() > 0) {
         element.addAttribute("systemID", systemID);
      }

      element.addText(e.getMessage());
   }
}
