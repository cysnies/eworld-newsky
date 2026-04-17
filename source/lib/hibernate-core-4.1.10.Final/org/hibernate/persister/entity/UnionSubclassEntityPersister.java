package org.hibernate.persister.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import org.hibernate.AssertionFailure;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.MappingException;
import org.hibernate.cache.spi.access.EntityRegionAccessStrategy;
import org.hibernate.cache.spi.access.NaturalIdRegionAccessStrategy;
import org.hibernate.cfg.Settings;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.ExecuteUpdateResultCheckStyle;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.id.IdentityGenerator;
import org.hibernate.internal.FilterAliasGenerator;
import org.hibernate.internal.StaticFilterAliasGenerator;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.internal.util.collections.JoinedIterator;
import org.hibernate.internal.util.collections.SingletonIterator;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Subclass;
import org.hibernate.mapping.Table;
import org.hibernate.metamodel.binding.EntityBinding;
import org.hibernate.sql.SelectFragment;
import org.hibernate.sql.SimpleSelect;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;

public class UnionSubclassEntityPersister extends AbstractEntityPersister {
   private final String subquery;
   private final String tableName;
   private final String[] subclassClosure;
   private final String[] spaces;
   private final String[] subclassSpaces;
   private final Object discriminatorValue;
   private final String discriminatorSQLValue;
   private final Map subclassByDiscriminatorValue = new HashMap();
   private final String[] constraintOrderedTableNames;
   private final String[][] constraintOrderedKeyColumnNames;

   public UnionSubclassEntityPersister(PersistentClass persistentClass, EntityRegionAccessStrategy cacheAccessStrategy, NaturalIdRegionAccessStrategy naturalIdRegionAccessStrategy, SessionFactoryImplementor factory, Mapping mapping) throws HibernateException {
      super(persistentClass, cacheAccessStrategy, naturalIdRegionAccessStrategy, factory);
      if (this.getIdentifierGenerator() instanceof IdentityGenerator) {
         throw new MappingException("Cannot use identity column key generation with <union-subclass> mapping for: " + this.getEntityName());
      } else {
         this.tableName = persistentClass.getTable().getQualifiedName(factory.getDialect(), factory.getSettings().getDefaultCatalogName(), factory.getSettings().getDefaultSchemaName());
         boolean callable = false;
         ExecuteUpdateResultCheckStyle checkStyle = null;
         String sql = persistentClass.getCustomSQLInsert();
         callable = sql != null && persistentClass.isCustomInsertCallable();
         checkStyle = sql == null ? ExecuteUpdateResultCheckStyle.COUNT : (persistentClass.getCustomSQLInsertCheckStyle() == null ? ExecuteUpdateResultCheckStyle.determineDefault(sql, callable) : persistentClass.getCustomSQLInsertCheckStyle());
         this.customSQLInsert = new String[]{sql};
         this.insertCallable = new boolean[]{callable};
         this.insertResultCheckStyles = new ExecuteUpdateResultCheckStyle[]{checkStyle};
         sql = persistentClass.getCustomSQLUpdate();
         callable = sql != null && persistentClass.isCustomUpdateCallable();
         checkStyle = sql == null ? ExecuteUpdateResultCheckStyle.COUNT : (persistentClass.getCustomSQLUpdateCheckStyle() == null ? ExecuteUpdateResultCheckStyle.determineDefault(sql, callable) : persistentClass.getCustomSQLUpdateCheckStyle());
         this.customSQLUpdate = new String[]{sql};
         this.updateCallable = new boolean[]{callable};
         this.updateResultCheckStyles = new ExecuteUpdateResultCheckStyle[]{checkStyle};
         sql = persistentClass.getCustomSQLDelete();
         callable = sql != null && persistentClass.isCustomDeleteCallable();
         checkStyle = sql == null ? ExecuteUpdateResultCheckStyle.COUNT : (persistentClass.getCustomSQLDeleteCheckStyle() == null ? ExecuteUpdateResultCheckStyle.determineDefault(sql, callable) : persistentClass.getCustomSQLDeleteCheckStyle());
         this.customSQLDelete = new String[]{sql};
         this.deleteCallable = new boolean[]{callable};
         this.deleteResultCheckStyles = new ExecuteUpdateResultCheckStyle[]{checkStyle};
         this.discriminatorValue = persistentClass.getSubclassId();
         this.discriminatorSQLValue = String.valueOf(persistentClass.getSubclassId());
         int subclassSpan = persistentClass.getSubclassSpan() + 1;
         this.subclassClosure = new String[subclassSpan];
         this.subclassClosure[0] = this.getEntityName();
         this.subclassByDiscriminatorValue.put(persistentClass.getSubclassId(), persistentClass.getEntityName());
         if (persistentClass.isPolymorphic()) {
            Iterator iter = persistentClass.getSubclassIterator();
            int k = 1;

            while(iter.hasNext()) {
               Subclass sc = (Subclass)iter.next();
               this.subclassClosure[k++] = sc.getEntityName();
               this.subclassByDiscriminatorValue.put(sc.getSubclassId(), sc.getEntityName());
            }
         }

         int spacesSize = 1 + persistentClass.getSynchronizedTables().size();
         this.spaces = new String[spacesSize];
         this.spaces[0] = this.tableName;
         Iterator iter = persistentClass.getSynchronizedTables().iterator();

         for(int i = 1; i < spacesSize; ++i) {
            this.spaces[i] = (String)iter.next();
         }

         HashSet subclassTables = new HashSet();
         iter = persistentClass.getSubclassTableClosureIterator();

         while(iter.hasNext()) {
            Table table = (Table)iter.next();
            subclassTables.add(table.getQualifiedName(factory.getDialect(), factory.getSettings().getDefaultCatalogName(), factory.getSettings().getDefaultSchemaName()));
         }

         this.subclassSpaces = ArrayHelper.toStringArray((Collection)subclassTables);
         this.subquery = this.generateSubquery(persistentClass, mapping);
         if (this.isMultiTable()) {
            int idColumnSpan = this.getIdentifierColumnSpan();
            ArrayList tableNames = new ArrayList();
            ArrayList keyColumns = new ArrayList();
            if (!this.isAbstract()) {
               tableNames.add(this.tableName);
               keyColumns.add(this.getIdentifierColumnNames());
            }

            iter = persistentClass.getSubclassTableClosureIterator();

            while(iter.hasNext()) {
               Table tab = (Table)iter.next();
               if (!tab.isAbstractUnionTable()) {
                  String tableName = tab.getQualifiedName(factory.getDialect(), factory.getSettings().getDefaultCatalogName(), factory.getSettings().getDefaultSchemaName());
                  tableNames.add(tableName);
                  String[] key = new String[idColumnSpan];
                  Iterator citer = tab.getPrimaryKey().getColumnIterator();

                  for(int k = 0; k < idColumnSpan; ++k) {
                     key[k] = ((Column)citer.next()).getQuotedName(factory.getDialect());
                  }

                  keyColumns.add(key);
               }
            }

            this.constraintOrderedTableNames = ArrayHelper.toStringArray((Collection)tableNames);
            this.constraintOrderedKeyColumnNames = ArrayHelper.to2DStringArray(keyColumns);
         } else {
            this.constraintOrderedTableNames = new String[]{this.tableName};
            this.constraintOrderedKeyColumnNames = new String[][]{this.getIdentifierColumnNames()};
         }

         this.initLockers();
         this.initSubclassPropertyAliasesMap(persistentClass);
         this.postConstruct(mapping);
      }
   }

