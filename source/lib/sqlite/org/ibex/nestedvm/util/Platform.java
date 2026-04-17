package org.ibex.nestedvm.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.text.DateFormatSymbols;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.TimeZone;

public abstract class Platform {
   private static final Platform p;
   // $FF: synthetic field
   static Class class$org$ibex$nestedvm$util$Platform;

   Platform() {
   }

   public static String getProperty(String var0) {
      try {
         return System.getProperty(var0);
      } catch (SecurityException var2) {
         return null;
      }
   }

   abstract boolean _atomicCreateFile(File var1) throws IOException;

   public static boolean atomicCreateFile(File var0) throws IOException {
      return p._atomicCreateFile(var0);
   }

   abstract Seekable.Lock _lockFile(Seekable var1, RandomAccessFile var2, long var3, long var5, boolean var7) throws IOException;

   public static Seekable.Lock lockFile(Seekable var0, RandomAccessFile var1, long var2, long var4, boolean var6) throws IOException {
      return p._lockFile(var0, var1, var2, var4, var6);
   }

   abstract void _socketHalfClose(Socket var1, boolean var2) throws IOException;

   public static void socketHalfClose(Socket var0, boolean var1) throws IOException {
      p._socketHalfClose(var0, var1);
   }

   abstract void _socketSetKeepAlive(Socket var1, boolean var2) throws SocketException;

   public static void socketSetKeepAlive(Socket var0, boolean var1) throws SocketException {
      p._socketSetKeepAlive(var0, var1);
   }

   abstract InetAddress _inetAddressFromBytes(byte[] var1) throws UnknownHostException;

   public static InetAddress inetAddressFromBytes(byte[] var0) throws UnknownHostException {
      return p._inetAddressFromBytes(var0);
   }

   abstract String _timeZoneGetDisplayName(TimeZone var1, boolean var2, boolean var3, Locale var4);

   public static String timeZoneGetDisplayName(TimeZone var0, boolean var1, boolean var2, Locale var3) {
      return p._timeZoneGetDisplayName(var0, var1, var2, var3);
   }

   public static String timeZoneGetDisplayName(TimeZone var0, boolean var1, boolean var2) {
      return timeZoneGetDisplayName(var0, var1, var2, Locale.getDefault());
   }

   abstract void _setFileLength(RandomAccessFile var1, int var2) throws IOException;

   public static void setFileLength(RandomAccessFile var0, int var1) throws IOException {
      p._setFileLength(var0, var1);
   }

   abstract File[] _listRoots();

   public static File[] listRoots() {
      return p._listRoots();
   }

   abstract File _getRoot(File var1);

   public static File getRoot(File var0) {
      return p._getRoot(var0);
   }

   // $FF: synthetic method
   static Class class$(String var0) {
      try {
         return Class.forName(var0);
      } catch (ClassNotFoundException var2) {
         throw new NoClassDefFoundError(var2.getMessage());
      }
   }

   static {
      float var0;
      try {
         if (getProperty("java.vm.name").equals("SableVM")) {
            var0 = 1.2F;
         } else {
            var0 = Float.valueOf(getProperty("java.specification.version"));
         }
      } catch (Exception var4) {
         System.err.println("WARNING: " + var4 + " while trying to find jvm version -  assuming 1.1");
         var0 = 1.1F;
      }

      String var1;
      if (var0 >= 1.4F) {
         var1 = "Jdk14";
      } else if (var0 >= 1.3F) {
         var1 = "Jdk13";
      } else if (var0 >= 1.2F) {
         var1 = "Jdk12";
      } else {
         if (!(var0 >= 1.1F)) {
            throw new Error("JVM Specification version: " + var0 + " is too old. (see org.ibex.util.Platform to add support)");
         }

         var1 = "Jdk11";
      }

      try {
         p = (Platform)Class.forName((class$org$ibex$nestedvm$util$Platform == null ? (class$org$ibex$nestedvm$util$Platform = class$("org.ibex.nestedvm.util.Platform")) : class$org$ibex$nestedvm$util$Platform).getName() + "$" + var1).newInstance();
      } catch (Exception var3) {
         var3.printStackTrace();
         throw new Error("Error instansiating platform class");
      }
   }

   static class Jdk11 extends Platform {
      boolean _atomicCreateFile(File var1) throws IOException {
         if (var1.exists()) {
            return false;
         } else {
            (new FileOutputStream(var1)).close();
            return true;
         }
      }

      Seekable.Lock _lockFile(Seekable var1, RandomAccessFile var2, long var3, long var5, boolean var7) throws IOException {
         throw new IOException("file locking requires jdk 1.4+");
      }

      void _socketHalfClose(Socket var1, boolean var2) throws IOException {
         throw new IOException("half closing sockets not supported");
      }

      InetAddress _inetAddressFromBytes(byte[] var1) throws UnknownHostException {
         if (var1.length != 4) {
            throw new UnknownHostException("only ipv4 addrs supported");
         } else {
            return InetAddress.getByName("" + (var1[0] & 255) + "." + (var1[1] & 255) + "." + (var1[2] & 255) + "." + (var1[3] & 255));
         }
      }

      void _socketSetKeepAlive(Socket var1, boolean var2) throws SocketException {
         if (var2) {
            throw new SocketException("keepalive not supported");
         }
      }

