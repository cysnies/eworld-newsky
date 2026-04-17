package org.hibernate.mapping;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;
import org.hibernate.EntityMode;
import org.hibernate.MappingException;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.ExecuteUpdateResultCheckStyle;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.internal.FilterConfiguration;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.internal.util.collections.EmptyIterator;
import org.hibernate.internal.util.collections.JoinedIterator;
import org.hibernate.internal.util.collections.SingletonIterator;
import org.hibernate.sql.Alias;

public abstract class PersistentClass implements Serializable, Filterable, MetaAttributable {
   private static final Alias PK_ALIAS = new Alias(15, "PK");
   public static final String NULL_DISCRIMINATOR_MAPPING = "null";
   public static final String NOT_NULL_DISCRIMINATOR_MAPPING = "not null";
   private String entityName;
   private String className;
   private String proxyInterfaceName;
   private String nodeName;
   private String jpaEntityName;
   private String discriminatorValue;
   private boolean lazy;
   private ArrayList properties = new ArrayList();
   private ArrayList declaredProperties = new ArrayList();
   private final ArrayList subclasses = new ArrayList();
   private final ArrayList subclassProperties = new ArrayList();
   private final ArrayList subclassTables = new ArrayList();
   private boolean dynamicInsert;
   private boolean dynamicUpdate;
   private int batchSize = -1;
   private boolean selectBeforeUpdate;
   private java.util.Map metaAttributes;
   private ArrayList joins = new ArrayList();
   private final ArrayList subclassJoins = new ArrayList();
   private final java.util.List filters = new ArrayList();
   protected final java.util.Set synchronizedTables = new HashSet();
   private String loaderName;
   private Boolean isAbstract;
   private boolean hasSubselectLoadableCollections;
   private Component identifierMapper;
   private String customSQLInsert;
   private boolean customInsertCallable;
   private ExecuteUpdateResultCheckStyle insertCheckStyle;
   private String customSQLUpdate;
   private boolean customUpdateCallable;
   private ExecuteUpdateResultCheckStyle updateCheckStyle;
   private String customSQLDelete;
   private boolean customDeleteCallable;
   private ExecuteUpdateResultCheckStyle deleteCheckStyle;
   private String temporaryIdTableName;
   private String temporaryIdTableDDL;
   private java.util.Map tuplizerImpls;
   protected int optimisticLockMode;
   private MappedSuperclass superMappedSuperclass;
   private Component declaredIdentifierMapper;

   public String getClassName() {
      return this.className;
   }

   public void setClassName(String className) {
      this.className = className == null ? null : className.intern();
   }

   public String getProxyInterfaceName() {
      return this.proxyInterfaceName;
   }

   public void setProxyInterfaceName(String proxyInterfaceName) {
      this.proxyInterfaceName = proxyInterfaceName;
   }

   public Class getMappedClass() throws MappingException {
      if (this.className == null) {
         return null;
      } else {
         try {
            return ReflectHelper.classForName(this.className);
         } catch (ClassNotFoundException cnfe) {
            throw new MappingException("entity class not found: " + this.className, cnfe);
         }
      }
   }

   public Class getProxyInterface() {
      if (this.proxyInterfaceName == null) {
         return null;
      } else {
         try {
            return ReflectHelper.classForName(this.proxyInterfaceName);
         } catch (ClassNotFoundException cnfe) {
            throw new MappingException("proxy class not found: " + this.proxyInterfaceName, cnfe);
         }
      }
   }

   public boolean useDynamicInsert() {
      return this.dynamicInsert;
   }

   abstract int nextSubclassId();

   public abstract int getSubclassId();

   public boolean useDynamicUpdate() {
      return this.dynamicUpdate;
   }

   public void setDynamicInsert(boolean dynamicInsert) {
      this.dynamicInsert = dynamicInsert;
   }

   public void setDynamicUpdate(boolean dynamicUpdate) {
      this.dynamicUpdate = dynamicUpdate;
   }

   public String getDiscriminatorValue() {
      return this.discriminatorValue;
   }

   public void addSubclass(Subclass subclass) throws MappingException {
      for(PersistentClass superclass = this.getSuperclass(); superclass != null; superclass = superclass.getSuperclass()) {
         if (subclass.getEntityName().equals(superclass.getEntityName())) {
            throw new MappingException("Circular inheritance mapping detected: " + subclass.getEntityName() + " will have it self as superclass when extending " + this.getEntityName());
         }
      }

      this.subclasses.add(subclass);
   }