   public UnionSubclassEntityPersister(EntityBinding entityBinding, EntityRegionAccessStrategy cacheAccessStrategy, NaturalIdRegionAccessStrategy naturalIdRegionAccessStrategy, SessionFactoryImplementor factory, Mapping mapping) throws HibernateException {
      super(entityBinding, cacheAccessStrategy, naturalIdRegionAccessStrategy, factory);
      this.subquery = null;
      this.tableName = null;
      this.subclassClosure = null;
      this.spaces = null;
      this.subclassSpaces = null;
      this.discriminatorValue = null;
      this.discriminatorSQLValue = null;
      this.constraintOrderedTableNames = null;
      this.constraintOrderedKeyColumnNames = (String[][])null;
   }

   public Serializable[] getQuerySpaces() {
      return this.subclassSpaces;
   }

   public String getTableName() {
      return this.subquery;
   }

   public Type getDiscriminatorType() {
      return StandardBasicTypes.INTEGER;
   }

   public Object getDiscriminatorValue() {
      return this.discriminatorValue;
   }

   public String getDiscriminatorSQLValue() {
      return this.discriminatorSQLValue;
   }

   public String[] getSubclassClosure() {
      return this.subclassClosure;
   }

   public String getSubclassForDiscriminatorValue(Object value) {
      return (String)this.subclassByDiscriminatorValue.get(value);
   }

   public Serializable[] getPropertySpaces() {
      return this.spaces;
   }

   protected boolean isDiscriminatorFormula() {
      return false;
   }

   protected String generateSelectString(LockMode lockMode) {
      SimpleSelect select = (new SimpleSelect(this.getFactory().getDialect())).setLockMode(lockMode).setTableName(this.getTableName()).addColumns(this.getIdentifierColumnNames()).addColumns(this.getSubclassColumnClosure(), this.getSubclassColumnAliasClosure(), this.getSubclassColumnLazyiness()).addColumns(this.getSubclassFormulaClosure(), this.getSubclassFormulaAliasClosure(), this.getSubclassFormulaLazyiness());
      if (this.hasSubclasses()) {
         if (this.isDiscriminatorFormula()) {
            select.addColumn(this.getDiscriminatorFormula(), this.getDiscriminatorAlias());
         } else {
            select.addColumn(this.getDiscriminatorColumnName(), this.getDiscriminatorAlias());
         }
      }

      if (this.getFactory().getSettings().isCommentsEnabled()) {
         select.setComment("load " + this.getEntityName());
      }

      return select.addCondition(this.getIdentifierColumnNames(), "=?").toStatementString();
   }

