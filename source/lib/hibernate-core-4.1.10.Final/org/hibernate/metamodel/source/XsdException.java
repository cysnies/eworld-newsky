package org.hibernate.metamodel.source;

import org.hibernate.HibernateException;

public class XsdException extends HibernateException {
   private final String xsdName;

   public XsdException(String message, String xsdName) {
      super(message);
      this.xsdName = xsdName;
   }

   public XsdException(String message, Throwable root, String xsdName) {
      super(message, root);
      this.xsdName = xsdName;
   }

   public String getXsdName() {
      return this.xsdName;
   }
}
