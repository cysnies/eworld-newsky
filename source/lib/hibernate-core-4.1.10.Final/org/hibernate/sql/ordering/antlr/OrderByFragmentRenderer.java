package org.hibernate.sql.ordering.antlr;

import antlr.collections.AST;
import org.hibernate.hql.internal.ast.util.ASTPrinter;
import org.hibernate.internal.util.StringHelper;
import org.jboss.logging.Logger;

public class OrderByFragmentRenderer extends GeneratedOrderByFragmentRenderer {
   private static final Logger LOG = Logger.getLogger(OrderByFragmentRenderer.class.getName());
   private static final ASTPrinter printer = new ASTPrinter(GeneratedOrderByFragmentRendererTokenTypes.class);
   private int traceDepth = 0;

   protected void out(AST ast) {
      this.out(((Node)ast).getRenderableText());
   }

   public void traceIn(String ruleName, AST tree) {
      if (this.inputState.guessing <= 0) {
         String prefix = StringHelper.repeat('-', this.traceDepth++ * 2) + "-> ";
         String traceText = ruleName + " (" + this.buildTraceNodeName(tree) + ")";
         LOG.trace(prefix + traceText);
      }
   }

   private String buildTraceNodeName(AST tree) {
      return tree == null ? "???" : tree.getText() + " [" + printer.getTokenTypeName(tree.getType()) + "]";
   }

   public void traceOut(String ruleName, AST tree) {
      if (this.inputState.guessing <= 0) {
         String prefix = "<-" + StringHelper.repeat('-', --this.traceDepth * 2) + " ";
         LOG.trace(prefix + ruleName);
      }
   }
}
