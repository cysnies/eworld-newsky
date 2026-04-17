package org.hibernate.hql.internal.ast.tree;

import antlr.SemanticException;
import antlr.collections.AST;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.hql.internal.ast.util.ColumnHelper;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

public class AggregateNode extends AbstractSelectExpression implements SelectExpression, FunctionNode {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, AggregateNode.class.getName());
   private SQLFunction sqlFunction;

   public SQLFunction getSQLFunction() {
      return this.sqlFunction;
   }

   public void resolve() {
      this.resolveFunction();
   }

   private SQLFunction resolveFunction() {
      if (this.sqlFunction == null) {
         String name = this.getText();
         this.sqlFunction = this.getSessionFactoryHelper().findSQLFunction(this.getText());
         if (this.sqlFunction == null) {
            LOG.unableToResolveAggregateFunction(name);
            this.sqlFunction = new StandardSQLFunction(name);
         }
      }

      return this.sqlFunction;
   }

   public Type getFirstArgumentType() {
      AST argument = this.getFirstChild();

      while(argument != null) {
         if (argument instanceof SqlNode) {
            Type type = ((SqlNode)argument).getDataType();
            if (type != null) {
               return type;
            }

            argument = argument.getNextSibling();
         }
      }

      return null;
   }

   public Type getDataType() {
      return this.getSessionFactoryHelper().findFunctionReturnType(this.getText(), this.resolveFunction(), this.getFirstChild());
   }

   public void setScalarColumnText(int i) throws SemanticException {
      ColumnHelper.generateSingleScalarColumn(this, i);
   }

   public boolean isScalar() throws SemanticException {
      return true;
   }
}
