package org.hibernate.mapping;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import org.hibernate.FetchMode;
import org.hibernate.MappingException;
import org.hibernate.cfg.Mappings;
import org.hibernate.engine.spi.ExecuteUpdateResultCheckStyle;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.internal.FilterConfiguration;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.internal.util.collections.EmptyIterator;
import org.hibernate.type.CollectionType;
import org.hibernate.type.Type;

public abstract class Collection implements Fetchable, Value, Filterable {
   public static final String DEFAULT_ELEMENT_COLUMN_NAME = "elt";
   public static final String DEFAULT_KEY_COLUMN_NAME = "id";
   private final Mappings mappings;
   private PersistentClass owner;
   private KeyValue key;
   private Value element;
   private Table collectionTable;
   private String role;
   private boolean lazy;
   private boolean extraLazy;
   private boolean inverse;
   private boolean mutable = true;
   private boolean subselectLoadable;
   private String cacheConcurrencyStrategy;
   private String cacheRegionName;
   private String orderBy;
   private String where;
   private String manyToManyWhere;
   private String manyToManyOrderBy;
   private String referencedPropertyName;
   private String nodeName;
   private String elementNodeName;
   private boolean sorted;
   private Comparator comparator;
   private String comparatorClassName;
   private boolean orphanDelete;
   private int batchSize = -1;
   private FetchMode fetchMode;
   private boolean embedded = true;
   private boolean optimisticLocked = true;
   private Class collectionPersisterClass;
   private String typeName;
   private Properties typeParameters;
   private final java.util.List filters = new ArrayList();
   private final java.util.List manyToManyFilters = new ArrayList();
   private final java.util.Set synchronizedTables = new HashSet();
   private String customSQLInsert;
   private boolean customInsertCallable;
   private ExecuteUpdateResultCheckStyle insertCheckStyle;
   private String customSQLUpdate;
   private boolean customUpdateCallable;
   private ExecuteUpdateResultCheckStyle updateCheckStyle;
   private String customSQLDelete;
   private boolean customDeleteCallable;
   private ExecuteUpdateResultCheckStyle deleteCheckStyle;
   private String customSQLDeleteAll;
   private boolean customDeleteAllCallable;
   private ExecuteUpdateResultCheckStyle deleteAllCheckStyle;
   private String loaderName;

   protected Collection(Mappings mappings, PersistentClass owner) {
      this.mappings = mappings;
      this.owner = owner;
   }

   public Mappings getMappings() {
      return this.mappings;
   }

   public boolean isSet() {
      return false;
   }

   public KeyValue getKey() {
      return this.key;
   }

   public Value getElement() {
      return this.element;
   }

   public boolean isIndexed() {
      return false;
   }

   public Table getCollectionTable() {
      return this.collectionTable;
   }

   public void setCollectionTable(Table table) {
      this.collectionTable = table;
   }

   public boolean isSorted() {
      return this.sorted;
   }

   public Comparator getComparator() {
      if (this.comparator == null && this.comparatorClassName != null) {
         try {
            this.setComparator((Comparator)ReflectHelper.classForName(this.comparatorClassName).newInstance());
         } catch (Exception var2) {
            throw new MappingException("Could not instantiate comparator class [" + this.comparatorClassName + "] for collection " + this.getRole());
         }
      }

      return this.comparator;
   }

   public boolean isLazy() {
      return this.lazy;
   }

   public void setLazy(boolean lazy) {
      this.lazy = lazy;
   }

   public String getRole() {
      return this.role;
   }

   public abstract CollectionType getDefaultCollectionType() throws MappingException;

   public boolean isPrimitiveArray() {
      return false;
   }

   public boolean isArray() {
      return false;
   }

   public boolean hasFormula() {
      return false;
   }

   public boolean isOneToMany() {
      return this.element instanceof OneToMany;
   }

   public boolean isInverse() {
      return this.inverse;
   }

   public String getOwnerEntityName() {
      return this.owner.getEntityName();
   }

   public String getOrderBy() {
      return this.orderBy;
   }

   public void setComparator(Comparator comparator) {
      this.comparator = comparator;
   }

