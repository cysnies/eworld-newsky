package org.hibernate.hql.internal.ast.tree;

import antlr.SemanticException;
import antlr.collections.AST;
import java.util.List;
import org.hibernate.QueryException;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.hql.internal.ast.util.ColumnHelper;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.persister.collection.QueryableCollection;
import org.hibernate.persister.entity.Queryable;
import org.hibernate.sql.JoinType;
import org.hibernate.type.CollectionType;
import org.hibernate.type.Type;

public class IdentNode extends FromReferenceNode implements SelectExpression {
   private static final int UNKNOWN = 0;
   private static final int PROPERTY_REF = 1;
   private static final int COMPONENT_REF = 2;
   private boolean nakedPropertyRef = false;

   public void resolveIndex(AST parent) throws SemanticException {
      if (this.isResolved() && this.nakedPropertyRef) {
         String propertyName = this.getOriginalText();
         if (!this.getDataType().isCollectionType()) {
            throw new SemanticException("Collection expected; [" + propertyName + "] does not refer to a collection property");
         } else {
            CollectionType type = (CollectionType)this.getDataType();
            String role = type.getRole();
            QueryableCollection queryableCollection = this.getSessionFactoryHelper().requireQueryableCollection(role);
            String alias = null;
            String columnTableAlias = this.getFromElement().getTableAlias();
            JoinType joinType = JoinType.INNER_JOIN;
            boolean fetch = false;
            FromElementFactory factory = new FromElementFactory(this.getWalker().getCurrentFromClause(), this.getFromElement(), propertyName, alias, this.getFromElement().toColumns(columnTableAlias, propertyName, false), true);
            FromElement elem = factory.createCollection(queryableCollection, role, joinType, fetch, true);
            this.setFromElement(elem);
            this.getWalker().addQuerySpaces(queryableCollection.getCollectionSpaces());
         }
      } else {
         throw new UnsupportedOperationException();
      }
   }

   public void resolve(boolean generateJoin, boolean implicitJoin, String classAlias, AST parent) {
      if (!this.isResolved()) {
         if (this.getWalker().getCurrentFromClause().isFromElementAlias(this.getText())) {
            if (this.resolveAsAlias()) {
               this.setResolved();
            }
         } else if (parent != null && parent.getType() == 15) {
            DotNode dot = (DotNode)parent;
            if (parent.getFirstChild() == this) {
               if (this.resolveAsNakedComponentPropertyRefLHS(dot)) {
                  this.setResolved();
               }
            } else if (this.resolveAsNakedComponentPropertyRefRHS(dot)) {
               this.setResolved();
            }
         } else {
            int result = this.resolveAsNakedPropertyRef();
            if (result == 1) {
               this.setResolved();
            } else if (result == 2) {
               return;
            }
         }

         if (!this.isResolved()) {
            try {
               this.getWalker().getLiteralProcessor().processConstant(this, false);
            } catch (Throwable var6) {
            }
         }
      }

   }

   private boolean resolveAsAlias() {
      FromElement element = this.getWalker().getCurrentFromClause().getFromElement(this.getText());
      if (element != null) {
         this.setType(140);
         this.setFromElement(element);
         String[] columnExpressions = element.getIdentityColumns();
         boolean isInNonDistinctCount = this.getWalker().isInCount() && !this.getWalker().isInCountDistinct();
         boolean isCompositeValue = columnExpressions.length > 1;
         if (isCompositeValue) {
            if (isInNonDistinctCount && !this.getWalker().getSessionFactoryHelper().getFactory().getDialect().supportsTupleCounts()) {
               this.setText(columnExpressions[0]);
            } else {
               String joinedFragment = StringHelper.join(", ", columnExpressions);
               boolean shouldSkipWrappingInParenthesis = this.getWalker().isInCount() || this.getWalker().getCurrentTopLevelClauseType() == 41 || this.getWalker().getCurrentTopLevelClauseType() == 24;
               if (!shouldSkipWrappingInParenthesis) {
                  joinedFragment = "(" + joinedFragment + ")";
               }

               this.setText(joinedFragment);
            }

            return true;
         }

         if (columnExpressions.length > 0) {
            this.setText(columnExpressions[0]);
            return true;
         }
      }

      return false;
   }

   private Type getNakedPropertyType(FromElement fromElement) {
      if (fromElement == null) {
         return null;
      } else {
         String property = this.getOriginalText();
         Type propertyType = null;

         try {
            propertyType = fromElement.getPropertyType(property, property);
         } catch (Throwable var5) {
         }

         return propertyType;
      }
   }

