package org.hibernate.persister.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.cache.spi.access.EntityRegionAccessStrategy;
import org.hibernate.cache.spi.access.NaturalIdRegionAccessStrategy;
import org.hibernate.engine.spi.ExecuteUpdateResultCheckStyle;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.DynamicFilterAliasGenerator;
import org.hibernate.internal.FilterAliasGenerator;
import org.hibernate.internal.util.MarkerObject;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Formula;
import org.hibernate.mapping.Join;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.Selectable;
import org.hibernate.mapping.Subclass;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.Value;
import org.hibernate.metamodel.binding.AttributeBinding;
import org.hibernate.metamodel.binding.CustomSQL;
import org.hibernate.metamodel.binding.EntityBinding;
import org.hibernate.metamodel.binding.SimpleValueBinding;
import org.hibernate.metamodel.binding.SingularAttributeBinding;
import org.hibernate.metamodel.relational.DerivedValue;
import org.hibernate.metamodel.relational.SimpleValue;
import org.hibernate.metamodel.relational.TableSpecification;
import org.hibernate.sql.InFragment;
import org.hibernate.sql.Insert;
import org.hibernate.sql.SelectFragment;
import org.hibernate.type.AssociationType;
import org.hibernate.type.Type;

public class SingleTableEntityPersister extends AbstractEntityPersister {
   private final int joinSpan;
   private final String[] qualifiedTableNames;
   private final boolean[] isInverseTable;
   private final boolean[] isNullableTable;
   private final String[][] keyColumnNames;
   private final boolean[] cascadeDeleteEnabled;
   private final boolean hasSequentialSelects;
   private final String[] spaces;
   private final String[] subclassClosure;
   private final String[] subclassTableNameClosure;
   private final boolean[] subclassTableIsLazyClosure;
   private final boolean[] isInverseSubclassTable;
   private final boolean[] isNullableSubclassTable;
   private final boolean[] subclassTableSequentialSelect;
   private final String[][] subclassTableKeyColumnClosure;
   private final boolean[] isClassOrSuperclassTable;
   private final int[] propertyTableNumbers;
   private final int[] subclassPropertyTableNumberClosure;
   private final int[] subclassColumnTableNumberClosure;
   private final int[] subclassFormulaTableNumberClosure;
   private final Map subclassesByDiscriminatorValue = new HashMap();
   private final boolean forceDiscriminator;
   private final String discriminatorColumnName;
   private final String discriminatorColumnReaders;
   private final String discriminatorColumnReaderTemplate;
   private final String discriminatorFormula;
   private final String discriminatorFormulaTemplate;
   private final String discriminatorAlias;
   private final Type discriminatorType;
   private final Object discriminatorValue;
   private final String discriminatorSQLValue;
   private final boolean discriminatorInsertable;
   private final String[] constraintOrderedTableNames;
   private final String[][] constraintOrderedKeyColumnNames;
   private final Map propertyTableNumbersByNameAndSubclass = new HashMap();
   private final Map sequentialSelectStringsByEntityName = new HashMap();
   private static final Object NULL_DISCRIMINATOR = new MarkerObject("<null discriminator>");
   private static final Object NOT_NULL_DISCRIMINATOR = new MarkerObject("<not null discriminator>");
   private static final String NULL_STRING = "null";
   private static final String NOT_NULL_STRING = "not null";

