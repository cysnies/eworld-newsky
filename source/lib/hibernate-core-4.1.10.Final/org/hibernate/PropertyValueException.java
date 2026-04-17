package org.hibernate;

import org.hibernate.internal.util.StringHelper;

public class PropertyValueException extends HibernateException {
   private final String entityName;
   private final String propertyName;

   public PropertyValueException(String s, String entityName, String propertyName) {
      super(s);
      this.entityName = entityName;
      this.propertyName = propertyName;
   }

   public String getEntityName() {
      return this.entityName;
   }

   public String getPropertyName() {
      return this.propertyName;
   }

   public String getMessage() {
      return super.getMessage() + ": " + StringHelper.qualify(this.entityName, this.propertyName);
   }

   public static String buildPropertyPath(String parent, String child) {
      return parent + '.' + child;
   }
}
