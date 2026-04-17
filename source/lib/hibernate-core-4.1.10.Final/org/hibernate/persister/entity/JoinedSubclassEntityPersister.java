package org.hibernate.persister.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.hibernate.AssertionFailure;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.QueryException;
import org.hibernate.cache.spi.access.EntityRegionAccessStrategy;
import org.hibernate.cache.spi.access.NaturalIdRegionAccessStrategy;
import org.hibernate.engine.OptimisticLockStyle;
import org.hibernate.engine.spi.ExecuteUpdateResultCheckStyle;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.DynamicFilterAliasGenerator;
import org.hibernate.internal.FilterAliasGenerator;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Join;
import org.hibernate.mapping.KeyValue;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.Selectable;
import org.hibernate.mapping.Subclass;
import org.hibernate.mapping.Table;
import org.hibernate.metamodel.binding.EntityBinding;
import org.hibernate.sql.CaseFragment;
import org.hibernate.sql.SelectFragment;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;

public class JoinedSubclassEntityPersister extends AbstractEntityPersister {
   private final int tableSpan;
   private final String[] tableNames;
   private final String[] naturalOrderTableNames;
   private final String[][] tableKeyColumns;
   private final String[][] tableKeyColumnReaders;
   private final String[][] tableKeyColumnReaderTemplates;
   private final String[][] naturalOrderTableKeyColumns;
   private final String[][] naturalOrderTableKeyColumnReaders;
   private final String[][] naturalOrderTableKeyColumnReaderTemplates;
   private final boolean[] naturalOrderCascadeDeleteEnabled;
   private final String[] spaces;
   private final String[] subclassClosure;
   private final String[] subclassTableNameClosure;
   private final String[][] subclassTableKeyColumnClosure;
   private final boolean[] isClassOrSuperclassTable;
   private final int[] naturalOrderPropertyTableNumbers;
   private final int[] propertyTableNumbers;
   private final int[] subclassPropertyTableNumberClosure;
   private final int[] subclassColumnTableNumberClosure;
   private final int[] subclassFormulaTableNumberClosure;
   private final boolean[] subclassTableSequentialSelect;
   private final boolean[] subclassTableIsLazyClosure;
   private final Map subclassesByDiscriminatorValue = new HashMap();
   private final String[] discriminatorValues;
   private final String[] notNullColumnNames;
   private final int[] notNullColumnTableNumbers;
   private final String[] constraintOrderedTableNames;
   private final String[][] constraintOrderedKeyColumnNames;
   private final Object discriminatorValue;
   private final String discriminatorSQLString;
   private final int coreTableSpan;
   private final boolean[] isNullableTable;

