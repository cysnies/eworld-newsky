package org.hibernate.cfg;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import javax.persistence.Access;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import org.hibernate.AnnotationException;
import org.hibernate.MappingException;
import org.hibernate.annotations.Any;
import org.hibernate.annotations.ManyToAny;
import org.hibernate.annotations.Target;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.common.reflection.XClass;
import org.hibernate.annotations.common.reflection.XProperty;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.StringHelper;
import org.jboss.logging.Logger;

class PropertyContainer {
   private static final CoreMessageLogger LOG;
   private final AccessType explicitClassDefinedAccessType;
   private final TreeMap fieldAccessMap;
   private final TreeMap propertyAccessMap;
   private final XClass xClass;
   private final XClass entityAtStake;

   PropertyContainer(XClass clazz, XClass entityAtStake) {
      this.xClass = clazz;
      this.entityAtStake = entityAtStake;
      this.explicitClassDefinedAccessType = this.determineClassDefinedAccessStrategy();
      this.fieldAccessMap = this.initProperties(AccessType.FIELD);
      this.propertyAccessMap = this.initProperties(AccessType.PROPERTY);
      this.considerExplicitFieldAndPropertyAccess();
   }

   public XClass getEntityAtStake() {
      return this.entityAtStake;
   }

   public XClass getDeclaringClass() {
      return this.xClass;
   }

   public AccessType getExplicitAccessStrategy() {
      return this.explicitClassDefinedAccessType;
   }

   public boolean hasExplicitAccessStrategy() {
      return !this.explicitClassDefinedAccessType.equals(AccessType.DEFAULT);
   }

   public Collection getProperties(AccessType accessType) {
      this.assertTypesAreResolvable(accessType);
      return AccessType.DEFAULT != accessType && AccessType.PROPERTY != accessType ? Collections.unmodifiableCollection(this.fieldAccessMap.values()) : Collections.unmodifiableCollection(this.propertyAccessMap.values());
   }

   private void assertTypesAreResolvable(AccessType access) {
      Map<String, XProperty> xprops;
      if (!AccessType.PROPERTY.equals(access) && !AccessType.DEFAULT.equals(access)) {
         xprops = this.fieldAccessMap;
      } else {
         xprops = this.propertyAccessMap;
      }

      for(XProperty property : xprops.values()) {
         if (!property.isTypeResolved() && !discoverTypeWithoutReflection(property)) {
            String msg = "Property " + StringHelper.qualify(this.xClass.getName(), property.getName()) + " has an unbound type and no explicit target entity. Resolve this Generic usage issue" + " or set an explicit target attribute (eg @OneToMany(target=) or use an explicit @Type";
            throw new AnnotationException(msg);
         }
      }

   }

   private void considerExplicitFieldAndPropertyAccess() {
      for(XProperty property : this.fieldAccessMap.values()) {
         Access access = (Access)property.getAnnotation(Access.class);
         if (access != null) {
            AccessType accessType = AccessType.getAccessStrategy(access.value());
            if (accessType == AccessType.FIELD) {
               this.propertyAccessMap.put(property.getName(), property);
            } else {
               LOG.debug("Placing @Access(AccessType.FIELD) on a field does not have any effect.");
            }
         }
      }

      for(XProperty property : this.propertyAccessMap.values()) {
         Access access = (Access)property.getAnnotation(Access.class);
         if (access != null) {
            AccessType accessType = AccessType.getAccessStrategy(access.value());
            if (accessType == AccessType.PROPERTY) {
               this.fieldAccessMap.put(property.getName(), property);
            } else {
               LOG.debug("Placing @Access(AccessType.PROPERTY) on a field does not have any effect.");
            }
         }
      }

   }

   private TreeMap initProperties(AccessType access) {
      if (!AccessType.PROPERTY.equals(access) && !AccessType.FIELD.equals(access)) {
         throw new IllegalArgumentException("Access type has to be AccessType.FIELD or AccessType.Property");
      } else {
         TreeMap<String, XProperty> propertiesMap = new TreeMap();

         for(XProperty property : this.xClass.getDeclaredProperties(access.getType())) {
            if (!mustBeSkipped(property)) {
               propertiesMap.put(property.getName(), property);
            }
         }

         return propertiesMap;
      }
   }

   private AccessType determineClassDefinedAccessStrategy() {
      AccessType hibernateDefinedAccessType = AccessType.DEFAULT;
      AccessType jpaDefinedAccessType = AccessType.DEFAULT;
      org.hibernate.annotations.AccessType accessType = (org.hibernate.annotations.AccessType)this.xClass.getAnnotation(org.hibernate.annotations.AccessType.class);
      if (accessType != null) {
         hibernateDefinedAccessType = AccessType.getAccessStrategy(accessType.value());
      }

      Access access = (Access)this.xClass.getAnnotation(Access.class);
      if (access != null) {
         jpaDefinedAccessType = AccessType.getAccessStrategy(access.value());
      }

      if (hibernateDefinedAccessType != AccessType.DEFAULT && jpaDefinedAccessType != AccessType.DEFAULT && hibernateDefinedAccessType != jpaDefinedAccessType) {
         throw new MappingException("@AccessType and @Access specified with contradicting values. Use of @Access only is recommended. ");
      } else {
         AccessType classDefinedAccessType;
         if (hibernateDefinedAccessType != AccessType.DEFAULT) {
            classDefinedAccessType = hibernateDefinedAccessType;
         } else {
            classDefinedAccessType = jpaDefinedAccessType;
         }

         return classDefinedAccessType;
      }
   }

   private static boolean discoverTypeWithoutReflection(XProperty p) {
      if (p.isAnnotationPresent(OneToOne.class) && !((OneToOne)p.getAnnotation(OneToOne.class)).targetEntity().equals(Void.TYPE)) {
         return true;
      } else if (p.isAnnotationPresent(OneToMany.class) && !((OneToMany)p.getAnnotation(OneToMany.class)).targetEntity().equals(Void.TYPE)) {
         return true;
      } else if (p.isAnnotationPresent(ManyToOne.class) && !((ManyToOne)p.getAnnotation(ManyToOne.class)).targetEntity().equals(Void.TYPE)) {
         return true;
      } else if (p.isAnnotationPresent(ManyToMany.class) && !((ManyToMany)p.getAnnotation(ManyToMany.class)).targetEntity().equals(Void.TYPE)) {
         return true;
      } else if (p.isAnnotationPresent(Any.class)) {
         return true;
      } else if (p.isAnnotationPresent(ManyToAny.class)) {
         if (!p.isCollection() && !p.isArray()) {
            throw new AnnotationException("@ManyToAny used on a non collection non array property: " + p.getName());
         } else {
            return true;
         }
      } else if (p.isAnnotationPresent(Type.class)) {
         return true;
      } else {
         return p.isAnnotationPresent(Target.class);
      }
   }

   private static boolean mustBeSkipped(XProperty property) {
      return property.isAnnotationPresent(Transient.class) || "net.sf.cglib.transform.impl.InterceptFieldCallback".equals(property.getType().getName()) || "org.hibernate.bytecode.internal.javassist.FieldHandler".equals(property.getType().getName());
   }

   static {
      System.setProperty("jboss.i18n.generate-proxies", "true");
      LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, PropertyContainer.class.getName());
   }
}