   public boolean hasSubclasses() {
      return this.subclasses.size() > 0;
   }

   public int getSubclassSpan() {
      int n = this.subclasses.size();

      for(Iterator iter = this.subclasses.iterator(); iter.hasNext(); n += ((Subclass)iter.next()).getSubclassSpan()) {
      }

      return n;
   }

   public Iterator getSubclassIterator() {
      Iterator[] iters = new Iterator[this.subclasses.size() + 1];
      Iterator iter = this.subclasses.iterator();

      int i;
      for(i = 0; iter.hasNext(); iters[i++] = ((Subclass)iter.next()).getSubclassIterator()) {
      }

      iters[i] = this.subclasses.iterator();
      return new JoinedIterator(iters);
   }

   public Iterator getSubclassClosureIterator() {
      ArrayList iters = new ArrayList();
      iters.add(new SingletonIterator(this));
      Iterator iter = this.getSubclassIterator();

      while(iter.hasNext()) {
         PersistentClass clazz = (PersistentClass)iter.next();
         iters.add(clazz.getSubclassClosureIterator());
      }

      return new JoinedIterator(iters);
   }

   public Table getIdentityTable() {
      return this.getRootTable();
   }

   public Iterator getDirectSubclasses() {
      return this.subclasses.iterator();
   }

   public void addProperty(Property p) {
      this.properties.add(p);
      this.declaredProperties.add(p);
      p.setPersistentClass(this);
   }

   public abstract Table getTable();

   public String getEntityName() {
      return this.entityName;
   }

   public abstract boolean isMutable();

   public abstract boolean hasIdentifierProperty();

   public abstract Property getIdentifierProperty();

   public abstract Property getDeclaredIdentifierProperty();

   public abstract KeyValue getIdentifier();

   public abstract Property getVersion();

   public abstract Property getDeclaredVersion();

   public abstract Value getDiscriminator();

   public abstract boolean isInherited();

   public abstract boolean isPolymorphic();

   public abstract boolean isVersioned();

   public abstract String getNaturalIdCacheRegionName();

   public abstract String getCacheConcurrencyStrategy();

   public abstract PersistentClass getSuperclass();

   public abstract boolean isExplicitPolymorphism();

   public abstract boolean isDiscriminatorInsertable();

   public abstract Iterator getPropertyClosureIterator();

   public abstract Iterator getTableClosureIterator();

   public abstract Iterator getKeyClosureIterator();

   protected void addSubclassProperty(Property prop) {
      this.subclassProperties.add(prop);
   }

   protected void addSubclassJoin(Join join) {
      this.subclassJoins.add(join);
   }

   protected void addSubclassTable(Table subclassTable) {
      this.subclassTables.add(subclassTable);
   }

   public Iterator getSubclassPropertyClosureIterator() {
      ArrayList iters = new ArrayList();
      iters.add(this.getPropertyClosureIterator());
      iters.add(this.subclassProperties.iterator());

      for(int i = 0; i < this.subclassJoins.size(); ++i) {
         Join join = (Join)this.subclassJoins.get(i);
         iters.add(join.getPropertyIterator());
      }

      return new JoinedIterator(iters);
   }

   public Iterator getSubclassJoinClosureIterator() {
      return new JoinedIterator(this.getJoinClosureIterator(), this.subclassJoins.iterator());
   }

   public Iterator getSubclassTableClosureIterator() {
      return new JoinedIterator(this.getTableClosureIterator(), this.subclassTables.iterator());
   }

   public boolean isClassOrSuperclassJoin(Join join) {
      return this.joins.contains(join);
   }

   public boolean isClassOrSuperclassTable(Table closureTable) {
      return this.getTable() == closureTable;
   }

   public boolean isLazy() {
      return this.lazy;
   }

   public void setLazy(boolean lazy) {
      this.lazy = lazy;
   }

   public abstract boolean hasEmbeddedIdentifier();

   public abstract Class getEntityPersisterClass();

   public abstract void setEntityPersisterClass(Class var1);

   public abstract Table getRootTable();

   public abstract RootClass getRootClass();

   public abstract KeyValue getKey();

   public void setDiscriminatorValue(String discriminatorValue) {
      this.discriminatorValue = discriminatorValue;
   }

   public void setEntityName(String entityName) {
      this.entityName = entityName == null ? null : entityName.intern();
   }