   private int resolveAsNakedPropertyRef() {
      FromElement fromElement = this.locateSingleFromElement();
      if (fromElement == null) {
         return 0;
      } else {
         Queryable persister = fromElement.getQueryable();
         if (persister == null) {
            return 0;
         } else {
            Type propertyType = this.getNakedPropertyType(fromElement);
            if (propertyType == null) {
               return 0;
            } else if (!propertyType.isComponentType() && !propertyType.isAssociationType()) {
               this.setFromElement(fromElement);
               String property = this.getText();
               String[] columns = this.getWalker().isSelectStatement() ? persister.toColumns(fromElement.getTableAlias(), property) : persister.toColumns(property);
               String text = StringHelper.join(", ", columns);
               this.setText(columns.length == 1 ? text : "(" + text + ")");
               this.setType(142);
               super.setDataType(propertyType);
               this.nakedPropertyRef = true;
               return 1;
            } else {
               return 2;
            }
         }
      }
   }

   private boolean resolveAsNakedComponentPropertyRefLHS(DotNode parent) {
      FromElement fromElement = this.locateSingleFromElement();
      if (fromElement == null) {
         return false;
      } else {
         Type componentType = this.getNakedPropertyType(fromElement);
         if (componentType == null) {
            throw new QueryException("Unable to resolve path [" + parent.getPath() + "], unexpected token [" + this.getOriginalText() + "]");
         } else if (!componentType.isComponentType()) {
            throw new QueryException("Property '" + this.getOriginalText() + "' is not a component.  Use an alias to reference associations or collections.");
         } else {
            Type propertyType = null;
            String propertyPath = this.getText() + "." + this.getNextSibling().getText();

            try {
               propertyType = fromElement.getPropertyType(this.getText(), propertyPath);
            } catch (Throwable var7) {
               return false;
            }

            this.setFromElement(fromElement);
            parent.setPropertyPath(propertyPath);
            parent.setDataType(propertyType);
            return true;
         }
      }
   }

   private boolean resolveAsNakedComponentPropertyRefRHS(DotNode parent) {
      FromElement fromElement = this.locateSingleFromElement();
      if (fromElement == null) {
         return false;
      } else {
         Type propertyType = null;
         String propertyPath = parent.getLhs().getText() + "." + this.getText();

         try {
            propertyType = fromElement.getPropertyType(this.getText(), propertyPath);
         } catch (Throwable var6) {
            return false;
         }

         this.setFromElement(fromElement);
         super.setDataType(propertyType);
         this.nakedPropertyRef = true;
         return true;
      }
   }

   private FromElement locateSingleFromElement() {
      List fromElements = this.getWalker().getCurrentFromClause().getFromElements();
      if (fromElements != null && fromElements.size() == 1) {
         FromElement element = (FromElement)fromElements.get(0);
         return element.getClassAlias() != null ? null : element;
      } else {
         return null;
      }
   }

   public Type getDataType() {
      Type type = super.getDataType();
      if (type != null) {
         return type;
      } else {
         FromElement fe = this.getFromElement();
         if (fe != null) {
            return fe.getDataType();
         } else {
            SQLFunction sf = this.getWalker().getSessionFactoryHelper().findSQLFunction(this.getText());
            return sf != null ? sf.getReturnType((Type)null, this.getWalker().getSessionFactoryHelper().getFactory()) : null;
         }
      }
   }

   public void setScalarColumnText(int i) throws SemanticException {
      if (this.nakedPropertyRef) {
         ColumnHelper.generateSingleScalarColumn(this, i);
      } else {
         FromElement fe = this.getFromElement();
         if (fe != null) {
            this.setText(fe.renderScalarIdentifierSelect(i));
         } else {
            ColumnHelper.generateSingleScalarColumn(this, i);
         }
      }

   }

   public String getDisplayText() {
      StringBuilder buf = new StringBuilder();
      if (this.getType() == 140) {
         buf.append("{alias=").append(this.getOriginalText());
         if (this.getFromElement() == null) {
            buf.append(", no from element");
         } else {
            buf.append(", className=").append(this.getFromElement().getClassName());
            buf.append(", tableAlias=").append(this.getFromElement().getTableAlias());
         }

         buf.append("}");
      } else {
         buf.append("{originalText=" + this.getOriginalText()).append("}");
      }

      return buf.toString();
   }
}
