package com.comphenix.net.sf.cglib.beans;

import com.comphenix.net.sf.cglib.asm.ClassVisitor;
import com.comphenix.net.sf.cglib.asm.Type;
import com.comphenix.net.sf.cglib.core.AbstractClassGenerator;
import com.comphenix.net.sf.cglib.core.ClassEmitter;
import com.comphenix.net.sf.cglib.core.CodeEmitter;
import com.comphenix.net.sf.cglib.core.Constants;
import com.comphenix.net.sf.cglib.core.Converter;
import com.comphenix.net.sf.cglib.core.EmitUtils;
import com.comphenix.net.sf.cglib.core.KeyFactory;
import com.comphenix.net.sf.cglib.core.Local;
import com.comphenix.net.sf.cglib.core.MethodInfo;
import com.comphenix.net.sf.cglib.core.ReflectUtils;
import com.comphenix.net.sf.cglib.core.Signature;
import com.comphenix.net.sf.cglib.core.TypeUtils;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public abstract class BeanCopier {
   private static final BeanCopierKey KEY_FACTORY;
   private static final Type CONVERTER;
   private static final Type BEAN_COPIER;
   private static final Signature COPY;
   private static final Signature CONVERT;
   // $FF: synthetic field
   static Class class$net$sf$cglib$beans$BeanCopier$BeanCopierKey;
   // $FF: synthetic field
   static Class class$net$sf$cglib$beans$BeanCopier;

   public static BeanCopier create(Class source, Class target, boolean useConverter) {
      Generator gen = new Generator();
      gen.setSource(source);
      gen.setTarget(target);
      gen.setUseConverter(useConverter);
      return gen.create();
   }

   public abstract void copy(Object var1, Object var2, Converter var3);

   // $FF: synthetic method
   static Class class$(String x0) {
      try {
         return Class.forName(x0);
      } catch (ClassNotFoundException x1) {
         throw new NoClassDefFoundError(x1.getMessage());
      }
   }

   static {
      KEY_FACTORY = (BeanCopierKey)KeyFactory.create(class$net$sf$cglib$beans$BeanCopier$BeanCopierKey == null ? (class$net$sf$cglib$beans$BeanCopier$BeanCopierKey = class$("com.comphenix.net.sf.cglib.beans.BeanCopier$BeanCopierKey")) : class$net$sf$cglib$beans$BeanCopier$BeanCopierKey);
      CONVERTER = TypeUtils.parseType("com.comphenix.net.sf.cglib.core.Converter");
      BEAN_COPIER = TypeUtils.parseType("com.comphenix.net.sf.cglib.beans.BeanCopier");
      COPY = new Signature("copy", Type.VOID_TYPE, new Type[]{Constants.TYPE_OBJECT, Constants.TYPE_OBJECT, CONVERTER});
      CONVERT = TypeUtils.parseSignature("Object convert(Object, Class, Object)");
   }

   public static class Generator extends AbstractClassGenerator {
      private static final AbstractClassGenerator.Source SOURCE;
      private Class source;
      private Class target;
      private boolean useConverter;

      public Generator() {
         super(SOURCE);
      }

      public void setSource(Class source) {
         if (!Modifier.isPublic(source.getModifiers())) {
            this.setNamePrefix(source.getName());
         }

         this.source = source;
      }

      public void setTarget(Class target) {
         if (!Modifier.isPublic(target.getModifiers())) {
            this.setNamePrefix(target.getName());
         }

         this.target = target;
      }

      public void setUseConverter(boolean useConverter) {
         this.useConverter = useConverter;
      }

      protected ClassLoader getDefaultClassLoader() {
         return this.source.getClassLoader();
      }

      public BeanCopier create() {
         Object key = BeanCopier.KEY_FACTORY.newInstance(this.source.getName(), this.target.getName(), this.useConverter);
         return (BeanCopier)super.create(key);
      }

      public void generateClass(ClassVisitor v) {
         Type sourceType = Type.getType(this.source);
         Type targetType = Type.getType(this.target);
         ClassEmitter ce = new ClassEmitter(v);
         ce.begin_class(46, 1, this.getClassName(), BeanCopier.BEAN_COPIER, (Type[])null, "<generated>");
         EmitUtils.null_constructor(ce);
         CodeEmitter e = ce.begin_method(1, BeanCopier.COPY, (Type[])null);
         PropertyDescriptor[] getters = ReflectUtils.getBeanGetters(this.source);
         PropertyDescriptor[] setters = ReflectUtils.getBeanGetters(this.target);
         Map names = new HashMap();

         for(int i = 0; i < getters.length; ++i) {
            names.put(getters[i].getName(), getters[i]);
         }

         Local targetLocal = e.make_local();
         Local sourceLocal = e.make_local();
         if (this.useConverter) {
            e.load_arg(1);
            e.checkcast(targetType);
            e.store_local(targetLocal);
            e.load_arg(0);
            e.checkcast(sourceType);
            e.store_local(sourceLocal);
         } else {
            e.load_arg(1);
            e.checkcast(targetType);
            e.load_arg(0);
            e.checkcast(sourceType);
         }

         for(int i = 0; i < setters.length; ++i) {
            PropertyDescriptor setter = setters[i];
            PropertyDescriptor getter = (PropertyDescriptor)names.get(setter.getName());
            if (getter != null) {
               MethodInfo read = ReflectUtils.getMethodInfo(getter.getReadMethod());
               MethodInfo write = ReflectUtils.getMethodInfo(setter.getWriteMethod());
               if (this.useConverter) {
                  Type setterType = write.getSignature().getArgumentTypes()[0];
                  e.load_local(targetLocal);
                  e.load_arg(2);
                  e.load_local(sourceLocal);
                  e.invoke(read);
                  e.box(read.getSignature().getReturnType());
                  EmitUtils.load_class(e, setterType);
                  e.push(write.getSignature().getName());
                  e.invoke_interface(BeanCopier.CONVERTER, BeanCopier.CONVERT);
                  e.unbox_or_zero(setterType);
                  e.invoke(write);
               } else if (compatible(getter, setter)) {
                  e.dup2();
                  e.invoke(read);
                  e.invoke(write);
               }
            }
         }

         e.return_value();
         e.end_method();
         ce.end_class();
      }

      private static boolean compatible(PropertyDescriptor getter, PropertyDescriptor setter) {
         return setter.getPropertyType().isAssignableFrom(getter.getPropertyType());
      }

      protected Object firstInstance(Class type) {
         return ReflectUtils.newInstance(type);
      }

      protected Object nextInstance(Object instance) {
         return instance;
      }

      static {
         SOURCE = new AbstractClassGenerator.Source((BeanCopier.class$net$sf$cglib$beans$BeanCopier == null ? (BeanCopier.class$net$sf$cglib$beans$BeanCopier = BeanCopier.class$("com.comphenix.net.sf.cglib.beans.BeanCopier")) : BeanCopier.class$net$sf$cglib$beans$BeanCopier).getName());
      }
   }

   interface BeanCopierKey {
      Object newInstance(String var1, String var2, boolean var3);
   }
}