   public SingleTableEntityPersister(PersistentClass persistentClass, EntityRegionAccessStrategy cacheAccessStrategy, NaturalIdRegionAccessStrategy naturalIdRegionAccessStrategy, SessionFactoryImplementor factory, Mapping mapping) throws HibernateException {
      super(persistentClass, cacheAccessStrategy, naturalIdRegionAccessStrategy, factory);
      this.joinSpan = persistentClass.getJoinClosureSpan() + 1;
      this.qualifiedTableNames = new String[this.joinSpan];
      this.isInverseTable = new boolean[this.joinSpan];
      this.isNullableTable = new boolean[this.joinSpan];
      this.keyColumnNames = new String[this.joinSpan][];
      Table table = persistentClass.getRootTable();
      this.qualifiedTableNames[0] = table.getQualifiedName(factory.getDialect(), factory.getSettings().getDefaultCatalogName(), factory.getSettings().getDefaultSchemaName());
      this.isInverseTable[0] = false;
      this.isNullableTable[0] = false;
      this.keyColumnNames[0] = this.getIdentifierColumnNames();
      this.cascadeDeleteEnabled = new boolean[this.joinSpan];
      this.customSQLInsert = new String[this.joinSpan];
      this.customSQLUpdate = new String[this.joinSpan];
      this.customSQLDelete = new String[this.joinSpan];
      this.insertCallable = new boolean[this.joinSpan];
      this.updateCallable = new boolean[this.joinSpan];
      this.deleteCallable = new boolean[this.joinSpan];
      this.insertResultCheckStyles = new ExecuteUpdateResultCheckStyle[this.joinSpan];
      this.updateResultCheckStyles = new ExecuteUpdateResultCheckStyle[this.joinSpan];
      this.deleteResultCheckStyles = new ExecuteUpdateResultCheckStyle[this.joinSpan];
      this.customSQLInsert[0] = persistentClass.getCustomSQLInsert();
      this.insertCallable[0] = this.customSQLInsert[0] != null && persistentClass.isCustomInsertCallable();
      this.insertResultCheckStyles[0] = persistentClass.getCustomSQLInsertCheckStyle() == null ? ExecuteUpdateResultCheckStyle.determineDefault(this.customSQLInsert[0], this.insertCallable[0]) : persistentClass.getCustomSQLInsertCheckStyle();
      this.customSQLUpdate[0] = persistentClass.getCustomSQLUpdate();
      this.updateCallable[0] = this.customSQLUpdate[0] != null && persistentClass.isCustomUpdateCallable();
      this.updateResultCheckStyles[0] = persistentClass.getCustomSQLUpdateCheckStyle() == null ? ExecuteUpdateResultCheckStyle.determineDefault(this.customSQLUpdate[0], this.updateCallable[0]) : persistentClass.getCustomSQLUpdateCheckStyle();
      this.customSQLDelete[0] = persistentClass.getCustomSQLDelete();
      this.deleteCallable[0] = this.customSQLDelete[0] != null && persistentClass.isCustomDeleteCallable();
      this.deleteResultCheckStyles[0] = persistentClass.getCustomSQLDeleteCheckStyle() == null ? ExecuteUpdateResultCheckStyle.determineDefault(this.customSQLDelete[0], this.deleteCallable[0]) : persistentClass.getCustomSQLDeleteCheckStyle();
      Iterator joinIter = persistentClass.getJoinClosureIterator();

      for(int j = 1; joinIter.hasNext(); ++j) {
         Join join = (Join)joinIter.next();
         this.qualifiedTableNames[j] = join.getTable().getQualifiedName(factory.getDialect(), factory.getSettings().getDefaultCatalogName(), factory.getSettings().getDefaultSchemaName());
         this.isInverseTable[j] = join.isInverse();
         this.isNullableTable[j] = join.isOptional();
         this.cascadeDeleteEnabled[j] = join.getKey().isCascadeDeleteEnabled() && factory.getDialect().supportsCascadeDelete();
         this.customSQLInsert[j] = join.getCustomSQLInsert();
         this.insertCallable[j] = this.customSQLInsert[j] != null && join.isCustomInsertCallable();
         this.insertResultCheckStyles[j] = join.getCustomSQLInsertCheckStyle() == null ? ExecuteUpdateResultCheckStyle.determineDefault(this.customSQLInsert[j], this.insertCallable[j]) : join.getCustomSQLInsertCheckStyle();
         this.customSQLUpdate[j] = join.getCustomSQLUpdate();
         this.updateCallable[j] = this.customSQLUpdate[j] != null && join.isCustomUpdateCallable();
         this.updateResultCheckStyles[j] = join.getCustomSQLUpdateCheckStyle() == null ? ExecuteUpdateResultCheckStyle.determineDefault(this.customSQLUpdate[j], this.updateCallable[j]) : join.getCustomSQLUpdateCheckStyle();
         this.customSQLDelete[j] = join.getCustomSQLDelete();
         this.deleteCallable[j] = this.customSQLDelete[j] != null && join.isCustomDeleteCallable();
         this.deleteResultCheckStyles[j] = join.getCustomSQLDeleteCheckStyle() == null ? ExecuteUpdateResultCheckStyle.determineDefault(this.customSQLDelete[j], this.deleteCallable[j]) : join.getCustomSQLDeleteCheckStyle();
         Iterator iter = join.getKey().getColumnIterator();
         this.keyColumnNames[j] = new String[join.getKey().getColumnSpan()];

         Column col;
         for(int i = 0; iter.hasNext(); this.keyColumnNames[j][i++] = col.getQuotedName(factory.getDialect())) {
            col = (Column)iter.next();
         }
      }

      this.constraintOrderedTableNames = new String[this.qualifiedTableNames.length];
      this.constraintOrderedKeyColumnNames = new String[this.qualifiedTableNames.length][];
      int i = this.qualifiedTableNames.length - 1;

      for(int position = 0; i >= 0; ++position) {
         this.constraintOrderedTableNames[position] = this.qualifiedTableNames[i];
         this.constraintOrderedKeyColumnNames[position] = this.keyColumnNames[i];
         --i;
      }

      this.spaces = ArrayHelper.join(this.qualifiedTableNames, ArrayHelper.toStringArray((Collection)persistentClass.getSynchronizedTables()));
      i = this.isInstrumented();
      boolean hasDeferred = false;
      ArrayList subclassTables = new ArrayList();
      ArrayList joinKeyColumns = new ArrayList();
      ArrayList<Boolean> isConcretes = new ArrayList();
      ArrayList<Boolean> isDeferreds = new ArrayList();
      ArrayList<Boolean> isInverses = new ArrayList();
      ArrayList<Boolean> isNullables = new ArrayList();
      ArrayList<Boolean> isLazies = new ArrayList();
      subclassTables.add(this.qualifiedTableNames[0]);
      joinKeyColumns.add(this.getIdentifierColumnNames());
      isConcretes.add(Boolean.TRUE);
      isDeferreds.add(Boolean.FALSE);
      isInverses.add(Boolean.FALSE);
      isNullables.add(Boolean.FALSE);
      isLazies.add(Boolean.FALSE);
      joinIter = persistentClass.getSubclassJoinClosureIterator();

      while(joinIter.hasNext()) {
         Join join = (Join)joinIter.next();
         isConcretes.add(persistentClass.isClassOrSuperclassJoin(join));
         isDeferreds.add(join.isSequentialSelect());
         isInverses.add(join.isInverse());
         isNullables.add(join.isOptional());
         isLazies.add(i && join.isLazy());
         if (join.isSequentialSelect() && !persistentClass.isClassOrSuperclassJoin(join)) {
            hasDeferred = true;
         }

         subclassTables.add(join.getTable().getQualifiedName(factory.getDialect(), factory.getSettings().getDefaultCatalogName(), factory.getSettings().getDefaultSchemaName()));
         Iterator iter = join.getKey().getColumnIterator();
         String[] keyCols = new String[join.getKey().getColumnSpan()];

         Column col;
         for(int i = 0; iter.hasNext(); keyCols[i++] = col.getQuotedName(factory.getDialect())) {
            col = (Column)iter.next();
         }

         joinKeyColumns.add(keyCols);
      }

      this.subclassTableSequentialSelect = ArrayHelper.toBooleanArray(isDeferreds);
      this.subclassTableNameClosure = ArrayHelper.toStringArray((Collection)subclassTables);
      this.subclassTableIsLazyClosure = ArrayHelper.toBooleanArray(isLazies);
      this.subclassTableKeyColumnClosure = ArrayHelper.to2DStringArray(joinKeyColumns);
      this.isClassOrSuperclassTable = ArrayHelper.toBooleanArray(isConcretes);
      this.isInverseSubclassTable = ArrayHelper.toBooleanArray(isInverses);
      this.isNullableSubclassTable = ArrayHelper.toBooleanArray(isNullables);
      this.hasSequentialSelects = hasDeferred;
      if (persistentClass.isPolymorphic()) {
         Value discrimValue = persistentClass.getDiscriminator();
         if (discrimValue == null) {
            throw new MappingException("discriminator mapping required for single table polymorphic persistence");
         }

         this.forceDiscriminator = persistentClass.isForceDiscriminator();
         Selectable selectable = (Selectable)discrimValue.getColumnIterator().next();
         if (discrimValue.hasFormula()) {
            Formula formula = (Formula)selectable;
            this.discriminatorFormula = formula.getFormula();
            this.discriminatorFormulaTemplate = formula.getTemplate(factory.getDialect(), factory.getSqlFunctionRegistry());
            this.discriminatorColumnName = null;
            this.discriminatorColumnReaders = null;
            this.discriminatorColumnReaderTemplate = null;
            this.discriminatorAlias = "clazz_";
         } else {
            Column column = (Column)selectable;
            this.discriminatorColumnName = column.getQuotedName(factory.getDialect());
            this.discriminatorColumnReaders = column.getReadExpr(factory.getDialect());
            this.discriminatorColumnReaderTemplate = column.getTemplate(factory.getDialect(), factory.getSqlFunctionRegistry());
            this.discriminatorAlias = column.getAlias(factory.getDialect(), persistentClass.getRootTable());
            this.discriminatorFormula = null;
            this.discriminatorFormulaTemplate = null;
         }

         this.discriminatorType = persistentClass.getDiscriminator().getType();
         if (persistentClass.isDiscriminatorValueNull()) {
            this.discriminatorValue = NULL_DISCRIMINATOR;
            this.discriminatorSQLValue = "null";
            this.discriminatorInsertable = false;
         } else if (persistentClass.isDiscriminatorValueNotNull()) {
            this.discriminatorValue = NOT_NULL_DISCRIMINATOR;
            this.discriminatorSQLValue = "not null";
            this.discriminatorInsertable = false;
         } else {
            this.discriminatorInsertable = persistentClass.isDiscriminatorInsertable() && !discrimValue.hasFormula();

            try {
               org.hibernate.type.DiscriminatorType dtype = (org.hibernate.type.DiscriminatorType)this.discriminatorType;
               this.discriminatorValue = dtype.stringToObject(persistentClass.getDiscriminatorValue());
               this.discriminatorSQLValue = dtype.objectToSQLString(this.discriminatorValue, factory.getDialect());
            } catch (ClassCastException var29) {
               throw new MappingException("Illegal discriminator type: " + this.discriminatorType.getName());
            } catch (Exception e) {
               throw new MappingException("Could not format discriminator value to SQL string", e);
            }
         }
      } else {
         this.forceDiscriminator = false;
         this.discriminatorInsertable = false;
         this.discriminatorColumnName = null;
         this.discriminatorColumnReaders = null;
         this.discriminatorColumnReaderTemplate = null;
         this.discriminatorAlias = null;
         this.discriminatorType = null;
         this.discriminatorValue = null;
         this.discriminatorSQLValue = null;
         this.discriminatorFormula = null;
         this.discriminatorFormulaTemplate = null;
      }

      this.propertyTableNumbers = new int[this.getPropertySpan()];
      Iterator iter = persistentClass.getPropertyClosureIterator();

      Property prop;
      for(int i = 0; iter.hasNext(); this.propertyTableNumbers[i++] = persistentClass.getJoinNumber(prop)) {
         prop = (Property)iter.next();
      }

      ArrayList columnJoinNumbers = new ArrayList();
      ArrayList formulaJoinedNumbers = new ArrayList();
      ArrayList propertyJoinNumbers = new ArrayList();
      iter = persistentClass.getSubclassPropertyClosureIterator();

      while(iter.hasNext()) {
         Property prop = (Property)iter.next();
         Integer join = persistentClass.getJoinNumber(prop);
         propertyJoinNumbers.add(join);
         this.propertyTableNumbersByNameAndSubclass.put(prop.getPersistentClass().getEntityName() + '.' + prop.getName(), join);
         Iterator citer = prop.getColumnIterator();

         while(citer.hasNext()) {
            Selectable thing = (Selectable)citer.next();
            if (thing.isFormula()) {
               formulaJoinedNumbers.add(join);
            } else {
               columnJoinNumbers.add(join);
            }
         }
      }

      this.subclassColumnTableNumberClosure = ArrayHelper.toIntArray(columnJoinNumbers);
      this.subclassFormulaTableNumberClosure = ArrayHelper.toIntArray(formulaJoinedNumbers);
      this.subclassPropertyTableNumberClosure = ArrayHelper.toIntArray(propertyJoinNumbers);
      int subclassSpan = persistentClass.getSubclassSpan() + 1;
      this.subclassClosure = new String[subclassSpan];
      this.subclassClosure[0] = this.getEntityName();
      if (persistentClass.isPolymorphic()) {
         this.subclassesByDiscriminatorValue.put(this.discriminatorValue, this.getEntityName());
      }

      if (persistentClass.isPolymorphic()) {
         iter = persistentClass.getSubclassIterator();
         int k = 1;

         while(iter.hasNext()) {
            Subclass sc = (Subclass)iter.next();
            this.subclassClosure[k++] = sc.getEntityName();
            if (sc.isDiscriminatorValueNull()) {
               this.subclassesByDiscriminatorValue.put(NULL_DISCRIMINATOR, sc.getEntityName());
            } else if (sc.isDiscriminatorValueNotNull()) {
               this.subclassesByDiscriminatorValue.put(NOT_NULL_DISCRIMINATOR, sc.getEntityName());
            } else {
               try {
                  org.hibernate.type.DiscriminatorType dtype = (org.hibernate.type.DiscriminatorType)this.discriminatorType;
                  this.subclassesByDiscriminatorValue.put(dtype.stringToObject(sc.getDiscriminatorValue()), sc.getEntityName());
               } catch (ClassCastException var27) {
                  throw new MappingException("Illegal discriminator type: " + this.discriminatorType.getName());
               } catch (Exception e) {
                  throw new MappingException("Error parsing discriminator value", e);
               }
            }
         }
      }

      this.initLockers();
      this.initSubclassPropertyAliasesMap(persistentClass);
      this.postConstruct(mapping);
   }

