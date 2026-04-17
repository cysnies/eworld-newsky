package org.hibernate.mapping;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import org.hibernate.FetchMode;
import org.hibernate.MappingException;
import org.hibernate.annotations.common.reflection.XProperty;
import org.hibernate.cfg.Mappings;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.IdentityGenerator;
import org.hibernate.id.factory.IdentifierGeneratorFactory;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.type.Type;
import org.hibernate.usertype.DynamicParameterizedType;

public class SimpleValue implements KeyValue {
   public static final String DEFAULT_ID_GEN_STRATEGY = "assigned";
   private final Mappings mappings;
   private final java.util.List columns;
   private String typeName;
   private Properties identifierGeneratorProperties;
   private String identifierGeneratorStrategy;
   private String nullValue;
   private Table table;
   private String foreignKeyName;
   private boolean alternateUniqueKey;
   private Properties typeParameters;
   private boolean cascadeDeleteEnabled;

   public SimpleValue(Mappings mappings) {
      this.columns = new ArrayList();
      this.identifierGeneratorStrategy = "assigned";
      this.mappings = mappings;
   }

   public SimpleValue(Mappings mappings, Table table) {
      this(mappings);
      this.table = table;
   }

   public Mappings getMappings() {
      return this.mappings;
   }

   public boolean isCascadeDeleteEnabled() {
      return this.cascadeDeleteEnabled;
   }

   public void setCascadeDeleteEnabled(boolean cascadeDeleteEnabled) {
      this.cascadeDeleteEnabled = cascadeDeleteEnabled;
   }

   public void addColumn(Column column) {
      if (!this.columns.contains(column)) {
         this.columns.add(column);
      }

      column.setValue(this);
      column.setTypeIndex(this.columns.size() - 1);
   }

   public void addFormula(Formula formula) {
      this.columns.add(formula);
   }

   public boolean hasFormula() {
      Iterator iter = this.getColumnIterator();

      while(iter.hasNext()) {
         Object o = iter.next();
         if (o instanceof Formula) {
            return true;
         }
      }

      return false;
   }

   public int getColumnSpan() {
      return this.columns.size();
   }

   public Iterator getColumnIterator() {
      return this.columns.iterator();
   }

   public java.util.List getConstraintColumns() {
      return this.columns;
   }

   public String getTypeName() {
      return this.typeName;
   }

   public void setTypeName(String type) {
      this.typeName = type;
   }

   public void setTable(Table table) {
      this.table = table;
   }

   public void createForeignKey() throws MappingException {
   }

   public void createForeignKeyOfEntity(String entityName) {
      if (!this.hasFormula() && !"none".equals(this.getForeignKeyName())) {
         ForeignKey fk = this.table.createForeignKey(this.getForeignKeyName(), this.getConstraintColumns(), entityName);
         fk.setCascadeDeleteEnabled(this.cascadeDeleteEnabled);
      }

   }

   public IdentifierGenerator createIdentifierGenerator(IdentifierGeneratorFactory identifierGeneratorFactory, Dialect dialect, String defaultCatalog, String defaultSchema, RootClass rootClass) throws MappingException {
      Properties params = new Properties();
      if (defaultSchema != null) {
         params.setProperty("schema", defaultSchema);
      }

      if (defaultCatalog != null) {
         params.setProperty("catalog", defaultCatalog);
      }

      if (rootClass != null) {
         params.setProperty("entity_name", rootClass.getEntityName());
         params.setProperty("jpa_entity_name", rootClass.getJpaEntityName());
      }

      String tableName = this.getTable().getQuotedName(dialect);
      params.setProperty("target_table", tableName);
      String columnName = ((Column)this.getColumnIterator().next()).getQuotedName(dialect);
      params.setProperty("target_column", columnName);
      if (rootClass != null) {
         StringBuilder tables = new StringBuilder();
         Iterator iter = rootClass.getIdentityTables().iterator();

         while(iter.hasNext()) {
            Table table = (Table)iter.next();
            tables.append(table.getQuotedName(dialect));
            if (iter.hasNext()) {
               tables.append(", ");
            }
         }

         params.setProperty("identity_tables", tables.toString());
      } else {
         params.setProperty("identity_tables", tableName);
      }

      if (this.identifierGeneratorProperties != null) {
         params.putAll(this.identifierGeneratorProperties);
      }

      params.put("hibernate.id.optimizer.pooled.prefer_lo", this.mappings.getConfigurationProperties().getProperty("hibernate.id.optimizer.pooled.prefer_lo", "false"));
      identifierGeneratorFactory.setDialect(dialect);
      return identifierGeneratorFactory.createIdentifierGenerator(this.identifierGeneratorStrategy, this.getType(), params);
   }

   public boolean isUpdateable() {
      return true;
   }

   public FetchMode getFetchMode() {
      return FetchMode.SELECT;
   }

   public Properties getIdentifierGeneratorProperties() {
      return this.identifierGeneratorProperties;
   }

   public String getNullValue() {
      return this.nullValue;
   }

   public Table getTable() {
      return this.table;
   }

   public String getIdentifierGeneratorStrategy() {
      return this.identifierGeneratorStrategy;
   }

   public boolean isIdentityColumn(IdentifierGeneratorFactory identifierGeneratorFactory, Dialect dialect) {
      identifierGeneratorFactory.setDialect(dialect);
      return identifierGeneratorFactory.getIdentifierGeneratorClass(this.identifierGeneratorStrategy).equals(IdentityGenerator.class);
   }

