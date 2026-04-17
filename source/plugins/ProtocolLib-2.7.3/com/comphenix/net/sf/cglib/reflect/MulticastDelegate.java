package com.comphenix.net.sf.cglib.reflect;

import com.comphenix.net.sf.cglib.asm.ClassVisitor;
import com.comphenix.net.sf.cglib.asm.Type;
import com.comphenix.net.sf.cglib.core.AbstractClassGenerator;
import com.comphenix.net.sf.cglib.core.ClassEmitter;
import com.comphenix.net.sf.cglib.core.CodeEmitter;
import com.comphenix.net.sf.cglib.core.Constants;
import com.comphenix.net.sf.cglib.core.EmitUtils;
import com.comphenix.net.sf.cglib.core.Local;
import com.comphenix.net.sf.cglib.core.MethodInfo;
import com.comphenix.net.sf.cglib.core.ProcessArrayCallback;
import com.comphenix.net.sf.cglib.core.ReflectUtils;
import com.comphenix.net.sf.cglib.core.Signature;
import com.comphenix.net.sf.cglib.core.TypeUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class MulticastDelegate implements Cloneable {
   protected Object[] targets = new Object[0];
   // $FF: synthetic field
   static Class class$net$sf$cglib$reflect$MulticastDelegate;

   protected MulticastDelegate() {
   }

   public List getTargets() {
      return new ArrayList(Arrays.asList(this.targets));
   }

   public abstract MulticastDelegate add(Object var1);

   protected MulticastDelegate addHelper(Object target) {
      MulticastDelegate copy = this.newInstance();
      copy.targets = new Object[this.targets.length + 1];
      System.arraycopy(this.targets, 0, copy.targets, 0, this.targets.length);
      copy.targets[this.targets.length] = target;
      return copy;
   }

   public MulticastDelegate remove(Object target) {
      for(int i = this.targets.length - 1; i >= 0; --i) {
         if (this.targets[i].equals(target)) {
            MulticastDelegate copy = this.newInstance();
            copy.targets = new Object[this.targets.length - 1];
            System.arraycopy(this.targets, 0, copy.targets, 0, i);
            System.arraycopy(this.targets, i + 1, copy.targets, i, this.targets.length - i - 1);
            return copy;
         }
      }

      return this;
   }

   public abstract MulticastDelegate newInstance();

   public static MulticastDelegate create(Class iface) {
      Generator gen = new Generator();
      gen.setInterface(iface);
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

   public static class Generator extends AbstractClassGenerator {
      private static final AbstractClassGenerator.Source SOURCE;
      private static final Type MULTICAST_DELEGATE;
      private static final Signature NEW_INSTANCE;
      private static final Signature ADD_DELEGATE;
      private static final Signature ADD_HELPER;
      private Class iface;

      public Generator() {
         super(SOURCE);
      }

      protected ClassLoader getDefaultClassLoader() {
         return this.iface.getClassLoader();
      }

      public void setInterface(Class iface) {
         this.iface = iface;
      }

      public MulticastDelegate create() {
         this.setNamePrefix((MulticastDelegate.class$net$sf$cglib$reflect$MulticastDelegate == null ? (MulticastDelegate.class$net$sf$cglib$reflect$MulticastDelegate = MulticastDelegate.class$("com.comphenix.net.sf.cglib.reflect.MulticastDelegate")) : MulticastDelegate.class$net$sf$cglib$reflect$MulticastDelegate).getName());
         return (MulticastDelegate)super.create(this.iface.getName());
      }

      public void generateClass(ClassVisitor cv) {
         MethodInfo method = ReflectUtils.getMethodInfo(ReflectUtils.findInterfaceMethod(this.iface));
         ClassEmitter ce = new ClassEmitter(cv);
         ce.begin_class(46, 1, this.getClassName(), MULTICAST_DELEGATE, new Type[]{Type.getType(this.iface)}, "<generated>");
         EmitUtils.null_constructor(ce);
         this.emitProxy(ce, method);
         CodeEmitter e = ce.begin_method(1, NEW_INSTANCE, (Type[])null);
         e.new_instance_this();
         e.dup();
         e.invoke_constructor_this();
         e.return_value();
         e.end_method();
         e = ce.begin_method(1, ADD_DELEGATE, (Type[])null);
         e.load_this();
         e.load_arg(0);
         e.checkcast(Type.getType(this.iface));
         e.invoke_virtual_this(ADD_HELPER);
         e.return_value();
         e.end_method();
         ce.end_class();
      }

      private void emitProxy(ClassEmitter ce, final MethodInfo method) {
         final CodeEmitter e = EmitUtils.begin_method(ce, method, 1);
         Type returnType = method.getSignature().getReturnType();
         final boolean returns = returnType != Type.VOID_TYPE;
         final Local result = null;
         if (returns) {
            result = e.make_local(returnType);
            e.zero_or_null(returnType);
            e.store_local(result);
         }

         e.load_this();
         e.super_getfield("targets", Constants.TYPE_OBJECT_ARRAY);
         EmitUtils.process_array(e, Constants.TYPE_OBJECT_ARRAY, new ProcessArrayCallback() {
            public void processElement(Type type) {
               e.checkcast(Type.getType(Generator.this.iface));
               e.load_args();
               e.invoke(method);
               if (returns) {
                  e.store_local(result);
               }

            }
         });
         if (returns) {
            e.load_local(result);
         }

         e.return_value();
         e.end_method();
      }

      protected Object firstInstance(Class type) {
         return ((MulticastDelegate)ReflectUtils.newInstance(type)).newInstance();
      }

      protected Object nextInstance(Object instance) {
         return ((MulticastDelegate)instance).newInstance();
      }

      static {
         SOURCE = new AbstractClassGenerator.Source((MulticastDelegate.class$net$sf$cglib$reflect$MulticastDelegate == null ? (MulticastDelegate.class$net$sf$cglib$reflect$MulticastDelegate = MulticastDelegate.class$("com.comphenix.net.sf.cglib.reflect.MulticastDelegate")) : MulticastDelegate.class$net$sf$cglib$reflect$MulticastDelegate).getName());
         MULTICAST_DELEGATE = TypeUtils.parseType("com.comphenix.net.sf.cglib.reflect.MulticastDelegate");
         NEW_INSTANCE = new Signature("newInstance", MULTICAST_DELEGATE, new Type[0]);
         ADD_DELEGATE = new Signature("add", MULTICAST_DELEGATE, new Type[]{Constants.TYPE_OBJECT});
         ADD_HELPER = new Signature("addHelper", MULTICAST_DELEGATE, new Type[]{Constants.TYPE_OBJECT});
      }
   }
}
