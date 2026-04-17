package org.hibernate.sql.ordering.antlr;

import antlr.CommonAST;
import antlr.TokenStream;
import antlr.collections.AST;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

public class OrderByFragmentParser extends GeneratedOrderByFragmentParser {
   private static final Logger LOG = Logger.getLogger(OrderByFragmentParser.class.getName());
   private final TranslationContext context;
   private Set columnReferences = new HashSet();
   private static final int TEMPLATE_MARKER_LENGTH = "$PlaceHolder$".length();
   private int traceDepth = 0;

   public OrderByFragmentParser(TokenStream lexer, TranslationContext context) {
      super(lexer);
      super.setASTFactory(new Factory());
      this.context = context;
   }

   public Set getColumnReferences() {
      return this.columnReferences;
   }

   protected AST quotedIdentifier(AST ident) {
      String columnName = this.context.getDialect().quote('`' + ident.getText() + '`');
      this.columnReferences.add(columnName);
      String marker = '{' + columnName + '}';
      return this.getASTFactory().create(17, marker);
   }

   protected AST quotedString(AST ident) {
      return this.getASTFactory().create(17, this.context.getDialect().quote(ident.getText()));
   }

   protected boolean isFunctionName(AST ast) {
      AST child = ast.getFirstChild();
      if (child != null && "{param list}".equals(child.getText())) {
         return true;
      } else {
         SQLFunction function = this.context.getSqlFunctionRegistry().findSQLFunction(ast.getText());
         if (function == null) {
            return false;
         } else {
            return !function.hasParenthesesIfNoArguments();
         }
      }
   }

   protected AST resolveFunction(AST ast) {
      AST child = ast.getFirstChild();
      if (child != null) {
         assert "{param list}".equals(child.getText());

         child = child.getFirstChild();
      }

      String functionName = ast.getText();
      SQLFunction function = this.context.getSqlFunctionRegistry().findSQLFunction(functionName);
      if (function == null) {
         String text = functionName;
         if (child != null) {
            text = functionName + '(';

            while(child != null) {
               text = text + this.resolveFunctionArgument(child);
               child = child.getNextSibling();
               if (child != null) {
                  text = text + ", ";
               }
            }

            text = text + ')';
         }

         return this.getASTFactory().create(17, text);
      } else {
         ArrayList expressions;
         for(expressions = new ArrayList(); child != null; child = child.getNextSibling()) {
            expressions.add(this.resolveFunctionArgument(child));
         }

         String text = function.render((Type)null, expressions, this.context.getSessionFactory());
         return this.getASTFactory().create(17, text);
      }
   }

   private String resolveFunctionArgument(AST argumentNode) {
      String nodeText = argumentNode.getText();
      String adjustedText;
      if (nodeText.contains("$PlaceHolder$")) {
         adjustedText = this.adjustTemplateReferences(nodeText);
      } else {
         if (nodeText.startsWith("{") && nodeText.endsWith("}")) {
            this.columnReferences.add(nodeText.substring(1, nodeText.length() - 1));
            return nodeText;
         }

         adjustedText = nodeText;
         Pattern pattern = Pattern.compile("\\{(.*)\\}");
         Matcher matcher = pattern.matcher(nodeText);

         while(matcher.find()) {
            this.columnReferences.add(matcher.group(1));
         }
      }

      return adjustedText;
   }

   protected AST resolveIdent(AST ident) {
      String text = ident.getText();

      SqlValueReference[] sqlValueReferences;
      try {
         sqlValueReferences = this.context.getColumnMapper().map(text);
      } catch (Throwable var9) {
         sqlValueReferences = null;
      }

      if (sqlValueReferences != null && sqlValueReferences.length != 0) {
         if (sqlValueReferences.length == 1) {
            return this.processSqlValueReference(sqlValueReferences[0]);
         } else {
            AST root = this.getASTFactory().create(10, "{ident list}");

            for(SqlValueReference sqlValueReference : sqlValueReferences) {
               root.addChild(this.processSqlValueReference(sqlValueReference));
            }

            return root;
         }
      } else {
         return this.getASTFactory().create(17, this.makeColumnReference(text));
      }
   }

