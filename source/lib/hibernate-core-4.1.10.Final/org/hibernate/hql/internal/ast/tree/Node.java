package org.hibernate.hql.internal.ast.tree;

import antlr.CommonAST;
import antlr.Token;
import antlr.collections.AST;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.util.StringHelper;

public class Node extends CommonAST {
   private String filename;
   private int line;
   private int column;
   private int textLength;

   public Node() {
   }

   public Node(Token tok) {
      super(tok);
   }

   public String getRenderText(SessionFactoryImplementor sessionFactory) {
      return this.getText();
   }

   public void initialize(Token tok) {
      super.initialize(tok);
      this.filename = tok.getFilename();
      this.line = tok.getLine();
      this.column = tok.getColumn();
      String text = tok.getText();
      this.textLength = StringHelper.isEmpty(text) ? 0 : text.length();
   }

   public void initialize(AST t) {
      super.initialize(t);
      if (t instanceof Node) {
         Node n = (Node)t;
         this.filename = n.filename;
         this.line = n.line;
         this.column = n.column;
         this.textLength = n.textLength;
      }

   }

   public String getFilename() {
      return this.filename;
   }

   public int getLine() {
      return this.line;
   }

   public int getColumn() {
      return this.column;
   }

   public int getTextLength() {
      return this.textLength;
   }
}
