package org.hibernate.hql.internal.ast.util;

import antlr.SemanticException;
import antlr.collections.AST;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.QueryException;
import org.hibernate.dialect.Dialect;
import org.hibernate.hql.internal.antlr.HqlSqlTokenTypes;
import org.hibernate.hql.internal.ast.HqlSqlWalker;
import org.hibernate.hql.internal.ast.InvalidPathException;
import org.hibernate.hql.internal.ast.tree.DotNode;
import org.hibernate.hql.internal.ast.tree.FromClause;
import org.hibernate.hql.internal.ast.tree.IdentNode;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.persister.entity.Queryable;
import org.hibernate.type.LiteralType;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

public class LiteralProcessor implements HqlSqlTokenTypes {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, LiteralProcessor.class.getName());
   public static DecimalLiteralFormat DECIMAL_LITERAL_FORMAT;
   private HqlSqlWalker walker;

   public LiteralProcessor(HqlSqlWalker hqlSqlWalker) {
      this.walker = hqlSqlWalker;
   }

   public boolean isAlias(String alias) {
      FromClause from;
      for(from = this.walker.getCurrentFromClause(); from.isSubQuery(); from = from.getParentFromClause()) {
         if (from.containsClassAlias(alias)) {
            return true;
         }
      }

      return from.containsClassAlias(alias);
   }

   public void processConstant(AST constant, boolean resolveIdent) throws SemanticException {
      boolean isIdent = constant.getType() == 126 || constant.getType() == 93;
      if (resolveIdent && isIdent && this.isAlias(constant.getText())) {
         IdentNode ident = (IdentNode)constant;
         ident.resolve(false, true);
      } else {
         Queryable queryable = this.walker.getSessionFactoryHelper().findQueryableUsingImports(constant.getText());
         if (isIdent && queryable != null) {
            constant.setText(queryable.getDiscriminatorSQLValue());
         } else {
            this.processLiteral(constant);
         }
      }

   }

   public void lookupConstant(DotNode node) throws SemanticException {
      String text = ASTUtil.getPathText(node);
      Queryable persister = this.walker.getSessionFactoryHelper().findQueryableUsingImports(text);
      if (persister != null) {
         String discrim = persister.getDiscriminatorSQLValue();
         node.setDataType(persister.getDiscriminatorType());
         if ("null".equals(discrim) || "not null".equals(discrim)) {
            throw new InvalidPathException("subclass test not allowed for null or not null discriminator: '" + text + "'");
         }

         this.setSQLValue(node, text, discrim);
      } else {
         Object value = ReflectHelper.getConstantValue(text);
         if (value == null) {
            throw new InvalidPathException("Invalid path: '" + text + "'");
         }

         this.setConstantValue(node, text, value);
      }

   }

   private void setSQLValue(DotNode node, String text, String value) {
      LOG.debugf("setSQLValue() %s -> %s", text, value);
      node.setFirstChild((AST)null);
      node.setType(142);
      node.setText(value);
      node.setResolvedConstant(text);
   }

   private void setConstantValue(DotNode node, String text, Object value) {
      if (LOG.isDebugEnabled()) {
         LOG.debugf("setConstantValue() %s -> %s %s", text, value, value.getClass().getName());
      }

      node.setFirstChild((AST)null);
      if (value instanceof String) {
         node.setType(125);
      } else if (value instanceof Character) {
         node.setType(125);
      } else if (value instanceof Byte) {
         node.setType(124);
      } else if (value instanceof Short) {
         node.setType(124);
      } else if (value instanceof Integer) {
         node.setType(124);
      } else if (value instanceof Long) {
         node.setType(97);
      } else if (value instanceof Double) {
         node.setType(95);
      } else if (value instanceof Float) {
         node.setType(96);
      } else {
         node.setType(94);
      }

      Type type;
      try {
         type = this.walker.getSessionFactoryHelper().getFactory().getTypeResolver().heuristicType(value.getClass().getName());
      } catch (MappingException me) {
         throw new QueryException(me);
      }

      if (type == null) {
         throw new QueryException("Could not determine type of: " + node.getText());
      } else {
         try {
            LiteralType literalType = (LiteralType)type;
            Dialect dialect = this.walker.getSessionFactoryHelper().getFactory().getDialect();
            node.setText(literalType.objectToSQLString(value, dialect));
         } catch (Exception e) {
            throw new QueryException("Could not format constant value to SQL literal: " + node.getText(), e);
         }

         node.setDataType(type);
         node.setResolvedConstant(text);
      }
   }

   public void processBoolean(AST constant) {
      String replacement = (String)this.walker.getTokenReplacements().get(constant.getText());
      if (replacement != null) {
         constant.setText(replacement);
      } else {
         boolean bool = "true".equals(constant.getText().toLowerCase());
         Dialect dialect = this.walker.getSessionFactoryHelper().getFactory().getDialect();
         constant.setText(dialect.toBooleanValueString(bool));
      }

   }

   private void processLiteral(AST constant) {
      String replacement = (String)this.walker.getTokenReplacements().get(constant.getText());
      if (replacement != null) {
         if (LOG.isDebugEnabled()) {
            LOG.debugf("processConstant() : Replacing '%s' with '%s'", constant.getText(), replacement);
         }

         constant.setText(replacement);
      }

   }

   public void processNumeric(AST literal) {
      if (literal.getType() != 124 && literal.getType() != 97 && literal.getType() != 98) {
         if (literal.getType() != 96 && literal.getType() != 95 && literal.getType() != 99) {
            LOG.unexpectedLiteralTokenType(literal.getType());
         } else {
            literal.setText(this.determineDecimalRepresentation(literal.getText(), literal.getType()));
         }
      } else {
         literal.setText(this.determineIntegerRepresentation(literal.getText(), literal.getType()));
      }

   }

   private String determineIntegerRepresentation(String text, int type) {
      try {
         if (type == 98) {
            String literalValue = text;
            if (text.endsWith("bi") || text.endsWith("BI")) {
               literalValue = text.substring(0, text.length() - 2);
            }

            return (new BigInteger(literalValue)).toString();
         } else {
            if (type == 124) {
               try {
                  return Integer.valueOf(text).toString();
               } catch (NumberFormatException var4) {
                  LOG.tracev("Could not format incoming text [{0}] as a NUM_INT; assuming numeric overflow and attempting as NUM_LONG", text);
               }
            }

            String literalValue = text;
            if (text.endsWith("l") || text.endsWith("L")) {
               literalValue = text.substring(0, text.length() - 1);
            }

            return Long.valueOf(literalValue).toString();
         }
      } catch (Throwable t) {
         throw new HibernateException("Could not parse literal [" + text + "] as integer", t);
      }
   }

   public String determineDecimalRepresentation(String text, int type) {
      String literalValue = text;
      if (type == 96) {
         if (text.endsWith("f") || text.endsWith("F")) {
            literalValue = text.substring(0, text.length() - 1);
         }
      } else if (type == 95) {
         if (text.endsWith("d") || text.endsWith("D")) {
            literalValue = text.substring(0, text.length() - 1);
         }
      } else if (type == 99 && (text.endsWith("bd") || text.endsWith("BD"))) {
         literalValue = text.substring(0, text.length() - 2);
      }

      BigDecimal number;
      try {
         number = new BigDecimal(literalValue);
      } catch (Throwable t) {
         throw new HibernateException("Could not parse literal [" + text + "] as big-decimal", t);
      }

      return DECIMAL_LITERAL_FORMAT.getFormatter().format(number);
   }

   static {
      DECIMAL_LITERAL_FORMAT = LiteralProcessor.DecimalLiteralFormat.EXACT;
   }

   private static class ExactDecimalFormatter implements DecimalFormatter {
      public static final ExactDecimalFormatter INSTANCE = new ExactDecimalFormatter();

      public String format(BigDecimal number) {
         return number.toString();
      }
   }

   private static class ApproximateDecimalFormatter implements DecimalFormatter {
      public static final ApproximateDecimalFormatter INSTANCE = new ApproximateDecimalFormatter();
      private static final String FORMAT_STRING = "#0.0E0";

      public String format(BigDecimal number) {
         try {
            DecimalFormat jdkFormatter = new DecimalFormat("#0.0E0");
            jdkFormatter.setMinimumIntegerDigits(1);
            jdkFormatter.setMaximumFractionDigits(Integer.MAX_VALUE);
            return jdkFormatter.format(number);
         } catch (Throwable t) {
            throw new HibernateException("Unable to format decimal literal in approximate format [" + number.toString() + "]", t);
         }
      }
   }

   public static enum DecimalLiteralFormat {
      EXACT {
         public DecimalFormatter getFormatter() {
            return LiteralProcessor.ExactDecimalFormatter.INSTANCE;
         }
      },
      APPROXIMATE {
         public DecimalFormatter getFormatter() {
            return LiteralProcessor.ApproximateDecimalFormatter.INSTANCE;
         }
      };

      private DecimalLiteralFormat() {
      }

      public abstract DecimalFormatter getFormatter();
   }

   private interface DecimalFormatter {
      String format(BigDecimal var1);
   }
}
