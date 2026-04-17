package org.hibernate.mapping;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.internal.util.collections.SingletonIterator;
import org.jboss.logging.Logger;

public class RootClass extends PersistentClass implements TableOwner {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, RootClass.class.getName());
   public static final String DEFAULT_IDENTIFIER_COLUMN_NAME = "id";
   public static final String DEFAULT_DISCRIMINATOR_COLUMN_NAME = "class";
   private Property identifierProperty;
   private KeyValue identifier;
   private Property version;
   private boolean polymorphic;
   private String cacheConcurrencyStrategy;
   private String cacheRegionName;
   private String naturalIdCacheRegionName;
   private boolean lazyPropertiesCacheable = true;
   private Value discriminator;
   private boolean mutable = true;
   private boolean embeddedIdentifier = false;
   private boolean explicitPolymorphism;
   private Class entityPersisterClass;
   private boolean forceDiscriminator = false;
   private String where;
   private Table table;
   private boolean discriminatorInsertable = true;
   private int nextSubclassId = 0;
   private Property declaredIdentifierProperty;
   private Property declaredVersion;

   int nextSubclassId() {
      return ++this.nextSubclassId;
   }

   public int getSubclassId() {
      return 0;
   }

   public void setTable(Table table) {
      this.table = table;
   }

   public Table getTable() {
      return this.table;
   }

   public Property getIdentifierProperty() {
      return this.identifierProperty;
   }

   public Property getDeclaredIdentifierProperty() {
      return this.declaredIdentifierProperty;
   }

   public void setDeclaredIdentifierProperty(Property declaredIdentifierProperty) {
      this.declaredIdentifierProperty = declaredIdentifierProperty;
   }

   public KeyValue getIdentifier() {
      return this.identifier;
   }

   public boolean hasIdentifierProperty() {
      return this.identifierProperty != null;
   }

   public Value getDiscriminator() {
      return this.discriminator;
   }

   public boolean isInherited() {
      return false;
   }

   public boolean isPolymorphic() {
      return this.polymorphic;
   }

   public void setPolymorphic(boolean polymorphic) {
      this.polymorphic = polymorphic;
   }

   public RootClass getRootClass() {
      return this;
   }

   public Iterator getPropertyClosureIterator() {
      return this.getPropertyIterator();
   }

   public Iterator getTableClosureIterator() {
      return new SingletonIterator(this.getTable());
   }

   public Iterator getKeyClosureIterator() {
      return new SingletonIterator(this.getKey());
   }

   public void addSubclass(Subclass subclass) throws MappingException {
      super.addSubclass(subclass);
      this.setPolymorphic(true);
   }

   public boolean isExplicitPolymorphism() {
      return this.explicitPolymorphism;
   }

   public Property getVersion() {
      return this.version;
   }

   public Property getDeclaredVersion() {
      return this.declaredVersion;
   }

   public void setDeclaredVersion(Property declaredVersion) {
      this.declaredVersion = declaredVersion;
   }

   public void setVersion(Property version) {
      this.version = version;
   }

   public boolean isVersioned() {
      return this.version != null;
   }

   public boolean isMutable() {
      return this.mutable;
   }

   public boolean hasEmbeddedIdentifier() {
      return this.embeddedIdentifier;
   }

   public Class getEntityPersisterClass() {
      return this.entityPersisterClass;
   }

   public Table getRootTable() {
      return this.getTable();
   }

   public void setEntityPersisterClass(Class persister) {
      this.entityPersisterClass = persister;
   }

   public PersistentClass getSuperclass() {
      return null;
   }

   public KeyValue getKey() {
      return this.getIdentifier();
   }

   public void setDiscriminator(Value discriminator) {
      this.discriminator = discriminator;
   }

   public void setEmbeddedIdentifier(boolean embeddedIdentifier) {
      this.embeddedIdentifier = embeddedIdentifier;
   }

   public void setExplicitPolymorphism(boolean explicitPolymorphism) {
      this.explicitPolymorphism = explicitPolymorphism;
   }

   public void setIdentifier(KeyValue identifier) {
      this.identifier = identifier;
   }

   public void setIdentifierProperty(Property identifierProperty) {
      this.identifierProperty = identifierProperty;
      identifierProperty.setPersistentClass(this);
   }

   public void setMutable(boolean mutable) {
      this.mutable = mutable;
   }

   public boolean isDiscriminatorInsertable() {
      return this.discriminatorInsertable;
   }

   public void setDiscriminatorInsertable(boolean insertable) {
      this.discriminatorInsertable = insertable;
   }

   public boolean isForceDiscriminator() {
      return this.forceDiscriminator;
   }

   public void setForceDiscriminator(boolean forceDiscriminator) {
      this.forceDiscriminator = forceDiscriminator;
   }

   public String getWhere() {
      return this.where;
   }

   public void setWhere(String string) {
      this.where = string;
   }

   public void validate(Mapping mapping) throws MappingException {
      super.validate(mapping);
      if (!this.getIdentifier().isValid(mapping)) {
         throw new MappingException("identifier mapping has wrong number of columns: " + this.getEntityName() + " type: " + this.getIdentifier().getType().getName());
      } else {
         this.checkCompositeIdentifier();
      }
   }

   private void checkCompositeIdentifier() {
      if (this.getIdentifier() instanceof Component) {
         Component id = (Component)this.getIdentifier();
         if (!id.isDynamic()) {
            Class idClass = id.getComponentClass();
            String idComponendClassName = idClass.getName();
            if (idClass != null && !ReflectHelper.overridesEquals(idClass)) {
               LOG.compositeIdClassDoesNotOverrideEquals(idComponendClassName);
            }

            if (!ReflectHelper.overridesHashCode(idClass)) {
               LOG.compositeIdClassDoesNotOverrideHashCode(idComponendClassName);
            }

            if (!Serializable.class.isAssignableFrom(idClass)) {
               throw new MappingException("Composite-id class must implement Serializable: " + idComponendClassName);
            }
         }
      }

   }

   public String getCacheConcurrencyStrategy() {
      return this.cacheConcurrencyStrategy;
   }

   public void setCacheConcurrencyStrategy(String cacheConcurrencyStrategy) {
      this.cacheConcurrencyStrategy = cacheConcurrencyStrategy;
   }

   public String getCacheRegionName() {
      return this.cacheRegionName == null ? this.getEntityName() : this.cacheRegionName;
   }

   public void setCacheRegionName(String cacheRegionName) {
      this.cacheRegionName = cacheRegionName;
   }

   public String getNaturalIdCacheRegionName() {
      return this.naturalIdCacheRegionName;
   }

   public void setNaturalIdCacheRegionName(String naturalIdCacheRegionName) {
      this.naturalIdCacheRegionName = naturalIdCacheRegionName;
   }

   public boolean isLazyPropertiesCacheable() {
      return this.lazyPropertiesCacheable;
   }

   public void setLazyPropertiesCacheable(boolean lazyPropertiesCacheable) {
      this.lazyPropertiesCacheable = lazyPropertiesCacheable;
   }

   public boolean isJoinedSubclass() {
      return false;
   }

   public java.util.Set getSynchronizedTables() {
      return this.synchronizedTables;
   }

   public java.util.Set getIdentityTables() {
      java.util.Set tables = new HashSet();
      Iterator iter = this.getSubclassClosureIterator();

      while(iter.hasNext()) {
         PersistentClass clazz = (PersistentClass)iter.next();
         if (clazz.isAbstract() == null || !clazz.isAbstract()) {
            tables.add(clazz.getIdentityTable());
         }
      }

      return tables;
   }

   public Object accept(PersistentClassVisitor mv) {
      return mv.accept(this);
   }

   public int getOptimisticLockMode() {
      return this.optimisticLockMode;
   }
}
