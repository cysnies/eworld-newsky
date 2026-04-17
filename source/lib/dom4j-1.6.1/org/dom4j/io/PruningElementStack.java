package org.dom4j.io;

import org.dom4j.Element;
import org.dom4j.ElementHandler;

class PruningElementStack extends ElementStack {
   private ElementHandler elementHandler;
   private String[] path;
   private int matchingElementIndex;

   public PruningElementStack(String[] path, ElementHandler elementHandler) {
      this.path = path;
      this.elementHandler = elementHandler;
      this.checkPath();
   }

   public PruningElementStack(String[] path, ElementHandler elementHandler, int defaultCapacity) {
      super(defaultCapacity);
      this.path = path;
      this.elementHandler = elementHandler;
      this.checkPath();
   }

   public Element popElement() {
      Element answer = super.popElement();
      if (this.lastElementIndex == this.matchingElementIndex && this.lastElementIndex >= 0 && this.validElement(answer, this.lastElementIndex + 1)) {
         Element parent = null;

         for(int i = 0; i <= this.lastElementIndex; ++i) {
            parent = this.stack[i];
            if (!this.validElement(parent, i)) {
               parent = null;
               break;
            }
         }

         if (parent != null) {
            this.pathMatches(parent, answer);
         }
      }

      return answer;
   }

   protected void pathMatches(Element parent, Element selectedNode) {
      this.elementHandler.onEnd(this);
      parent.remove((Element)selectedNode);
   }

   protected boolean validElement(Element element, int index) {
      String requiredName = this.path[index];
      String name = element.getName();
      if (requiredName == name) {
         return true;
      } else {
         return requiredName != null && name != null ? requiredName.equals(name) : false;
      }
   }

   private void checkPath() {
      if (this.path.length < 2) {
         throw new RuntimeException("Invalid path of length: " + this.path.length + " it must be greater than 2");
      } else {
         this.matchingElementIndex = this.path.length - 2;
      }
   }
}
