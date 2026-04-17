package org.hibernate.hql.internal.ast;

import antlr.RecognitionException;
import antlr.collections.AST;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.hibernate.QueryException;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.hql.internal.antlr.SqlGeneratorBase;
import org.hibernate.hql.internal.antlr.SqlTokenTypes;
import org.hibernate.hql.internal.ast.tree.FromElement;
import org.hibernate.hql.internal.ast.tree.FunctionNode;
import org.hibernate.hql.internal.ast.tree.Node;
import org.hibernate.hql.internal.ast.tree.ParameterContainer;
import org.hibernate.hql.internal.ast.tree.ParameterNode;
import org.hibernate.hql.internal.ast.util.ASTPrinter;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.param.ParameterSpecification;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

public class SqlGenerator extends SqlGeneratorBase implements ErrorReporter {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, SqlGenerator.class.getName());
   public static boolean REGRESSION_STYLE_CROSS_JOINS = false;
   private SqlWriter writer = new DefaultWriter();
   private ParseErrorHandler parseErrorHandler = new ErrorCounter();
   private SessionFactoryImplementor sessionFactory;
   private LinkedList outputStack = new LinkedList();
   private final ASTPrinter printer = new ASTPrinter(SqlTokenTypes.class);
   private List collectedParameters = new ArrayList();
   private int traceDepth = 0;

   public void traceIn(String ruleName, AST tree) {
      if (LOG.isTraceEnabled()) {
         if (this.inputState.guessing <= 0) {
            String prefix = StringHelper.repeat('-', this.traceDepth++ * 2) + "-> ";
            String traceText = ruleName + " (" + this.buildTraceNodeName(tree) + ")";
            LOG.trace(prefix + traceText);
         }
      }
   }

   private String buildTraceNodeName(AST tree) {
      return tree == null ? "???" : tree.getText() + " [" + this.printer.getTokenTypeName(tree.getType()) + "]";
   }

   public void traceOut(String ruleName, AST tree) {
      if (LOG.isTraceEnabled()) {
         if (this.inputState.guessing <= 0) {
            String prefix = "<-" + StringHelper.repeat('-', --this.traceDepth * 2) + " ";
            LOG.trace(prefix + ruleName);
         }
      }
   }

   public List getCollectedParameters() {
      return this.collectedParameters;
   }

   protected void out(String s) {
      this.writer.clause(s);
   }

   protected void out(AST n) {
      if (n instanceof Node) {
         this.out(((Node)n).getRenderText(this.sessionFactory));
      } else {
         super.out(n);
      }

      if (n instanceof ParameterNode) {
         this.collectedParameters.add(((ParameterNode)n).getHqlParameterSpecification());
      } else if (n instanceof ParameterContainer && ((ParameterContainer)n).hasEmbeddedParameters()) {
         ParameterSpecification[] specifications = ((ParameterContainer)n).getEmbeddedParameters();
         if (specifications != null) {
            this.collectedParameters.addAll(Arrays.asList(specifications));
         }
      }

   }

   protected void commaBetweenParameters(String comma) {
      this.writer.commaBetweenParameters(comma);
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

   public SqlGenerator(SessionFactoryImplementor sfi) {
      this.sessionFactory = sfi;
   }

   public String getSQL() {
      return this.getStringBuilder().toString();
   }

   protected void optionalSpace() {
      int c = this.getLastChar();
      switch (c) {
         case -1:
            return;
         case 32:
            return;
         case 40:
            return;
         case 41:
            return;
         default:
            this.out(" ");
      }
   }

   protected void beginFunctionTemplate(AST node, AST nameNode) {
      FunctionNode functionNode = (FunctionNode)node;
      SQLFunction sqlFunction = functionNode.getSQLFunction();
      if (sqlFunction == null) {
         super.beginFunctionTemplate(node, nameNode);
      } else {
         this.outputStack.addFirst(this.writer);
         this.writer = new FunctionArguments();
      }

   }

   protected void endFunctionTemplate(AST node) {
      FunctionNode functionNode = (FunctionNode)node;
      SQLFunction sqlFunction = functionNode.getSQLFunction();
      if (sqlFunction == null) {
         super.endFunctionTemplate(node);
      } else {
         Type functionType = functionNode.getFirstArgumentType();
         FunctionArguments functionArguments = (FunctionArguments)this.writer;
         this.writer = (SqlWriter)this.outputStack.removeFirst();
         this.out(sqlFunction.render(functionType, functionArguments.getArgs(), this.sessionFactory));
      }

   }

   public static void panic() {
      throw new QueryException("TreeWalker: panic");
   }

   protected void fromFragmentSeparator(AST a) {
      AST next = a.getNextSibling();
      if (next != null && this.hasText(a)) {
         FromElement left = (FromElement)a;

         FromElement right;
         for(right = (FromElement)next; right != null && !this.hasText(right); right = (FromElement)right.getNextSibling()) {
         }

         if (right != null) {
            if (this.hasText(right)) {
               if (right.getRealOrigin() == left || right.getRealOrigin() != null && right.getRealOrigin() == left.getRealOrigin()) {
                  if (right.getJoinSequence() != null && right.getJoinSequence().isThetaStyle()) {
                     this.writeCrossJoinSeparator();
                  } else {
                     this.out(" ");
                  }
               } else {
                  this.writeCrossJoinSeparator();
               }

            }
         }
      }
   }

   private void writeCrossJoinSeparator() {
      if (REGRESSION_STYLE_CROSS_JOINS) {
         this.out(", ");
      } else {
         this.out(this.sessionFactory.getDialect().getCrossJoinSeparator());
      }

   }

   protected void nestedFromFragment(AST d, AST parent) {
      if (d != null && this.hasText(d)) {
         if (parent != null && this.hasText(parent)) {
            FromElement left = (FromElement)parent;
            FromElement right = (FromElement)d;
            if (right.getRealOrigin() == left) {
               if (right.getJoinSequence() != null && right.getJoinSequence().isThetaStyle()) {
                  this.out(", ");
               } else {
                  this.out(" ");
               }
            } else {
               this.out(", ");
            }
         }

         this.out(d);
      }

   }

   class FunctionArguments implements SqlWriter {
      private int argInd;
      private final List args = new ArrayList(3);

      public void clause(String clause) {
         if (this.argInd == this.args.size()) {
            this.args.add(clause);
         } else {
            this.args.set(this.argInd, (String)this.args.get(this.argInd) + clause);
         }

      }

      public void commaBetweenParameters(String comma) {
         ++this.argInd;
      }

      public List getArgs() {
         return this.args;
      }
   }

   class DefaultWriter implements SqlWriter {
      public void clause(String clause) {
         SqlGenerator.this.getStringBuilder().append(clause);
      }

      public void commaBetweenParameters(String comma) {
         SqlGenerator.this.getStringBuilder().append(comma);
      }
   }

   interface SqlWriter {
      void clause(String var1);

      void commaBetweenParameters(String var1);
   }
}