   private AST processSqlValueReference(SqlValueReference sqlValueReference) {
      if (ColumnReference.class.isInstance(sqlValueReference)) {
         String columnName = ((ColumnReference)sqlValueReference).getColumnName();
         return this.getASTFactory().create(17, this.makeColumnReference(columnName));
      } else {
         String formulaFragment = ((FormulaReference)sqlValueReference).getFormulaFragment();
         String adjustedText = this.adjustTemplateReferences(formulaFragment);
         return this.getASTFactory().create(17, adjustedText);
      }
   }

   private String makeColumnReference(String text) {
      this.columnReferences.add(text);
      return "{" + text + "}";
   }

   private String adjustTemplateReferences(String template) {
      int templateLength = template.length();

      for(int startPos = template.indexOf("$PlaceHolder$"); startPos != -1 && startPos < templateLength; templateLength = template.length()) {
         int dotPos = startPos + TEMPLATE_MARKER_LENGTH;

         int pos;
         for(pos = dotPos + 1; pos < templateLength && isValidIdentifierCharacter(template.charAt(pos)); ++pos) {
         }

         String columnReference = template.substring(dotPos + 1, pos);
         String replacement = "{" + columnReference + "}";
         template = template.replace(template.substring(startPos, pos), replacement);
         this.columnReferences.add(columnReference);
         startPos = template.indexOf("$PlaceHolder$", pos - TEMPLATE_MARKER_LENGTH + 1);
      }

      return template;
   }

   private static boolean isValidIdentifierCharacter(char c) {
      return Character.isLetter(c) || Character.isDigit(c) || '_' == c || '"' == c;
   }

   protected AST postProcessSortSpecification(AST sortSpec) {
      assert 5 == sortSpec.getType();

      SortSpecification sortSpecification = (SortSpecification)sortSpec;
      AST sortKey = sortSpecification.getSortKey();
      if (10 == sortKey.getFirstChild().getType()) {
         AST identList = sortKey.getFirstChild();
         AST ident = identList.getFirstChild();
         AST holder = new CommonAST();

         do {
            holder.addChild(this.createSortSpecification(ident, sortSpecification.getCollation(), sortSpecification.getOrdering()));
            ident = ident.getNextSibling();
         } while(ident != null);

         sortSpec = holder.getFirstChild();
      }

      return sortSpec;
   }

   private SortSpecification createSortSpecification(AST ident, CollationSpecification collationSpecification, OrderingSpecification orderingSpecification) {
      AST sortSpecification = this.getASTFactory().create(5, "{{sort specification}}");
      AST sortKey = this.getASTFactory().create(7, "{{sort key}}");
      AST newIdent = this.getASTFactory().create(ident.getType(), ident.getText());
      sortKey.setFirstChild(newIdent);
      sortSpecification.setFirstChild(sortKey);
      if (collationSpecification != null) {
         sortSpecification.addChild(collationSpecification);
      }

      if (orderingSpecification != null) {
         sortSpecification.addChild(orderingSpecification);
      }

      return (SortSpecification)sortSpecification;
   }

   public void traceIn(String ruleName) {
      if (this.inputState.guessing <= 0) {
         String prefix = StringHelper.repeat('-', this.traceDepth++ * 2) + "-> ";
         LOG.trace(prefix + ruleName);
      }
   }

   public void traceOut(String ruleName) {
      if (this.inputState.guessing <= 0) {
         String prefix = "<-" + StringHelper.repeat('-', --this.traceDepth * 2) + " ";
         LOG.trace(prefix + ruleName);
      }
   }

   protected void trace(String msg) {
      LOG.trace(msg);
   }
}