   public void createPrimaryKey() {
      PrimaryKey pk = new PrimaryKey();
      Table table = this.getTable();
      pk.setTable(table);
      pk.setName(PK_ALIAS.toAliasString(table.getName()));
      table.setPrimaryKey(pk);
      pk.addColumns(this.getKey().getColumnIterator());
   }

   public abstract String getWhere();

   public int getBatchSize() {
      return this.batchSize;
   }

   public void setBatchSize(int batchSize) {
      this.batchSize = batchSize;
   }

   public boolean hasSelectBeforeUpdate() {
      return this.selectBeforeUpdate;
   }

   public void setSelectBeforeUpdate(boolean selectBeforeUpdate) {
      this.selectBeforeUpdate = selectBeforeUpdate;
   }

   public Iterator getReferenceablePropertyIterator() {
      return this.getPropertyClosureIterator();
   }

   public Property getReferencedProperty(String propertyPath) throws MappingException {
      try {
         return this.getRecursiveProperty(propertyPath, this.getReferenceablePropertyIterator());
      } catch (MappingException e) {
         throw new MappingException("property-ref [" + propertyPath + "] not found on entity [" + this.getEntityName() + "]", e);
      }
   }

   public Property getRecursiveProperty(String propertyPath) throws MappingException {
      try {
         return this.getRecursiveProperty(propertyPath, this.getPropertyIterator());
      } catch (MappingException e) {
         throw new MappingException("property [" + propertyPath + "] not found on entity [" + this.getEntityName() + "]", e);
      }
   }

   private Property getRecursiveProperty(String propertyPath, Iterator iter) throws MappingException {
      Property property = null;
      StringTokenizer st = new StringTokenizer(propertyPath, ".", false);

      try {
         while(st.hasMoreElements()) {
            String element = (String)st.nextElement();
            if (property == null) {
               Property identifierProperty = this.getIdentifierProperty();
               if (identifierProperty != null && identifierProperty.getName().equals(element)) {
                  property = identifierProperty;
               } else if (identifierProperty == null && this.getIdentifierMapper() != null) {
                  try {
                     identifierProperty = this.getProperty(element, this.getIdentifierMapper().getPropertyIterator());
                     if (identifierProperty != null) {
                        property = identifierProperty;
                     }
                  } catch (MappingException var8) {
                  }
               }

               if (property == null) {
                  property = this.getProperty(element, iter);
               }
            } else {
               property = ((Component)property.getValue()).getProperty(element);
            }
         }

         return property;
      } catch (MappingException var9) {
         throw new MappingException("property [" + propertyPath + "] not found on entity [" + this.getEntityName() + "]");
      }
   }

   private Property getProperty(String propertyName, Iterator iterator) throws MappingException {
      while(true) {
         if (iterator.hasNext()) {
            Property prop = (Property)iterator.next();
            if (!prop.getName().equals(StringHelper.root(propertyName))) {
               continue;
            }

            return prop;
         }

         throw new MappingException("property [" + propertyName + "] not found on entity [" + this.getEntityName() + "]");
      }
   }

   public Property getProperty(String propertyName) throws MappingException {
      Iterator iter = this.getPropertyClosureIterator();
      Property identifierProperty = this.getIdentifierProperty();
      return identifierProperty != null && identifierProperty.getName().equals(StringHelper.root(propertyName)) ? identifierProperty : this.getProperty(propertyName, iter);
   }

   public abstract int getOptimisticLockMode();

   public void setOptimisticLockMode(int optimisticLockMode) {
      this.optimisticLockMode = optimisticLockMode;
   }

   public void validate(Mapping mapping) throws MappingException {
      Iterator iter = this.getPropertyIterator();

      while(iter.hasNext()) {
         Property prop = (Property)iter.next();
         if (!prop.isValid(mapping)) {
            throw new MappingException("property mapping has wrong number of columns: " + StringHelper.qualify(this.getEntityName(), prop.getName()) + " type: " + prop.getType().getName());
         }
      }

      this.checkPropertyDuplication();
      this.checkColumnDuplication();
   }

   private void checkPropertyDuplication() throws MappingException {
      HashSet names = new HashSet();
      Iterator iter = this.getPropertyIterator();

      while(iter.hasNext()) {
         Property prop = (Property)iter.next();
         if (!names.add(prop.getName())) {
            throw new MappingException("Duplicate property mapping of " + prop.getName() + " found in " + this.getEntityName());
         }
      }

   }

