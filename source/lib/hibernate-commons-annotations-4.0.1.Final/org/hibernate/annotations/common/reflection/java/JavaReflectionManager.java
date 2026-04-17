package org.hibernate.annotations.common.reflection.java;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import org.hibernate.annotations.common.Version;
import org.hibernate.annotations.common.reflection.AnnotationReader;
import org.hibernate.annotations.common.reflection.MetadataProvider;
import org.hibernate.annotations.common.reflection.MetadataProviderInjector;
import org.hibernate.annotations.common.reflection.ReflectionManager;
import org.hibernate.annotations.common.reflection.XClass;
import org.hibernate.annotations.common.reflection.XMethod;
import org.hibernate.annotations.common.reflection.XPackage;
import org.hibernate.annotations.common.reflection.XProperty;
import org.hibernate.annotations.common.reflection.java.generics.IdentityTypeEnvironment;
import org.hibernate.annotations.common.reflection.java.generics.TypeEnvironment;
import org.hibernate.annotations.common.reflection.java.generics.TypeEnvironmentFactory;
import org.hibernate.annotations.common.reflection.java.generics.TypeSwitch;
import org.hibernate.annotations.common.reflection.java.generics.TypeUtils;
import org.hibernate.annotations.common.util.ReflectHelper;

public class JavaReflectionManager implements ReflectionManager, MetadataProviderInjector {
   private MetadataProvider metadataProvider;
   private final Map xClasses = new HashMap();
   private final Map packagesToXPackages = new HashMap();
   private final Map xProperties = new HashMap();
   private final Map xMethods = new HashMap();
   private final TypeEnvironmentFactory typeEnvs = new TypeEnvironmentFactory();

   public MetadataProvider getMetadataProvider() {
      if (this.metadataProvider == null) {
         this.setMetadataProvider(new JavaMetadataProvider());
      }

      return this.metadataProvider;
   }

   public void setMetadataProvider(MetadataProvider metadataProvider) {
      this.metadataProvider = metadataProvider;
   }

   public XClass toXClass(Class clazz) {
      return this.toXClass(clazz, IdentityTypeEnvironment.INSTANCE);
   }

   public Class toClass(XClass xClazz) {
      if (!(xClazz instanceof JavaXClass)) {
         throw new IllegalArgumentException("XClass not coming from this ReflectionManager implementation");
      } else {
         return (Class)((JavaXClass)xClazz).toAnnotatedElement();
      }
   }

   public Method toMethod(XMethod xMethod) {
      if (!(xMethod instanceof JavaXMethod)) {
         throw new IllegalArgumentException("XMethod not coming from this ReflectionManager implementation");
      } else {
         return (Method)((JavaXAnnotatedElement)xMethod).toAnnotatedElement();
      }
   }

   public XClass classForName(String name, Class caller) throws ClassNotFoundException {
      return this.toXClass(ReflectHelper.classForName(name, caller));
   }

   public XPackage packageForName(String packageName) throws ClassNotFoundException {
      return this.getXAnnotatedElement(ReflectHelper.classForName(packageName + ".package-info").getPackage());
   }

   XClass toXClass(Type t, final TypeEnvironment context) {
      return (XClass)(new TypeSwitch() {
         public XClass caseClass(Class classType) {
            TypeKey key = new TypeKey(classType, context);
            JavaXClass result = (JavaXClass)JavaReflectionManager.this.xClasses.get(key);
            if (result == null) {
               result = new JavaXClass(classType, context, JavaReflectionManager.this);
               JavaReflectionManager.this.xClasses.put(key, result);
            }

            return result;
         }

         public XClass caseParameterizedType(ParameterizedType parameterizedType) {
            return JavaReflectionManager.this.toXClass(parameterizedType.getRawType(), JavaReflectionManager.this.typeEnvs.getEnvironment(parameterizedType, context));
         }
      }).doSwitch(context.bind(t));
   }

   XPackage getXAnnotatedElement(Package pkg) {
      JavaXPackage xPackage = (JavaXPackage)this.packagesToXPackages.get(pkg);
      if (xPackage == null) {
         xPackage = new JavaXPackage(pkg, this);
         this.packagesToXPackages.put(pkg, xPackage);
      }

      return xPackage;
   }

   XProperty getXProperty(Member member, TypeEnvironment context) {
      MemberKey key = new MemberKey(member, context);
      JavaXProperty xProperty = (JavaXProperty)this.xProperties.get(key);
      if (xProperty == null) {
         xProperty = JavaXProperty.create(member, context, this);
         this.xProperties.put(key, xProperty);
      }

      return xProperty;
   }

   XMethod getXMethod(Member member, TypeEnvironment context) {
      MemberKey key = new MemberKey(member, context);
      JavaXMethod xMethod = (JavaXMethod)this.xMethods.get(key);
      if (xMethod == null) {
         xMethod = JavaXMethod.create(member, context, this);
         this.xMethods.put(key, xMethod);
      }

      return xMethod;
   }

   TypeEnvironment getTypeEnvironment(Type t) {
      return (TypeEnvironment)(new TypeSwitch() {
         public TypeEnvironment caseClass(Class classType) {
            return JavaReflectionManager.this.typeEnvs.getEnvironment(classType);
         }

         public TypeEnvironment caseParameterizedType(ParameterizedType parameterizedType) {
            return JavaReflectionManager.this.typeEnvs.getEnvironment((Type)parameterizedType);
         }

         public TypeEnvironment defaultCase(Type type) {
            return IdentityTypeEnvironment.INSTANCE;
         }
      }).doSwitch(t);
   }

   public JavaXType toXType(TypeEnvironment context, Type propType) {
      Type boundType = this.toApproximatingEnvironment(context).bind(propType);
      if (TypeUtils.isArray(boundType)) {
         return new JavaXArrayType(propType, context, this);
      } else if (TypeUtils.isCollection(boundType)) {
         return new JavaXCollectionType(propType, context, this);
      } else if (TypeUtils.isSimple(boundType)) {
         return new JavaXSimpleType(propType, context, this);
      } else {
         throw new IllegalArgumentException("No PropertyTypeExtractor available for type void ");
      }
   }

   public boolean equals(XClass class1, Class class2) {
      if (class1 == null) {
         return class2 == null;
      } else {
         return ((JavaXClass)class1).toClass().equals(class2);
      }
   }

   public TypeEnvironment toApproximatingEnvironment(TypeEnvironment context) {
      return this.typeEnvs.toApproximatingEnvironment(context);
   }

   public AnnotationReader buildAnnotationReader(AnnotatedElement annotatedElement) {
      return this.getMetadataProvider().getAnnotationReader(annotatedElement);
   }

   public Map getDefaults() {
      return this.getMetadataProvider().getDefaults();
   }

   static {
      Version.touch();
   }

   private static class TypeKey extends Pair {
      TypeKey(Type t, TypeEnvironment context) {
         super(t, context);
      }
   }

   private static class MemberKey extends Pair {
      MemberKey(Member member, TypeEnvironment context) {
         super(member, context);
      }
   }
}