      String _timeZoneGetDisplayName(TimeZone var1, boolean var2, boolean var3, Locale var4) {
         String[][] var5 = (new DateFormatSymbols(var4)).getZoneStrings();
         String var6 = var1.getID();

         for(int var7 = 0; var7 < var5.length; ++var7) {
            if (var5[var7][0].equals(var6)) {
               return var5[var7][var2 ? (var3 ? 3 : 4) : (var3 ? 1 : 2)];
            }
         }

         StringBuffer var9 = new StringBuffer("GMT");
         int var8 = var1.getRawOffset() / 1000;
         if (var8 < 0) {
            var9.append("-");
            var8 = -var8;
         } else {
            var9.append("+");
         }

         var9.append(var8 / 3600);
         var8 %= 3600;
         if (var8 > 0) {
            var9.append(":").append(var8 / 60);
         }

         var8 %= 60;
         if (var8 > 0) {
            var9.append(":").append(var8);
         }

         return var9.toString();
      }

      void _setFileLength(RandomAccessFile var1, int var2) throws IOException {
         FileInputStream var3 = new FileInputStream(var1.getFD());
         FileOutputStream var4 = new FileOutputStream(var1.getFD());

         byte[] var5;
         int var6;
         for(var5 = new byte[1024]; var2 > 0; var2 -= var6) {
            var6 = ((InputStream)var3).read(var5, 0, Math.min(var2, var5.length));
            if (var6 == -1) {
               break;
            }

            ((OutputStream)var4).write(var5, 0, var6);
         }

         if (var2 != 0) {
            for(int var7 = 0; var7 < var5.length; ++var7) {
               var5[var7] = 0;
            }

            while(var2 > 0) {
               ((OutputStream)var4).write(var5, 0, Math.min(var2, var5.length));
               var2 -= var5.length;
            }

         }
      }

      RandomAccessFile _truncatedRandomAccessFile(File var1, String var2) throws IOException {
         (new FileOutputStream(var1)).close();
         return new RandomAccessFile(var1, var2);
      }

      File[] _listRoots() {
         String[] var1 = new String[]{"java.home", "java.class.path", "java.library.path", "java.io.tmpdir", "java.ext.dirs", "user.home", "user.dir"};
         Hashtable var2 = new Hashtable();

         for(int var3 = 0; var3 < var1.length; ++var3) {
            String var4 = getProperty(var1[var3]);
            if (var4 != null) {
               while(true) {
                  String var5 = var4;
                  int var6;
                  if ((var6 = var4.indexOf(File.pathSeparatorChar)) != -1) {
                     var5 = var4.substring(0, var6);
                     var4 = var4.substring(var6 + 1);
                  }

                  File var7 = getRoot(new File(var5));
                  var2.put(var7, Boolean.TRUE);
                  if (var6 == -1) {
                     break;
                  }
               }
            }
         }

         File[] var8 = new File[var2.size()];
         int var9 = 0;

         for(Enumeration var10 = var2.keys(); var10.hasMoreElements(); var8[var9++] = (File)var10.nextElement()) {
         }

         return var8;
      }

      File _getRoot(File var1) {
         if (!var1.isAbsolute()) {
            var1 = new File(var1.getAbsolutePath());
         }

         String var2;
         while((var2 = var1.getParent()) != null) {
            var1 = new File(var2);
         }

         if (var1.getPath().length() == 0) {
            var1 = new File("/");
         }

         return var1;
      }
   }

   static class Jdk12 extends Jdk11 {
      boolean _atomicCreateFile(File var1) throws IOException {
         return var1.createNewFile();
      }

      String _timeZoneGetDisplayName(TimeZone var1, boolean var2, boolean var3, Locale var4) {
         return var1.getDisplayName(var2, var3 ? 1 : 0, var4);
      }

      void _setFileLength(RandomAccessFile var1, int var2) throws IOException {
         var1.setLength((long)var2);
      }

      File[] _listRoots() {
         return File.listRoots();
      }
   }

   static class Jdk13 extends Jdk12 {
      void _socketHalfClose(Socket var1, boolean var2) throws IOException {
         if (var2) {
            var1.shutdownOutput();
         } else {
            var1.shutdownInput();
         }

      }

      void _socketSetKeepAlive(Socket var1, boolean var2) throws SocketException {
         var1.setKeepAlive(var2);
      }
   }

   static class Jdk14 extends Jdk13 {
      InetAddress _inetAddressFromBytes(byte[] var1) throws UnknownHostException {
         return InetAddress.getByAddress(var1);
      }

      Seekable.Lock _lockFile(Seekable var1, RandomAccessFile var2, long var3, long var5, boolean var7) throws IOException {
         FileLock var8;
         try {
            var8 = var3 == 0L && var5 == 0L ? var2.getChannel().lock() : var2.getChannel().tryLock(var3, var5, var7);
         } catch (OverlappingFileLockException var10) {
            var8 = null;
         }

         return var8 == null ? null : new Jdk14FileLock(var1, var8);
      }
   }

   private static final class Jdk14FileLock extends Seekable.Lock {
      private final Seekable s;
      private final FileLock l;

      Jdk14FileLock(Seekable var1, FileLock var2) {
         this.s = var1;
         this.l = var2;
      }

      public Seekable seekable() {
         return this.s;
      }

      public boolean isShared() {
         return this.l.isShared();
      }

      public boolean isValid() {
         return this.l.isValid();
      }

      public void release() throws IOException {
         this.l.release();
      }

      public long position() {
         return this.l.position();
      }

      public long size() {
         return this.l.size();
      }

      public String toString() {
         return this.l.toString();
      }
   }
}
