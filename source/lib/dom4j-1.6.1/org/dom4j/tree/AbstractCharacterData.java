package org.dom4j.tree;

import org.dom4j.CharacterData;
import org.dom4j.Element;

public abstract class AbstractCharacterData extends AbstractNode implements CharacterData {
   public String getPath(Element context) {
      Element parent = this.getParent();
      return parent != null && parent != context ? parent.getPath(context) + "/text()" : "text()";
   }

   public String getUniquePath(Element context) {
      Element parent = this.getParent();
      return parent != null && parent != context ? parent.getUniquePath(context) + "/text()" : "text()";
   }

   public void appendText(String text) {
      this.setText(this.getText() + text);
   }
}
