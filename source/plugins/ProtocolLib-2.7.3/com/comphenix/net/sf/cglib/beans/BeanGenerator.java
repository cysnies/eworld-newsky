package com.comphenix.net.sf.cglib.beans;

import com.comphenix.net.sf.cglib.asm.ClassVisitor;
import com.comphenix.net.sf.cglib.asm.Type;
import com.comphenix.net.sf.cglib.core.AbstractClassGenerator;
import com.comphenix.net.sf.cglib.core.ClassEmitter;
import com.comphenix.net.sf.cglib.core.Constants;
import com.comphenix.net.sf.cglib.core.EmitUtils;
import com.comphenix.net.sf.cglib.core.KeyFactory;
import com.comphenix.net.sf.cglib.core.ReflectUtils;
import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;

public class BeanGenerator extends AbstractClassGenerator {
   private static final AbstractClassGenerator.Source SOURCE;
   private static final BeanGeneratorKey KEY_FACTORY;
   private Class superclass;
   private Map props = new HashMap();
   private boolean classOnly;
   // $FF: synthetic field
   static Class class$net$sf$cglib$beans$BeanGenerator;
   // $FF: synthetic field
   static Class class$net$sf$cglib$beans$BeanGenerator$BeanGeneratorKey;
   // $FF: synthetic field
   static Class class$java$lang$Object;

   public BeanGenerator() {
      super(SOURCE);
   }

   public void setSuperclass(Class superclass) {
      if (superclass != null && superclass.equals(class$java$lang$Object == null ? (class$java$lang$Object = class$("java.lang.Object")) : class$java$lang$Object)) {
         superclass = null;
      }

      this.superclass = superclass;
   }

   public void addProperty(String name, Class type) {
      if (this.props.containsKey(name)) {
         throw new IllegalArgumentException("Duplicate property name \"" + name + "\"");
      } else {
         this.props.put(name, Type.getType(type));
      }
   }

   protected ClassLoader getDefaultClassLoader() {
      return this.superclass != null ? this.superclass.getClassLoader() : null;
   }

   public Object create() {
      this.classOnly = false;
      return this.createHelper();
   }

   public Object createClass() {
      this.classOnly = true;
      return this.createHelper();
   }

   private Object createHelper() {
      if (this.superclass != null) {
         this.setNamePrefix(this.superclass.getName());
      }

      String superName = this.superclass != null ? this.superclass.getName() : "java.lang.Object";
      Object key = KEY_FACTORY.newInstance(superName, this.props);
      return super.create(key);
   }

   public void generateClass(ClassVisitor v) throws Exception {
      int size = this.props.size();
      String[] names = (String[])this.props.keySet().toArray(new String[size]);
      Type[] types = new Type[size];

      for(int i = 0; i < size; ++i) {
         types[i] = (Type)this.props.get(names[i]);
      }

      ClassEmitter ce = new ClassEmitter(v);
      ce.begin_class(46, 1, this.getClassName(), this.superclass != null ? Type.getType(this.superclass) : Constants.TYPE_OBJECT, (Type[])null, (String)null);
      EmitUtils.null_constructor(ce);
      EmitUtils.add_properties(ce, names, types);
      ce.end_class();
   }

   protected Object firstInstance(Class type) {
      return this.classOnly ? type : ReflectUtils.newInstance(type);
   }

   protected Object nextInstance(Object instance) {
      Class protoclass = instance instanceof Class ? (Class)instance : instance.getClass();
      return this.classOnly ? protoclass : ReflectUtils.newInstance(protoclass);
   }

   public static void addProperties(BeanGenerator gen, Map props) {
      for(String name : props.keySet()) {
         gen.addProperty(name, (Class)props.get(name));
      }

   }

   public static void addProperties(BeanGenerator gen, Class type) {
      addProperties(gen, ReflectUtils.getBeanProperties(type));
   }

   public static void addProperties(BeanGenerator gen, PropertyDescriptor[] descriptors) {
      for(int i = 0; i < descriptors.length; ++i) {
         gen.addProperty(descriptors[i].getName(), descriptors[i].getPropertyType());
      }

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
      SOURCE = new AbstractClassGenerator.Source((class$net$sf$cglib$beans$BeanGenerator == null ? (class$net$sf$cglib$beans$BeanGenerator = class$("com.comphenix.net.sf.cglib.beans.BeanGenerator")) : class$net$sf$cglib$beans$BeanGenerator).getName());
      KEY_FACTORY = (BeanGeneratorKey)KeyFactory.create(class$net$sf$cglib$beans$BeanGenerator$BeanGeneratorKey == null ? (class$net$sf$cglib$beans$BeanGenerator$BeanGeneratorKey = class$("com.comphenix.net.sf.cglib.beans.BeanGenerator$BeanGeneratorKey")) : class$net$sf$cglib$beans$BeanGenerator$BeanGeneratorKey);
   }

   interface BeanGeneratorKey {
      Object newInstance(String var1, Map var2);
   }
}
