package com.comphenix.net.sf.cglib.util;

import com.comphenix.net.sf.cglib.asm.ClassVisitor;
import com.comphenix.net.sf.cglib.asm.Label;
import com.comphenix.net.sf.cglib.asm.Type;
import com.comphenix.net.sf.cglib.core.AbstractClassGenerator;
import com.comphenix.net.sf.cglib.core.ClassEmitter;
import com.comphenix.net.sf.cglib.core.CodeEmitter;
import com.comphenix.net.sf.cglib.core.EmitUtils;
import com.comphenix.net.sf.cglib.core.KeyFactory;
import com.comphenix.net.sf.cglib.core.ObjectSwitchCallback;
import com.comphenix.net.sf.cglib.core.ReflectUtils;
import com.comphenix.net.sf.cglib.core.Signature;
import com.comphenix.net.sf.cglib.core.TypeUtils;
import java.util.Arrays;
import java.util.List;

public abstract class StringSwitcher {
   private static final Type STRING_SWITCHER = TypeUtils.parseType("com.comphenix.net.sf.cglib.util.StringSwitcher");
   private static final Signature INT_VALUE = TypeUtils.parseSignature("int intValue(String)");
   private static final StringSwitcherKey KEY_FACTORY;
   // $FF: synthetic field
   static Class class$net$sf$cglib$util$StringSwitcher$StringSwitcherKey;
   // $FF: synthetic field
   static Class class$net$sf$cglib$util$StringSwitcher;

   public static StringSwitcher create(String[] strings, int[] ints, boolean fixedInput) {
      Generator gen = new Generator();
      gen.setStrings(strings);
      gen.setInts(ints);
      gen.setFixedInput(fixedInput);
      return gen.create();
   }

   protected StringSwitcher() {
   }

   public abstract int intValue(String var1);

   // $FF: synthetic method
   static Class class$(String x0) {
      try {
         return Class.forName(x0);
      } catch (ClassNotFoundException x1) {
         throw new NoClassDefFoundError(x1.getMessage());
      }
   }

   static {
      KEY_FACTORY = (StringSwitcherKey)KeyFactory.create(class$net$sf$cglib$util$StringSwitcher$StringSwitcherKey == null ? (class$net$sf$cglib$util$StringSwitcher$StringSwitcherKey = class$("com.comphenix.net.sf.cglib.util.StringSwitcher$StringSwitcherKey")) : class$net$sf$cglib$util$StringSwitcher$StringSwitcherKey);
   }

   public static class Generator extends AbstractClassGenerator {
      private static final AbstractClassGenerator.Source SOURCE;
      private String[] strings;
      private int[] ints;
      private boolean fixedInput;

      public Generator() {
         super(SOURCE);
      }

      public void setStrings(String[] strings) {
         this.strings = strings;
      }

      public void setInts(int[] ints) {
         this.ints = ints;
      }

      public void setFixedInput(boolean fixedInput) {
         this.fixedInput = fixedInput;
      }

      protected ClassLoader getDefaultClassLoader() {
         return this.getClass().getClassLoader();
      }

      public StringSwitcher create() {
         this.setNamePrefix((StringSwitcher.class$net$sf$cglib$util$StringSwitcher == null ? (StringSwitcher.class$net$sf$cglib$util$StringSwitcher = StringSwitcher.class$("com.comphenix.net.sf.cglib.util.StringSwitcher")) : StringSwitcher.class$net$sf$cglib$util$StringSwitcher).getName());
         Object key = StringSwitcher.KEY_FACTORY.newInstance(this.strings, this.ints, this.fixedInput);
         return (StringSwitcher)super.create(key);
      }

      public void generateClass(ClassVisitor v) throws Exception {
         ClassEmitter ce = new ClassEmitter(v);
         ce.begin_class(46, 1, this.getClassName(), StringSwitcher.STRING_SWITCHER, (Type[])null, "<generated>");
         EmitUtils.null_constructor(ce);
         final CodeEmitter e = ce.begin_method(1, StringSwitcher.INT_VALUE, (Type[])null);
         e.load_arg(0);
         final List stringList = Arrays.asList(this.strings);
         int style = this.fixedInput ? 2 : 1;
         EmitUtils.string_switch(e, this.strings, style, new ObjectSwitchCallback() {
            public void processCase(Object key, Label end) {
               e.push(Generator.this.ints[stringList.indexOf(key)]);
               e.return_value();
            }

            public void processDefault() {
               e.push(-1);
               e.return_value();
            }
         });
         e.end_method();
         ce.end_class();
      }

      protected Object firstInstance(Class type) {
         return (StringSwitcher)ReflectUtils.newInstance(type);
      }

      protected Object nextInstance(Object instance) {
         return instance;
      }

      static {
         SOURCE = new AbstractClassGenerator.Source((StringSwitcher.class$net$sf$cglib$util$StringSwitcher == null ? (StringSwitcher.class$net$sf$cglib$util$StringSwitcher = StringSwitcher.class$("com.comphenix.net.sf.cglib.util.StringSwitcher")) : StringSwitcher.class$net$sf$cglib$util$StringSwitcher).getName());
      }
   }

   interface StringSwitcherKey {
      Object newInstance(String[] var1, int[] var2, boolean var3);
   }
}