   public SingleTableEntityPersister(EntityBinding entityBinding, EntityRegionAccessStrategy cacheAccessStrategy, NaturalIdRegionAccessStrategy naturalIdRegionAccessStrategy, SessionFactoryImplementor factory, Mapping mapping) throws HibernateException {
      super(entityBinding, cacheAccessStrategy, naturalIdRegionAccessStrategy, factory);
      this.joinSpan = 1;
      this.qualifiedTableNames = new String[this.joinSpan];
      this.isInverseTable = new boolean[this.joinSpan];
      this.isNullableTable = new boolean[this.joinSpan];
      this.keyColumnNames = new String[this.joinSpan][];
      TableSpecification table = entityBinding.getPrimaryTable();
      this.qualifiedTableNames[0] = table.getQualifiedName(factory.getDialect());
      this.isInverseTable[0] = false;
      this.isNullableTable[0] = false;
      this.keyColumnNames[0] = this.getIdentifierColumnNames();
      this.cascadeDeleteEnabled = new boolean[this.joinSpan];
      this.customSQLInsert = new String[this.joinSpan];
      this.customSQLUpdate = new String[this.joinSpan];
      this.customSQLDelete = new String[this.joinSpan];
      this.insertCallable = new boolean[this.joinSpan];
      this.updateCallable = new boolean[this.joinSpan];
      this.deleteCallable = new boolean[this.joinSpan];
      this.insertResultCheckStyles = new ExecuteUpdateResultCheckStyle[this.joinSpan];
      this.updateResultCheckStyles = new ExecuteUpdateResultCheckStyle[this.joinSpan];
      this.deleteResultCheckStyles = new ExecuteUpdateResultCheckStyle[this.joinSpan];
      initializeCustomSql(entityBinding.getCustomInsert(), 0, this.customSQLInsert, this.insertCallable, this.insertResultCheckStyles);
      initializeCustomSql(entityBinding.getCustomUpdate(), 0, this.customSQLUpdate, this.updateCallable, this.updateResultCheckStyles);
      initializeCustomSql(entityBinding.getCustomDelete(), 0, this.customSQLDelete, this.deleteCallable, this.deleteResultCheckStyles);
      this.constraintOrderedTableNames = new String[this.qualifiedTableNames.length];
      this.constraintOrderedKeyColumnNames = new String[this.qualifiedTableNames.length][];
      int i = this.qualifiedTableNames.length - 1;

      for(int position = 0; i >= 0; ++position) {
         this.constraintOrderedTableNames[position] = this.qualifiedTableNames[i];
         this.constraintOrderedKeyColumnNames[position] = this.keyColumnNames[i];
         --i;
      }

      this.spaces = ArrayHelper.join(this.qualifiedTableNames, ArrayHelper.toStringArray((Collection)entityBinding.getSynchronizedTableNames()));
      i = this.isInstrumented();
      boolean hasDeferred = false;
      ArrayList subclassTables = new ArrayList();
      ArrayList joinKeyColumns = new ArrayList();
      ArrayList<Boolean> isConcretes = new ArrayList();
      ArrayList<Boolean> isDeferreds = new ArrayList();
      ArrayList<Boolean> isInverses = new ArrayList();
      ArrayList<Boolean> isNullables = new ArrayList();
      ArrayList<Boolean> isLazies = new ArrayList();
      subclassTables.add(this.qualifiedTableNames[0]);
      joinKeyColumns.add(this.getIdentifierColumnNames());
      isConcretes.add(Boolean.TRUE);
      isDeferreds.add(Boolean.FALSE);
      isInverses.add(Boolean.FALSE);
      isNullables.add(Boolean.FALSE);
      isLazies.add(Boolean.FALSE);
      this.subclassTableSequentialSelect = ArrayHelper.toBooleanArray(isDeferreds);
      this.subclassTableNameClosure = ArrayHelper.toStringArray((Collection)subclassTables);
      this.subclassTableIsLazyClosure = ArrayHelper.toBooleanArray(isLazies);
      this.subclassTableKeyColumnClosure = ArrayHelper.to2DStringArray(joinKeyColumns);
      this.isClassOrSuperclassTable = ArrayHelper.toBooleanArray(isConcretes);
      this.isInverseSubclassTable = ArrayHelper.toBooleanArray(isInverses);
      this.isNullableSubclassTable = ArrayHelper.toBooleanArray(isNullables);
      this.hasSequentialSelects = hasDeferred;
      if (entityBinding.isPolymorphic()) {
         SimpleValue discriminatorRelationalValue = entityBinding.getHierarchyDetails().getEntityDiscriminator().getBoundValue();
         if (discriminatorRelationalValue == null) {
            throw new MappingException("discriminator mapping required for single table polymorphic persistence");
         }

         this.forceDiscriminator = entityBinding.getHierarchyDetails().getEntityDiscriminator().isForced();
         if (DerivedValue.class.isInstance(discriminatorRelationalValue)) {
            DerivedValue formula = (DerivedValue)discriminatorRelationalValue;
            this.discriminatorFormula = formula.getExpression();
            this.discriminatorFormulaTemplate = getTemplateFromString(formula.getExpression(), factory);
            this.discriminatorColumnName = null;
            this.discriminatorColumnReaders = null;
            this.discriminatorColumnReaderTemplate = null;
            this.discriminatorAlias = "clazz_";
         } else {
            org.hibernate.metamodel.relational.Column column = (org.hibernate.metamodel.relational.Column)discriminatorRelationalValue;
            this.discriminatorColumnName = column.getColumnName().encloseInQuotesIfQuoted(factory.getDialect());
            this.discriminatorColumnReaders = column.getReadFragment() == null ? column.getColumnName().encloseInQuotesIfQuoted(factory.getDialect()) : column.getReadFragment();
            this.discriminatorColumnReaderTemplate = this.getTemplateFromColumn(column, factory);
            this.discriminatorAlias = column.getAlias(factory.getDialect());
            this.discriminatorFormula = null;
            this.discriminatorFormulaTemplate = null;
         }

         this.discriminatorType = entityBinding.getHierarchyDetails().getEntityDiscriminator().getExplicitHibernateTypeDescriptor().getResolvedTypeMapping();
         if (entityBinding.getDiscriminatorMatchValue() == null) {
            this.discriminatorValue = NULL_DISCRIMINATOR;
            this.discriminatorSQLValue = "null";
            this.discriminatorInsertable = false;
         } else if (entityBinding.getDiscriminatorMatchValue().equals("null")) {
            this.discriminatorValue = NOT_NULL_DISCRIMINATOR;
            this.discriminatorSQLValue = "not null";
            this.discriminatorInsertable = false;
         } else if (entityBinding.getDiscriminatorMatchValue().equals("not null")) {
            this.discriminatorValue = NOT_NULL_DISCRIMINATOR;
            this.discriminatorSQLValue = "not null";
            this.discriminatorInsertable = false;
         } else {
            this.discriminatorInsertable = entityBinding.getHierarchyDetails().getEntityDiscriminator().isInserted() && !DerivedValue.class.isInstance(discriminatorRelationalValue);

            try {
               org.hibernate.type.DiscriminatorType dtype = (org.hibernate.type.DiscriminatorType)this.discriminatorType;
               this.discriminatorValue = dtype.stringToObject(entityBinding.getDiscriminatorMatchValue());
               this.discriminatorSQLValue = dtype.objectToSQLString(this.discriminatorValue, factory.getDialect());
            } catch (ClassCastException var28) {
               throw new MappingException("Illegal discriminator type: " + this.discriminatorType.getName());
            } catch (Exception e) {
               throw new MappingException("Could not format discriminator value to SQL string", e);
            }
         }
      } else {
         this.forceDiscriminator = false;
         this.discriminatorInsertable = false;
         this.discriminatorColumnName = null;
         this.discriminatorColumnReaders = null;
         this.discriminatorColumnReaderTemplate = null;
         this.discriminatorAlias = null;
         this.discriminatorType = null;
         this.discriminatorValue = null;
         this.discriminatorSQLValue = null;
         this.discriminatorFormula = null;
         this.discriminatorFormulaTemplate = null;
      }

      this.propertyTableNumbers = new int[this.getPropertySpan()];
      int i = 0;

      for(AttributeBinding attributeBinding : entityBinding.getAttributeBindingClosure()) {
         if (attributeBinding != entityBinding.getHierarchyDetails().getEntityIdentifier().getValueBinding() && attributeBinding.getAttribute().isSingular()) {
            this.propertyTableNumbers[i++] = 0;
         }
      }

      ArrayList columnJoinNumbers = new ArrayList();
      ArrayList formulaJoinedNumbers = new ArrayList();
      ArrayList propertyJoinNumbers = new ArrayList();

      for(AttributeBinding attributeBinding : entityBinding.getSubEntityAttributeBindingClosure()) {
         if (attributeBinding.getAttribute().isSingular()) {
            SingularAttributeBinding singularAttributeBinding = (SingularAttributeBinding)attributeBinding;
            int join = 0;
            propertyJoinNumbers.add(join);
            this.propertyTableNumbersByNameAndSubclass.put(singularAttributeBinding.getContainer().getPathBase() + '.' + singularAttributeBinding.getAttribute().getName(), join);

            for(SimpleValueBinding simpleValueBinding : singularAttributeBinding.getSimpleValueBindings()) {
               if (DerivedValue.class.isInstance(simpleValueBinding.getSimpleValue())) {
                  formulaJoinedNumbers.add(join);
               } else {
                  columnJoinNumbers.add(join);
               }
            }
         }
      }

      this.subclassColumnTableNumberClosure = ArrayHelper.toIntArray(columnJoinNumbers);
      this.subclassFormulaTableNumberClosure = ArrayHelper.toIntArray(formulaJoinedNumbers);
      this.subclassPropertyTableNumberClosure = ArrayHelper.toIntArray(propertyJoinNumbers);
      int subclassSpan = entityBinding.getSubEntityBindingClosureSpan() + 1;
      this.subclassClosure = new String[subclassSpan];
      this.subclassClosure[0] = this.getEntityName();
      if (entityBinding.isPolymorphic()) {
         this.subclassesByDiscriminatorValue.put(this.discriminatorValue, this.getEntityName());
      }

      if (entityBinding.isPolymorphic()) {
         int k = 1;

         for(EntityBinding subEntityBinding : entityBinding.getPostOrderSubEntityBindingClosure()) {
            this.subclassClosure[k++] = subEntityBinding.getEntity().getName();
            if (subEntityBinding.isDiscriminatorMatchValueNull()) {
               this.subclassesByDiscriminatorValue.put(NULL_DISCRIMINATOR, subEntityBinding.getEntity().getName());
            } else if (subEntityBinding.isDiscriminatorMatchValueNotNull()) {
               this.subclassesByDiscriminatorValue.put(NOT_NULL_DISCRIMINATOR, subEntityBinding.getEntity().getName());
            } else {
               try {
                  org.hibernate.type.DiscriminatorType dtype = (org.hibernate.type.DiscriminatorType)this.discriminatorType;
                  this.subclassesByDiscriminatorValue.put(dtype.stringToObject(subEntityBinding.getDiscriminatorMatchValue()), subEntityBinding.getEntity().getName());
               } catch (ClassCastException var26) {
                  throw new MappingException("Illegal discriminator type: " + this.discriminatorType.getName());
               } catch (Exception e) {
                  throw new MappingException("Error parsing discriminator value", e);
               }
            }
         }
      }

      this.initLockers();
      this.initSubclassPropertyAliasesMap(entityBinding);
      this.postConstruct(mapping);
   }

