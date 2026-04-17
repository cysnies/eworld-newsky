package com.mysql.jdbc;

import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

public class Util {
   protected static Method systemNanoTimeMethod;
   private static Method CAST_METHOD;
   private static final TimeZone DEFAULT_TIMEZONE;
   private static Util enclosingInstance;
   private static boolean isJdbc4;
   private static boolean isColdFusion;
   // $FF: synthetic field
   static Class class$java$lang$System;
   // $FF: synthetic field
   static Class class$java$lang$Object;
   // $FF: synthetic field
   static Class class$java$lang$Class;

   public static boolean nanoTimeAvailable() {
      return systemNanoTimeMethod != null;
   }

   static final TimeZone getDefaultTimeZone() {
      return (TimeZone)DEFAULT_TIMEZONE.clone();
   }

   public static boolean isJdbc4() {
      return isJdbc4;
   }

   public static boolean isColdFusion() {
      return isColdFusion;
   }

   static String newCrypt(String password, String seed) {
      if (password != null && password.length() != 0) {
         long[] pw = newHash(seed);
         long[] msg = newHash(password);
         long max = 1073741823L;
         long seed1 = (pw[0] ^ msg[0]) % max;
         long seed2 = (pw[1] ^ msg[1]) % max;
         char[] chars = new char[seed.length()];

         for(int i = 0; i < seed.length(); ++i) {
            seed1 = (seed1 * 3L + seed2) % max;
            seed2 = (seed1 + seed2 + 33L) % max;
            double d = (double)seed1 / (double)max;
            byte b = (byte)((int)Math.floor(d * (double)31.0F + (double)64.0F));
            chars[i] = (char)b;
         }

         seed1 = (seed1 * 3L + seed2) % max;
         seed2 = (seed1 + seed2 + 33L) % max;
         double d = (double)seed1 / (double)max;
         byte b = (byte)((int)Math.floor(d * (double)31.0F));

         for(int i = 0; i < seed.length(); ++i) {
            chars[i] ^= (char)b;
         }

         return new String(chars);
      } else {
         return password;
      }
   }

   static long[] newHash(String password) {
      long nr = 1345345333L;
      long add = 7L;
      long nr2 = 305419889L;

      for(int i = 0; i < password.length(); ++i) {
         if (password.charAt(i) != ' ' && password.charAt(i) != '\t') {
            long tmp = (long)(255 & password.charAt(i));
            nr ^= ((nr & 63L) + add) * tmp + (nr << 8);
            nr2 += nr2 << 8 ^ nr;
            add += tmp;
         }
      }

      long[] result = new long[2];
      result[0] = nr & 2147483647L;
      result[1] = nr2 & 2147483647L;
      return result;
   }

   static String oldCrypt(String password, String seed) {
      long max = 33554431L;
      if (password != null && password.length() != 0) {
         long hp = oldHash(seed);
         long hm = oldHash(password);
         long nr = hp ^ hm;
         nr %= max;
         long s1 = nr;
         long s2 = nr / 2L;
         char[] chars = new char[seed.length()];

         for(int i = 0; i < seed.length(); ++i) {
            s1 = (s1 * 3L + s2) % max;
            s2 = (s1 + s2 + 33L) % max;
            double d = (double)s1 / (double)max;
            byte b = (byte)((int)Math.floor(d * (double)31.0F + (double)64.0F));
            chars[i] = (char)b;
         }

         return new String(chars);
      } else {
         return password;
      }
   }

   static long oldHash(String password) {
      long nr = 1345345333L;
      long nr2 = 7L;

      for(int i = 0; i < password.length(); ++i) {
         if (password.charAt(i) != ' ' && password.charAt(i) != '\t') {
            long tmp = (long)password.charAt(i);
            nr ^= ((nr & 63L) + nr2) * tmp + (nr << 8);
            nr2 += tmp;
         }
      }

      return nr & 2147483647L;
   }

   private static RandStructcture randomInit(long seed1, long seed2) {
      RandStructcture randStruct = enclosingInstance.new RandStructcture();
      randStruct.maxValue = 1073741823L;
      randStruct.maxValueDbl = (double)randStruct.maxValue;
      randStruct.seed1 = seed1 % randStruct.maxValue;
      randStruct.seed2 = seed2 % randStruct.maxValue;
      return randStruct;
   }

