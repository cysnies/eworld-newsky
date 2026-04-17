package org.hibernate.hql.internal.ast.tree;

import antlr.collections.AST;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.hibernate.QueryException;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.persister.entity.Queryable;
import org.hibernate.type.ComponentType;
import org.hibernate.type.Type;

public class IntoClause extends HqlSqlWalkerNode implements DisplayableNode {
   private Queryable persister;
   private String columnSpec = "";
   private Type[] types;
   private boolean discriminated;
   private boolean explicitIdInsertion;
   private boolean explicitVersionInsertion;
   private Set componentIds;
   private List explicitComponentIds;

   public void initialize(Queryable persister) {
      if (persister.isAbstract()) {
         throw new QueryException("cannot insert into abstract class (no table)");
      } else {
         this.persister = persister;
         this.initializeColumns();
         if (this.getWalker().getSessionFactoryHelper().hasPhysicalDiscriminatorColumn(persister)) {
            this.discriminated = true;
            this.columnSpec = this.columnSpec + ", " + persister.getDiscriminatorColumnName();
         }

         this.resetText();
      }
   }

   private void resetText() {
      this.setText("into " + this.getTableName() + " ( " + this.columnSpec + " )");
   }

   public String getTableName() {
      return this.persister.getSubclassTableName(0);
   }

   public Queryable getQueryable() {
      return this.persister;
   }

   public String getEntityName() {
      return this.persister.getEntityName();
   }

   public Type[] getInsertionTypes() {
      return this.types;
   }

   public boolean isDiscriminated() {
      return this.discriminated;
   }

   public boolean isExplicitIdInsertion() {
      return this.explicitIdInsertion;
   }

   public boolean isExplicitVersionInsertion() {
      return this.explicitVersionInsertion;
   }

   public void prependIdColumnSpec() {
      this.columnSpec = this.persister.getIdentifierColumnNames()[0] + ", " + this.columnSpec;
      this.resetText();
   }

   public void prependVersionColumnSpec() {
      this.columnSpec = this.persister.getPropertyColumnNames(this.persister.getVersionProperty())[0] + ", " + this.columnSpec;
      this.resetText();
   }

   public void validateTypes(SelectClause selectClause) throws QueryException {
      Type[] selectTypes = selectClause.getQueryReturnTypes();
      if (selectTypes.length != this.types.length) {
         throw new QueryException("number of select types did not match those for insert");
      } else {
         for(int i = 0; i < this.types.length; ++i) {
            if (!this.areCompatible(this.types[i], selectTypes[i])) {
               throw new QueryException("insertion type [" + this.types[i] + "] and selection type [" + selectTypes[i] + "] at position " + i + " are not compatible");
            }
         }

      }
   }

   public String getDisplayText() {
      StringBuilder buf = new StringBuilder();
      buf.append("IntoClause{");
      buf.append("entityName=").append(this.getEntityName());
      buf.append(",tableName=").append(this.getTableName());
      buf.append(",columns={").append(this.columnSpec).append("}");
      buf.append("}");
      return buf.toString();
   }

   private void initializeColumns() {
      AST propertySpec = this.getFirstChild();
      List types = new ArrayList();
      this.visitPropertySpecNodes(propertySpec.getFirstChild(), types);
      this.types = ArrayHelper.toTypeArray(types);
      this.columnSpec = this.columnSpec.substring(0, this.columnSpec.length() - 2);
   }

   private void visitPropertySpecNodes(AST propertyNode, List types) {
      if (propertyNode != null) {
         String name = propertyNode.getText();
         if (this.isSuperclassProperty(name)) {
            throw new QueryException("INSERT statements cannot refer to superclass/joined properties [" + name + "]");
         } else {
            if (!this.explicitIdInsertion) {
               if (this.persister.getIdentifierType() instanceof ComponentType) {
                  if (this.componentIds == null) {
                     String[] propertyNames = ((ComponentType)this.persister.getIdentifierType()).getPropertyNames();
                     this.componentIds = new HashSet();

                     for(int i = 0; i < propertyNames.length; ++i) {
                        this.componentIds.add(propertyNames[i]);
                     }
                  }

                  if (this.componentIds.contains(name)) {
                     if (this.explicitComponentIds == null) {
                        this.explicitComponentIds = new ArrayList(this.componentIds.size());
                     }

                     this.explicitComponentIds.add(name);
                     this.explicitIdInsertion = this.explicitComponentIds.size() == this.componentIds.size();
                  }
               } else if (name.equals(this.persister.getIdentifierPropertyName())) {
                  this.explicitIdInsertion = true;
               }
            }

            if (this.persister.isVersioned() && name.equals(this.persister.getPropertyNames()[this.persister.getVersionProperty()])) {
               this.explicitVersionInsertion = true;
            }

            String[] columnNames = this.persister.toColumns(name);
            this.renderColumns(columnNames);
            types.add(this.persister.toType(name));
            this.visitPropertySpecNodes(propertyNode.getNextSibling(), types);
            this.visitPropertySpecNodes(propertyNode.getFirstChild(), types);
         }
      }
   }

   private void renderColumns(String[] columnNames) {
      for(int i = 0; i < columnNames.length; ++i) {
         this.columnSpec = this.columnSpec + columnNames[i] + ", ";
      }

   }

   private boolean isSuperclassProperty(String propertyName) {
      return this.persister.getSubclassPropertyTableNumber(propertyName) != 0;
   }

   private boolean areCompatible(Type target, Type source) {
      if (target.equals(source)) {
         return true;
      } else if (!target.getReturnedClass().isAssignableFrom(source.getReturnedClass())) {
         return false;
      } else {
         int[] targetDatatypes = target.sqlTypes(this.getSessionFactoryHelper().getFactory());
         int[] sourceDatatypes = source.sqlTypes(this.getSessionFactoryHelper().getFactory());
         if (targetDatatypes.length != sourceDatatypes.length) {
            return false;
         } else {
            for(int i = 0; i < targetDatatypes.length; ++i) {
               if (!this.areSqlTypesCompatible(targetDatatypes[i], sourceDatatypes[i])) {
                  return false;
               }
            }

            return true;
         }
      }
   }

   private boolean areSqlTypesCompatible(int target, int source) {
      switch (target) {
         case 91:
            return source == 91 || source == 93;
         case 92:
            return source == 92 || source == 93;
         case 93:
            return source == 91 || source == 92 || source == 93;
         default:
            return target == source;
      }
   }
}