   public void setElement(Value element) {
      this.element = element;
   }

   public void setKey(KeyValue key) {
      this.key = key;
   }

   public void setOrderBy(String orderBy) {
      this.orderBy = orderBy;
   }

   public void setRole(String role) {
      this.role = role == null ? null : role.intern();
   }

   public void setSorted(boolean sorted) {
      this.sorted = sorted;
   }

   public void setInverse(boolean inverse) {
      this.inverse = inverse;
   }

   public PersistentClass getOwner() {
      return this.owner;
   }

   /** @deprecated */
   @Deprecated
   public void setOwner(PersistentClass owner) {
      this.owner = owner;
   }

   public String getWhere() {
      return this.where;
   }

   public void setWhere(String where) {
      this.where = where;
   }

   public String getManyToManyWhere() {
      return this.manyToManyWhere;
   }

   public void setManyToManyWhere(String manyToManyWhere) {
      this.manyToManyWhere = manyToManyWhere;
   }

   public String getManyToManyOrdering() {
      return this.manyToManyOrderBy;
   }

   public void setManyToManyOrdering(String orderFragment) {
      this.manyToManyOrderBy = orderFragment;
   }

   public boolean isIdentified() {
      return false;
   }

   public boolean hasOrphanDelete() {
      return this.orphanDelete;
   }

   public void setOrphanDelete(boolean orphanDelete) {
      this.orphanDelete = orphanDelete;
   }

   public int getBatchSize() {
      return this.batchSize;
   }

   public void setBatchSize(int i) {
      this.batchSize = i;
   }

   public FetchMode getFetchMode() {
      return this.fetchMode;
   }

   public void setFetchMode(FetchMode fetchMode) {
      this.fetchMode = fetchMode;
   }

   public void setCollectionPersisterClass(Class persister) {
      this.collectionPersisterClass = persister;
   }

   public Class getCollectionPersisterClass() {
      return this.collectionPersisterClass;
   }

   public void validate(Mapping mapping) throws MappingException {
      if (!this.getKey().isCascadeDeleteEnabled() || this.isInverse() && this.isOneToMany()) {
         if (!this.getKey().isValid(mapping)) {
            throw new MappingException("collection foreign key mapping has wrong number of columns: " + this.getRole() + " type: " + this.getKey().getType().getName());
         } else if (!this.getElement().isValid(mapping)) {
            throw new MappingException("collection element mapping has wrong number of columns: " + this.getRole() + " type: " + this.getElement().getType().getName());
         } else {
            this.checkColumnDuplication();
            if (this.elementNodeName != null && this.elementNodeName.startsWith("@")) {
               throw new MappingException("element node must not be an attribute: " + this.elementNodeName);
            } else if (this.elementNodeName != null && this.elementNodeName.equals(".")) {
               throw new MappingException("element node must not be the parent: " + this.elementNodeName);
            } else if (this.nodeName != null && this.nodeName.indexOf(64) > -1) {
               throw new MappingException("collection node must not be an attribute: " + this.elementNodeName);
            }
         }
      } else {
         throw new MappingException("only inverse one-to-many associations may use on-delete=\"cascade\": " + this.getRole());
      }
   }

   private void checkColumnDuplication(java.util.Set distinctColumns, Iterator columns) throws MappingException {
      while(true) {
         if (columns.hasNext()) {
            Selectable s = (Selectable)columns.next();
            if (s.isFormula()) {
               continue;
            }

            Column col = (Column)s;
            if (distinctColumns.add(col.getName())) {
               continue;
            }

            throw new MappingException("Repeated column in mapping for collection: " + this.getRole() + " column: " + col.getName());
         }

         return;
      }
   }

   private void checkColumnDuplication() throws MappingException {
      HashSet cols = new HashSet();
      this.checkColumnDuplication(cols, this.getKey().getColumnIterator());
      if (this.isIndexed()) {
         this.checkColumnDuplication(cols, ((IndexedCollection)this).getIndex().getColumnIterator());
      }

      if (this.isIdentified()) {
         this.checkColumnDuplication(cols, ((IdentifierCollection)this).getIdentifier().getColumnIterator());
      }

      if (!this.isOneToMany()) {
         this.checkColumnDuplication(cols, this.getElement().getColumnIterator());
      }

   }

