package org.hibernate.cfg;

import java.util.Map;
import org.hibernate.AnnotationException;
import org.hibernate.AssertionFailure;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.ColumnTransformers;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.common.reflection.XProperty;
import org.hibernate.cfg.annotations.Nullability;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Formula;
import org.hibernate.mapping.Join;
import org.hibernate.mapping.SimpleValue;
import org.hibernate.mapping.Table;
import org.jboss.logging.Logger;

public class Ejb3Column {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, Ejb3Column.class.getName());
   private Column mappingColumn;
   private boolean insertable = true;
   private boolean updatable = true;
   private String secondaryTableName;
   protected Map joins;
   protected PropertyHolder propertyHolder;
   private Mappings mappings;
   private boolean isImplicit;
   public static final int DEFAULT_COLUMN_LENGTH = 255;
   public String sqlType;
   private int length = 255;
   private int precision;
   private int scale;
   private String logicalColumnName;
   private String propertyName;
   private boolean unique;
   private boolean nullable = true;
   private String formulaString;
   private Formula formula;
   private Table table;
   private String readExpression;
   private String writeExpression;

   public void setTable(Table table) {
      this.table = table;
   }

   public String getLogicalColumnName() {
      return this.logicalColumnName;
   }

   public String getSqlType() {
      return this.sqlType;
   }

   public int getLength() {
      return this.length;
   }

   public int getPrecision() {
      return this.precision;
   }

   public int getScale() {
      return this.scale;
   }

   public boolean isUnique() {
      return this.unique;
   }

   public boolean isFormula() {
      return StringHelper.isNotEmpty(this.formulaString);
   }

   public String getFormulaString() {
      return this.formulaString;
   }

   public String getSecondaryTableName() {
      return this.secondaryTableName;
   }

   public void setFormula(String formula) {
      this.formulaString = formula;
   }

   public boolean isImplicit() {
      return this.isImplicit;
   }

   public void setInsertable(boolean insertable) {
      this.insertable = insertable;
   }

   public void setUpdatable(boolean updatable) {
      this.updatable = updatable;
   }

   protected Mappings getMappings() {
      return this.mappings;
   }

   public void setMappings(Mappings mappings) {
      this.mappings = mappings;
   }

   public void setImplicit(boolean implicit) {
      this.isImplicit = implicit;
   }

   public void setSqlType(String sqlType) {
      this.sqlType = sqlType;
   }

   public void setLength(int length) {
      this.length = length;
   }

   public void setPrecision(int precision) {
      this.precision = precision;
   }

   public void setScale(int scale) {
      this.scale = scale;
   }

   public void setLogicalColumnName(String logicalColumnName) {
      this.logicalColumnName = logicalColumnName;
   }

   public void setPropertyName(String propertyName) {
      this.propertyName = propertyName;
   }

   public String getPropertyName() {
      return this.propertyName;
   }

   public void setUnique(boolean unique) {
      this.unique = unique;
   }

   public boolean isNullable() {
      return this.mappingColumn.isNullable();
   }

   public void bind() {
      if (StringHelper.isNotEmpty(this.formulaString)) {
         LOG.debugf("Binding formula %s", this.formulaString);
         this.formula = new Formula();
         this.formula.setFormula(this.formulaString);
      } else {
         this.initMappingColumn(this.logicalColumnName, this.propertyName, this.length, this.precision, this.scale, this.nullable, this.sqlType, this.unique, true);
         if (LOG.isDebugEnabled()) {
            LOG.debugf("Binding column: %s", this.toString());
         }
      }

   }

   protected void initMappingColumn(String columnName, String propertyName, int length, int precision, int scale, boolean nullable, String sqlType, boolean unique, boolean applyNamingStrategy) {
      if (StringHelper.isNotEmpty(this.formulaString)) {
         this.formula = new Formula();
         this.formula.setFormula(this.formulaString);
      } else {
         this.mappingColumn = new Column();
         this.redefineColumnName(columnName, propertyName, applyNamingStrategy);
         this.mappingColumn.setLength(length);
         if (precision > 0) {
            this.mappingColumn.setPrecision(precision);
            this.mappingColumn.setScale(scale);
         }

         this.mappingColumn.setNullable(nullable);
         this.mappingColumn.setSqlType(sqlType);
         this.mappingColumn.setUnique(unique);
         if (this.writeExpression != null && !this.writeExpression.matches("[^?]*\\?[^?]*")) {
            throw new AnnotationException("@WriteExpression must contain exactly one value placeholder ('?') character: property [" + propertyName + "] and column [" + this.logicalColumnName + "]");
         }

         if (this.readExpression != null) {
            this.mappingColumn.setCustomRead(this.readExpression);
         }

         if (this.writeExpression != null) {
            this.mappingColumn.setCustomWrite(this.writeExpression);
         }
      }

   }

   public boolean isNameDeferred() {
      return this.mappingColumn == null || StringHelper.isEmpty(this.mappingColumn.getName());
   }

   public void redefineColumnName(String columnName, String propertyName, boolean applyNamingStrategy) {
      if (applyNamingStrategy) {
         if (StringHelper.isEmpty(columnName)) {
            if (propertyName != null) {
               this.mappingColumn.setName(this.mappings.getObjectNameNormalizer().normalizeIdentifierQuoting(this.mappings.getNamingStrategy().propertyToColumnName(propertyName)));
            }
         } else {
            columnName = this.mappings.getObjectNameNormalizer().normalizeIdentifierQuoting(columnName);
            columnName = this.mappings.getNamingStrategy().columnName(columnName);
            columnName = this.mappings.getObjectNameNormalizer().normalizeIdentifierQuoting(columnName);
            this.mappingColumn.setName(columnName);
         }
      } else if (StringHelper.isNotEmpty(columnName)) {
         this.mappingColumn.setName(this.mappings.getObjectNameNormalizer().normalizeIdentifierQuoting(columnName));
      }

   }

   public String getName() {
      return this.mappingColumn.getName();
   }

   public Column getMappingColumn() {
      return this.mappingColumn;
   }

   public boolean isInsertable() {
      return this.insertable;
   }

   public boolean isUpdatable() {
      return this.updatable;
   }

   public void setNullable(boolean nullable) {
      if (this.mappingColumn != null) {
         this.mappingColumn.setNullable(nullable);
      } else {
         this.nullable = nullable;
      }

   }

   public void setJoins(Map joins) {
      this.joins = joins;
   }

   public PropertyHolder getPropertyHolder() {
      return this.propertyHolder;
   }

   public void setPropertyHolder(PropertyHolder propertyHolder) {
      this.propertyHolder = propertyHolder;
   }

   protected void setMappingColumn(Column mappingColumn) {
      this.mappingColumn = mappingColumn;
   }

   public void linkWithValue(SimpleValue value) {
      if (this.formula != null) {
         value.addFormula(this.formula);
      } else {
         this.getMappingColumn().setValue(value);
         value.addColumn(this.getMappingColumn());
         value.getTable().addColumn(this.getMappingColumn());
         this.addColumnBinding(value);
         this.table = value.getTable();
      }

   }

   protected void addColumnBinding(SimpleValue value) {
      String logicalColumnName = this.mappings.getNamingStrategy().logicalColumnName(this.logicalColumnName, this.propertyName);
      this.mappings.addColumnBinding(logicalColumnName, this.getMappingColumn(), value.getTable());
   }

   public Table getTable() {
      if (this.table != null) {
         return this.table;
      } else {
         return this.isSecondary() ? this.getJoin().getTable() : this.propertyHolder.getTable();
      }
   }

   public boolean isSecondary() {
      if (this.propertyHolder == null) {
         throw new AssertionFailure("Should not call getTable() on column wo persistent class defined");
      } else {
         return StringHelper.isNotEmpty(this.secondaryTableName);
      }
   }

   public Join getJoin() {
      Join join = (Join)this.joins.get(this.secondaryTableName);
      if (join == null) {
         throw new AnnotationException("Cannot find the expected secondary table: no " + this.secondaryTableName + " available for " + this.propertyHolder.getClassName());
      } else {
         return join;
      }
   }

   public void forceNotNull() {
      this.mappingColumn.setNullable(false);
   }

   public void setSecondaryTableName(String secondaryTableName) {
      if ("``".equals(secondaryTableName)) {
         this.secondaryTableName = "";
      } else {
         this.secondaryTableName = secondaryTableName;
      }

   }

   public static Ejb3Column[] buildColumnFromAnnotation(javax.persistence.Column[] anns, org.hibernate.annotations.Formula formulaAnn, Nullability nullability, PropertyHolder propertyHolder, PropertyData inferredData, Map secondaryTables, Mappings mappings) {
      return buildColumnFromAnnotation(anns, formulaAnn, nullability, propertyHolder, inferredData, (String)null, secondaryTables, mappings);
   }

   public static Ejb3Column[] buildColumnFromAnnotation(javax.persistence.Column[] anns, org.hibernate.annotations.Formula formulaAnn, Nullability nullability, PropertyHolder propertyHolder, PropertyData inferredData, String suffixForDefaultColumnName, Map secondaryTables, Mappings mappings) {
      Ejb3Column[] columns;
      if (formulaAnn != null) {
         Ejb3Column formulaColumn = new Ejb3Column();
         formulaColumn.setFormula(formulaAnn.value());
         formulaColumn.setImplicit(false);
         formulaColumn.setMappings(mappings);
         formulaColumn.setPropertyHolder(propertyHolder);
         formulaColumn.bind();
         columns = new Ejb3Column[]{formulaColumn};
      } else {
         javax.persistence.Column[] actualCols = anns;
         javax.persistence.Column[] overriddenCols = propertyHolder.getOverriddenColumn(StringHelper.qualify(propertyHolder.getPath(), inferredData.getPropertyName()));
         if (overriddenCols != null) {
            if (anns != null && overriddenCols.length != anns.length) {
               throw new AnnotationException("AttributeOverride.column() should override all columns for now");
            }

            actualCols = overriddenCols.length == 0 ? null : overriddenCols;
            LOG.debugf("Column(s) overridden for property %s", inferredData.getPropertyName());
         }

         if (actualCols == null) {
            columns = buildImplicitColumn(inferredData, suffixForDefaultColumnName, secondaryTables, propertyHolder, nullability, mappings);
         } else {
            int length = actualCols.length;
            columns = new Ejb3Column[length];

            for(int index = 0; index < length; ++index) {
               ObjectNameNormalizer nameNormalizer = mappings.getObjectNameNormalizer();
               javax.persistence.Column col = actualCols[index];
               String sqlType = col.columnDefinition().equals("") ? null : nameNormalizer.normalizeIdentifierQuoting(col.columnDefinition());
               String tableName = !StringHelper.isEmpty(col.table()) ? nameNormalizer.normalizeIdentifierQuoting(mappings.getNamingStrategy().tableName(col.table())) : "";
               String columnName = nameNormalizer.normalizeIdentifierQuoting(col.name());
               Ejb3Column column = new Ejb3Column();
               column.setImplicit(false);
               column.setSqlType(sqlType);
               column.setLength(col.length());
               column.setPrecision(col.precision());
               column.setScale(col.scale());
               if (StringHelper.isEmpty(columnName) && !StringHelper.isEmpty(suffixForDefaultColumnName)) {
                  column.setLogicalColumnName(inferredData.getPropertyName() + suffixForDefaultColumnName);
               } else {
                  column.setLogicalColumnName(columnName);
               }

               column.setPropertyName(BinderHelper.getRelativePath(propertyHolder, inferredData.getPropertyName()));
               column.setNullable(col.nullable());
               column.setUnique(col.unique());
               column.setInsertable(col.insertable());
               column.setUpdatable(col.updatable());
               column.setSecondaryTableName(tableName);
               column.setPropertyHolder(propertyHolder);
               column.setJoins(secondaryTables);
               column.setMappings(mappings);
               column.extractDataFromPropertyData(inferredData);
               column.bind();
               columns[index] = column;
            }
         }
      }

      return columns;
   }

   private void extractDataFromPropertyData(PropertyData inferredData) {
      if (inferredData != null) {
         XProperty property = inferredData.getProperty();
         if (property != null) {
            this.processExpression((ColumnTransformer)property.getAnnotation(ColumnTransformer.class));
            ColumnTransformers annotations = (ColumnTransformers)property.getAnnotation(ColumnTransformers.class);
            if (annotations != null) {
               for(ColumnTransformer annotation : annotations.value()) {
                  this.processExpression(annotation);
               }
            }
         }
      }

   }

   private void processExpression(ColumnTransformer annotation) {
      String nonNullLogicalColumnName = this.logicalColumnName != null ? this.logicalColumnName : "";
      if (annotation != null && (StringHelper.isEmpty(annotation.forColumn()) || annotation.forColumn().equals(nonNullLogicalColumnName))) {
         this.readExpression = annotation.read();
         if (StringHelper.isEmpty(this.readExpression)) {
            this.readExpression = null;
         }

         this.writeExpression = annotation.write();
         if (StringHelper.isEmpty(this.writeExpression)) {
            this.writeExpression = null;
         }
      }

   }

   private static Ejb3Column[] buildImplicitColumn(PropertyData inferredData, String suffixForDefaultColumnName, Map secondaryTables, PropertyHolder propertyHolder, Nullability nullability, Mappings mappings) {
      Ejb3Column column = new Ejb3Column();
      Ejb3Column[] columns = new Ejb3Column[1];
      columns[0] = column;
      if (nullability != Nullability.FORCED_NULL && inferredData.getClassOrElement().isPrimitive() && !inferredData.getProperty().isArray()) {
         column.setNullable(false);
      }

      column.setLength(255);
      String propertyName = inferredData.getPropertyName();
      column.setPropertyName(BinderHelper.getRelativePath(propertyHolder, propertyName));
      column.setPropertyHolder(propertyHolder);
      column.setJoins(secondaryTables);
      column.setMappings(mappings);
      if (!StringHelper.isEmpty(suffixForDefaultColumnName)) {
         column.setLogicalColumnName(propertyName + suffixForDefaultColumnName);
         column.setImplicit(false);
      } else {
         column.setImplicit(true);
      }

      column.extractDataFromPropertyData(inferredData);
      column.bind();
      return columns;
   }

   public static void checkPropertyConsistency(Ejb3Column[] columns, String propertyName) {
      int nbrOfColumns = columns.length;
      if (nbrOfColumns > 1) {
         for(int currentIndex = 1; currentIndex < nbrOfColumns; ++currentIndex) {
            if (!columns[currentIndex].isFormula() && !columns[currentIndex - 1].isFormula()) {
               if (columns[currentIndex].isInsertable() != columns[currentIndex - 1].isInsertable()) {
                  throw new AnnotationException("Mixing insertable and non insertable columns in a property is not allowed: " + propertyName);
               }

               if (columns[currentIndex].isNullable() != columns[currentIndex - 1].isNullable()) {
                  throw new AnnotationException("Mixing nullable and non nullable columns in a property is not allowed: " + propertyName);
               }

               if (columns[currentIndex].isUpdatable() != columns[currentIndex - 1].isUpdatable()) {
                  throw new AnnotationException("Mixing updatable and non updatable columns in a property is not allowed: " + propertyName);
               }

               if (!columns[currentIndex].getTable().equals(columns[currentIndex - 1].getTable())) {
                  throw new AnnotationException("Mixing different tables in a property is not allowed: " + propertyName);
               }
            }
         }
      }

   }

   public void addIndex(Index index, boolean inSecondPass) {
      if (index != null) {
         String indexName = index.name();
         this.addIndex(indexName, inSecondPass);
      }
   }

   void addIndex(String indexName, boolean inSecondPass) {
      IndexOrUniqueKeySecondPass secondPass = new IndexOrUniqueKeySecondPass(indexName, this, this.mappings, false);
      if (inSecondPass) {
         secondPass.doSecondPass(this.mappings.getClasses());
      } else {
         this.mappings.addSecondPass(secondPass);
      }

   }

   void addUniqueKey(String uniqueKeyName, boolean inSecondPass) {
      IndexOrUniqueKeySecondPass secondPass = new IndexOrUniqueKeySecondPass(uniqueKeyName, this, this.mappings, true);
      if (inSecondPass) {
         secondPass.doSecondPass(this.mappings.getClasses());
      } else {
         this.mappings.addSecondPass(secondPass);
      }

   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("Ejb3Column");
      sb.append("{table=").append(this.getTable());
      sb.append(", mappingColumn=").append(this.mappingColumn.getName());
      sb.append(", insertable=").append(this.insertable);
      sb.append(", updatable=").append(this.updatable);
      sb.append(", unique=").append(this.unique);
      sb.append('}');
      return sb.toString();
   }
}
