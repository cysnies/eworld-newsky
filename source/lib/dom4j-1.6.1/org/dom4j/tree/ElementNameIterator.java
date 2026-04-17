package org.dom4j.tree;

import java.util.Iterator;
import org.dom4j.Element;

/** @deprecated */
public class ElementNameIterator extends FilterIterator {
   private String name;

   public ElementNameIterator(Iterator proxy, String name) {
      super(proxy);
      this.name = name;
   }

   protected boolean matches(Object object) {
      if (object instanceof Element) {
         Element element = (Element)object;
         return this.name.equals(element.getName());
      } else {
         return false;
      }
   }
}