   private static void initializeCustomSql(CustomSQL customSql, int i, String[] sqlStrings, boolean[] callable, ExecuteUpdateResultCheckStyle[] checkStyles) {
      sqlStrings[i] = customSql != null ? customSql.getSql() : null;
      callable[i] = sqlStrings[i] != null && customSql.isCallable();
      checkStyles[i] = customSql != null && customSql.getCheckStyle() != null ? customSql.getCheckStyle() : ExecuteUpdateResultCheckStyle.determineDefault(sqlStrings[i], callable[i]);
   }

   protected boolean isInverseTable(int j) {
      return this.isInverseTable[j];
   }

   protected boolean isInverseSubclassTable(int j) {
      return this.isInverseSubclassTable[j];
   }

   public String getDiscriminatorColumnName() {
      return this.discriminatorColumnName;
   }

   public String getDiscriminatorColumnReaders() {
      return this.discriminatorColumnReaders;
   }

   public String getDiscriminatorColumnReaderTemplate() {
      return this.discriminatorColumnReaderTemplate;
   }

   protected String getDiscriminatorAlias() {
      return this.discriminatorAlias;
   }

   protected String getDiscriminatorFormulaTemplate() {
      return this.discriminatorFormulaTemplate;
   }

   public String getTableName() {
      return this.qualifiedTableNames[0];
   }

