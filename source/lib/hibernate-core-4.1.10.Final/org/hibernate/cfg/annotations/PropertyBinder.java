package org.hibernate.cfg.annotations;

import java.util.Map;
import javax.persistence.EmbeddedId;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Version;
import org.hibernate.AnnotationException;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.OptimisticLock;
import org.hibernate.annotations.common.AssertionFailure;
import org.hibernate.annotations.common.reflection.XClass;
import org.hibernate.annotations.common.reflection.XProperty;
import org.hibernate.cfg.AccessType;
import org.hibernate.cfg.AnnotationBinder;
import org.hibernate.cfg.BinderHelper;
import org.hibernate.cfg.Ejb3Column;
import org.hibernate.cfg.InheritanceState;
import org.hibernate.cfg.Mappings;
import org.hibernate.cfg.PropertyHolder;
import org.hibernate.cfg.PropertyPreloadedData;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.KeyValue;
import org.hibernate.mapping.MappedSuperclass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.PropertyGeneration;
import org.hibernate.mapping.RootClass;
import org.hibernate.mapping.SimpleValue;
import org.hibernate.mapping.ToOne;
import org.hibernate.mapping.Value;
import org.jboss.logging.Logger;

public class PropertyBinder {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, PropertyBinder.class.getName());
   private String name;
   private String returnedClassName;
   private boolean lazy;
   private AccessType accessType;
   private Ejb3Column[] columns;
   private PropertyHolder holder;
   private Mappings mappings;
   private Value value;
   private boolean insertable = true;
   private boolean updatable = true;
   private String cascade;
   private SimpleValueBinder simpleValueBinder;
   private XClass declaringClass;
   private boolean declaringClassSet;
   private boolean embedded;
   private EntityBinder entityBinder;
   private boolean isXToMany;
   private String referencedEntityName;
   private XProperty property;
   private XClass returnedClass;
   private boolean isId;
   private Map inheritanceStatePerClass;
   private Property mappingProperty;

   public void setReferencedEntityName(String referencedEntityName) {
      this.referencedEntityName = referencedEntityName;
   }

   public void setEmbedded(boolean embedded) {
      this.embedded = embedded;
   }

   public void setEntityBinder(EntityBinder entityBinder) {
      this.entityBinder = entityBinder;
   }

   public void setInsertable(boolean insertable) {
      this.insertable = insertable;
   }

   public void setUpdatable(boolean updatable) {
      this.updatable = updatable;
   }

   public void setName(String name) {
      this.name = name;
   }

   public void setReturnedClassName(String returnedClassName) {
      this.returnedClassName = returnedClassName;
   }

   public void setLazy(boolean lazy) {
      this.lazy = lazy;
   }

   public void setAccessType(AccessType accessType) {
      this.accessType = accessType;
   }

   public void setColumns(Ejb3Column[] columns) {
      this.insertable = columns[0].isInsertable();
      this.updatable = columns[0].isUpdatable();
      this.columns = columns;
   }

   public void setHolder(PropertyHolder holder) {
      this.holder = holder;
   }

   public void setValue(Value value) {
      this.value = value;
   }

   public void setCascade(String cascadeStrategy) {
      this.cascade = cascadeStrategy;
   }

   public void setMappings(Mappings mappings) {
      this.mappings = mappings;
   }

   public void setDeclaringClass(XClass declaringClass) {
      this.declaringClass = declaringClass;
      this.declaringClassSet = true;
   }

   private void validateBind() {
      if (this.property.isAnnotationPresent(Immutable.class)) {
         throw new AnnotationException("@Immutable on property not allowed. Only allowed on entity level or on a collection.");
      } else if (!this.declaringClassSet) {
         throw new AssertionFailure("declaringClass has not been set before a bind");
      }
   }

   private void validateMake() {
   }

   private Property makePropertyAndValue() {
      this.validateBind();
      LOG.debugf("MetadataSourceProcessor property %s with lazy=%s", this.name, this.lazy);
      String containerClassName = this.holder == null ? null : this.holder.getClassName();
      this.simpleValueBinder = new SimpleValueBinder();
      this.simpleValueBinder.setMappings(this.mappings);
      this.simpleValueBinder.setPropertyName(this.name);
      this.simpleValueBinder.setReturnedClassName(this.returnedClassName);
      this.simpleValueBinder.setColumns(this.columns);
      this.simpleValueBinder.setPersistentClassName(containerClassName);
      this.simpleValueBinder.setType(this.property, this.returnedClass, containerClassName);
      this.simpleValueBinder.setMappings(this.mappings);
      this.simpleValueBinder.setReferencedEntityName(this.referencedEntityName);
      this.simpleValueBinder.setAccessType(this.accessType);
      SimpleValue propertyValue = this.simpleValueBinder.make();
      this.setValue(propertyValue);
      return this.makeProperty();
   }

   public Property makePropertyAndBind() {
      return this.bind(this.makeProperty());
   }

   public Property makePropertyValueAndBind() {
      return this.bind(this.makePropertyAndValue());
   }

   public void setXToMany(boolean xToMany) {
      this.isXToMany = xToMany;
   }

   private Property bind(Property prop) {
      if (this.isId) {
         RootClass rootClass = (RootClass)this.holder.getPersistentClass();
         if (!this.isXToMany && !this.entityBinder.wrapIdsInEmbeddedComponents()) {
            rootClass.setIdentifier((KeyValue)this.getValue());
            if (this.embedded) {
               rootClass.setEmbeddedIdentifier(true);
            } else {
               rootClass.setIdentifierProperty(prop);
               MappedSuperclass superclass = BinderHelper.getMappedSuperclassOrNull(this.declaringClass, this.inheritanceStatePerClass, this.mappings);
               if (superclass != null) {
                  superclass.setDeclaredIdentifierProperty(prop);
               } else {
                  rootClass.setDeclaredIdentifierProperty(prop);
               }
            }
         } else {
            Component identifier = (Component)rootClass.getIdentifier();
            if (identifier == null) {
               identifier = AnnotationBinder.createComponent(this.holder, new PropertyPreloadedData((AccessType)null, (String)null, (XClass)null), true, false, this.mappings);
               rootClass.setIdentifier(identifier);
               identifier.setNullValue("undefined");
               rootClass.setEmbeddedIdentifier(true);
               rootClass.setIdentifierMapper(identifier);
            }

            identifier.addProperty(prop);
         }
      } else {
         this.holder.addProperty(prop, this.columns, this.declaringClass);
      }

      return prop;
   }

   public Property makeProperty() {
      this.validateMake();
      LOG.debugf("Building property %s", this.name);
      Property prop = new Property();
      prop.setName(this.name);
      prop.setNodeName(this.name);
      prop.setValue(this.value);
      prop.setLazy(this.lazy);
      prop.setCascade(this.cascade);
      prop.setPropertyAccessorName(this.accessType.getType());
      Generated ann = this.property != null ? (Generated)this.property.getAnnotation(Generated.class) : null;
      GenerationTime generated = ann != null ? ann.value() : null;
      if (generated != null && !GenerationTime.NEVER.equals(generated)) {
         if (this.property.isAnnotationPresent(Version.class) && GenerationTime.INSERT.equals(generated)) {
            throw new AnnotationException("@Generated(INSERT) on a @Version property not allowed, use ALWAYS: " + StringHelper.qualify(this.holder.getPath(), this.name));
         }

         this.insertable = false;
         if (GenerationTime.ALWAYS.equals(generated)) {
            this.updatable = false;
         }

         prop.setGeneration(PropertyGeneration.parse(generated.toString().toLowerCase()));
      }

      NaturalId naturalId = this.property != null ? (NaturalId)this.property.getAnnotation(NaturalId.class) : null;
      if (naturalId != null) {
         if (!this.entityBinder.isRootEntity()) {
            throw new AnnotationException("@NaturalId only valid on root entity (or its @MappedSuperclasses)");
         }

         if (!naturalId.mutable()) {
            this.updatable = false;
         }

         prop.setNaturalIdentifier(true);
      }

      Lob lob = this.property != null ? (Lob)this.property.getAnnotation(Lob.class) : null;
      prop.setLob(lob != null);
      prop.setInsertable(this.insertable);
      prop.setUpdateable(this.updatable);
      if (Collection.class.isInstance(this.value)) {
         prop.setOptimisticLocked(((Collection)this.value).isOptimisticLocked());
      } else {
         OptimisticLock lockAnn = this.property != null ? (OptimisticLock)this.property.getAnnotation(OptimisticLock.class) : null;
         if (lockAnn != null && lockAnn.excluded() && (this.property.isAnnotationPresent(Version.class) || this.property.isAnnotationPresent(Id.class) || this.property.isAnnotationPresent(EmbeddedId.class))) {
            throw new AnnotationException("@OptimisticLock.exclude=true incompatible with @Id, @EmbeddedId and @Version: " + StringHelper.qualify(this.holder.getPath(), this.name));
         }

         boolean isOwnedValue = !this.isToOneValue(this.value) || this.insertable;
         boolean includeInOptimisticLockChecks = lockAnn != null ? !lockAnn.excluded() : isOwnedValue;
         prop.setOptimisticLocked(includeInOptimisticLockChecks);
      }

      LOG.tracev("Cascading {0} with {1}", this.name, this.cascade);
      this.mappingProperty = prop;
      return prop;
   }

   private boolean isCollection(Value value) {
      return Collection.class.isInstance(value);
   }

   private boolean isToOneValue(Value value) {
      return ToOne.class.isInstance(value);
   }

   public void setProperty(XProperty property) {
      this.property = property;
   }

   public void setReturnedClass(XClass returnedClass) {
      this.returnedClass = returnedClass;
   }

   public SimpleValueBinder getSimpleValueBinder() {
      return this.simpleValueBinder;
   }

   public Value getValue() {
      return this.value;
   }

   public void setId(boolean id) {
      this.isId = id;
   }

   public void setInheritanceStatePerClass(Map inheritanceStatePerClass) {
      this.inheritanceStatePerClass = inheritanceStatePerClass;
   }
}