   public void setIdentifierGeneratorProperties(Properties identifierGeneratorProperties) {
      this.identifierGeneratorProperties = identifierGeneratorProperties;
   }

   public void setIdentifierGeneratorStrategy(String identifierGeneratorStrategy) {
      this.identifierGeneratorStrategy = identifierGeneratorStrategy;
   }

   public void setNullValue(String nullValue) {
      this.nullValue = nullValue;
   }

   public String getForeignKeyName() {
      return this.foreignKeyName;
   }

   public void setForeignKeyName(String foreignKeyName) {
      this.foreignKeyName = foreignKeyName;
   }

   public boolean isAlternateUniqueKey() {
      return this.alternateUniqueKey;
   }

   public void setAlternateUniqueKey(boolean unique) {
      this.alternateUniqueKey = unique;
   }

   public boolean isNullable() {
      if (this.hasFormula()) {
         return true;
      } else {
         boolean nullable = true;
         Iterator iter = this.getColumnIterator();

         while(iter.hasNext()) {
            if (!((Column)iter.next()).isNullable()) {
               nullable = false;
               return nullable;
            }
         }

         return nullable;
      }
   }

   public boolean isSimpleValue() {
      return true;
   }

   public boolean isValid(Mapping mapping) throws MappingException {
      return this.getColumnSpan() == this.getType().getColumnSpan(mapping);
   }

   public Type getType() throws MappingException {
      if (this.typeName == null) {
         throw new MappingException("No type name");
      } else {
         if (this.typeParameters != null && Boolean.valueOf(this.typeParameters.getProperty("org.hibernate.type.ParameterType.dynamic")) && this.typeParameters.get("org.hibernate.type.ParameterType") == null) {
            this.createParameterImpl();
         }

         Type result = this.mappings.getTypeResolver().heuristicType(this.typeName, this.typeParameters);
         if (result == null) {
            String msg = "Could not determine type for: " + this.typeName;
            if (this.table != null) {
               msg = msg + ", at table: " + this.table.getName();
            }

            if (this.columns != null && this.columns.size() > 0) {
               msg = msg + ", for columns: " + this.columns;
            }

            throw new MappingException(msg);
         } else {
            return result;
         }
      }
   }

   public void setTypeUsingReflection(String className, String propertyName) throws MappingException {
      if (this.typeName == null) {
         if (className == null) {
            throw new MappingException("you must specify types for a dynamic entity: " + propertyName);
         }

         this.typeName = ReflectHelper.reflectedPropertyClass(className, propertyName).getName();
      }

   }

   public boolean isTypeSpecified() {
      return this.typeName != null;
   }

   public void setTypeParameters(Properties parameterMap) {
      this.typeParameters = parameterMap;
   }

   public Properties getTypeParameters() {
      return this.typeParameters;
   }

   public String toString() {
      return this.getClass().getName() + '(' + this.columns.toString() + ')';
   }

   public Object accept(ValueVisitor visitor) {
      return visitor.accept(this);
   }

   public boolean[] getColumnInsertability() {
      boolean[] result = new boolean[this.getColumnSpan()];
      int i = 0;

      Selectable s;
      for(Iterator iter = this.getColumnIterator(); iter.hasNext(); result[i++] = !s.isFormula()) {
         s = (Selectable)iter.next();
      }

      return result;
   }

   public boolean[] getColumnUpdateability() {
      return this.getColumnInsertability();
   }

   private void createParameterImpl() {
      try {
         String[] columnsNames = new String[this.columns.size()];

         for(int i = 0; i < this.columns.size(); ++i) {
            columnsNames[i] = ((Column)this.columns.get(i)).getName();
         }

         XProperty xProperty = (XProperty)this.typeParameters.get("org.hibernate.type.ParameterType.xproperty");
         Annotation[] annotations = xProperty == null ? null : xProperty.getAnnotations();
         this.typeParameters.put("org.hibernate.type.ParameterType", new ParameterTypeImpl(ReflectHelper.classForName(this.typeParameters.getProperty("org.hibernate.type.ParameterType.returnedClass")), annotations, this.table.getCatalog(), this.table.getSchema(), this.table.getName(), Boolean.valueOf(this.typeParameters.getProperty("org.hibernate.type.ParameterType.primaryKey")), columnsNames));
      } catch (ClassNotFoundException cnfe) {
         throw new MappingException("Could not create DynamicParameterizedType for type: " + this.typeName, cnfe);
      }
   }

   private final class ParameterTypeImpl implements DynamicParameterizedType.ParameterType {
      private final Class returnedClass;
      private final Annotation[] annotationsMethod;
      private final String catalog;
      private final String schema;
      private final String table;
      private final boolean primaryKey;
      private final String[] columns;

      private ParameterTypeImpl(Class returnedClass, Annotation[] annotationsMethod, String catalog, String schema, String table, boolean primaryKey, String[] columns) {
         this.returnedClass = returnedClass;
         this.annotationsMethod = annotationsMethod;
         this.catalog = catalog;
         this.schema = schema;
         this.table = table;
         this.primaryKey = primaryKey;
         this.columns = columns;
      }

      public Class getReturnedClass() {
         return this.returnedClass;
      }

      public Annotation[] getAnnotationsMethod() {
         return this.annotationsMethod;
      }

      public String getCatalog() {
         return this.catalog;
      }

      public String getSchema() {
         return this.schema;
      }

      public String getTable() {
         return this.table;
      }

      public boolean isPrimaryKey() {
         return this.primaryKey;
      }

      public String[] getColumns() {
         return this.columns;
      }
   }
}
