package fr.neatmonster.nocheatplus.utilities.build;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class ResourceUtil {
   public static String fetchResource(Class clazz, String path) {
      String className = clazz.getSimpleName() + ".class";
      String classPath = clazz.getResource(className).toString();
      if (!classPath.startsWith("jar")) {
         return null;
      } else {
         String absPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) + "/" + path;

         try {
            URL url = new URL(absPath);
            BufferedReader r = null;

            try {
               Object obj = url.getContent();
               if (!(obj instanceof InputStream)) {
                  return null;
               } else {
                  r = new BufferedReader(new InputStreamReader((InputStream)obj));
                  StringBuilder builder = new StringBuilder();

                  for(String last = r.readLine(); last != null; last = r.readLine()) {
                     builder.append(last);
                     builder.append("\n");
                  }

                  r.close();
                  return builder.toString();
               }
            } catch (IOException var11) {
               if (r != null) {
                  try {
                     r.close();
                  } catch (IOException var10) {
                  }
               }

               return null;
            }
         } catch (MalformedURLException var12) {
            return null;
         }
      }
   }

   public static void parseToMap(String input, Map map) {
      String[] split = input.split("\n");

      for(String line : split) {
         String trimmed = line.trim();
         if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
            String[] parts = line.split("=", 2);
            if (parts.length == 1) {
               map.put(parts[0].trim(), "");
            } else {
               map.put(parts[0].trim(), parts[1].trim());
            }
         }
      }

   }

   public static Boolean getBoolean(String input, Boolean preset) {
      if (input == null) {
         return preset;
      } else {
         input = input.trim().toLowerCase();
         if (input.matches("1|true|yes")) {
            return true;
         } else {
            return input.matches("0|false|no") ? false : preset;
         }
      }
   }

   public static Integer getInteger(String input, Integer preset) {
      if (input == null) {
         return preset;
      } else {
         try {
            return Integer.parseInt(input);
         } catch (NumberFormatException var3) {
            return preset;
         }
      }
   }
}