   public static Object readObject(ResultSet resultSet, int index) throws Exception {
      ObjectInputStream objIn = new ObjectInputStream(resultSet.getBinaryStream(index));
      Object obj = objIn.readObject();
      objIn.close();
      return obj;
   }

   private static double rnd(RandStructcture randStruct) {
      randStruct.seed1 = (randStruct.seed1 * 3L + randStruct.seed2) % randStruct.maxValue;
      randStruct.seed2 = (randStruct.seed1 + randStruct.seed2 + 33L) % randStruct.maxValue;
      return (double)randStruct.seed1 / randStruct.maxValueDbl;
   }

   public static String scramble(String message, String password) {
      byte[] to = new byte[8];
      String val = "";
      message = message.substring(0, 8);
      if (password != null && password.length() > 0) {
         long[] hashPass = newHash(password);
         long[] hashMessage = newHash(message);
         RandStructcture randStruct = randomInit(hashPass[0] ^ hashMessage[0], hashPass[1] ^ hashMessage[1]);
         int msgPos = 0;
         int msgLength = message.length();

         for(int toPos = 0; msgPos++ < msgLength; to[toPos++] = (byte)((int)(Math.floor(rnd(randStruct) * (double)31.0F) + (double)64.0F))) {
         }

         byte extra = (byte)((int)Math.floor(rnd(randStruct) * (double)31.0F));

         for(int i = 0; i < to.length; ++i) {
            to[i] ^= extra;
         }

         val = new String(to);
      }

      return val;
   }

   public static String stackTraceToString(Throwable ex) {
      StringBuffer traceBuf = new StringBuffer();
      traceBuf.append(Messages.getString("Util.1"));
      if (ex != null) {
         traceBuf.append(ex.getClass().getName());
         String message = ex.getMessage();
         if (message != null) {
            traceBuf.append(Messages.getString("Util.2"));
            traceBuf.append(message);
         }

         StringWriter out = new StringWriter();
         PrintWriter printOut = new PrintWriter(out);
         ex.printStackTrace(printOut);
         traceBuf.append(Messages.getString("Util.3"));
         traceBuf.append(out.toString());
      }

      traceBuf.append(Messages.getString("Util.4"));
      return traceBuf.toString();
   }

   public static Object getInstance(String className, Class[] argTypes, Object[] args) throws SQLException {
      try {
         return handleNewInstance(Class.forName(className).getConstructor(argTypes), args);
      } catch (SecurityException e) {
         throw SQLError.createSQLException("Can't instantiate required class", "S1000", e);
      } catch (NoSuchMethodException e) {
         throw SQLError.createSQLException("Can't instantiate required class", "S1000", e);
      } catch (ClassNotFoundException e) {
         throw SQLError.createSQLException("Can't instantiate required class", "S1000", e);
      }
   }

   public static final Object handleNewInstance(Constructor ctor, Object[] args) throws SQLException {
      try {
         return ctor.newInstance(args);
      } catch (IllegalArgumentException e) {
         throw SQLError.createSQLException("Can't instantiate required class", "S1000", e);
      } catch (InstantiationException e) {
         throw SQLError.createSQLException("Can't instantiate required class", "S1000", e);
      } catch (IllegalAccessException e) {
         throw SQLError.createSQLException("Can't instantiate required class", "S1000", e);
      } catch (InvocationTargetException e) {
         Throwable target = e.getTargetException();
         if (target instanceof SQLException) {
            throw (SQLException)target;
         } else {
            if (target instanceof ExceptionInInitializerError) {
               target = ((ExceptionInInitializerError)target).getException();
            }

            throw SQLError.createSQLException(target.toString(), "S1000");
         }
      }
   }

   public static boolean interfaceExists(String hostname) {
      try {
         Class networkInterfaceClass = Class.forName("java.net.NetworkInterface");
         return networkInterfaceClass.getMethod("getByName", (Class[])null).invoke(networkInterfaceClass, hostname) != null;
      } catch (Throwable var2) {
         return false;
      }
   }

   public static Object cast(Object invokeOn, Object toCast) {
      if (CAST_METHOD != null) {
         try {
            return CAST_METHOD.invoke(invokeOn, toCast);
         } catch (Throwable var3) {
            return null;
         }
      } else {
         return null;
      }
   }

   public static long getCurrentTimeNanosOrMillis() {
      if (systemNanoTimeMethod != null) {
         try {
            return (Long)systemNanoTimeMethod.invoke((Object)null, (Object[])null);
         } catch (IllegalArgumentException var1) {
         } catch (IllegalAccessException var2) {
         } catch (InvocationTargetException var3) {
         }
      }

      return System.currentTimeMillis();
   }

