package org.dom4j.rule;

import java.util.List;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.XPath;

public class Stylesheet {
   private RuleManager ruleManager = new RuleManager();
   private String modeName;

   public void addRule(Rule rule) {
      this.ruleManager.addRule(rule);
   }

   public void removeRule(Rule rule) {
      this.ruleManager.removeRule(rule);
   }

   public void run(Object input) throws Exception {
      this.run(input, this.modeName);
   }

   public void run(Object input, String mode) throws Exception {
      if (input instanceof Node) {
         this.run((Node)input, mode);
      } else if (input instanceof List) {
         this.run((List)input, mode);
      }

   }

   public void run(List list) throws Exception {
      this.run(list, this.modeName);
   }

   public void run(List list, String mode) throws Exception {
      int i = 0;

      for(int size = list.size(); i < size; ++i) {
         Object object = list.get(i);
         if (object instanceof Node) {
            this.run((Node)object, mode);
         }
      }

   }

   public void run(Node node) throws Exception {
      this.run(node, this.modeName);
   }

   public void run(Node node, String mode) throws Exception {
      Mode mod = this.ruleManager.getMode(mode);
      mod.fireRule(node);
   }

   public void applyTemplates(Object input, XPath xpath) throws Exception {
      this.applyTemplates(input, xpath, this.modeName);
   }

   public void applyTemplates(Object input, XPath xpath, String mode) throws Exception {
      Mode mod = this.ruleManager.getMode(mode);

      for(Node current : xpath.selectNodes(input)) {
         mod.fireRule(current);
      }

   }

   /** @deprecated */
   public void applyTemplates(Object input, org.jaxen.XPath xpath) throws Exception {
      this.applyTemplates(input, xpath, this.modeName);
   }

   /** @deprecated */
   public void applyTemplates(Object input, org.jaxen.XPath xpath, String mode) throws Exception {
      Mode mod = this.ruleManager.getMode(mode);

      for(Node current : xpath.selectNodes(input)) {
         mod.fireRule(current);
      }

   }

   public void applyTemplates(Object input) throws Exception {
      this.applyTemplates(input, this.modeName);
   }

   public void applyTemplates(Object input, String mode) throws Exception {
      Mode mod = this.ruleManager.getMode(mode);
      if (input instanceof Element) {
         Element element = (Element)input;
         int i = 0;

         for(int size = element.nodeCount(); i < size; ++i) {
            Node node = element.node(i);
            mod.fireRule(node);
         }
      } else if (input instanceof Document) {
         Document document = (Document)input;
         int i = 0;

         for(int size = document.nodeCount(); i < size; ++i) {
            Node node = document.node(i);
            mod.fireRule(node);
         }
      } else if (input instanceof List) {
         List list = (List)input;
         int i = 0;

         for(int size = list.size(); i < size; ++i) {
            Object object = list.get(i);
            if (object instanceof Element) {
               this.applyTemplates((Element)object, (String)mode);
            } else if (object instanceof Document) {
               this.applyTemplates((Document)object, (String)mode);
            }
         }
      }

   }

   public void clear() {
      this.ruleManager.clear();
   }

   public String getModeName() {
      return this.modeName;
   }

   public void setModeName(String modeName) {
      this.modeName = modeName;
   }

   public Action getValueOfAction() {
      return this.ruleManager.getValueOfAction();
   }

   public void setValueOfAction(Action valueOfAction) {
      this.ruleManager.setValueOfAction(valueOfAction);
   }
}
