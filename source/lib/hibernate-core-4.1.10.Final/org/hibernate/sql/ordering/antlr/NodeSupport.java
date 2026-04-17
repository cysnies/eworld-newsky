package org.hibernate.sql.ordering.antlr;

import antlr.CommonAST;

public class NodeSupport extends CommonAST implements Node {
   public String getDebugText() {
      return this.getText();
   }

   public String getRenderableText() {
      return this.getText();
   }
}
