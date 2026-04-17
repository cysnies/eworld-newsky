package org.hibernate.cfg;

import javax.persistence.Access;
import org.hibernate.MappingException;
import org.hibernate.annotations.Target;
import org.hibernate.annotations.common.reflection.ReflectionManager;
import org.hibernate.annotations.common.reflection.XClass;
import org.hibernate.annotations.common.reflection.XProperty;

public class PropertyInferredData implements PropertyData {
   private final AccessType defaultAccess;
   private final XProperty property;
   private final ReflectionManager reflectionManager;
   private final XClass declaringClass;

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("PropertyInferredData");
      sb.append("{property=").append(this.property);
      sb.append(", declaringClass=").append(this.declaringClass);
      sb.append('}');
      return sb.toString();
   }

   public PropertyInferredData(XClass declaringClass, XProperty property, String propertyAccessor, ReflectionManager reflectionManager) {
      this.declaringClass = declaringClass;
      this.property = property;
      this.defaultAccess = AccessType.getAccessStrategy(propertyAccessor);
      this.reflectionManager = reflectionManager;
   }

   public AccessType getDefaultAccess() throws MappingException {
      AccessType accessType = this.defaultAccess;
      AccessType hibernateAccessType = AccessType.DEFAULT;
      AccessType jpaAccessType = AccessType.DEFAULT;
      org.hibernate.annotations.AccessType accessTypeAnnotation = (org.hibernate.annotations.AccessType)this.property.getAnnotation(org.hibernate.annotations.AccessType.class);
      if (accessTypeAnnotation != null) {
         hibernateAccessType = AccessType.getAccessStrategy(accessTypeAnnotation.value());
      }

      Access access = (Access)this.property.getAnnotation(Access.class);
      if (access != null) {
         jpaAccessType = AccessType.getAccessStrategy(access.value());
      }

      if (hibernateAccessType != AccessType.DEFAULT && jpaAccessType != AccessType.DEFAULT && hibernateAccessType != jpaAccessType) {
         StringBuilder builder = new StringBuilder();
         builder.append(this.property.toString());
         builder.append(" defines @AccessType and @Access with contradicting values. Use of @Access only is recommended.");
         throw new MappingException(builder.toString());
      } else {
         if (hibernateAccessType != AccessType.DEFAULT) {
            accessType = hibernateAccessType;
         } else if (jpaAccessType != AccessType.DEFAULT) {
            accessType = jpaAccessType;
         }

         return accessType;
      }
   }

   public String getPropertyName() throws MappingException {
      return this.property.getName();
   }

   public XClass getPropertyClass() throws MappingException {
      return this.property.isAnnotationPresent(Target.class) ? this.reflectionManager.toXClass(((Target)this.property.getAnnotation(Target.class)).value()) : this.property.getType();
   }

   public XClass getClassOrElement() throws MappingException {
      return this.property.isAnnotationPresent(Target.class) ? this.reflectionManager.toXClass(((Target)this.property.getAnnotation(Target.class)).value()) : this.property.getClassOrElementClass();
   }

   public String getClassOrElementName() throws MappingException {
      return this.getClassOrElement().getName();
   }

   public String getTypeName() throws MappingException {
      return this.getPropertyClass().getName();
   }

   public XProperty getProperty() {
      return this.property;
   }

   public XClass getDeclaringClass() {
      return this.declaringClass;
   }
}
