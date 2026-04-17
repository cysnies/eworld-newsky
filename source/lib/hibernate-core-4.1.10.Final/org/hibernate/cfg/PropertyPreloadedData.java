package org.hibernate.cfg;

import org.hibernate.MappingException;
import org.hibernate.annotations.common.reflection.XClass;
import org.hibernate.annotations.common.reflection.XProperty;

public class PropertyPreloadedData implements PropertyData {
   private final AccessType defaultAccess;
   private final String propertyName;
   private final XClass returnedClass;

   public PropertyPreloadedData(AccessType defaultAccess, String propertyName, XClass returnedClass) {
      this.defaultAccess = defaultAccess;
      this.propertyName = propertyName;
      this.returnedClass = returnedClass;
   }

   public AccessType getDefaultAccess() throws MappingException {
      return this.defaultAccess;
   }

   public String getPropertyName() throws MappingException {
      return this.propertyName;
   }

   public XClass getClassOrElement() throws MappingException {
      return this.getPropertyClass();
   }

   public XClass getPropertyClass() throws MappingException {
      return this.returnedClass;
   }

   public String getClassOrElementName() throws MappingException {
      return this.getTypeName();
   }

   public String getTypeName() throws MappingException {
      return this.returnedClass == null ? null : this.returnedClass.getName();
   }

   public XProperty getProperty() {
      return null;
   }

   public XClass getDeclaringClass() {
      return null;
   }
}
