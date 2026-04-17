package org.hibernate.hql.internal.ast;

import antlr.ASTPair;
import antlr.MismatchedTokenException;
import antlr.RecognitionException;
import antlr.Token;
import antlr.TokenStream;
import antlr.TokenStreamException;
import antlr.collections.AST;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import org.hibernate.QueryException;
import org.hibernate.hql.internal.antlr.HqlBaseParser;
import org.hibernate.hql.internal.antlr.HqlTokenTypes;
import org.hibernate.hql.internal.ast.util.ASTPrinter;
import org.hibernate.hql.internal.ast.util.ASTUtil;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.StringHelper;
import org.jboss.logging.Logger;

public final class HqlParser extends HqlBaseParser {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, HqlParser.class.getName());
   private ParseErrorHandler parseErrorHandler;
   private ASTPrinter printer = getASTPrinter();
   private int traceDepth = 0;

   private static ASTPrinter getASTPrinter() {
      return new ASTPrinter(HqlTokenTypes.class);
   }

   public static HqlParser getInstance(String hql) {
      HqlLexer lexer = new HqlLexer(new StringReader(hql));
      return new HqlParser(lexer);
   }

   private HqlParser(TokenStream lexer) {
      super(lexer);
      this.initialize();
   }

   public void traceIn(String ruleName) {
      if (LOG.isTraceEnabled()) {
         if (this.inputState.guessing <= 0) {
            String prefix = StringHelper.repeat('-', this.traceDepth++ * 2) + "-> ";
            LOG.trace(prefix + ruleName);
         }
      }
   }

   public void traceOut(String ruleName) {
      if (LOG.isTraceEnabled()) {
         if (this.inputState.guessing <= 0) {
            String prefix = "<-" + StringHelper.repeat('-', --this.traceDepth * 2) + " ";
            LOG.trace(prefix + ruleName);
         }
      }
   }

   public void reportError(RecognitionException e) {
      this.parseErrorHandler.reportError(e);
   }

   public void reportError(String s) {
      this.parseErrorHandler.reportError(s);
   }

   public void reportWarning(String s) {
      this.parseErrorHandler.reportWarning(s);
   }

   public ParseErrorHandler getParseErrorHandler() {
      return this.parseErrorHandler;
   }

   public AST handleIdentifierError(Token token, RecognitionException ex) throws RecognitionException, TokenStreamException {
      if (token instanceof HqlToken) {
         HqlToken hqlToken = (HqlToken)token;
         if (hqlToken.isPossibleID() && ex instanceof MismatchedTokenException) {
            MismatchedTokenException mte = (MismatchedTokenException)ex;
            if (mte.expecting == 126) {
               this.reportWarning("Keyword  '" + token.getText() + "' is being interpreted as an identifier due to: " + mte.getMessage());
               ASTPair currentAST = new ASTPair();
               token.setType(93);
               this.astFactory.addASTChild(currentAST, this.astFactory.create(token));
               this.consume();
               AST identifierAST = currentAST.root;
               return identifierAST;
            }
         }
      }

      return super.handleIdentifierError(token, ex);
   }

   public AST negateNode(AST x) {
      switch (x.getType()) {
         case 6:
            x.setType(40);
            x.setText("{or}");
            x.setFirstChild(this.negateNode(x.getFirstChild()));
            x.getFirstChild().setNextSibling(this.negateNode(x.getFirstChild().getNextSibling()));
            return x;
         case 10:
            x.setType(82);
            x.setText("{not}" + x.getText());
            return x;
         case 26:
            x.setType(83);
            x.setText("{not}" + x.getText());
            return x;
         case 34:
            x.setType(84);
            x.setText("{not}" + x.getText());
            return x;
         case 40:
            x.setType(6);
            x.setText("{and}");
            x.setFirstChild(this.negateNode(x.getFirstChild()));
            x.getFirstChild().setNextSibling(this.negateNode(x.getFirstChild().getNextSibling()));
            return x;
         case 79:
            x.setType(80);
            x.setText("{not}" + x.getText());
            return x;
         case 80:
            x.setType(79);
            x.setText("{not}" + x.getText());
            return x;
         case 82:
            x.setType(10);
            x.setText("{not}" + x.getText());
            return x;
         case 83:
            x.setType(26);
            x.setText("{not}" + x.getText());
            return x;
         case 84:
            x.setType(34);
            x.setText("{not}" + x.getText());
            return x;
         case 102:
            x.setType(108);
            x.setText("{not}" + x.getText());
            return x;
         case 108:
            x.setType(102);
            x.setText("{not}" + x.getText());
            return x;
         case 110:
            x.setType(113);
            x.setText("{not}" + x.getText());
            return x;
         case 111:
            x.setType(112);
            x.setText("{not}" + x.getText());
            return x;
         case 112:
            x.setType(111);
            x.setText("{not}" + x.getText());
            return x;
         case 113:
            x.setType(110);
            x.setText("{not}" + x.getText());
            return x;
         default:
            AST not = super.negateNode(x);
            if (not != x) {
               not.setNextSibling(x.getNextSibling());
               x.setNextSibling((AST)null);
            }

            return not;
      }
   }

   public AST processEqualityExpression(AST x) {
      if (x == null) {
         LOG.processEqualityExpression();
         return null;
      } else {
         int type = x.getType();
         if (type != 102 && type != 108) {
            return x;
         } else {
            boolean negated = type == 108;
            if (x.getNumberOfChildren() == 2) {
               AST a = x.getFirstChild();
               AST b = a.getNextSibling();
               if (a.getType() == 39 && b.getType() != 39) {
                  return this.createIsNullParent(b, negated);
               } else if (b.getType() == 39 && a.getType() != 39) {
                  return this.createIsNullParent(a, negated);
               } else {
                  return b.getType() == 62 ? this.processIsEmpty(a, negated) : x;
               }
            } else {
               return x;
            }
         }
      }
   }

   private AST createIsNullParent(AST node, boolean negated) {
      node.setNextSibling((AST)null);
      int type = negated ? 79 : 80;
      String text = negated ? "is not null" : "is null";
      return ASTUtil.createParent(this.astFactory, type, text, node);
   }

   private AST processIsEmpty(AST node, boolean negated) {
      node.setNextSibling((AST)null);
      AST ast = this.createSubquery(node);
      ast = ASTUtil.createParent(this.astFactory, 19, "exists", ast);
      if (!negated) {
         ast = ASTUtil.createParent(this.astFactory, 38, "not", ast);
      }

      return ast;
   }

   private AST createSubquery(AST node) {
      AST ast = ASTUtil.createParent(this.astFactory, 87, "RANGE", node);
      ast = ASTUtil.createParent(this.astFactory, 22, "from", ast);
      ast = ASTUtil.createParent(this.astFactory, 89, "SELECT_FROM", ast);
      ast = ASTUtil.createParent(this.astFactory, 86, "QUERY", ast);
      return ast;
   }

   public void showAst(AST ast, PrintStream out) {
      this.showAst(ast, new PrintWriter(out));
   }

   private void showAst(AST ast, PrintWriter pw) {
      this.printer.showAst(ast, pw);
   }

   private void initialize() {
      this.parseErrorHandler = new ErrorCounter();
      this.setASTFactory(new HqlASTFactory());
   }

   public void weakKeywords() throws TokenStreamException {
      int t = this.LA(1);
      switch (t) {
         case 24:
         case 41:
            if (this.LA(2) != 105) {
               this.LT(1).setType(126);
               if (LOG.isDebugEnabled()) {
                  LOG.debugf("weakKeywords() : new LT(1) token - %s", this.LT(1));
               }
            }
            break;
         default:
            if (this.LA(0) == 22 && t != 126 && this.LA(2) == 15) {
               HqlToken hqlToken = (HqlToken)this.LT(1);
               if (hqlToken.isPossibleID()) {
                  hqlToken.setType(126);
                  if (LOG.isDebugEnabled()) {
                     LOG.debugf("weakKeywords() : new LT(1) token - %s", this.LT(1));
                  }
               }
            }
      }

   }

   public void handleDotIdent() throws TokenStreamException {
      if (this.LA(1) == 15 && this.LA(2) != 126) {
         HqlToken t = (HqlToken)this.LT(2);
         if (t.isPossibleID()) {
            this.LT(2).setType(126);
            if (LOG.isDebugEnabled()) {
               LOG.debugf("handleDotIdent() : new LT(2) token - %s", this.LT(1));
            }
         }
      }

   }

   public void processMemberOf(Token n, AST p, ASTPair currentAST) {
      AST inNode = n == null ? this.astFactory.create(26, "in") : this.astFactory.create(83, "not in");
      this.astFactory.makeASTRoot(currentAST, inNode);
      AST inListNode = this.astFactory.create(77, "inList");
      inNode.addChild(inListNode);
      AST elementsNode = this.astFactory.create(17, "elements");
      inListNode.addChild(elementsNode);
      elementsNode.addChild(p);
   }

   public static void panic() {
      throw new QueryException("Parser: panic");
   }
}
