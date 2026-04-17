package org.hibernate.metamodel.source.annotations.xml.mocker;

import java.beans.Introspector;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.CascadeType;
import org.hibernate.HibernateException;
import org.hibernate.internal.jaxb.mapping.orm.JaxbCascadeType;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.metamodel.source.annotations.JPADotNames;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.classloading.spi.ClassLoaderService;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.jboss.jandex.Type.Kind;

public class MockHelper {
   static final AnnotationValue[] EMPTY_ANNOTATION_VALUE_ARRAY = new AnnotationValue[0];
   static final Type[] EMPTY_TYPE_ARRAY = new Type[0];

   static void stringArrayValue(String name, List values, List annotationValueList) {
      if (isNotEmpty(values)) {
         AnnotationValue[] annotationValues = new AnnotationValue[values.size()];

         for(int j = 0; j < values.size(); ++j) {
            annotationValues[j] = stringValue("", (String)values.get(j));
         }

         annotationValueList.add(AnnotationValue.createArrayValue(name, annotationValues));
      }

   }

   static AnnotationValue[] stringValueArray(String name, String value) {
      return nullSafe(stringValue(name, value));
   }

   private static AnnotationValue stringValue(String name, String value) {
      return StringHelper.isNotEmpty(value) ? AnnotationValue.createStringValue(name, value) : null;
   }

   static void stringValue(String name, String value, List annotationValueList) {
      addToCollectionIfNotNull(annotationValueList, stringValue(name, value));
   }

   private static AnnotationValue integerValue(String name, Integer value) {
      return value == null ? null : AnnotationValue.createIntegerValue(name, value);
   }

   static void integerValue(String name, Integer value, List annotationValueList) {
      addToCollectionIfNotNull(annotationValueList, integerValue(name, value));
   }

   static AnnotationValue[] booleanValueArray(String name, Boolean value) {
      return nullSafe(booleanValue(name, value));
   }

   static void booleanValue(String name, Boolean value, List annotationValueList) {
      addToCollectionIfNotNull(annotationValueList, booleanValue(name, value));
   }

   private static AnnotationValue booleanValue(String name, Boolean value) {
      return value == null ? null : AnnotationValue.createBooleanValue(name, value);
   }

   private static AnnotationValue classValue(String name, String className, ServiceRegistry serviceRegistry) {
      return StringHelper.isNotEmpty(className) ? AnnotationValue.createClassValue(name, getType(className, serviceRegistry)) : null;
   }

   static void classValue(String name, String className, List list, ServiceRegistry serviceRegistry) {
      addToCollectionIfNotNull(list, classValue(name, className, serviceRegistry));
   }

   static AnnotationValue[] classValueArray(String name, String className, ServiceRegistry serviceRegistry) {
      return nullSafe(classValue(name, className, serviceRegistry));
   }

   static AnnotationValue nestedAnnotationValue(String name, AnnotationInstance value) {
      return value == null ? null : AnnotationValue.createNestedAnnotationValue(name, value);
   }

   static void nestedAnnotationValue(String name, AnnotationInstance value, List list) {
      addToCollectionIfNotNull(list, nestedAnnotationValue(name, value));
   }

   private static AnnotationValue[] nullSafe(AnnotationValue value) {
      return value == null ? EMPTY_ANNOTATION_VALUE_ARRAY : new AnnotationValue[]{value};
   }

   static void classArrayValue(String name, List classNameList, List list, ServiceRegistry serviceRegistry) {
      if (isNotEmpty(classNameList)) {
         List<AnnotationValue> clazzValueList = new ArrayList(classNameList.size());

         for(String clazz : classNameList) {
            addToCollectionIfNotNull(clazzValueList, classValue("", clazz, serviceRegistry));
         }

         list.add(AnnotationValue.createArrayValue(name, toArray(clazzValueList)));
      }

   }

   public static AnnotationValue[] toArray(List list) {
      AnnotationValue[] values = EMPTY_ANNOTATION_VALUE_ARRAY;
      if (isNotEmpty(list)) {
         values = (AnnotationValue[])list.toArray(new AnnotationValue[list.size()]);
      }

      return values;
   }

