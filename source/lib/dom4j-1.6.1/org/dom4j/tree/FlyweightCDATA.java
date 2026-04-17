package org.dom4j.tree;

import org.dom4j.CDATA;
import org.dom4j.Element;
import org.dom4j.Node;

public class FlyweightCDATA extends AbstractCDATA implements CDATA {
   protected String text;

   public FlyweightCDATA(String text) {
      this.text = text;
   }

   public String getText() {
      return this.text;
   }

   protected Node createXPathResult(Element parent) {
      return new DefaultCDATA(parent, this.getText());
   }
}