   public JoinedSubclassEntityPersister(PersistentClass persistentClass, EntityRegionAccessStrategy cacheAccessStrategy, NaturalIdRegionAccessStrategy naturalIdRegionAccessStrategy, SessionFactoryImplementor factory, Mapping mapping) throws HibernateException {
      super(persistentClass, cacheAccessStrategy, naturalIdRegionAccessStrategy, factory);
      if (persistentClass.isPolymorphic()) {
         try {
            this.discriminatorValue = persistentClass.getSubclassId();
            this.discriminatorSQLString = this.discriminatorValue.toString();
         } catch (Exception e) {
            throw new MappingException("Could not format discriminator value to SQL string", e);
         }
      } else {
         this.discriminatorValue = null;
         this.discriminatorSQLString = null;
      }

      if (this.optimisticLockStyle() != OptimisticLockStyle.ALL && this.optimisticLockStyle() != OptimisticLockStyle.DIRTY) {
         int idColumnSpan = this.getIdentifierColumnSpan();
         ArrayList tables = new ArrayList();
         ArrayList keyColumns = new ArrayList();
         ArrayList keyColumnReaders = new ArrayList();
         ArrayList keyColumnReaderTemplates = new ArrayList();
         ArrayList cascadeDeletes = new ArrayList();
         Iterator titer = persistentClass.getTableClosureIterator();
         Iterator kiter = persistentClass.getKeyClosureIterator();

         while(titer.hasNext()) {
            Table tab = (Table)titer.next();
            KeyValue key = (KeyValue)kiter.next();
            String tabname = tab.getQualifiedName(factory.getDialect(), factory.getSettings().getDefaultCatalogName(), factory.getSettings().getDefaultSchemaName());
            tables.add(tabname);
            String[] keyCols = new String[idColumnSpan];
            String[] keyColReaders = new String[idColumnSpan];
            String[] keyColReaderTemplates = new String[idColumnSpan];
            Iterator citer = key.getColumnIterator();

            for(int k = 0; k < idColumnSpan; ++k) {
               Column column = (Column)citer.next();
               keyCols[k] = column.getQuotedName(factory.getDialect());
               keyColReaders[k] = column.getReadExpr(factory.getDialect());
               keyColReaderTemplates[k] = column.getTemplate(factory.getDialect(), factory.getSqlFunctionRegistry());
            }

            keyColumns.add(keyCols);
            keyColumnReaders.add(keyColReaders);
            keyColumnReaderTemplates.add(keyColReaderTemplates);
            cascadeDeletes.add(key.isCascadeDeleteEnabled() && factory.getDialect().supportsCascadeDelete());
         }

         this.coreTableSpan = tables.size();
         this.isNullableTable = new boolean[persistentClass.getJoinClosureSpan()];
         int tableIndex = 0;
         Iterator joinIter = persistentClass.getJoinClosureIterator();

         while(joinIter.hasNext()) {
            Join join = (Join)joinIter.next();
            this.isNullableTable[tableIndex++] = join.isOptional();
            Table table = join.getTable();
            String tableName = table.getQualifiedName(factory.getDialect(), factory.getSettings().getDefaultCatalogName(), factory.getSettings().getDefaultSchemaName());
            tables.add(tableName);
            KeyValue key = join.getKey();
            int joinIdColumnSpan = key.getColumnSpan();
            String[] keyCols = new String[joinIdColumnSpan];
            String[] keyColReaders = new String[joinIdColumnSpan];
            String[] keyColReaderTemplates = new String[joinIdColumnSpan];
            Iterator citer = key.getColumnIterator();

            for(int k = 0; k < joinIdColumnSpan; ++k) {
               Column column = (Column)citer.next();
               keyCols[k] = column.getQuotedName(factory.getDialect());
               keyColReaders[k] = column.getReadExpr(factory.getDialect());
               keyColReaderTemplates[k] = column.getTemplate(factory.getDialect(), factory.getSqlFunctionRegistry());
            }

            keyColumns.add(keyCols);
            keyColumnReaders.add(keyColReaders);
            keyColumnReaderTemplates.add(keyColReaderTemplates);
            cascadeDeletes.add(key.isCascadeDeleteEnabled() && factory.getDialect().supportsCascadeDelete());
         }

         this.naturalOrderTableNames = ArrayHelper.toStringArray((Collection)tables);
         this.naturalOrderTableKeyColumns = ArrayHelper.to2DStringArray(keyColumns);
         this.naturalOrderTableKeyColumnReaders = ArrayHelper.to2DStringArray(keyColumnReaders);
         this.naturalOrderTableKeyColumnReaderTemplates = ArrayHelper.to2DStringArray(keyColumnReaderTemplates);
         this.naturalOrderCascadeDeleteEnabled = ArrayHelper.toBooleanArray(cascadeDeletes);
         ArrayList subtables = new ArrayList();
         ArrayList isConcretes = new ArrayList();
         ArrayList isDeferreds = new ArrayList();
         ArrayList isLazies = new ArrayList();
         keyColumns = new ArrayList();
         titer = persistentClass.getSubclassTableClosureIterator();

         while(titer.hasNext()) {
            Table tab = (Table)titer.next();
            isConcretes.add(persistentClass.isClassOrSuperclassTable(tab));
            isDeferreds.add(Boolean.FALSE);
            isLazies.add(Boolean.FALSE);
            String tabname = tab.getQualifiedName(factory.getDialect(), factory.getSettings().getDefaultCatalogName(), factory.getSettings().getDefaultSchemaName());
            subtables.add(tabname);
            String[] key = new String[idColumnSpan];
            Iterator citer = tab.getPrimaryKey().getColumnIterator();

            for(int k = 0; k < idColumnSpan; ++k) {
               key[k] = ((Column)citer.next()).getQuotedName(factory.getDialect());
            }

            keyColumns.add(key);
         }

         joinIter = persistentClass.getSubclassJoinClosureIterator();

         while(joinIter.hasNext()) {
            Join join = (Join)joinIter.next();
            Table tab = join.getTable();
            isConcretes.add(persistentClass.isClassOrSuperclassTable(tab));
            isDeferreds.add(join.isSequentialSelect());
            isLazies.add(join.isLazy());
            String tabname = tab.getQualifiedName(factory.getDialect(), factory.getSettings().getDefaultCatalogName(), factory.getSettings().getDefaultSchemaName());
            subtables.add(tabname);
            String[] key = new String[idColumnSpan];
            Iterator citer = tab.getPrimaryKey().getColumnIterator();

            for(int k = 0; k < idColumnSpan; ++k) {
               key[k] = ((Column)citer.next()).getQuotedName(factory.getDialect());
            }

            keyColumns.add(key);
         }

         String[] naturalOrderSubclassTableNameClosure = ArrayHelper.toStringArray((Collection)subtables);
         String[][] naturalOrderSubclassTableKeyColumnClosure = ArrayHelper.to2DStringArray(keyColumns);
         this.isClassOrSuperclassTable = ArrayHelper.toBooleanArray(isConcretes);
         this.subclassTableSequentialSelect = ArrayHelper.toBooleanArray(isDeferreds);
         this.subclassTableIsLazyClosure = ArrayHelper.toBooleanArray(isLazies);
         this.constraintOrderedTableNames = new String[naturalOrderSubclassTableNameClosure.length];
         this.constraintOrderedKeyColumnNames = new String[naturalOrderSubclassTableNameClosure.length][];
         int currentPosition = 0;

         for(int i = naturalOrderSubclassTableNameClosure.length - 1; i >= 0; ++currentPosition) {
            this.constraintOrderedTableNames[currentPosition] = naturalOrderSubclassTableNameClosure[i];
            this.constraintOrderedKeyColumnNames[currentPosition] = naturalOrderSubclassTableKeyColumnClosure[i];
            --i;
         }

         this.tableSpan = this.naturalOrderTableNames.length;
         this.tableNames = reverse(this.naturalOrderTableNames, this.coreTableSpan);
         this.tableKeyColumns = reverse(this.naturalOrderTableKeyColumns, this.coreTableSpan);
         this.tableKeyColumnReaders = reverse(this.naturalOrderTableKeyColumnReaders, this.coreTableSpan);
         this.tableKeyColumnReaderTemplates = reverse(this.naturalOrderTableKeyColumnReaderTemplates, this.coreTableSpan);
         this.subclassTableNameClosure = reverse(naturalOrderSubclassTableNameClosure, this.coreTableSpan);
         this.subclassTableKeyColumnClosure = reverse(naturalOrderSubclassTableKeyColumnClosure, this.coreTableSpan);
         this.spaces = ArrayHelper.join(this.tableNames, ArrayHelper.toStringArray((Collection)persistentClass.getSynchronizedTables()));
         this.customSQLInsert = new String[this.tableSpan];
         this.customSQLUpdate = new String[this.tableSpan];
         this.customSQLDelete = new String[this.tableSpan];
         this.insertCallable = new boolean[this.tableSpan];
         this.updateCallable = new boolean[this.tableSpan];
         this.deleteCallable = new boolean[this.tableSpan];
         this.insertResultCheckStyles = new ExecuteUpdateResultCheckStyle[this.tableSpan];
         this.updateResultCheckStyles = new ExecuteUpdateResultCheckStyle[this.tableSpan];
         this.deleteResultCheckStyles = new ExecuteUpdateResultCheckStyle[this.tableSpan];
         PersistentClass pc = persistentClass;

         int jk;
         for(jk = this.coreTableSpan - 1; pc != null; pc = pc.getSuperclass()) {
            this.customSQLInsert[jk] = pc.getCustomSQLInsert();
            this.insertCallable[jk] = this.customSQLInsert[jk] != null && pc.isCustomInsertCallable();
            this.insertResultCheckStyles[jk] = pc.getCustomSQLInsertCheckStyle() == null ? ExecuteUpdateResultCheckStyle.determineDefault(this.customSQLInsert[jk], this.insertCallable[jk]) : pc.getCustomSQLInsertCheckStyle();
            this.customSQLUpdate[jk] = pc.getCustomSQLUpdate();
            this.updateCallable[jk] = this.customSQLUpdate[jk] != null && pc.isCustomUpdateCallable();
            this.updateResultCheckStyles[jk] = pc.getCustomSQLUpdateCheckStyle() == null ? ExecuteUpdateResultCheckStyle.determineDefault(this.customSQLUpdate[jk], this.updateCallable[jk]) : pc.getCustomSQLUpdateCheckStyle();
            this.customSQLDelete[jk] = pc.getCustomSQLDelete();
            this.deleteCallable[jk] = this.customSQLDelete[jk] != null && pc.isCustomDeleteCallable();
            this.deleteResultCheckStyles[jk] = pc.getCustomSQLDeleteCheckStyle() == null ? ExecuteUpdateResultCheckStyle.determineDefault(this.customSQLDelete[jk], this.deleteCallable[jk]) : pc.getCustomSQLDeleteCheckStyle();
            --jk;
         }

         if (jk != -1) {
            throw new AssertionFailure("Tablespan does not match height of joined-subclass hiearchy.");
         } else {
            joinIter = persistentClass.getJoinClosureIterator();

            for(int j = this.coreTableSpan; joinIter.hasNext(); ++j) {
               Join join = (Join)joinIter.next();
               this.customSQLInsert[j] = join.getCustomSQLInsert();
               this.insertCallable[j] = this.customSQLInsert[j] != null && join.isCustomInsertCallable();
               this.insertResultCheckStyles[j] = join.getCustomSQLInsertCheckStyle() == null ? ExecuteUpdateResultCheckStyle.determineDefault(this.customSQLInsert[j], this.insertCallable[j]) : join.getCustomSQLInsertCheckStyle();
               this.customSQLUpdate[j] = join.getCustomSQLUpdate();
               this.updateCallable[j] = this.customSQLUpdate[j] != null && join.isCustomUpdateCallable();
               this.updateResultCheckStyles[j] = join.getCustomSQLUpdateCheckStyle() == null ? ExecuteUpdateResultCheckStyle.determineDefault(this.customSQLUpdate[j], this.updateCallable[j]) : join.getCustomSQLUpdateCheckStyle();
               this.customSQLDelete[j] = join.getCustomSQLDelete();
               this.deleteCallable[j] = this.customSQLDelete[j] != null && join.isCustomDeleteCallable();
               this.deleteResultCheckStyles[j] = join.getCustomSQLDeleteCheckStyle() == null ? ExecuteUpdateResultCheckStyle.determineDefault(this.customSQLDelete[j], this.deleteCallable[j]) : join.getCustomSQLDeleteCheckStyle();
            }

            int hydrateSpan = this.getPropertySpan();
            this.naturalOrderPropertyTableNumbers = new int[hydrateSpan];
            this.propertyTableNumbers = new int[hydrateSpan];
            Iterator iter = persistentClass.getPropertyClosureIterator();

            for(int i = 0; iter.hasNext(); ++i) {
               Property prop = (Property)iter.next();
               String tabname = prop.getValue().getTable().getQualifiedName(factory.getDialect(), factory.getSettings().getDefaultCatalogName(), factory.getSettings().getDefaultSchemaName());
               this.propertyTableNumbers[i] = getTableId(tabname, this.tableNames);
               this.naturalOrderPropertyTableNumbers[i] = getTableId(tabname, this.naturalOrderTableNames);
            }

            ArrayList columnTableNumbers = new ArrayList();
            ArrayList formulaTableNumbers = new ArrayList();
            ArrayList propTableNumbers = new ArrayList();
            iter = persistentClass.getSubclassPropertyClosureIterator();

            while(iter.hasNext()) {
               Property prop = (Property)iter.next();
               Table tab = prop.getValue().getTable();
               String tabname = tab.getQualifiedName(factory.getDialect(), factory.getSettings().getDefaultCatalogName(), factory.getSettings().getDefaultSchemaName());
               Integer tabnum = getTableId(tabname, this.subclassTableNameClosure);
               propTableNumbers.add(tabnum);
               Iterator citer = prop.getColumnIterator();

               while(citer.hasNext()) {
                  Selectable thing = (Selectable)citer.next();
                  if (thing.isFormula()) {
                     formulaTableNumbers.add(tabnum);
                  } else {
                     columnTableNumbers.add(tabnum);
                  }
               }
            }

            this.subclassColumnTableNumberClosure = ArrayHelper.toIntArray(columnTableNumbers);
            this.subclassPropertyTableNumberClosure = ArrayHelper.toIntArray(propTableNumbers);
            this.subclassFormulaTableNumberClosure = ArrayHelper.toIntArray(formulaTableNumbers);
            int subclassSpan = persistentClass.getSubclassSpan() + 1;
            this.subclassClosure = new String[subclassSpan];
            this.subclassClosure[subclassSpan - 1] = this.getEntityName();
            if (persistentClass.isPolymorphic()) {
               this.subclassesByDiscriminatorValue.put(this.discriminatorValue, this.getEntityName());
               this.discriminatorValues = new String[subclassSpan];
               this.discriminatorValues[subclassSpan - 1] = this.discriminatorSQLString;
               this.notNullColumnTableNumbers = new int[subclassSpan];
               int id = getTableId(persistentClass.getTable().getQualifiedName(factory.getDialect(), factory.getSettings().getDefaultCatalogName(), factory.getSettings().getDefaultSchemaName()), this.subclassTableNameClosure);
               this.notNullColumnTableNumbers[subclassSpan - 1] = id;
               this.notNullColumnNames = new String[subclassSpan];
               this.notNullColumnNames[subclassSpan - 1] = this.subclassTableKeyColumnClosure[id][0];
            } else {
               this.discriminatorValues = null;
               this.notNullColumnTableNumbers = null;
               this.notNullColumnNames = null;
            }

            iter = persistentClass.getSubclassIterator();

            for(int k = 0; iter.hasNext(); ++k) {
               Subclass sc = (Subclass)iter.next();
               this.subclassClosure[k] = sc.getEntityName();

               try {
                  if (persistentClass.isPolymorphic()) {
                     Integer subclassId = sc.getSubclassId();
                     this.subclassesByDiscriminatorValue.put(subclassId, sc.getEntityName());
                     this.discriminatorValues[k] = subclassId.toString();
                     int id = getTableId(sc.getTable().getQualifiedName(factory.getDialect(), factory.getSettings().getDefaultCatalogName(), factory.getSettings().getDefaultSchemaName()), this.subclassTableNameClosure);
                     this.notNullColumnTableNumbers[k] = id;
                     this.notNullColumnNames[k] = this.subclassTableKeyColumnClosure[id][0];
                  }
               } catch (Exception e) {
                  throw new MappingException("Error parsing discriminator value", e);
               }
            }

            this.initLockers();
            this.initSubclassPropertyAliasesMap(persistentClass);
            this.postConstruct(mapping);
         }
      } else {
         throw new MappingException("optimistic-lock=all|dirty not supported for joined-subclass mappings [" + this.getEntityName() + "]");
      }
   }

