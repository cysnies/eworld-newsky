package org.hibernate.annotations.common.reflection.java;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.SortedMap;
import org.hibernate.annotations.common.reflection.XClass;
import org.hibernate.annotations.common.reflection.java.generics.TypeEnvironment;
import org.hibernate.annotations.common.reflection.java.generics.TypeSwitch;
import org.hibernate.annotations.common.reflection.java.generics.TypeUtils;

class JavaXCollectionType extends JavaXType {
   public JavaXCollectionType(Type type, TypeEnvironment context, JavaReflectionManager factory) {
      super(type, context, factory);
   }

   public boolean isArray() {
      return false;
   }

   public boolean isCollection() {
      return true;
   }

   public XClass getElementClass() {
      return (XClass)(new TypeSwitch() {
         public XClass caseParameterizedType(ParameterizedType parameterizedType) {
            Type[] args = parameterizedType.getActualTypeArguments();
            Class<? extends Collection> collectionClass = JavaXCollectionType.this.getCollectionClass();
            Type componentType;
            if (!Map.class.isAssignableFrom(collectionClass) && !SortedMap.class.isAssignableFrom(collectionClass)) {
               componentType = args[0];
            } else {
               componentType = args[1];
            }

            return JavaXCollectionType.this.toXClass(componentType);
         }
      }).doSwitch(this.approximate());
   }

   public XClass getMapKey() {
      return (XClass)(new TypeSwitch() {
         public XClass caseParameterizedType(ParameterizedType parameterizedType) {
            return Map.class.isAssignableFrom(JavaXCollectionType.this.getCollectionClass()) ? JavaXCollectionType.this.toXClass(parameterizedType.getActualTypeArguments()[0]) : null;
         }
      }).doSwitch(this.approximate());
   }

   public XClass getClassOrElementClass() {
      return this.toXClass(this.approximate());
   }

   public Class getCollectionClass() {
      return TypeUtils.getCollectionClass(this.approximate());
   }

   public XClass getType() {
      return this.toXClass(this.approximate());
   }
}
