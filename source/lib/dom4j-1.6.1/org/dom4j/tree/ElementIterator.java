package org.dom4j.tree;

import java.util.Iterator;
import org.dom4j.Element;

/** @deprecated */
public class ElementIterator extends FilterIterator {
   public ElementIterator(Iterator proxy) {
      super(proxy);
   }

   protected boolean matches(Object element) {
      return element instanceof Element;
   }
}
