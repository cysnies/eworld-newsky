package org.hibernate.metamodel.source.annotations;

import java.beans.Introspector;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.AssertionFailure;
import org.hibernate.HibernateException;
import org.hibernate.service.classloading.spi.ClassLoaderService;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;

public class JandexHelper {
   private static final Map DEFAULT_VALUES_BY_ELEMENT = new HashMap();

   private JandexHelper() {
   }

   public static Object getValue(AnnotationInstance annotation, String element, Class type) throws AssertionFailure {
      if (Class.class.equals(type)) {
         throw new AssertionFailure("Annotation parameters of type Class should be retrieved as strings (fully qualified class names)");
      } else {
         AnnotationValue annotationValue = annotation.value(element);

         try {
            return annotationValue != null ? explicitAnnotationParameter(annotationValue, type) : defaultAnnotationParameter(getDefaultValue(annotation, element), type);
         } catch (ClassCastException var5) {
            throw new AssertionFailure(String.format("the annotation property %s of annotation %s is not of type %s", element, annotation.name(), type.getName()));
         }
      }
   }

   public static Enum getEnumValue(AnnotationInstance annotation, String element, Class type) {
      AnnotationValue val = annotation.value(element);
      return val == null ? (Enum)getDefaultValue(annotation, element) : Enum.valueOf(type, val.asEnum());
   }

   public static String getPropertyName(AnnotationTarget target) {
      if (!(target instanceof MethodInfo) && !(target instanceof FieldInfo)) {
         throw new AssertionFailure("Unexpected annotation target " + target.toString());
      } else if (target instanceof FieldInfo) {
         return ((FieldInfo)target).name();
      } else {
         String methodName = ((MethodInfo)target).name();
         String propertyName;
         if (methodName.startsWith("is")) {
            propertyName = Introspector.decapitalize(methodName.substring(2));
         } else if (methodName.startsWith("has")) {
            propertyName = Introspector.decapitalize(methodName.substring(3));
         } else {
            if (!methodName.startsWith("get")) {
               throw new AssertionFailure("Expected a method following the Java Bean notation");
            }

            propertyName = Introspector.decapitalize(methodName.substring(3));
         }

         return propertyName;
      }
   }

   public static AnnotationInstance getSingleAnnotation(ClassInfo classInfo, DotName annotationName) throws AssertionFailure {
      return getSingleAnnotation(classInfo.annotations(), annotationName);
   }

   public static AnnotationInstance getSingleAnnotation(Map annotations, DotName annotationName) throws AssertionFailure {
      List<AnnotationInstance> annotationList = (List)annotations.get(annotationName);
      if (annotationList == null) {
         return null;
      } else if (annotationList.size() == 1) {
         return (AnnotationInstance)annotationList.get(0);
      } else {
         throw new AssertionFailure("Found more than one instance of the annotation " + ((AnnotationInstance)annotationList.get(0)).name().toString() + ". Expected was one.");
      }
   }

   public static boolean containsSingleAnnotations(Map annotations, DotName annotationName) throws AssertionFailure {
      return getSingleAnnotation(annotations, annotationName) != null;
   }

   public static Index indexForClass(ClassLoaderService classLoaderService, Class... classes) {
      Indexer indexer = new Indexer();

      for(Class clazz : classes) {
         InputStream stream = classLoaderService.locateResourceStream(clazz.getName().replace('.', '/') + ".class");

         try {
            indexer.index(stream);
         } catch (IOException var15) {
            StringBuilder builder = new StringBuilder();
            builder.append("[");
            int count = 0;

            for(Class c : classes) {
               builder.append(c.getName());
               if (count < classes.length - 1) {
                  builder.append(",");
               }

               ++count;
            }

            builder.append("]");
            throw new HibernateException("Unable to create annotation index for " + builder.toString());
         }
      }

      return indexer.complete();
   }

   public static Map getMemberAnnotations(ClassInfo classInfo, String name) {
      if (classInfo == null) {
         throw new IllegalArgumentException("classInfo cannot be null");
      } else if (name == null) {
         throw new IllegalArgumentException("name cannot be null");
      } else {
         Map<DotName, List<AnnotationInstance>> annotations = new HashMap();

         for(List annotationList : classInfo.annotations().values()) {
            for(AnnotationInstance instance : annotationList) {
               String targetName = null;
               if (instance.target() instanceof FieldInfo) {
                  targetName = ((FieldInfo)instance.target()).name();
               } else if (instance.target() instanceof MethodInfo) {
                  targetName = ((MethodInfo)instance.target()).name();
               }

               if (targetName != null && name.equals(targetName)) {
                  addAnnotationToMap(instance, annotations);
               }
            }
         }

         return annotations;
      }
   }

   private static void addAnnotationToMap(AnnotationInstance instance, Map annotations) {
      DotName dotName = instance.name();
      List<AnnotationInstance> list;
      if (annotations.containsKey(dotName)) {
         list = (List)annotations.get(dotName);
      } else {
         list = new ArrayList();
         annotations.put(dotName, list);
      }

      list.add(instance);
   }

   private static Object getDefaultValue(AnnotationInstance annotation, String element) {
      String name = annotation.name().toString();
      String fqElement = name + '.' + element;
      Object val = DEFAULT_VALUES_BY_ELEMENT.get(fqElement);
      if (val != null) {
         return val;
      } else {
         try {
            val = Index.class.getClassLoader().loadClass(name).getMethod(element).getDefaultValue();
            DEFAULT_VALUES_BY_ELEMENT.put(fqElement, val);
            return val == null ? null : val;
         } catch (RuntimeException error) {
            throw error;
         } catch (Exception error) {
            throw new AssertionFailure(String.format("The annotation %s does not define a parameter '%s'", name, element), error);
         }
      }
   }

   private static Object defaultAnnotationParameter(Object defaultValue, Class type) {
      Object returnValue = defaultValue;
      if (defaultValue.getClass().isArray() && defaultValue.getClass().getComponentType().isAnnotation()) {
         returnValue = new AnnotationInstance[0];
      }

      return type.cast(returnValue);
   }

   private static Object explicitAnnotationParameter(AnnotationValue annotationValue, Class type) {
      Object returnValue = annotationValue.value();
      if (returnValue instanceof Type) {
         returnValue = ((Type)returnValue).name().toString();
      }

      if (type.isArray()) {
         AnnotationValue[] values = (AnnotationValue[])returnValue;
         Class<?> componentType = type.getComponentType();
         Object[] arr = Array.newInstance(componentType, values.length);

         for(int i = 0; i < values.length; ++i) {
            arr[i] = componentType.cast(values[i].value());
         }

         returnValue = arr;
      }

      return type.cast(returnValue);
   }
}