   private static AnnotationValue enumValue(String name, DotName typeName, Enum value) {
      return value != null && StringHelper.isNotEmpty(value.toString()) ? AnnotationValue.createEnumValue(name, typeName, value.toString()) : null;
   }

   static void cascadeValue(String name, JaxbCascadeType cascadeType, boolean isCascadePersistDefault, List annotationValueList) {
      List<Enum> enumList = new ArrayList();
      if (isCascadePersistDefault) {
         enumList.add(CascadeType.PERSIST);
      }

      if (cascadeType != null) {
         if (cascadeType.getCascadeAll() != null) {
            enumList.add(CascadeType.ALL);
         }

         if (cascadeType.getCascadePersist() != null && !isCascadePersistDefault) {
            enumList.add(CascadeType.PERSIST);
         }

         if (cascadeType.getCascadeMerge() != null) {
            enumList.add(CascadeType.MERGE);
         }

         if (cascadeType.getCascadeRemove() != null) {
            enumList.add(CascadeType.REMOVE);
         }

         if (cascadeType.getCascadeRefresh() != null) {
            enumList.add(CascadeType.REFRESH);
         }

         if (cascadeType.getCascadeDetach() != null) {
            enumList.add(CascadeType.DETACH);
         }
      }

      if (!enumList.isEmpty()) {
         enumArrayValue(name, JPADotNames.CASCADE_TYPE, enumList, annotationValueList);
      }

   }

   static void enumArrayValue(String name, DotName typeName, List valueList, List list) {
      if (isNotEmpty(valueList)) {
         List<AnnotationValue> enumValueList = new ArrayList(valueList.size());

         for(Enum e : valueList) {
            addToCollectionIfNotNull(enumValueList, enumValue("", typeName, e));
         }

         list.add(AnnotationValue.createArrayValue(name, toArray(enumValueList)));
      }

   }

   static void enumValue(String name, DotName typeName, Enum value, List list) {
      addToCollectionIfNotNull(list, enumValue(name, typeName, value));
   }

   static AnnotationValue[] enumValueArray(String name, DotName typeName, Enum value) {
      return nullSafe(enumValue(name, typeName, value));
   }

   public static void addToCollectionIfNotNull(Collection collection, Object value) {
      if (value != null && collection != null) {
         collection.add(value);
      }

   }

   public static boolean targetEquals(AnnotationTarget t1, AnnotationTarget t2) {
      if (t1 == t2) {
         return true;
      } else if (t1 != null && t2 != null && t1.getClass() == t2.getClass()) {
         if (t1.getClass() == ClassInfo.class) {
            return ((ClassInfo)t1).name().equals(((ClassInfo)t2).name());
         } else {
            return t1.getClass() == MethodInfo.class ? ((MethodInfo)t1).name().equals(((MethodInfo)t2).name()) : ((FieldInfo)t1).name().equals(((FieldInfo)t2).name());
         }
      } else {
         return false;
      }
   }

   public static boolean isNotEmpty(Collection collection) {
      return collection != null && !collection.isEmpty();
   }

   static AnnotationInstance create(DotName name, AnnotationTarget target, List annotationValueList) {
      return create(name, target, toArray(annotationValueList));
   }

   static String buildSafeClassName(String className, String defaultPackageName) {
      if (className.indexOf(46) < 0 && StringHelper.isNotEmpty(defaultPackageName)) {
         className = StringHelper.qualify(defaultPackageName, className);
      }

      return className;
   }

   static AnnotationInstance create(DotName name, AnnotationTarget target, AnnotationValue[] values) {
      if (values == null || values.length == 0) {
         values = EMPTY_ANNOTATION_VALUE_ARRAY;
      }

      return AnnotationInstance.create(name, target, addMockMark(values));
   }

   private static AnnotationValue[] addMockMark(AnnotationValue[] values) {
      AnnotationValue[] newValues = new AnnotationValue[values.length + 1];
      System.arraycopy(values, 0, newValues, 0, values.length);
      newValues[values.length] = booleanValue("isMocked", true);
      return newValues;
   }