   public Type getDiscriminatorType() {
      return this.discriminatorType;
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
      if (value == null) {
         return (String)this.subclassesByDiscriminatorValue.get(NULL_DISCRIMINATOR);
      } else {
         String result = (String)this.subclassesByDiscriminatorValue.get(value);
         if (result == null) {
            result = (String)this.subclassesByDiscriminatorValue.get(NOT_NULL_DISCRIMINATOR);
         }

         return result;
      }
   }

   public Serializable[] getPropertySpaces() {
      return this.spaces;
   }

   protected boolean isDiscriminatorFormula() {
      return this.discriminatorColumnName == null;
   }

   protected String getDiscriminatorFormula() {
      return this.discriminatorFormula;
   }

   protected String getTableName(int j) {
      return this.qualifiedTableNames[j];
   }

   protected String[] getKeyColumns(int j) {
      return this.keyColumnNames[j];
   }

   protected boolean isTableCascadeDeleteEnabled(int j) {
      return this.cascadeDeleteEnabled[j];
   }

   protected boolean isPropertyOfTable(int property, int j) {
      return this.propertyTableNumbers[property] == j;
   }

   protected boolean isSubclassTableSequentialSelect(int j) {
      return this.subclassTableSequentialSelect[j] && !this.isClassOrSuperclassTable[j];
   }

