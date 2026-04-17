package org.hibernate.hql.internal.ast.util;

import antlr.collections.AST;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;
import org.hibernate.hql.internal.ast.tree.DisplayableNode;
import org.hibernate.internal.util.StringHelper;

public class ASTPrinter {
   private final Map tokenTypeNameCache;
   private final boolean showClassNames;

   public ASTPrinter(Class tokenTypeConstants) {
      this(ASTUtil.generateTokenNameCache(tokenTypeConstants), true);
   }

   public ASTPrinter(boolean showClassNames) {
      this((Map)null, showClassNames);
   }

   public ASTPrinter(Class tokenTypeConstants, boolean showClassNames) {
      this(ASTUtil.generateTokenNameCache(tokenTypeConstants), showClassNames);
   }

   private ASTPrinter(Map tokenTypeNameCache, boolean showClassNames) {
      this.tokenTypeNameCache = tokenTypeNameCache;
      this.showClassNames = showClassNames;
   }

   public boolean isShowClassNames() {
      return this.showClassNames;
   }

   public String showAsString(AST ast, String header) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      PrintStream ps = new PrintStream(baos);
      ps.println(header);
      this.showAst(ast, ps);
      ps.flush();
      return new String(baos.toByteArray());
   }

   public void showAst(AST ast, PrintStream out) {
      this.showAst(ast, new PrintWriter(out));
   }

   public void showAst(AST ast, PrintWriter pw) {
      ArrayList parents = new ArrayList();
      this.showAst(parents, pw, ast);
      pw.flush();
   }

   public String getTokenTypeName(int type) {
      Integer typeInteger = type;
      String value = null;
      if (this.tokenTypeNameCache != null) {
         value = (String)this.tokenTypeNameCache.get(typeInteger);
      }

      if (value == null) {
         value = typeInteger.toString();
      }

      return value;
   }

   private void showAst(ArrayList parents, PrintWriter pw, AST ast) {
      if (ast == null) {
         pw.println("AST is null!");
      } else {
         for(int i = 0; i < parents.size(); ++i) {
            AST parent = (AST)parents.get(i);
            if (parent.getNextSibling() == null) {
               pw.print("   ");
            } else {
               pw.print(" | ");
            }
         }

         if (ast.getNextSibling() == null) {
            pw.print(" \\-");
         } else {
            pw.print(" +-");
         }

         this.showNode(pw, ast);
         ArrayList newParents = new ArrayList(parents);
         newParents.add(ast);

         for(AST child = ast.getFirstChild(); child != null; child = child.getNextSibling()) {
            this.showAst(newParents, pw, child);
         }

         newParents.clear();
      }
   }

   private void showNode(PrintWriter pw, AST ast) {
      String s = this.nodeToString(ast, this.isShowClassNames());
      pw.println(s);
   }

   public String nodeToString(AST ast, boolean showClassName) {
      if (ast == null) {
         return "{node:null}";
      } else {
         StringBuilder buf = new StringBuilder();
         buf.append("[").append(this.getTokenTypeName(ast.getType())).append("] ");
         if (showClassName) {
            buf.append(StringHelper.unqualify(ast.getClass().getName())).append(": ");
         }

         buf.append("'");
         String text = ast.getText();
         if (text == null) {
            text = "{text:null}";
         }

         appendEscapedMultibyteChars(text, buf);
         buf.append("'");
         if (ast instanceof DisplayableNode) {
            DisplayableNode displayableNode = (DisplayableNode)ast;
            buf.append(" ").append(displayableNode.getDisplayText());
         }

         return buf.toString();
      }
   }

   public static void appendEscapedMultibyteChars(String text, StringBuilder buf) {
      char[] chars = text.toCharArray();

      for(int i = 0; i < chars.length; ++i) {
         char aChar = chars[i];
         if (aChar > 256) {
            buf.append("\\u");
            buf.append(Integer.toHexString(aChar));
         } else {
            buf.append(aChar);
         }
      }

   }

   public static String escapeMultibyteChars(String text) {
      StringBuilder buf = new StringBuilder();
      appendEscapedMultibyteChars(text, buf);
      return buf.toString();
   }
}