   public boolean isDiscriminatorValueNotNull() {
      return "not null".equals(this.getDiscriminatorValue());
   }

   public boolean isDiscriminatorValueNull() {
      return "null".equals(this.getDiscriminatorValue());
   }

   public java.util.Map getMetaAttributes() {
      return this.metaAttributes;
   }

   public void setMetaAttributes(java.util.Map metas) {
      this.metaAttributes = metas;
   }

   public MetaAttribute getMetaAttribute(String name) {
      return this.metaAttributes == null ? null : (MetaAttribute)this.metaAttributes.get(name);
   }

   public String toString() {
      return this.getClass().getName() + '(' + this.getEntityName() + ')';
   }

   public Iterator getJoinIterator() {
      return this.joins.iterator();
   }

   public Iterator getJoinClosureIterator() {
      return this.joins.iterator();
   }

   public void addJoin(Join join) {
      this.joins.add(join);
      join.setPersistentClass(this);
   }

   public int getJoinClosureSpan() {
      return this.joins.size();
   }

   public int getPropertyClosureSpan() {
      int span = this.properties.size();

      for(int i = 0; i < this.joins.size(); ++i) {
         Join join = (Join)this.joins.get(i);
         span += join.getPropertySpan();
      }

      return span;
   }

   public int getJoinNumber(Property prop) {
      int result = 1;

      for(Iterator iter = this.getSubclassJoinClosureIterator(); iter.hasNext(); ++result) {
         Join join = (Join)iter.next();
         if (join.containsProperty(prop)) {
            return result;
         }
      }

      return 0;
   }

   public Iterator getPropertyIterator() {
      ArrayList iterators = new ArrayList();
      iterators.add(this.properties.iterator());

      for(int i = 0; i < this.joins.size(); ++i) {
         Join join = (Join)this.joins.get(i);
         iterators.add(join.getPropertyIterator());
      }

      return new JoinedIterator(iterators);
   }