   private static MethodInfo getMethodInfo(ClassInfo classInfo, Method method) {
      Class returnTypeClass = method.getReturnType();
      short access_flags = (short)method.getModifiers();
      return MethodInfo.create(classInfo, method.getName(), getTypes(method.getParameterTypes()), getType(returnTypeClass), access_flags);
   }

   static AnnotationTarget getTarget(ServiceRegistry serviceRegistry, ClassInfo classInfo, String name, TargetType type) {
      Class clazz = ((ClassLoaderService)serviceRegistry.getService(ClassLoaderService.class)).classForName(classInfo.toString());
      switch (type) {
         case FIELD:
            Field field = getField(clazz, name);
            if (field == null) {
               throw new HibernateException("Unable to load field " + name + " of class " + clazz.getName());
            }

            return FieldInfo.create(classInfo, name, getType(field.getType()), (short)field.getModifiers());
         case METHOD:
            Method method = getMethod(clazz, name);
            if (method == null) {
               throw new HibernateException("Unable to load method " + name + " of class " + clazz.getName());
            }

            return getMethodInfo(classInfo, method);
         case PROPERTY:
            Method method = getterMethod(clazz, name);
            if (method == null) {
               throw new HibernateException("Unable to load property " + name + " of class " + clazz.getName());
            }

            return getMethodInfo(classInfo, method);
         default:
            throw new HibernateException("");
      }
   }

   private static Method getterMethod(Class theClass, String propertyName) {
      Method[] methods = theClass.getDeclaredMethods();
      Method.setAccessible(methods, true);

      for(Method method : methods) {
         if (method.getParameterTypes().length == 0 && !method.isBridge()) {
            String methodName = method.getName();
            if (methodName.startsWith("get") || methodName.startsWith("has")) {
               String testStdMethod = Introspector.decapitalize(methodName.substring(3));
               String testOldMethod = methodName.substring(3);
               if (testStdMethod.equals(propertyName) || testOldMethod.equals(propertyName)) {
                  return method;
               }
            }

            if (methodName.startsWith("is")) {
               String testStdMethod = Introspector.decapitalize(methodName.substring(2));
               String testOldMethod = methodName.substring(2);
               if (testStdMethod.equals(propertyName) || testOldMethod.equals(propertyName)) {
                  return method;
               }
            }
         }
      }

      return null;
   }

   private static Method getMethod(Class theClass, String propertyName) {
      Method[] methods = theClass.getDeclaredMethods();
      Method.setAccessible(methods, true);

      for(Method method : methods) {
         if (method.getParameterTypes().length == 0 && !method.isBridge()) {
            String methodName = method.getName();
            if (methodName.equals(propertyName)) {
               return method;
            }
         }
      }

      return null;
   }

   private static Field getField(Class clazz, String name) {
      Field[] fields = clazz.getDeclaredFields();
      Field.setAccessible(fields, true);

      for(Field field : fields) {
         if (field.getName().equals(name)) {
            return field;
         }
      }

      return null;
   }

   private static Type[] getTypes(Class[] classes) {
      if (classes != null && classes.length != 0) {
         Type[] types = new Type[classes.length];

         for(int i = 0; i < types.length; ++i) {
            types[i] = getType(classes[i]);
         }

         return types;
      } else {
         return EMPTY_TYPE_ARRAY;
      }
   }

   private static Type getType(String className, ServiceRegistry serviceRegistry) {
      return getType(((ClassLoaderService)serviceRegistry.getService(ClassLoaderService.class)).classForName(className));
   }

   private static Type getType(Class clazz) {
      return Type.create(DotName.createSimple(clazz.getName()), getTypeKind(clazz));
   }

   private static Type.Kind getTypeKind(Class clazz) {
      Type.Kind kind;
      if (clazz == Void.TYPE) {
         kind = Kind.VOID;
      } else if (clazz.isPrimitive()) {
         kind = Kind.PRIMITIVE;
      } else if (clazz.isArray()) {
         kind = Kind.ARRAY;
      } else {
         kind = Kind.CLASS;
      }

      return kind;
   }

   static enum TargetType {
      METHOD,
      FIELD,
      PROPERTY;
   }
}
