package org.dom4j.util;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.QName;

public class AttributeHelper {
   protected AttributeHelper() {
   }

   public static boolean booleanValue(Element element, String attributeName) {
      return booleanValue(element.attribute(attributeName));
   }

   public static boolean booleanValue(Element element, QName attributeQName) {
      return booleanValue(element.attribute(attributeQName));
   }

   protected static boolean booleanValue(Attribute attribute) {
      if (attribute == null) {
         return false;
      } else {
         Object value = attribute.getData();
         if (value == null) {
            return false;
         } else if (value instanceof Boolean) {
            Boolean b = (Boolean)value;
            return b;
         } else {
            return "true".equalsIgnoreCase(value.toString());
         }
      }
   }
}
