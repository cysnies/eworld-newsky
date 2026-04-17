package org.hibernate.internal.util.xml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.internal.CoreMessageLogger;
import org.jboss.logging.Logger;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

public class ErrorLogger implements ErrorHandler, Serializable {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, ErrorLogger.class.getName());
   private List errors;
   private String file;

   public ErrorLogger() {
   }

   public ErrorLogger(String file) {
      this.file = file;
   }

   public void error(SAXParseException error) {
      if (this.errors == null) {
         this.errors = new ArrayList();
      }

      this.errors.add(error);
   }

   public void fatalError(SAXParseException error) {
      this.error(error);
   }

   public void warning(SAXParseException warn) {
      LOG.parsingXmlWarning(warn.getLineNumber(), warn.getMessage());
   }

   public List getErrors() {
      return this.errors;
   }

   public void reset() {
      this.errors = null;
   }

   public boolean hasErrors() {
      return this.errors != null && this.errors.size() > 0;
   }

   public void logErrors() {
      if (this.errors != null) {
         for(SAXParseException e : this.errors) {
            if (this.file == null) {
               LOG.parsingXmlError(e.getLineNumber(), e.getMessage());
            } else {
               LOG.parsingXmlErrorForFile(this.file, e.getLineNumber(), e.getMessage());
            }
         }
      }

   }
}