   public JoinedSubclassEntityPersister(EntityBinding entityBinding, EntityRegionAccessStrategy cacheAccessStrategy, NaturalIdRegionAccessStrategy naturalIdRegionAccessStrategy, SessionFactoryImplementor factory, Mapping mapping) throws HibernateException {
      super(entityBinding, cacheAccessStrategy, naturalIdRegionAccessStrategy, factory);
      this.tableSpan = -1;
      this.tableNames = null;
      this.naturalOrderTableNames = null;
      this.tableKeyColumns = (String[][])null;
      this.tableKeyColumnReaders = (String[][])null;
      this.tableKeyColumnReaderTemplates = (String[][])null;
      this.naturalOrderTableKeyColumns = (String[][])null;
      this.naturalOrderTableKeyColumnReaders = (String[][])null;
      this.naturalOrderTableKeyColumnReaderTemplates = (String[][])null;
      this.naturalOrderCascadeDeleteEnabled = null;
      this.spaces = null;
      this.subclassClosure = null;
      this.subclassTableNameClosure = null;
      this.subclassTableKeyColumnClosure = (String[][])null;
      this.isClassOrSuperclassTable = null;
      this.naturalOrderPropertyTableNumbers = null;
      this.propertyTableNumbers = null;
      this.subclassPropertyTableNumberClosure = null;
      this.subclassColumnTableNumberClosure = null;
      this.subclassFormulaTableNumberClosure = null;
      this.subclassTableSequentialSelect = null;
      this.subclassTableIsLazyClosure = null;
      this.discriminatorValues = null;
      this.notNullColumnNames = null;
      this.notNullColumnTableNumbers = null;
      this.constraintOrderedTableNames = null;
      this.constraintOrderedKeyColumnNames = (String[][])null;
      this.discriminatorValue = null;
      this.discriminatorSQLString = null;
      this.coreTableSpan = -1;
      this.isNullableTable = null;
   }

