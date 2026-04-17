package com.mysql.jdbc;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {
   private static final String BUNDLE_NAME = "com.mysql.jdbc.LocalizedErrorMessages";
   private static final ResourceBundle RESOURCE_BUNDLE;
   // $FF: synthetic field
   static Class class$com$mysql$jdbc$Messages;

   public static String getString(String key) {
      if (RESOURCE_BUNDLE == null) {
         throw new RuntimeException("Localized messages from resource bundle 'com.mysql.jdbc.LocalizedErrorMessages' not loaded during initialization of driver.");
      } else {
         try {
            if (key == null) {
               throw new IllegalArgumentException("Message key can not be null");
            } else {
               String message = RESOURCE_BUNDLE.getString(key);
               if (message == null) {
                  message = "Missing error message for key '" + key + "'";
               }

               return message;
            }
         } catch (MissingResourceException var2) {
            return '!' + key + '!';
         }
      }
   }

   public static String getString(String key, Object[] args) {
      return MessageFormat.format(getString(key), args);
   }

   private Messages() {
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
      ResourceBundle temp = null;

      try {
         temp = ResourceBundle.getBundle("com.mysql.jdbc.LocalizedErrorMessages", Locale.getDefault(), (class$com$mysql$jdbc$Messages == null ? (class$com$mysql$jdbc$Messages = class$("com.mysql.jdbc.Messages")) : class$com$mysql$jdbc$Messages).getClassLoader());
      } catch (Throwable t) {
         try {
            temp = ResourceBundle.getBundle("com.mysql.jdbc.LocalizedErrorMessages");
         } catch (Throwable t2) {
            RuntimeException rt = new RuntimeException("Can't load resource bundle due to underlying exception " + t.toString());
            rt.initCause(t2);
            throw rt;
         }
      } finally {
         RESOURCE_BUNDLE = temp;
      }

   }
}