   public String fromTableFragment(String name) {
      return this.getTableName() + ' ' + name;
   }

   public String filterFragment(String alias) throws MappingException {
      String result = this.discriminatorFilterFragment(alias);
      if (this.hasWhere()) {
         result = result + " and " + this.getSQLWhereString(alias);
      }

      return result;
   }

   public String oneToManyFilterFragment(String alias) throws MappingException {
      return this.forceDiscriminator ? this.discriminatorFilterFragment(alias) : "";
   }

   private String discriminatorFilterFragment(String alias) throws MappingException {
      if (this.needsDiscriminator()) {
         InFragment frag = new InFragment();
         if (this.isDiscriminatorFormula()) {
            frag.setFormula(alias, this.getDiscriminatorFormulaTemplate());
         } else {
            frag.setColumn(alias, this.getDiscriminatorColumnName());
         }

         String[] subclasses = this.getSubclassClosure();

         for(int i = 0; i < subclasses.length; ++i) {
            Queryable queryable = (Queryable)this.getFactory().getEntityPersister(subclasses[i]);
            if (!queryable.isAbstract()) {
               frag.addValue(queryable.getDiscriminatorSQLValue());
            }
         }

         StringBuilder buf = (new StringBuilder(50)).append(" and ").append(frag.toFragmentString());
         return buf.toString();
      } else {
         return "";
      }
   }