   protected boolean isNullableTable(int j) {
      return j < this.coreTableSpan ? false : this.isNullableTable[j - this.coreTableSpan];
   }

   protected boolean isSubclassTableSequentialSelect(int j) {
      return this.subclassTableSequentialSelect[j] && !this.isClassOrSuperclassTable[j];
   }

   public String getSubclassPropertyTableName(int i) {
      return this.subclassTableNameClosure[this.subclassPropertyTableNumberClosure[i]];
   }

   public Type getDiscriminatorType() {
      return StandardBasicTypes.INTEGER;
   }

   public Object getDiscriminatorValue() {
      return this.discriminatorValue;
   }

   public String getDiscriminatorSQLValue() {
      return this.discriminatorSQLString;
   }

   public String getSubclassForDiscriminatorValue(Object value) {
      return (String)this.subclassesByDiscriminatorValue.get(value);
   }

   public Serializable[] getPropertySpaces() {
      return this.spaces;
   }

   protected String getTableName(int j) {
      return this.naturalOrderTableNames[j];
   }

   protected String[] getKeyColumns(int j) {
      return this.naturalOrderTableKeyColumns[j];
   }

   protected boolean isTableCascadeDeleteEnabled(int j) {
      return this.naturalOrderCascadeDeleteEnabled[j];
   }

