package org.hibernate.cfg;

import java.util.HashMap;
import java.util.Map;
import javax.persistence.JoinTable;
import org.hibernate.annotations.common.AssertionFailure;
import org.hibernate.annotations.common.reflection.XClass;
import org.hibernate.cfg.annotations.EntityBinder;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.Join;
import org.hibernate.mapping.KeyValue;
import org.hibernate.mapping.MappedSuperclass;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.Table;

public class ClassPropertyHolder extends AbstractPropertyHolder {
   private PersistentClass persistentClass;
   private Map joins;
   private transient Map joinsPerRealTableName;
   private EntityBinder entityBinder;
   private final Map inheritanceStatePerClass;

   public ClassPropertyHolder(PersistentClass persistentClass, XClass clazzToProcess, Map joins, Mappings mappings, Map inheritanceStatePerClass) {
      super(persistentClass.getEntityName(), (PropertyHolder)null, clazzToProcess, mappings);
      this.persistentClass = persistentClass;
      this.joins = joins;
      this.inheritanceStatePerClass = inheritanceStatePerClass;
   }

   public ClassPropertyHolder(PersistentClass persistentClass, XClass clazzToProcess, EntityBinder entityBinder, Mappings mappings, Map inheritanceStatePerClass) {
      this(persistentClass, clazzToProcess, entityBinder.getSecondaryTables(), mappings, inheritanceStatePerClass);
      this.entityBinder = entityBinder;
   }

   public String getEntityName() {
      return this.persistentClass.getEntityName();
   }

   public void addProperty(Property prop, Ejb3Column[] columns, XClass declaringClass) {
      if (columns != null && columns[0].isSecondary()) {
         Join join = columns[0].getJoin();
         this.addPropertyToJoin(prop, declaringClass, join);
      } else {
         this.addProperty(prop, declaringClass);
      }

   }

   public void addProperty(Property prop, XClass declaringClass) {
      if (prop.getValue() instanceof Component) {
         String tableName = prop.getValue().getTable().getName();
         if (this.getJoinsPerRealTableName().containsKey(tableName)) {
            Join join = (Join)this.getJoinsPerRealTableName().get(tableName);
            this.addPropertyToJoin(prop, declaringClass, join);
         } else {
            this.addPropertyToPersistentClass(prop, declaringClass);
         }
      } else {
         this.addPropertyToPersistentClass(prop, declaringClass);
      }

   }

   public Join addJoin(JoinTable joinTableAnn, boolean noDelayInPkColumnCreation) {
      Join join = this.entityBinder.addJoin(joinTableAnn, this, noDelayInPkColumnCreation);
      this.joins = this.entityBinder.getSecondaryTables();
      return join;
   }

   private void addPropertyToPersistentClass(Property prop, XClass declaringClass) {
      if (declaringClass != null) {
         InheritanceState inheritanceState = (InheritanceState)this.inheritanceStatePerClass.get(declaringClass);
         if (inheritanceState == null) {
            throw new AssertionFailure("Declaring class is not found in the inheritance state hierarchy: " + declaringClass);
         }

         if (inheritanceState.isEmbeddableSuperclass()) {
            this.persistentClass.addMappedsuperclassProperty(prop);
            this.addPropertyToMappedSuperclass(prop, declaringClass);
         } else {
            this.persistentClass.addProperty(prop);
         }
      } else {
         this.persistentClass.addProperty(prop);
      }

   }

   private void addPropertyToMappedSuperclass(Property prop, XClass declaringClass) {
      Mappings mappings = this.getMappings();
      Class type = mappings.getReflectionManager().toClass(declaringClass);
      MappedSuperclass superclass = mappings.getMappedSuperclass(type);
      superclass.addDeclaredProperty(prop);
   }

   private void addPropertyToJoin(Property prop, XClass declaringClass, Join join) {
      if (declaringClass != null) {
         InheritanceState inheritanceState = (InheritanceState)this.inheritanceStatePerClass.get(declaringClass);
         if (inheritanceState == null) {
            throw new AssertionFailure("Declaring class is not found in the inheritance state hierarchy: " + declaringClass);
         }

         if (inheritanceState.isEmbeddableSuperclass()) {
            join.addMappedsuperclassProperty(prop);
            this.addPropertyToMappedSuperclass(prop, declaringClass);
         } else {
            join.addProperty(prop);
         }
      } else {
         join.addProperty(prop);
      }

   }

   private Map getJoinsPerRealTableName() {
      if (this.joinsPerRealTableName == null) {
         this.joinsPerRealTableName = new HashMap(this.joins.size());

         for(Join join : this.joins.values()) {
            this.joinsPerRealTableName.put(join.getTable().getName(), join);
         }
      }

      return this.joinsPerRealTableName;
   }

   public String getClassName() {
      return this.persistentClass.getClassName();
   }

   public String getEntityOwnerClassName() {
      return this.getClassName();
   }

   public Table getTable() {
      return this.persistentClass.getTable();
   }

   public boolean isComponent() {
      return false;
   }

   public boolean isEntity() {
      return true;
   }

   public PersistentClass getPersistentClass() {
      return this.persistentClass;
   }

   public KeyValue getIdentifier() {
      return this.persistentClass.getIdentifier();
   }

   public boolean isOrWithinEmbeddedId() {
      return false;
   }
}
