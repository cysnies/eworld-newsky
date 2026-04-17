package org.hibernate.annotations.common.annotationfactory;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

public class AnnotationDescriptor {
   private final Class type;
   private final Map elements = new HashMap();

   public AnnotationDescriptor(Class annotationType) {
      this.type = annotationType;
   }

   public void setValue(String elementName, Object value) {
      this.elements.put(elementName, value);
   }

   public Object valueOf(String elementName) {
      return this.elements.get(elementName);
   }

   public boolean containsElement(String elementName) {
      return this.elements.containsKey(elementName);
   }

   public int numberOfElements() {
      return this.elements.size();
   }

   public Class type() {
      return this.type;
   }
}
