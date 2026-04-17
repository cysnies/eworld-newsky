package org.hibernate.usertype;

import java.lang.annotation.Annotation;

public interface DynamicParameterizedType extends ParameterizedType {
   String PARAMETER_TYPE = "org.hibernate.type.ParameterType";
   String IS_DYNAMIC = "org.hibernate.type.ParameterType.dynamic";
   String RETURNED_CLASS = "org.hibernate.type.ParameterType.returnedClass";
   String IS_PRIMARY_KEY = "org.hibernate.type.ParameterType.primaryKey";
   String ENTITY = "org.hibernate.type.ParameterType.entityClass";
   String PROPERTY = "org.hibernate.type.ParameterType.propertyName";
   String ACCESS_TYPE = "org.hibernate.type.ParameterType.accessType";
   String XPROPERTY = "org.hibernate.type.ParameterType.xproperty";

   public interface ParameterType {
      Class getReturnedClass();

      Annotation[] getAnnotationsMethod();

      String getCatalog();

      String getSchema();

      String getTable();

      boolean isPrimaryKey();

      String[] getColumns();
   }
}
