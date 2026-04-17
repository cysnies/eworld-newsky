package org.dom4j.xpath;

import java.util.ArrayList;
import org.dom4j.InvalidXPathException;
import org.dom4j.Node;
import org.dom4j.XPathException;
import org.dom4j.rule.Pattern;
import org.jaxen.Context;
import org.jaxen.ContextSupport;
import org.jaxen.JaxenException;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.SimpleVariableContext;
import org.jaxen.VariableContext;
import org.jaxen.XPathFunctionContext;
import org.jaxen.dom4j.DocumentNavigator;
import org.jaxen.pattern.PatternParser;
import org.jaxen.saxpath.SAXPathException;

public class XPathPattern implements Pattern {
   private String text;
   private org.jaxen.pattern.Pattern pattern;
   private Context context;

   public XPathPattern(org.jaxen.pattern.Pattern pattern) {
      this.pattern = pattern;
      this.text = pattern.getText();
      this.context = new Context(this.getContextSupport());
   }

   public XPathPattern(String text) {
      this.text = text;
      this.context = new Context(this.getContextSupport());

      try {
         this.pattern = PatternParser.parse(text);
      } catch (SAXPathException e) {
         throw new InvalidXPathException(text, e.getMessage());
      } catch (Throwable t) {
         throw new InvalidXPathException(text, t);
      }
   }

   public boolean matches(Node node) {
      try {
         ArrayList list = new ArrayList(1);
         list.add(node);
         this.context.setNodeSet(list);
         return this.pattern.matches(node, this.context);
      } catch (JaxenException e) {
         this.handleJaxenException(e);
         return false;
      }
   }

   public String getText() {
      return this.text;
   }

   public double getPriority() {
      return this.pattern.getPriority();
   }

   public Pattern[] getUnionPatterns() {
      org.jaxen.pattern.Pattern[] patterns = this.pattern.getUnionPatterns();
      if (patterns == null) {
         return null;
      } else {
         int size = patterns.length;
         XPathPattern[] answer = new XPathPattern[size];

         for(int i = 0; i < size; ++i) {
            answer[i] = new XPathPattern(patterns[i]);
         }

         return answer;
      }
   }

   public short getMatchType() {
      return this.pattern.getMatchType();
   }

   public String getMatchesNodeName() {
      return this.pattern.getMatchesNodeName();
   }

   public void setVariableContext(VariableContext variableContext) {
      this.context.getContextSupport().setVariableContext(variableContext);
   }

   public String toString() {
      return "[XPathPattern: text: " + this.text + " Pattern: " + this.pattern + "]";
   }

   protected ContextSupport getContextSupport() {
      return new ContextSupport(new SimpleNamespaceContext(), XPathFunctionContext.getInstance(), new SimpleVariableContext(), DocumentNavigator.getInstance());
   }

   protected void handleJaxenException(JaxenException exception) throws XPathException {
      throw new XPathException(this.text, exception);
   }
}