   protected boolean isPropertyOfTable(int property, int j) {
      return this.naturalOrderPropertyTableNumbers[property] == j;
   }

   private static final void reverse(Object[] objects, int len) {
      Object[] temp = new Object[len];

      for(int i = 0; i < len; ++i) {
         temp[i] = objects[len - i - 1];
      }

      for(int i = 0; i < len; ++i) {
         objects[i] = temp[i];
      }

   }

   private static String[] reverse(String[] objects, int n) {
      int size = objects.length;
      String[] temp = new String[size];

      for(int i = 0; i < n; ++i) {
         temp[i] = objects[n - i - 1];
      }

      for(int i = n; i < size; ++i) {
         temp[i] = objects[i];
      }

      return temp;
   }

   private static String[][] reverse(String[][] objects, int n) {
      int size = objects.length;
      String[][] temp = new String[size][];

      for(int i = 0; i < n; ++i) {
         temp[i] = objects[n - i - 1];
      }

      for(int i = n; i < size; ++i) {
         temp[i] = objects[i];
      }

      return temp;
   }

   public String fromTableFragment(String alias) {
      return this.getTableName() + ' ' + alias;
   }

   public String getTableName() {
      return this.tableNames[0];
   }

   public void addDiscriminatorToSelect(SelectFragment select, String name, String suffix) {
      if (this.hasSubclasses()) {
         select.setExtraSelectList(this.discriminatorFragment(name), this.getDiscriminatorAlias());
      }

   }