   public Iterator getColumnIterator() {
      return EmptyIterator.INSTANCE;
   }

   public int getColumnSpan() {
      return 0;
   }

   public Type getType() throws MappingException {
      return this.getCollectionType();
   }

   public CollectionType getCollectionType() {
      return this.typeName == null ? this.getDefaultCollectionType() : this.mappings.getTypeResolver().getTypeFactory().customCollection(this.typeName, this.typeParameters, this.role, this.referencedPropertyName);
   }

   public boolean isNullable() {
      return true;
   }

   public boolean isAlternateUniqueKey() {
      return false;
   }

   public Table getTable() {
      return this.owner.getTable();
   }

   public void createForeignKey() {
   }

   public boolean isSimpleValue() {
      return false;
   }

   public boolean isValid(Mapping mapping) throws MappingException {
      return true;
   }

   private void createForeignKeys() throws MappingException {
      if (this.referencedPropertyName == null) {
         this.getElement().createForeignKey();
         this.key.createForeignKeyOfEntity(this.getOwner().getEntityName());
      }

   }

   abstract void createPrimaryKey();

   public void createAllKeys() throws MappingException {
      this.createForeignKeys();
      if (!this.isInverse()) {
         this.createPrimaryKey();
      }

   }

   public String getCacheConcurrencyStrategy() {
      return this.cacheConcurrencyStrategy;
   }

   public void setCacheConcurrencyStrategy(String cacheConcurrencyStrategy) {
      this.cacheConcurrencyStrategy = cacheConcurrencyStrategy;
   }

   public void setTypeUsingReflection(String className, String propertyName) {
   }

   public String getCacheRegionName() {
      return this.cacheRegionName == null ? this.role : this.cacheRegionName;
   }

   public void setCacheRegionName(String cacheRegionName) {
      this.cacheRegionName = cacheRegionName;
   }

   public void setCustomSQLInsert(String customSQLInsert, boolean callable, ExecuteUpdateResultCheckStyle checkStyle) {
      this.customSQLInsert = customSQLInsert;
      this.customInsertCallable = callable;
      this.insertCheckStyle = checkStyle;
   }

   public String getCustomSQLInsert() {
      return this.customSQLInsert;
   }

   public boolean isCustomInsertCallable() {
      return this.customInsertCallable;
   }

   public ExecuteUpdateResultCheckStyle getCustomSQLInsertCheckStyle() {
      return this.insertCheckStyle;
   }

   public void setCustomSQLUpdate(String customSQLUpdate, boolean callable, ExecuteUpdateResultCheckStyle checkStyle) {
      this.customSQLUpdate = customSQLUpdate;
      this.customUpdateCallable = callable;
      this.updateCheckStyle = checkStyle;
   }

   public String getCustomSQLUpdate() {
      return this.customSQLUpdate;
   }

   public boolean isCustomUpdateCallable() {
      return this.customUpdateCallable;
   }

   public ExecuteUpdateResultCheckStyle getCustomSQLUpdateCheckStyle() {
      return this.updateCheckStyle;
   }

   public void setCustomSQLDelete(String customSQLDelete, boolean callable, ExecuteUpdateResultCheckStyle checkStyle) {
      this.customSQLDelete = customSQLDelete;
      this.customDeleteCallable = callable;
      this.deleteCheckStyle = checkStyle;
   }

   public String getCustomSQLDelete() {
      return this.customSQLDelete;
   }

   public boolean isCustomDeleteCallable() {
      return this.customDeleteCallable;
   }

   public ExecuteUpdateResultCheckStyle getCustomSQLDeleteCheckStyle() {
      return this.deleteCheckStyle;
   }

   public void setCustomSQLDeleteAll(String customSQLDeleteAll, boolean callable, ExecuteUpdateResultCheckStyle checkStyle) {
      this.customSQLDeleteAll = customSQLDeleteAll;
      this.customDeleteAllCallable = callable;
      this.deleteAllCheckStyle = checkStyle;
   }

