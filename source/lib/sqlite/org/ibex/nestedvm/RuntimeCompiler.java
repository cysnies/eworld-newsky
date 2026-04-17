package org.ibex.nestedvm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.ibex.nestedvm.util.Seekable;

public class RuntimeCompiler {
   public static Class compile(Seekable var0) throws IOException, Compiler.Exn {
      return compile(var0, (String)null);
   }

   public static Class compile(Seekable var0, String var1) throws IOException, Compiler.Exn {
      return compile(var0, var1, (String)null);
   }

   public static Class compile(Seekable var0, String var1, String var2) throws IOException, Compiler.Exn {
      String var3 = "nestedvm.runtimecompiled";

      byte[] var4;
      try {
         var4 = runCompiler(var0, var3, var1, var2, (String)null);
      } catch (Compiler.Exn var6) {
         if (var6.getMessage() == null && var6.getMessage().indexOf("constant pool full") == -1) {
            throw var6;
         }

         var4 = runCompiler(var0, var3, var1, var2, "lessconstants");
      }

      return (new SingleClassLoader()).fromBytes(var3, var4);
   }

   private static byte[] runCompiler(Seekable var0, String var1, String var2, String var3, String var4) throws IOException, Compiler.Exn {
      ByteArrayOutputStream var5 = new ByteArrayOutputStream();

      try {
         ClassFileCompiler var6 = new ClassFileCompiler(var0, var1, var5);
         var6.parseOptions("nosupportcall,maxinsnpermethod=256");
         var6.setSource(var3);
         if (var2 != null) {
            var6.parseOptions(var2);
         }

         if (var4 != null) {
            var6.parseOptions(var4);
         }

         var6.go();
      } finally {
         var0.seek(0);
      }

      var5.close();
      return var5.toByteArray();
   }

   public static void main(String[] var0) throws Exception {
      if (var0.length == 0) {
         System.err.println("Usage: RuntimeCompiler mipsbinary");
         System.exit(1);
      }

      UnixRuntime var1 = (UnixRuntime)compile(new Seekable.File(var0[0]), "unixruntime").newInstance();
      System.err.println("Instansiated: " + var1);
      System.exit(UnixRuntime.runAndExec(var1, var0));
   }

   private RuntimeCompiler() {
   }

   private static class SingleClassLoader extends ClassLoader {
      private SingleClassLoader() {
      }

      public Class loadClass(String var1, boolean var2) throws ClassNotFoundException {
         return super.loadClass(var1, var2);
      }

      public Class fromBytes(String var1, byte[] var2) {
         return this.fromBytes(var1, var2, 0, var2.length);
      }

      public Class fromBytes(String var1, byte[] var2, int var3, int var4) {
         Class var5 = super.defineClass(var1, var2, var3, var4);
         this.resolveClass(var5);
         return var5;
      }
   }
}