   protected String getDiscriminatorFormula() {
      return null;
   }

   protected String getTableName(int j) {
      return this.tableName;
   }

   protected String[] getKeyColumns(int j) {
      return this.getIdentifierColumnNames();
   }

   protected boolean isTableCascadeDeleteEnabled(int j) {
      return false;
   }

   protected boolean isPropertyOfTable(int property, int j) {
      return true;
   }

   public String fromTableFragment(String name) {
      return this.getTableName() + ' ' + name;
   }

   public String filterFragment(String name) {
      return this.hasWhere() ? " and " + this.getSQLWhereString(name) : "";
   }

   public String getSubclassPropertyTableName(int i) {
      return this.getTableName();
   }

   protected void addDiscriminatorToSelect(SelectFragment select, String name, String suffix) {
      select.addColumn(name, this.getDiscriminatorColumnName(), this.getDiscriminatorAlias());
   }

   protected int[] getPropertyTableNumbersInSelect() {
      return new int[this.getPropertySpan()];
   }

   protected int getSubclassPropertyTableNumber(int i) {
      return 0;
   }

   public int getSubclassPropertyTableNumber(String propertyName) {
      return 0;
   }

   public boolean isMultiTable() {
      return this.isAbstract() || this.hasSubclasses();
   }

   public int getTableSpan() {
      return 1;
   }

   protected int[] getSubclassColumnTableNumberClosure() {
      return new int[this.getSubclassColumnClosure().length];
   }

   protected int[] getSubclassFormulaTableNumberClosure() {
      return new int[this.getSubclassFormulaClosure().length];
   }

   protected boolean[] getTableHasColumns() {
      return new boolean[]{true};
   }

   protected int[] getPropertyTableNumbers() {
      return new int[this.getPropertySpan()];
   }

   protected String generateSubquery(PersistentClass model, Mapping mapping) {
      Dialect dialect = this.getFactory().getDialect();
      Settings settings = this.getFactory().getSettings();
      if (!model.hasSubclasses()) {
         return model.getTable().getQualifiedName(dialect, settings.getDefaultCatalogName(), settings.getDefaultSchemaName());
      } else {
         HashSet columns = new LinkedHashSet();
         Iterator titer = model.getSubclassTableClosureIterator();

         while(titer.hasNext()) {
            Table table = (Table)titer.next();
            if (!table.isAbstractUnionTable()) {
               Iterator citer = table.getColumnIterator();

               while(citer.hasNext()) {
                  columns.add(citer.next());
               }
            }
         }

         StringBuilder buf = (new StringBuilder()).append("( ");
         Iterator siter = new JoinedIterator(new SingletonIterator(model), model.getSubclassIterator());

         while(siter.hasNext()) {
            PersistentClass clazz = (PersistentClass)siter.next();
            Table table = clazz.getTable();
            if (!table.isAbstractUnionTable()) {
               buf.append("select ");

               for(Column col : columns) {
                  if (!table.containsColumn(col)) {
                     int sqlType = col.getSqlTypeCode(mapping);
                     buf.append(dialect.getSelectClauseNullString(sqlType)).append(" as ");
                  }

                  buf.append(col.getName());
                  buf.append(", ");
               }

               buf.append(clazz.getSubclassId()).append(" as clazz_");
               buf.append(" from ").append(table.getQualifiedName(dialect, settings.getDefaultCatalogName(), settings.getDefaultSchemaName()));
               buf.append(" union ");
               if (dialect.supportsUnionAll()) {
                  buf.append("all ");
               }
            }
         }

         if (buf.length() > 2) {
            buf.setLength(buf.length() - (dialect.supportsUnionAll() ? 11 : 7));
         }

         return buf.append(" )").toString();
      }
   }

   protected String[] getSubclassTableKeyColumns(int j) {
      if (j != 0) {
         throw new AssertionFailure("only one table");
      } else {
         return this.getIdentifierColumnNames();
      }
   }

   public String getSubclassTableName(int j) {
      if (j != 0) {
         throw new AssertionFailure("only one table");
      } else {
         return this.tableName;
      }
   }

   public int getSubclassTableSpan() {
      return 1;
   }

   protected boolean isClassOrSuperclassTable(int j) {
      if (j != 0) {
         throw new AssertionFailure("only one table");
      } else {
         return true;
      }
   }

   public String getPropertyTableName(String propertyName) {
      return this.getTableName();
   }

   public String[] getConstraintOrderedTableNameClosure() {
      return this.constraintOrderedTableNames;
   }

   public String[][] getContraintOrderedTableKeyColumnClosure() {
      return this.constraintOrderedKeyColumnNames;
   }

   public FilterAliasGenerator getFilterAliasGenerator(String rootAlias) {
      return new StaticFilterAliasGenerator(rootAlias);
   }
}