   public String getCustomSQLDeleteAll() {
      return this.customSQLDeleteAll;
   }

   public boolean isCustomDeleteAllCallable() {
      return this.customDeleteAllCallable;
   }

   public ExecuteUpdateResultCheckStyle getCustomSQLDeleteAllCheckStyle() {
      return this.deleteAllCheckStyle;
   }

   public void addFilter(String name, String condition, boolean autoAliasInjection, java.util.Map aliasTableMap, java.util.Map aliasEntityMap) {
      this.filters.add(new FilterConfiguration(name, condition, autoAliasInjection, aliasTableMap, aliasEntityMap, (PersistentClass)null));
   }

   public java.util.List getFilters() {
      return this.filters;
   }

   public void addManyToManyFilter(String name, String condition, boolean autoAliasInjection, java.util.Map aliasTableMap, java.util.Map aliasEntityMap) {
      this.manyToManyFilters.add(new FilterConfiguration(name, condition, autoAliasInjection, aliasTableMap, aliasEntityMap, (PersistentClass)null));
   }

   public java.util.List getManyToManyFilters() {
      return this.manyToManyFilters;
   }

   public String toString() {
      return this.getClass().getName() + '(' + this.getRole() + ')';
   }

   public java.util.Set getSynchronizedTables() {
      return this.synchronizedTables;
   }

   public String getLoaderName() {
      return this.loaderName;
   }

   public void setLoaderName(String name) {
      this.loaderName = name == null ? null : name.intern();
   }

   public String getReferencedPropertyName() {
      return this.referencedPropertyName;
   }

   public void setReferencedPropertyName(String propertyRef) {
      this.referencedPropertyName = propertyRef == null ? null : propertyRef.intern();
   }

   public boolean isOptimisticLocked() {
      return this.optimisticLocked;
   }

   public void setOptimisticLocked(boolean optimisticLocked) {
      this.optimisticLocked = optimisticLocked;
   }

   public boolean isMap() {
      return false;
   }

   public String getTypeName() {
      return this.typeName;
   }

   public void setTypeName(String typeName) {
      this.typeName = typeName;
   }

   public Properties getTypeParameters() {
      return this.typeParameters;
   }

   public void setTypeParameters(Properties parameterMap) {
      this.typeParameters = parameterMap;
   }

   public boolean[] getColumnInsertability() {
      return ArrayHelper.EMPTY_BOOLEAN_ARRAY;
   }

   public boolean[] getColumnUpdateability() {
      return ArrayHelper.EMPTY_BOOLEAN_ARRAY;
   }

   public String getNodeName() {
      return this.nodeName;
   }

   public void setNodeName(String nodeName) {
      this.nodeName = nodeName;
   }

   public String getElementNodeName() {
      return this.elementNodeName;
   }

   public void setElementNodeName(String elementNodeName) {
      this.elementNodeName = elementNodeName;
   }

   /** @deprecated */
   @Deprecated
   public boolean isEmbedded() {
      return this.embedded;
   }

   /** @deprecated */
   @Deprecated
   public void setEmbedded(boolean embedded) {
      this.embedded = embedded;
   }

   public boolean isSubselectLoadable() {
      return this.subselectLoadable;
   }

   public void setSubselectLoadable(boolean subqueryLoadable) {
      this.subselectLoadable = subqueryLoadable;
   }

   public boolean isMutable() {
      return this.mutable;
   }

   public void setMutable(boolean mutable) {
      this.mutable = mutable;
   }

   public boolean isExtraLazy() {
      return this.extraLazy;
   }

   public void setExtraLazy(boolean extraLazy) {
      this.extraLazy = extraLazy;
   }

   public boolean hasOrder() {
      return this.orderBy != null || this.manyToManyOrderBy != null;
   }

   public void setComparatorClassName(String comparatorClassName) {
      this.comparatorClassName = comparatorClassName;
   }

   public String getComparatorClassName() {
      return this.comparatorClassName;
   }
}
