package org.dom4j.tree;

import org.dom4j.Comment;
import org.dom4j.Element;
import org.dom4j.Node;

public class FlyweightComment extends AbstractComment implements Comment {
   protected String text;

   public FlyweightComment(String text) {
      this.text = text;
   }

   public String getText() {
      return this.text;
   }

   protected Node createXPathResult(Element parent) {
      return new DefaultComment(parent, this.getText());
   }
}
