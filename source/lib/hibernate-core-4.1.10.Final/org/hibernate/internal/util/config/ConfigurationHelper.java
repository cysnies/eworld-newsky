package org.hibernate.internal.util.config;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.internal.util.collections.ArrayHelper;

public final class ConfigurationHelper {
   private static final String PLACEHOLDER_START = "${";

   private ConfigurationHelper() {
   }

   public static String getString(String name, Map values) {
      Object value = values.get(name);
      if (value == null) {
         return null;
      } else {
         return String.class.isInstance(value) ? (String)value : value.toString();
      }
   }

   public static String getString(String name, Map values, String defaultValue) {
      String value = getString(name, values);
      return value == null ? defaultValue : value;
   }

   public static boolean getBoolean(String name, Map values) {
      return getBoolean(name, values, false);
   }

   public static boolean getBoolean(String name, Map values, boolean defaultValue) {
      Object value = values.get(name);
      if (value == null) {
         return defaultValue;
      } else if (Boolean.class.isInstance(value)) {
         return (Boolean)value;
      } else if (String.class.isInstance(value)) {
         return Boolean.parseBoolean((String)value);
      } else {
         throw new ConfigurationException("Could not determine how to handle configuration value [name=" + name + ", value=" + value + "] as boolean");
      }
   }

   public static int getInt(String name, Map values, int defaultValue) {
      Object value = values.get(name);
      if (value == null) {
         return defaultValue;
      } else if (Integer.class.isInstance(value)) {
         return (Integer)value;
      } else if (String.class.isInstance(value)) {
         return Integer.parseInt((String)value);
      } else {
         throw new ConfigurationException("Could not determine how to handle configuration value [name=" + name + ", value=" + value + "(" + value.getClass().getName() + ")] as int");
      }
   }

   public static Integer getInteger(String name, Map values) {
      Object value = values.get(name);
      if (value == null) {
         return null;
      } else if (Integer.class.isInstance(value)) {
         return (Integer)value;
      } else if (String.class.isInstance(value)) {
         String trimmed = value.toString().trim();
         return trimmed.isEmpty() ? null : Integer.valueOf(trimmed);
      } else {
         throw new ConfigurationException("Could not determine how to handle configuration value [name=" + name + ", value=" + value + "(" + value.getClass().getName() + ")] as Integer");
      }
   }

   public static Map clone(Map configurationValues) {
      if (configurationValues == null) {
         return null;
      } else if (Properties.class.isInstance(configurationValues)) {
         return (Properties)((Properties)configurationValues).clone();
      } else {
         HashMap clone = new HashMap();

         for(Map.Entry entry : configurationValues.entrySet()) {
            clone.put(entry.getKey(), entry.getValue());
         }

         return clone;
      }
   }

   public static Properties maskOut(Properties props, String key) {
      Properties clone = (Properties)props.clone();
      if (clone.get(key) != null) {
         clone.setProperty(key, "****");
      }

      return clone;
   }

   public static String extractPropertyValue(String propertyName, Properties properties) {
      String value = properties.getProperty(propertyName);
      if (value == null) {
         return null;
      } else {
         value = value.trim();
         return StringHelper.isEmpty(value) ? null : value;
      }
   }

   public static Map toMap(String propertyName, String delim, Properties properties) {
      Map map = new HashMap();
      String value = extractPropertyValue(propertyName, properties);
      if (value != null) {
         StringTokenizer tokens = new StringTokenizer(value, delim);

         while(tokens.hasMoreTokens()) {
            map.put(tokens.nextToken(), tokens.hasMoreElements() ? tokens.nextToken() : "");
         }
      }

      return map;
   }

   public static String[] toStringArray(String propertyName, String delim, Properties properties) {
      return toStringArray(extractPropertyValue(propertyName, properties), delim);
   }

   public static String[] toStringArray(String stringForm, String delim) {
      return stringForm != null ? StringHelper.split(delim, stringForm) : ArrayHelper.EMPTY_STRING_ARRAY;
   }

   public static void resolvePlaceHolders(Map configurationValues) {
      Iterator itr = configurationValues.entrySet().iterator();

      while(itr.hasNext()) {
         Map.Entry entry = (Map.Entry)itr.next();
         Object value = entry.getValue();
         if (value != null && String.class.isInstance(value)) {
            String resolved = resolvePlaceHolder((String)value);
            if (!value.equals(resolved)) {
               if (resolved == null) {
                  itr.remove();
               } else {
                  entry.setValue(resolved);
               }
            }
         }
      }

   }

   public static String resolvePlaceHolder(String property) {
      if (property.indexOf("${") < 0) {
         return property;
      } else {
         StringBuilder buff = new StringBuilder();
         char[] chars = property.toCharArray();

         for(int pos = 0; pos < chars.length; ++pos) {
            if (chars[pos] == '$' && chars[pos + 1] == '{') {
               String systemPropertyName = "";

               int x;
               for(x = pos + 2; x < chars.length && chars[x] != '}'; ++x) {
                  systemPropertyName = systemPropertyName + chars[x];
                  if (x == chars.length - 1) {
                     throw new IllegalArgumentException("unmatched placeholder start [" + property + "]");
                  }
               }

               String systemProperty = extractFromSystem(systemPropertyName);
               buff.append(systemProperty == null ? "" : systemProperty);
               pos = x + 1;
               if (pos >= chars.length) {
                  break;
               }
            }

            buff.append(chars[pos]);
         }

         String rtn = buff.toString();
         return StringHelper.isEmpty(rtn) ? null : rtn;
      }
   }

   private static String extractFromSystem(String systemPropertyName) {
      try {
         return System.getProperty(systemPropertyName);
      } catch (Throwable var2) {
         return null;
      }
   }
}
