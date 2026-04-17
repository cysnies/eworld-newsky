package org.hibernate;

import org.hibernate.internal.util.StringHelper;

public class PropertyAccessException extends HibernateException {
   private final Class persistentClass;
   private final String propertyName;
   private final boolean wasSetter;

   public PropertyAccessException(Throwable root, String s, boolean wasSetter, Class persistentClass, String propertyName) {
      super(s, root);
      this.persistentClass = persistentClass;
      this.wasSetter = wasSetter;
      this.propertyName = propertyName;
   }

   public Class getPersistentClass() {
      return this.persistentClass;
   }

   public String getPropertyName() {
      return this.propertyName;
   }

   public String getMessage() {
      return super.getMessage() + (this.wasSetter ? " setter of " : " getter of ") + StringHelper.qualify(this.persistentClass.getName(), this.propertyName);
   }
}