   private boolean needsDiscriminator() {
      return this.forceDiscriminator || this.isInherited();
   }

   public String getSubclassPropertyTableName(int i) {
      return this.subclassTableNameClosure[this.subclassPropertyTableNumberClosure[i]];
   }

   protected void addDiscriminatorToSelect(SelectFragment select, String name, String suffix) {
      if (this.isDiscriminatorFormula()) {
         select.addFormula(name, this.getDiscriminatorFormulaTemplate(), this.getDiscriminatorAlias());
      } else {
         select.addColumn(name, this.getDiscriminatorColumnName(), this.getDiscriminatorAlias());
      }

   }

   protected int[] getPropertyTableNumbersInSelect() {
      return this.propertyTableNumbers;
   }

   protected int getSubclassPropertyTableNumber(int i) {
      return this.subclassPropertyTableNumberClosure[i];
   }

   public int getTableSpan() {
      return this.joinSpan;
   }

   protected void addDiscriminatorToInsert(Insert insert) {
      if (this.discriminatorInsertable) {
         insert.addColumn(this.getDiscriminatorColumnName(), this.discriminatorSQLValue);
      }

   }

   protected int[] getSubclassColumnTableNumberClosure() {
      return this.subclassColumnTableNumberClosure;
   }

   protected int[] getSubclassFormulaTableNumberClosure() {
      return this.subclassFormulaTableNumberClosure;
   }