   public Iterator getUnjoinedPropertyIterator() {
      return this.properties.iterator();
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

   public void addFilter(String name, String condition, boolean autoAliasInjection, java.util.Map aliasTableMap, java.util.Map aliasEntityMap) {
      this.filters.add(new FilterConfiguration(name, condition, autoAliasInjection, aliasTableMap, aliasEntityMap, this));
   }

   public java.util.List getFilters() {
      return this.filters;
   }

   public boolean isForceDiscriminator() {
      return false;
   }

   public abstract boolean isJoinedSubclass();

   public String getLoaderName() {
      return this.loaderName;
   }

   public void setLoaderName(String loaderName) {
      this.loaderName = loaderName == null ? null : loaderName.intern();
   }

   public abstract java.util.Set getSynchronizedTables();

   public void addSynchronizedTable(String table) {
      this.synchronizedTables.add(table);
   }

   public Boolean isAbstract() {
      return this.isAbstract;
   }

   public void setAbstract(Boolean isAbstract) {
      this.isAbstract = isAbstract;
   }

   protected void checkColumnDuplication(java.util.Set distinctColumns, Iterator columns) throws MappingException {
      while(true) {
         if (columns.hasNext()) {
            Selectable columnOrFormula = (Selectable)columns.next();
            if (columnOrFormula.isFormula()) {
               continue;
            }

            Column col = (Column)columnOrFormula;
            if (distinctColumns.add(col.getName())) {
               continue;
            }

            throw new MappingException("Repeated column in mapping for entity: " + this.getEntityName() + " column: " + col.getName() + " (should be mapped with insert=\"false\" update=\"false\")");
         }

         return;
      }
   }

   protected void checkPropertyColumnDuplication(java.util.Set distinctColumns, Iterator properties) throws MappingException {
      while(properties.hasNext()) {
         Property prop = (Property)properties.next();
         if (prop.getValue() instanceof Component) {
            Component component = (Component)prop.getValue();
            this.checkPropertyColumnDuplication(distinctColumns, component.getPropertyIterator());
         } else if (prop.isUpdateable() || prop.isInsertable()) {
            this.checkColumnDuplication(distinctColumns, prop.getColumnIterator());
         }
      }

   }

   protected Iterator getNonDuplicatedPropertyIterator() {
      return this.getUnjoinedPropertyIterator();
   }

   protected Iterator getDiscriminatorColumnIterator() {
      return EmptyIterator.INSTANCE;
   }

   protected void checkColumnDuplication() {
      HashSet cols = new HashSet();
      if (this.getIdentifierMapper() == null) {
         this.checkColumnDuplication(cols, this.getKey().getColumnIterator());
      }

      this.checkColumnDuplication(cols, this.getDiscriminatorColumnIterator());
      this.checkPropertyColumnDuplication(cols, this.getNonDuplicatedPropertyIterator());
      Iterator iter = this.getJoinIterator();

      while(iter.hasNext()) {
         cols.clear();
         Join join = (Join)iter.next();
         this.checkColumnDuplication(cols, join.getKey().getColumnIterator());
         this.checkPropertyColumnDuplication(cols, join.getPropertyIterator());
      }

   }

   public abstract Object accept(PersistentClassVisitor var1);

   public String getNodeName() {
      return this.nodeName;
   }

   public void setNodeName(String nodeName) {
      this.nodeName = nodeName;
   }

   public String getJpaEntityName() {
      return this.jpaEntityName;
   }

   public void setJpaEntityName(String jpaEntityName) {
      this.jpaEntityName = jpaEntityName;
   }

   public boolean hasPojoRepresentation() {
      return this.getClassName() != null;
   }

   public boolean hasDom4jRepresentation() {
      return this.getNodeName() != null;
   }

   public boolean hasSubselectLoadableCollections() {
      return this.hasSubselectLoadableCollections;
   }

   public void setSubselectLoadableCollections(boolean hasSubselectCollections) {
      this.hasSubselectLoadableCollections = hasSubselectCollections;
   }

   public void prepareTemporaryTables(Mapping mapping, Dialect dialect) {
      this.temporaryIdTableName = dialect.generateTemporaryTableName(this.getTable().getName());
      if (dialect.supportsTemporaryTables()) {
         Table table = new Table();
         table.setName(this.temporaryIdTableName);
         Iterator itr = this.getTable().getPrimaryKey().getColumnIterator();

         while(itr.hasNext()) {
            Column column = (Column)itr.next();
            table.addColumn(column.clone());
         }

         this.temporaryIdTableDDL = table.sqlTemporaryTableCreateString(dialect, mapping);
      }

   }

   public String getTemporaryIdTableName() {
      return this.temporaryIdTableName;
   }

   public String getTemporaryIdTableDDL() {
      return this.temporaryIdTableDDL;
   }

   public Component getIdentifierMapper() {
      return this.identifierMapper;
   }

   public Component getDeclaredIdentifierMapper() {
      return this.declaredIdentifierMapper;
   }

   public void setDeclaredIdentifierMapper(Component declaredIdentifierMapper) {
      this.declaredIdentifierMapper = declaredIdentifierMapper;
   }

   public boolean hasIdentifierMapper() {
      return this.identifierMapper != null;
   }

   public void setIdentifierMapper(Component handle) {
      this.identifierMapper = handle;
   }

   public void addTuplizer(EntityMode entityMode, String implClassName) {
      if (this.tuplizerImpls == null) {
         this.tuplizerImpls = new HashMap();
      }

      this.tuplizerImpls.put(entityMode, implClassName);
   }

   public String getTuplizerImplClassName(EntityMode mode) {
      return this.tuplizerImpls == null ? null : (String)this.tuplizerImpls.get(mode);
   }

   public java.util.Map getTuplizerMap() {
      return this.tuplizerImpls == null ? null : Collections.unmodifiableMap(this.tuplizerImpls);
   }

   public boolean hasNaturalId() {
      Iterator props = this.getRootClass().getPropertyIterator();

      while(props.hasNext()) {
         if (((Property)props.next()).isNaturalIdentifier()) {
            return true;
         }
      }

      return false;
   }

   public abstract boolean isLazyPropertiesCacheable();

   public Iterator getDeclaredPropertyIterator() {
      ArrayList iterators = new ArrayList();
      iterators.add(this.declaredProperties.iterator());

      for(int i = 0; i < this.joins.size(); ++i) {
         Join join = (Join)this.joins.get(i);
         iterators.add(join.getDeclaredPropertyIterator());
      }

      return new JoinedIterator(iterators);
   }

   public void addMappedsuperclassProperty(Property p) {
      this.properties.add(p);
      p.setPersistentClass(this);
   }

   public MappedSuperclass getSuperMappedSuperclass() {
      return this.superMappedSuperclass;
   }

   public void setSuperMappedSuperclass(MappedSuperclass superMappedSuperclass) {
      this.superMappedSuperclass = superMappedSuperclass;
   }
}
