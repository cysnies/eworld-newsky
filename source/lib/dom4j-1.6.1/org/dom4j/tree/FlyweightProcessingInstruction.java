package org.dom4j.tree;

import java.util.Collections;
import java.util.Map;
import org.dom4j.Element;
import org.dom4j.Node;

public class FlyweightProcessingInstruction extends AbstractProcessingInstruction {
   protected String target;
   protected String text;
   protected Map values;

   public FlyweightProcessingInstruction() {
   }

   public FlyweightProcessingInstruction(String target, Map values) {
      this.target = target;
      this.values = values;
      this.text = this.toString(values);
   }

   public FlyweightProcessingInstruction(String target, String text) {
      this.target = target;
      this.text = text;
      this.values = this.parseValues(text);
   }

   public String getTarget() {
      return this.target;
   }

   public void setTarget(String target) {
      throw new UnsupportedOperationException("This PI is read-only and cannot be modified");
   }

   public String getText() {
      return this.text;
   }

   public String getValue(String name) {
      String answer = (String)this.values.get(name);
      return answer == null ? "" : answer;
   }

   public Map getValues() {
      return Collections.unmodifiableMap(this.values);
   }

   protected Node createXPathResult(Element parent) {
      return new DefaultProcessingInstruction(parent, this.getTarget(), this.getText());
   }
}
