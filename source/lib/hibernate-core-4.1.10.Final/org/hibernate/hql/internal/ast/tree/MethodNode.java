package org.hibernate.hql.internal.ast.tree;

import antlr.SemanticException;
import antlr.collections.AST;
import java.util.Arrays;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.hql.internal.CollectionProperties;
import org.hibernate.hql.internal.ast.TypeDiscriminatorMetadata;
import org.hibernate.hql.internal.ast.util.ASTUtil;
import org.hibernate.hql.internal.ast.util.ColumnHelper;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.persister.collection.QueryableCollection;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

public class MethodNode extends AbstractSelectExpression implements FunctionNode {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, MethodNode.class.getName());
   private String methodName;
   private FromElement fromElement;
   private String[] selectColumns;
   private SQLFunction function;
   private boolean inSelect;

   public void resolve(boolean inSelect) throws SemanticException {
      AST name = this.getFirstChild();
      this.initializeMethodNode(name, inSelect);
      AST exprList = name.getNextSibling();
      if (ASTUtil.hasExactlyOneChild(exprList)) {
         if ("type".equals(this.methodName)) {
            this.typeDiscriminator(exprList.getFirstChild());
            return;
         }

         if (this.isCollectionPropertyMethod()) {
            this.collectionProperty(exprList.getFirstChild(), name);
            return;
         }
      }

      this.dialectFunction(exprList);
   }

   private void typeDiscriminator(AST path) throws SemanticException {
      if (path == null) {
         throw new SemanticException("type() discriminator reference has no path!");
      } else {
         FromReferenceNode pathAsFromReferenceNode = (FromReferenceNode)path;
         FromElement fromElement = pathAsFromReferenceNode.getFromElement();
         TypeDiscriminatorMetadata typeDiscriminatorMetadata = fromElement.getTypeDiscriminatorMetadata();
         this.setDataType(typeDiscriminatorMetadata.getResolutionType());
         this.setText(typeDiscriminatorMetadata.getSqlFragment());
         this.setType(142);
      }
   }

   public SQLFunction getSQLFunction() {
      return this.function;
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

   private void dialectFunction(AST exprList) {
      this.function = this.getSessionFactoryHelper().findSQLFunction(this.methodName);
      if (this.function != null) {
         AST firstChild = exprList != null ? exprList.getFirstChild() : null;
         Type functionReturnType = this.getSessionFactoryHelper().findFunctionReturnType(this.methodName, firstChild);
         this.setDataType(functionReturnType);
      }

   }

   public boolean isCollectionPropertyMethod() {
      return CollectionProperties.isAnyCollectionProperty(this.methodName);
   }

   public void initializeMethodNode(AST name, boolean inSelect) {
      name.setType(147);
      String text = name.getText();
      this.methodName = text.toLowerCase();
      this.inSelect = inSelect;
   }

   private String getMethodName() {
      return this.methodName;
   }

   private void collectionProperty(AST path, AST name) throws SemanticException {
      if (path == null) {
         throw new SemanticException("Collection function " + name.getText() + " has no path!");
      } else {
         SqlNode expr = (SqlNode)path;
         Type type = expr.getDataType();
         LOG.debugf("collectionProperty() :  name=%s type=%s", name, type);
         this.resolveCollectionProperty(expr);
      }
   }

   public boolean isScalar() throws SemanticException {
      return true;
   }

   public void resolveCollectionProperty(AST expr) throws SemanticException {
      String propertyName = CollectionProperties.getNormalizedPropertyName(this.getMethodName());
      if (expr instanceof FromReferenceNode) {
         FromReferenceNode collectionNode = (FromReferenceNode)expr;
         if ("elements".equals(propertyName)) {
            this.handleElements(collectionNode, propertyName);
         } else {
            this.fromElement = collectionNode.getFromElement();
            this.setDataType(this.fromElement.getPropertyType(propertyName, propertyName));
            this.selectColumns = this.fromElement.toColumns(this.fromElement.getTableAlias(), propertyName, this.inSelect);
         }

         if (collectionNode instanceof DotNode) {
            this.prepareAnyImplicitJoins((DotNode)collectionNode);
         }

         if (!this.inSelect) {
            this.fromElement.setText("");
            this.fromElement.setUseWhereFragment(false);
         }

         this.prepareSelectColumns(this.selectColumns);
         this.setText(this.selectColumns[0]);
         this.setType(142);
      } else {
         throw new SemanticException("Unexpected expression " + expr + " found for collection function " + propertyName);
      }
   }

   private void prepareAnyImplicitJoins(DotNode dotNode) throws SemanticException {
      if (dotNode.getLhs() instanceof DotNode) {
         DotNode lhs = (DotNode)dotNode.getLhs();
         FromElement lhsOrigin = lhs.getFromElement();
         if (lhsOrigin != null && "".equals(lhsOrigin.getText())) {
            String lhsOriginText = lhsOrigin.getQueryable().getTableName() + " " + lhsOrigin.getTableAlias();
            lhsOrigin.setText(lhsOriginText);
         }

         this.prepareAnyImplicitJoins(lhs);
      }

   }

   private void handleElements(FromReferenceNode collectionNode, String propertyName) {
      FromElement collectionFromElement = collectionNode.getFromElement();
      QueryableCollection queryableCollection = collectionFromElement.getQueryableCollection();
      String path = collectionNode.getPath() + "[]." + propertyName;
      LOG.debugf("Creating elements for %s", path);
      this.fromElement = collectionFromElement;
      if (!collectionFromElement.isCollectionOfValuesOrComponents()) {
         this.getWalker().addQuerySpaces(queryableCollection.getElementPersister().getQuerySpaces());
      }

      this.setDataType(queryableCollection.getElementType());
      this.selectColumns = collectionFromElement.toColumns(this.fromElement.getTableAlias(), propertyName, this.inSelect);
   }

   public void setScalarColumnText(int i) throws SemanticException {
      if (this.selectColumns == null) {
         ColumnHelper.generateSingleScalarColumn(this, i);
      } else {
         ColumnHelper.generateScalarColumns(this, this.selectColumns, i);
      }

   }

   protected void prepareSelectColumns(String[] columns) {
   }

   public FromElement getFromElement() {
      return this.fromElement;
   }

   public String getDisplayText() {
      return "{method=" + this.getMethodName() + ",selectColumns=" + (this.selectColumns == null ? null : Arrays.asList(this.selectColumns)) + ",fromElement=" + this.fromElement.getTableAlias() + "}";
   }
}
