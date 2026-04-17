package org.hibernate.hql.internal.ast.tree;

import antlr.SemanticException;
import antlr.collections.AST;
import java.util.Map;
import org.hibernate.hql.internal.antlr.HqlSqlTokenTypes;
import org.hibernate.hql.internal.ast.util.ColumnHelper;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.persister.collection.QueryableCollection;
import org.hibernate.type.CollectionType;
import org.hibernate.type.Type;

public abstract class AbstractMapComponentNode extends FromReferenceNode implements HqlSqlTokenTypes {
   private String[] columns;

   public FromReferenceNode getMapReference() {
      return (FromReferenceNode)this.getFirstChild();
   }

   public String[] getColumns() {
      return this.columns;
   }

   public void setScalarColumnText(int i) throws SemanticException {
      ColumnHelper.generateScalarColumns(this, this.getColumns(), i);
   }

   public void resolve(boolean generateJoin, boolean implicitJoin, String classAlias, AST parent) throws SemanticException {
      if (parent != null) {
         throw this.attemptedDereference();
      } else {
         FromReferenceNode mapReference = this.getMapReference();
         mapReference.resolve(true, true);
         FromElement sourceFromElement = null;
         if (this.isAliasRef(mapReference)) {
            QueryableCollection collectionPersister = mapReference.getFromElement().getQueryableCollection();
            if (Map.class.isAssignableFrom(collectionPersister.getCollectionType().getReturnedClass())) {
               sourceFromElement = mapReference.getFromElement();
            }
         } else if (mapReference.getDataType().isCollectionType()) {
            CollectionType collectionType = (CollectionType)mapReference.getDataType();
            if (Map.class.isAssignableFrom(collectionType.getReturnedClass())) {
               sourceFromElement = mapReference.getFromElement();
            }
         }

         if (sourceFromElement == null) {
            throw this.nonMap();
         } else {
            this.setFromElement(sourceFromElement);
            this.setDataType(this.resolveType(sourceFromElement.getQueryableCollection()));
            this.columns = this.resolveColumns(sourceFromElement.getQueryableCollection());
            this.initText(this.columns);
            this.setFirstChild((AST)null);
         }
      }
   }

   private boolean isAliasRef(FromReferenceNode mapReference) {
      return 140 == mapReference.getType();
   }

   private void initText(String[] columns) {
      String text = StringHelper.join(", ", columns);
      if (columns.length > 1 && this.getWalker().isComparativeExpressionClause()) {
         text = "(" + text + ")";
      }

      this.setText(text);
   }

   protected abstract String expressionDescription();

   protected abstract String[] resolveColumns(QueryableCollection var1);

   protected abstract Type resolveType(QueryableCollection var1);

   protected SemanticException attemptedDereference() {
      return new SemanticException(this.expressionDescription() + " expression cannot be further de-referenced");
   }

   protected SemanticException nonMap() {
      return new SemanticException(this.expressionDescription() + " expression did not reference map property");
   }

   public void resolveIndex(AST parent) throws SemanticException {
      throw new UnsupportedOperationException(this.expressionDescription() + " expression cannot be the source for an index operation");
   }
}
