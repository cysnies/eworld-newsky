package org.hibernate.cfg;

import org.hibernate.MappingException;
import org.hibernate.annotations.common.reflection.XClass;
import org.hibernate.annotations.common.reflection.XProperty;
import org.hibernate.internal.util.StringHelper;

public class WrappedInferredData implements PropertyData {
   private PropertyData wrappedInferredData;
   private String propertyName;

   public XClass getClassOrElement() throws MappingException {
      return this.wrappedInferredData.getClassOrElement();
   }

   public String getClassOrElementName() throws MappingException {
      return this.wrappedInferredData.getClassOrElementName();
   }

   public AccessType getDefaultAccess() {
      return this.wrappedInferredData.getDefaultAccess();
   }

   public XProperty getProperty() {
      return this.wrappedInferredData.getProperty();
   }

   public XClass getDeclaringClass() {
      return this.wrappedInferredData.getDeclaringClass();
   }

   public XClass getPropertyClass() throws MappingException {
      return this.wrappedInferredData.getPropertyClass();
   }

   public String getPropertyName() throws MappingException {
      return this.propertyName;
   }

   public String getTypeName() throws MappingException {
      return this.wrappedInferredData.getTypeName();
   }

   public WrappedInferredData(PropertyData inferredData, String suffix) {
      this.wrappedInferredData = inferredData;
      this.propertyName = StringHelper.qualify(inferredData.getPropertyName(), suffix);
   }
}