   protected int[] getPropertyTableNumbers() {
      return this.propertyTableNumbers;
   }

   protected boolean isSubclassPropertyDeferred(String propertyName, String entityName) {
      return this.hasSequentialSelects && this.isSubclassTableSequentialSelect(this.getSubclassPropertyTableNumber(propertyName, entityName));
   }

   public boolean hasSequentialSelect() {
      return this.hasSequentialSelects;
   }

   private int getSubclassPropertyTableNumber(String propertyName, String entityName) {
      Type type = this.propertyMapping.toType(propertyName);
      if (type.isAssociationType() && ((AssociationType)type).useLHSPrimaryKey()) {
         return 0;
      } else {
         Integer tabnum = (Integer)this.propertyTableNumbersByNameAndSubclass.get(entityName + '.' + propertyName);
         return tabnum == null ? 0 : tabnum;
      }
   }

   protected String getSequentialSelect(String entityName) {
      return (String)this.sequentialSelectStringsByEntityName.get(entityName);
   }

   private String generateSequentialSelect(Loadable persister) {
      AbstractEntityPersister subclassPersister = (AbstractEntityPersister)persister;
      HashSet tableNumbers = new HashSet();
      String[] props = subclassPersister.getPropertyNames();
      String[] classes = subclassPersister.getPropertySubclassNames();

      for(int i = 0; i < props.length; ++i) {
         int propTableNumber = this.getSubclassPropertyTableNumber(props[i], classes[i]);
         if (this.isSubclassTableSequentialSelect(propTableNumber) && !this.isSubclassTableLazy(propTableNumber)) {
            tableNumbers.add(propTableNumber);
         }
      }

      if (tableNumbers.isEmpty()) {
         return null;
      } else {
         ArrayList columnNumbers = new ArrayList();
         int[] columnTableNumbers = this.getSubclassColumnTableNumberClosure();

         for(int i = 0; i < this.getSubclassColumnClosure().length; ++i) {
            if (tableNumbers.contains(columnTableNumbers[i])) {
               columnNumbers.add(i);
            }
         }

         ArrayList formulaNumbers = new ArrayList();
         int[] formulaTableNumbers = this.getSubclassColumnTableNumberClosure();

         for(int i = 0; i < this.getSubclassFormulaTemplateClosure().length; ++i) {
            if (tableNumbers.contains(formulaTableNumbers[i])) {
               formulaNumbers.add(i);
            }
         }

         return this.renderSelect(ArrayHelper.toIntArray(tableNumbers), ArrayHelper.toIntArray(columnNumbers), ArrayHelper.toIntArray(formulaNumbers));
      }
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

   protected boolean isClassOrSuperclassTable(int j) {
      return this.isClassOrSuperclassTable[j];
   }

   protected boolean isSubclassTableLazy(int j) {
      return this.subclassTableIsLazyClosure[j];
   }

   protected boolean isNullableTable(int j) {
      return this.isNullableTable[j];
   }

   protected boolean isNullableSubclassTable(int j) {
      return this.isNullableSubclassTable[j];
   }

   public String getPropertyTableName(String propertyName) {
      Integer index = this.getEntityMetamodel().getPropertyIndexOrNull(propertyName);
      return index == null ? null : this.qualifiedTableNames[this.propertyTableNumbers[index]];
   }

   public void postInstantiate() {
      super.postInstantiate();
      if (this.hasSequentialSelects) {
         String[] entityNames = this.getSubclassClosure();

         for(int i = 1; i < entityNames.length; ++i) {
            Loadable loadable = (Loadable)this.getFactory().getEntityPersister(entityNames[i]);
            if (!loadable.isAbstract()) {
               String sequentialSelect = this.generateSequentialSelect(loadable);
               this.sequentialSelectStringsByEntityName.put(entityNames[i], sequentialSelect);
            }
         }
      }

   }

   public boolean isMultiTable() {
      return this.getTableSpan() > 1;
   }

   public String[] getConstraintOrderedTableNameClosure() {
      return this.constraintOrderedTableNames;
   }

   public String[][] getContraintOrderedTableKeyColumnClosure() {
      return this.constraintOrderedKeyColumnNames;
   }

   public FilterAliasGenerator getFilterAliasGenerator(String rootAlias) {
      return new DynamicFilterAliasGenerator(this.qualifiedTableNames, rootAlias);
   }
}