   public static void resultSetToMap(Map mappedValues, ResultSet rs) throws SQLException {
      while(rs.next()) {
         mappedValues.put(rs.getObject(1), rs.getObject(2));
      }

   }

   public static Map calculateDifferences(Map map1, Map map2) {
      Map diffMap = new HashMap();
      Iterator map1Entries = map1.entrySet().iterator();

      while(true) {
         Object key;
         Object var10;
         Object var11;
         while(true) {
            if (!map1Entries.hasNext()) {
               return diffMap;
            }

            Map.Entry entry = (Map.Entry)map1Entries.next();
            key = entry.getKey();
            Number value1 = null;
            Number value2 = null;
            if (entry.getValue() instanceof Number) {
               var10 = (Number)entry.getValue();
               var11 = (Number)map2.get(key);
               break;
            }

            try {
               var10 = new Double(entry.getValue().toString());
               var11 = new Double(map2.get(key).toString());
               break;
            } catch (NumberFormatException var9) {
            }
         }

         if (!var10.equals(var11)) {
            if (var10 instanceof Byte) {
               diffMap.put(key, new Byte((byte)((Byte)var11 - (Byte)var10)));
            } else if (var10 instanceof Short) {
               diffMap.put(key, new Short((short)((Short)var11 - (Short)var10)));
            } else if (var10 instanceof Integer) {
               diffMap.put(key, new Integer((Integer)var11 - (Integer)var10));
            } else if (var10 instanceof Long) {
               diffMap.put(key, new Long((Long)var11 - (Long)var10));
            } else if (var10 instanceof Float) {
               diffMap.put(key, new Float((Float)var11 - (Float)var10));
            } else if (var10 instanceof Double) {
               diffMap.put(key, new Double((double)(((Double)var11).shortValue() - ((Double)var10).shortValue())));
            } else if (var10 instanceof BigDecimal) {
               diffMap.put(key, ((BigDecimal)var11).subtract((BigDecimal)var10));
            } else if (var10 instanceof BigInteger) {
               diffMap.put(key, ((BigInteger)var11).subtract((BigInteger)var10));
            }
         }
      }
   }

   public static List loadExtensions(Connection conn, Properties props, String extensionClassNames, String errorMessageKey) throws SQLException {
      List extensionList = new LinkedList();
      List interceptorsToCreate = StringUtils.split(extensionClassNames, ",", true);
      Iterator iter = interceptorsToCreate.iterator();
      String className = null;

      try {
         while(iter.hasNext()) {
            className = iter.next().toString();
            Extension extensionInstance = (Extension)Class.forName(className).newInstance();
            extensionInstance.init(conn, props);
            extensionList.add(extensionInstance);
         }

         return extensionList;
      } catch (Throwable t) {
         SQLException sqlEx = SQLError.createSQLException(Messages.getString(errorMessageKey, new Object[]{className}));
         sqlEx.initCause(t);
         throw sqlEx;
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
      try {
         systemNanoTimeMethod = (class$java$lang$System == null ? (class$java$lang$System = class$("java.lang.System")) : class$java$lang$System).getMethod("nanoTime", (Class[])null);
      } catch (SecurityException var3) {
         systemNanoTimeMethod = null;
      } catch (NoSuchMethodException var4) {
         systemNanoTimeMethod = null;
      }

      DEFAULT_TIMEZONE = TimeZone.getDefault();
      enclosingInstance = new Util();
      isJdbc4 = false;
      isColdFusion = false;

      try {
         CAST_METHOD = (class$java$lang$Class == null ? (class$java$lang$Class = class$("java.lang.Class")) : class$java$lang$Class).getMethod("cast", class$java$lang$Object == null ? (class$java$lang$Object = class$("java.lang.Object")) : class$java$lang$Object);
      } catch (Throwable var2) {
      }

      try {
         Class.forName("java.sql.NClob");
         isJdbc4 = true;
      } catch (Throwable var1) {
         isJdbc4 = false;
      }

      String loadedFrom = stackTraceToString(new Throwable());
      if (loadedFrom != null) {
         isColdFusion = loadedFrom.indexOf("coldfusion") != -1;
      } else {
         isColdFusion = false;
      }

   }

   class RandStructcture {
      long maxValue;
      double maxValueDbl;
      long seed1;
      long seed2;
   }
}
