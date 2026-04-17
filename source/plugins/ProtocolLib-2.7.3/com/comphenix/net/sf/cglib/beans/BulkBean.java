package com.comphenix.net.sf.cglib.beans;

import com.comphenix.net.sf.cglib.asm.ClassVisitor;
import com.comphenix.net.sf.cglib.core.AbstractClassGenerator;
import com.comphenix.net.sf.cglib.core.KeyFactory;
import com.comphenix.net.sf.cglib.core.ReflectUtils;

public abstract class BulkBean {
   private static final BulkBeanKey KEY_FACTORY;
   protected Class target;
   protected String[] getters;
   protected String[] setters;
   protected Class[] types;
   // $FF: synthetic field
   static Class class$net$sf$cglib$beans$BulkBean$BulkBeanKey;
   // $FF: synthetic field
   static Class class$net$sf$cglib$beans$BulkBean;

   protected BulkBean() {
   }

   public abstract void getPropertyValues(Object var1, Object[] var2);

   public abstract void setPropertyValues(Object var1, Object[] var2);

   public Object[] getPropertyValues(Object bean) {
      Object[] values = new Object[this.getters.length];
      this.getPropertyValues(bean, values);
      return values;
   }

   public Class[] getPropertyTypes() {
      return (Class[])this.types.clone();
   }

   public String[] getGetters() {
      return (String[])this.getters.clone();
   }

   public String[] getSetters() {
      return (String[])this.setters.clone();
   }

   public static BulkBean create(Class target, String[] getters, String[] setters, Class[] types) {
      Generator gen = new Generator();
      gen.setTarget(target);
      gen.setGetters(getters);
      gen.setSetters(setters);
      gen.setTypes(types);
      return gen.create();
   }

   // $FF: synthetic method
   static Class class$(String x0) {
      try {
         return Class.forName(x0);
      } catch (ClassNotFoundException x1) {
         throw new NoClassDefFoundError(x1.getMessage());
      }
   }

   static {
      KEY_FACTORY = (BulkBeanKey)KeyFactory.create(class$net$sf$cglib$beans$BulkBean$BulkBeanKey == null ? (class$net$sf$cglib$beans$BulkBean$BulkBeanKey = class$("com.comphenix.net.sf.cglib.beans.BulkBean$BulkBeanKey")) : class$net$sf$cglib$beans$BulkBean$BulkBeanKey);
   }

   public static class Generator extends AbstractClassGenerator {
      private static final AbstractClassGenerator.Source SOURCE;
      private Class target;
      private String[] getters;
      private String[] setters;
      private Class[] types;

      public Generator() {
         super(SOURCE);
      }

      public void setTarget(Class target) {
         this.target = target;
      }

      public void setGetters(String[] getters) {
         this.getters = getters;
      }

      public void setSetters(String[] setters) {
         this.setters = setters;
      }

      public void setTypes(Class[] types) {
         this.types = types;
      }

      protected ClassLoader getDefaultClassLoader() {
         return this.target.getClassLoader();
      }

      public BulkBean create() {
         this.setNamePrefix(this.target.getName());
         String targetClassName = this.target.getName();
         String[] typeClassNames = ReflectUtils.getNames(this.types);
         Object key = BulkBean.KEY_FACTORY.newInstance(targetClassName, this.getters, this.setters, typeClassNames);
         return (BulkBean)super.create(key);
      }

      public void generateClass(ClassVisitor v) throws Exception {
         new BulkBeanEmitter(v, this.getClassName(), this.target, this.getters, this.setters, this.types);
      }

      protected Object firstInstance(Class type) {
         BulkBean instance = (BulkBean)ReflectUtils.newInstance(type);
         instance.target = this.target;
         int length = this.getters.length;
         instance.getters = new String[length];
         System.arraycopy(this.getters, 0, instance.getters, 0, length);
         instance.setters = new String[length];
         System.arraycopy(this.setters, 0, instance.setters, 0, length);
         instance.types = new Class[this.types.length];
         System.arraycopy(this.types, 0, instance.types, 0, this.types.length);
         return instance;
      }

      protected Object nextInstance(Object instance) {
         return instance;
      }

      static {
         SOURCE = new AbstractClassGenerator.Source((BulkBean.class$net$sf$cglib$beans$BulkBean == null ? (BulkBean.class$net$sf$cglib$beans$BulkBean = BulkBean.class$("com.comphenix.net.sf.cglib.beans.BulkBean")) : BulkBean.class$net$sf$cglib$beans$BulkBean).getName());
      }
   }

   interface BulkBeanKey {
      Object newInstance(String var1, String[] var2, String[] var3, String[] var4);
   }
}