   private CaseFragment discriminatorFragment(String alias) {
      CaseFragment cases = this.getFactory().getDialect().createCaseFragment();

      for(int i = 0; i < this.discriminatorValues.length; ++i) {
         cases.addWhenColumnNotNull(generateTableAlias(alias, this.notNullColumnTableNumbers[i]), this.notNullColumnNames[i], this.discriminatorValues[i]);
      }

      return cases;
   }

   public String filterFragment(String alias) {
      return this.hasWhere() ? " and " + this.getSQLWhereString(this.generateFilterConditionAlias(alias)) : "";
   }

   public String generateFilterConditionAlias(String rootAlias) {
      return generateTableAlias(rootAlias, this.tableSpan - 1);
   }

   public String[] getIdentifierColumnNames() {
      return this.tableKeyColumns[0];
   }

   public String[] getIdentifierColumnReaderTemplates() {
      return this.tableKeyColumnReaderTemplates[0];
   }

   public String[] getIdentifierColumnReaders() {
      return this.tableKeyColumnReaders[0];
   }

   public String[] toColumns(String alias, String propertyName) throws QueryException {
      return "class".equals(propertyName) ? new String[]{this.discriminatorFragment(alias).toFragmentString()} : super.toColumns(alias, propertyName);
   }

   protected int[] getPropertyTableNumbersInSelect() {
      return this.propertyTableNumbers;
   }

