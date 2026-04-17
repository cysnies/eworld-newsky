package org.jboss.logging;

import java.util.Locale;

public final class Messages {
   private Messages() {
   }

   public static Object getBundle(Class type) {
      return getBundle(type, Locale.getDefault());
   }

   public static Object getBundle(Class type, Locale locale) {
      String language = locale.getLanguage();
      String country = locale.getCountry();
      String variant = locale.getVariant();
      Class<? extends T> bundleClass = null;
      if (variant != null && variant.length() > 0) {
         try {
            bundleClass = Class.forName(join(type.getName(), "$bundle", language, country, variant), true, type.getClassLoader()).asSubclass(type);
         } catch (ClassNotFoundException var13) {
         }
      }

      if (bundleClass == null && country != null && country.length() > 0) {
         try {
            bundleClass = Class.forName(join(type.getName(), "$bundle", language, country, (String)null), true, type.getClassLoader()).asSubclass(type);
         } catch (ClassNotFoundException var12) {
         }
      }

      if (bundleClass == null && language != null && language.length() > 0) {
         try {
            bundleClass = Class.forName(join(type.getName(), "$bundle", language, (String)null, (String)null), true, type.getClassLoader()).asSubclass(type);
         } catch (ClassNotFoundException var11) {
         }
      }

      if (bundleClass == null) {
         try {
            bundleClass = Class.forName(join(type.getName(), "$bundle", (String)null, (String)null, (String)null), true, type.getClassLoader()).asSubclass(type);
         } catch (ClassNotFoundException var10) {
            throw new IllegalArgumentException("Invalid bundle " + type + " (implementation not found)");
         }
      }

      java.lang.reflect.Field field;
      try {
         field = bundleClass.getField("INSTANCE");
      } catch (NoSuchFieldException var9) {
         throw new IllegalArgumentException("Bundle implementation " + bundleClass + " has no instance field");
      }

      try {
         return type.cast(field.get((Object)null));
      } catch (IllegalAccessException e) {
         throw new IllegalArgumentException("Bundle implementation " + bundleClass + " could not be instantiated", e);
      }
   }

   private static String join(String interfaceName, String a, String b, String c, String d) {
      StringBuilder build = new StringBuilder();
      build.append(interfaceName).append('_').append(a);
      if (b != null && b.length() > 0) {
         build.append('_');
         build.append(b);
      }

      if (c != null && c.length() > 0) {
         build.append('_');
         build.append(c);
      }

      if (d != null && d.length() > 0) {
         build.append('_');
         build.append(d);
      }

      return build.toString();
   }
}
