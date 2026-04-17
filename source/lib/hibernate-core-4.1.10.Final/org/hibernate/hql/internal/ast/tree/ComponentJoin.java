package org.hibernate.hql.internal.ast.tree;

import org.hibernate.QueryException;
import org.hibernate.hql.internal.NameGenerator;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.persister.collection.QueryableCollection;
import org.hibernate.persister.entity.PropertyMapping;
import org.hibernate.type.ComponentType;
import org.hibernate.type.Type;

public class ComponentJoin extends FromElement {
   private final String componentPath;
   private final ComponentType componentType;
   private final String componentProperty;
   private final String[] columns;
   private final String columnsFragment;

   public ComponentJoin(FromClause fromClause, FromElement origin, String alias, String componentPath, ComponentType componentType) {
      super(fromClause, origin, alias);
      this.componentPath = componentPath;
      this.componentType = componentType;
      this.componentProperty = StringHelper.unqualify(componentPath);
      fromClause.addJoinByPathMap(componentPath, this);
      this.initializeComponentJoin(new ComponentFromElementType(this));
      this.columns = origin.getPropertyMapping("").toColumns(this.getTableAlias(), this.componentProperty);
      StringBuilder buf = new StringBuilder();

      for(int j = 0; j < this.columns.length; ++j) {
         String column = this.columns[j];
         if (j > 0) {
            buf.append(", ");
         }

         buf.append(column);
      }

      this.columnsFragment = buf.toString();
   }

   public String getComponentPath() {
      return this.componentPath;
   }

   public String getComponentProperty() {
      return this.componentProperty;
   }

   public ComponentType getComponentType() {
      return this.componentType;
   }

   public Type getDataType() {
      return this.getComponentType();
   }

   public String getIdentityColumn() {
      return this.columnsFragment;
   }

   public String[] getIdentityColumns() {
      return this.columns;
   }

   public String getDisplayText() {
      return "ComponentJoin{path=" + this.getComponentPath() + ", type=" + this.componentType.getReturnedClass() + "}";
   }

   protected PropertyMapping getBasePropertyMapping() {
      return this.getOrigin().getPropertyMapping("");
   }

   public class ComponentFromElementType extends FromElementType {
      private final PropertyMapping propertyMapping = ComponentJoin.this.new ComponentPropertyMapping();

      public ComponentFromElementType(FromElement fromElement) {
         super(fromElement);
      }

      public Type getDataType() {
         return ComponentJoin.this.getComponentType();
      }

      public QueryableCollection getQueryableCollection() {
         return null;
      }

      public PropertyMapping getPropertyMapping(String propertyName) {
         return this.propertyMapping;
      }

      public Type getPropertyType(String propertyName, String propertyPath) {
         int index = ComponentJoin.this.getComponentType().getPropertyIndex(propertyName);
         return ComponentJoin.this.getComponentType().getSubtypes()[index];
      }

      public String renderScalarIdentifierSelect(int i) {
         String[] cols = ComponentJoin.this.getBasePropertyMapping().toColumns(ComponentJoin.this.getTableAlias(), ComponentJoin.this.getComponentProperty());
         StringBuilder buf = new StringBuilder();

         for(int j = 0; j < cols.length; ++j) {
            String column = cols[j];
            if (j > 0) {
               buf.append(", ");
            }

            buf.append(column).append(" as ").append(NameGenerator.scalarName(i, j));
         }

         return buf.toString();
      }
   }

   private final class ComponentPropertyMapping implements PropertyMapping {
      private ComponentPropertyMapping() {
      }

      public Type getType() {
         return ComponentJoin.this.getComponentType();
      }

      public Type toType(String propertyName) throws QueryException {
         return ComponentJoin.this.getBasePropertyMapping().toType(this.getPropertyPath(propertyName));
      }

      protected String getPropertyPath(String propertyName) {
         return ComponentJoin.this.getComponentPath() + '.' + propertyName;
      }

      public String[] toColumns(String alias, String propertyName) throws QueryException {
         return ComponentJoin.this.getBasePropertyMapping().toColumns(alias, this.getPropertyPath(propertyName));
      }

      public String[] toColumns(String propertyName) throws QueryException, UnsupportedOperationException {
         return ComponentJoin.this.getBasePropertyMapping().toColumns(this.getPropertyPath(propertyName));
      }
   }
}