   protected int getSubclassPropertyTableNumber(int i) {
      return this.subclassPropertyTableNumberClosure[i];
   }

   public int getTableSpan() {
      return this.tableSpan;
   }

   public boolean isMultiTable() {
      return true;
   }

   protected int[] getSubclassColumnTableNumberClosure() {
      return this.subclassColumnTableNumberClosure;
   }

   protected int[] getSubclassFormulaTableNumberClosure() {
      return this.subclassFormulaTableNumberClosure;
   }

   protected int[] getPropertyTableNumbers() {
      return this.naturalOrderPropertyTableNumbers;
   }

   protected String[] getSubclassTableKeyColumns(int j) {
      return this.subclassTableKeyColumnClosure[j];
   }

   public String getSubclassTableName(int j) {
      return this.subclassTableNameClosure[j];
   }

   public int getSubclassTableSpan() {
      return this.subclassTableNameClosure.length;
   }

   protected boolean isSubclassTableLazy(int j) {
      return this.subclassTableIsLazyClosure[j];
   }

   protected boolean isClassOrSuperclassTable(int j) {
      return this.isClassOrSuperclassTable[j];
   }

   public String getPropertyTableName(String propertyName) {
      Integer index = this.getEntityMetamodel().getPropertyIndexOrNull(propertyName);
      return index == null ? null : this.tableNames[this.propertyTableNumbers[index]];
   }

   public String[] getConstraintOrderedTableNameClosure() {
      return this.constraintOrderedTableNames;
   }

   public String[][] getContraintOrderedTableKeyColumnClosure() {
      return this.constraintOrderedKeyColumnNames;
   }

   public String getRootTableName() {
      return this.naturalOrderTableNames[0];
   }

   public String getRootTableAlias(String drivingAlias) {
      return generateTableAlias(drivingAlias, getTableId(this.getRootTableName(), this.tableNames));
   }

   public Queryable.Declarer getSubclassPropertyDeclarer(String propertyPath) {
      return "class".equals(propertyPath) ? Queryable.Declarer.SUBCLASS : super.getSubclassPropertyDeclarer(propertyPath);
   }

   public int determineTableNumberForColumn(String columnName) {
      String[] subclassColumnNameClosure = this.getSubclassColumnClosure();
      int i = 0;

      for(int max = subclassColumnNameClosure.length; i < max; ++i) {
         boolean quoted = subclassColumnNameClosure[i].startsWith("\"") && subclassColumnNameClosure[i].endsWith("\"");
         if (quoted) {
            if (subclassColumnNameClosure[i].equals(columnName)) {
               return this.getSubclassColumnTableNumberClosure()[i];
            }
         } else if (subclassColumnNameClosure[i].equalsIgnoreCase(columnName)) {
            return this.getSubclassColumnTableNumberClosure()[i];
         }
      }

      throw new HibernateException("Could not locate table which owns column [" + columnName + "] referenced in order-by mapping");
   }

   public FilterAliasGenerator getFilterAliasGenerator(String rootAlias) {
      return new DynamicFilterAliasGenerator(this.subclassTableNameClosure, rootAlias);
   }
}
