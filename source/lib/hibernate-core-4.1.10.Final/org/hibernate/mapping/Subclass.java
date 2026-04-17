package org.hibernate.mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import org.hibernate.AssertionFailure;
import org.hibernate.EntityMode;
import org.hibernate.internal.util.collections.JoinedIterator;
import org.hibernate.internal.util.collections.SingletonIterator;

public class Subclass extends PersistentClass {
   private PersistentClass superclass;
   private Class classPersisterClass;
   private final int subclassId;

   public Subclass(PersistentClass superclass) {
      this.superclass = superclass;
      this.subclassId = superclass.nextSubclassId();
   }

   int nextSubclassId() {
      return this.getSuperclass().nextSubclassId();
   }

   public int getSubclassId() {
      return this.subclassId;
   }

   public String getNaturalIdCacheRegionName() {
      return this.getSuperclass().getNaturalIdCacheRegionName();
   }

   public String getCacheConcurrencyStrategy() {
      return this.getSuperclass().getCacheConcurrencyStrategy();
   }

   public RootClass getRootClass() {
      return this.getSuperclass().getRootClass();
   }

   public PersistentClass getSuperclass() {
      return this.superclass;
   }

   public Property getIdentifierProperty() {
      return this.getSuperclass().getIdentifierProperty();
   }

   public Property getDeclaredIdentifierProperty() {
      return null;
   }

   public KeyValue getIdentifier() {
      return this.getSuperclass().getIdentifier();
   }

   public boolean hasIdentifierProperty() {
      return this.getSuperclass().hasIdentifierProperty();
   }

   public Value getDiscriminator() {
      return this.getSuperclass().getDiscriminator();
   }

   public boolean isMutable() {
      return this.getSuperclass().isMutable();
   }

   public boolean isInherited() {
      return true;
   }

   public boolean isPolymorphic() {
      return true;
   }

   public void addProperty(Property p) {
      super.addProperty(p);
      this.getSuperclass().addSubclassProperty(p);
   }

   public void addMappedsuperclassProperty(Property p) {
      super.addMappedsuperclassProperty(p);
      this.getSuperclass().addSubclassProperty(p);
   }

   public void addJoin(Join j) {
      super.addJoin(j);
      this.getSuperclass().addSubclassJoin(j);
   }

   public Iterator getPropertyClosureIterator() {
      return new JoinedIterator(this.getSuperclass().getPropertyClosureIterator(), this.getPropertyIterator());
   }

   public Iterator getTableClosureIterator() {
      return new JoinedIterator(this.getSuperclass().getTableClosureIterator(), new SingletonIterator(this.getTable()));
   }

   public Iterator getKeyClosureIterator() {
      return new JoinedIterator(this.getSuperclass().getKeyClosureIterator(), new SingletonIterator(this.getKey()));
   }

   protected void addSubclassProperty(Property p) {
      super.addSubclassProperty(p);
      this.getSuperclass().addSubclassProperty(p);
   }

   protected void addSubclassJoin(Join j) {
      super.addSubclassJoin(j);
      this.getSuperclass().addSubclassJoin(j);
   }

   protected void addSubclassTable(Table table) {
      super.addSubclassTable(table);
      this.getSuperclass().addSubclassTable(table);
   }

   public boolean isVersioned() {
      return this.getSuperclass().isVersioned();
   }

   public Property getVersion() {
      return this.getSuperclass().getVersion();
   }

   public Property getDeclaredVersion() {
      return null;
   }

   public boolean hasEmbeddedIdentifier() {
      return this.getSuperclass().hasEmbeddedIdentifier();
   }

   public Class getEntityPersisterClass() {
      return this.classPersisterClass == null ? this.getSuperclass().getEntityPersisterClass() : this.classPersisterClass;
   }

   public Table getRootTable() {
      return this.getSuperclass().getRootTable();
   }

   public KeyValue getKey() {
      return this.getSuperclass().getIdentifier();
   }

   public boolean isExplicitPolymorphism() {
      return this.getSuperclass().isExplicitPolymorphism();
   }

   public void setSuperclass(PersistentClass superclass) {
      this.superclass = superclass;
   }

   public String getWhere() {
      return this.getSuperclass().getWhere();
   }

   public boolean isJoinedSubclass() {
      return this.getTable() != this.getRootTable();
   }

   public void createForeignKey() {
      if (!this.isJoinedSubclass()) {
         throw new AssertionFailure("not a joined-subclass");
      } else {
         this.getKey().createForeignKeyOfEntity(this.getSuperclass().getEntityName());
      }
   }

   public void setEntityPersisterClass(Class classPersisterClass) {
      this.classPersisterClass = classPersisterClass;
   }

   public boolean isLazyPropertiesCacheable() {
      return this.getSuperclass().isLazyPropertiesCacheable();
   }

   public int getJoinClosureSpan() {
      return this.getSuperclass().getJoinClosureSpan() + super.getJoinClosureSpan();
   }

   public int getPropertyClosureSpan() {
      return this.getSuperclass().getPropertyClosureSpan() + super.getPropertyClosureSpan();
   }

   public Iterator getJoinClosureIterator() {
      return new JoinedIterator(this.getSuperclass().getJoinClosureIterator(), super.getJoinClosureIterator());
   }

   public boolean isClassOrSuperclassJoin(Join join) {
      return super.isClassOrSuperclassJoin(join) || this.getSuperclass().isClassOrSuperclassJoin(join);
   }

   public boolean isClassOrSuperclassTable(Table table) {
      return super.isClassOrSuperclassTable(table) || this.getSuperclass().isClassOrSuperclassTable(table);
   }

   public Table getTable() {
      return this.getSuperclass().getTable();
   }

   public boolean isForceDiscriminator() {
      return this.getSuperclass().isForceDiscriminator();
   }

   public boolean isDiscriminatorInsertable() {
      return this.getSuperclass().isDiscriminatorInsertable();
   }

   public java.util.Set getSynchronizedTables() {
      HashSet result = new HashSet();
      result.addAll(this.synchronizedTables);
      result.addAll(this.getSuperclass().getSynchronizedTables());
      return result;
   }

   public Object accept(PersistentClassVisitor mv) {
      return mv.accept(this);
   }

   public java.util.List getFilters() {
      java.util.List filters = new ArrayList(super.getFilters());
      filters.addAll(this.getSuperclass().getFilters());
      return filters;
   }

   public boolean hasSubselectLoadableCollections() {
      return super.hasSubselectLoadableCollections() || this.getSuperclass().hasSubselectLoadableCollections();
   }

   public String getTuplizerImplClassName(EntityMode mode) {
      String impl = super.getTuplizerImplClassName(mode);
      if (impl == null) {
         impl = this.getSuperclass().getTuplizerImplClassName(mode);
      }

      return impl;
   }

   public java.util.Map getTuplizerMap() {
      java.util.Map specificTuplizerDefs = super.getTuplizerMap();
      java.util.Map superclassTuplizerDefs = this.getSuperclass().getTuplizerMap();
      if (specificTuplizerDefs == null && superclassTuplizerDefs == null) {
         return null;
      } else {
         java.util.Map combined = new HashMap();
         if (superclassTuplizerDefs != null) {
            combined.putAll(superclassTuplizerDefs);
         }

         if (specificTuplizerDefs != null) {
            combined.putAll(specificTuplizerDefs);
         }

         return Collections.unmodifiableMap(combined);
      }
   }

   public Component getIdentifierMapper() {
      return this.superclass.getIdentifierMapper();
   }

   public int getOptimisticLockMode() {
      return this.superclass.getOptimisticLockMode();
   }
}
