package org.hibernate.internal;

import java.io.Serializable;
import java.util.Properties;
import org.hibernate.TypeHelper;
import org.hibernate.type.BasicType;
import org.hibernate.type.Type;
import org.hibernate.type.TypeResolver;
import org.hibernate.usertype.CompositeUserType;

public class TypeLocatorImpl implements TypeHelper, Serializable {
   private final TypeResolver typeResolver;

   public TypeLocatorImpl(TypeResolver typeResolver) {
      this.typeResolver = typeResolver;
   }

   public BasicType basic(String name) {
      return this.typeResolver.basic(name);
   }

   public BasicType basic(Class javaType) {
      BasicType type = this.typeResolver.basic(javaType.getName());
      if (type == null) {
         Class variant = this.resolvePrimitiveOrPrimitiveWrapperVariantJavaType(javaType);
         if (variant != null) {
            type = this.typeResolver.basic(variant.getName());
         }
      }

      return type;
   }

   private Class resolvePrimitiveOrPrimitiveWrapperVariantJavaType(Class javaType) {
      if (Boolean.TYPE.equals(javaType)) {
         return Boolean.class;
      } else if (Boolean.class.equals(javaType)) {
         return Boolean.TYPE;
      } else if (Character.TYPE.equals(javaType)) {
         return Character.class;
      } else if (Character.class.equals(javaType)) {
         return Character.TYPE;
      } else if (Byte.TYPE.equals(javaType)) {
         return Byte.class;
      } else if (Byte.class.equals(javaType)) {
         return Byte.TYPE;
      } else if (Short.TYPE.equals(javaType)) {
         return Short.class;
      } else if (Short.class.equals(javaType)) {
         return Short.TYPE;
      } else if (Integer.TYPE.equals(javaType)) {
         return Integer.class;
      } else if (Integer.class.equals(javaType)) {
         return Integer.TYPE;
      } else if (Long.TYPE.equals(javaType)) {
         return Long.class;
      } else if (Long.class.equals(javaType)) {
         return Long.TYPE;
      } else if (Float.TYPE.equals(javaType)) {
         return Float.class;
      } else if (Float.class.equals(javaType)) {
         return Float.TYPE;
      } else if (Double.TYPE.equals(javaType)) {
         return Double.class;
      } else {
         return Double.class.equals(javaType) ? Double.TYPE : null;
      }
   }

   public Type heuristicType(String name) {
      return this.typeResolver.heuristicType(name);
   }

   public Type entity(Class entityClass) {
      return this.entity(entityClass.getName());
   }

   public Type entity(String entityName) {
      return this.typeResolver.getTypeFactory().manyToOne(entityName);
   }

   public Type custom(Class userTypeClass) {
      return this.custom(userTypeClass, (Properties)null);
   }

   public Type custom(Class userTypeClass, Properties parameters) {
      return (Type)(CompositeUserType.class.isAssignableFrom(userTypeClass) ? this.typeResolver.getTypeFactory().customComponent(userTypeClass, parameters) : this.typeResolver.getTypeFactory().custom(userTypeClass, parameters));
   }

   public Type any(Type metaType, Type identifierType) {
      return this.typeResolver.getTypeFactory().any(metaType, identifierType);
   }
}
