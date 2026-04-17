package fr.neatmonster.nocheatplus.utilities.build;

import java.util.HashMap;
import java.util.Map;

public class BuildParameters {
   private static final Map fileContents = new HashMap();
   public static final String buildTimeString;
   public static final int buildNumber;
   public static final int testLevel;
   public static final int debugLevel;

   public static String getMappingValue(String path, String preset) {
      String input = (String)fileContents.get(path);
      return input == null ? preset : input;
   }

   public static String getString(String path, String preset) {
      String input = (String)fileContents.get(path);
      if (input == null) {
         return preset;
      } else {
         return input.startsWith("${") && input.endsWith("}") ? preset : input;
      }
   }

   public static Boolean getBoolean(String path, Boolean preset) {
      String input = (String)fileContents.get(path);
      return input == null ? preset : ResourceUtil.getBoolean(input, preset);
   }

   public static Integer getInteger(String path, Integer preset) {
      String input = (String)fileContents.get(path);
      return input == null ? preset : ResourceUtil.getInteger(input, preset);
   }

   static {
      String content = null;

      try {
         content = ResourceUtil.fetchResource(BuildParameters.class, "BuildParameters.properties");
      } catch (Throwable t) {
         t.printStackTrace();
      }

      if (content != null) {
         ResourceUtil.parseToMap(content, fileContents);
      }

      buildTimeString = getString("BUILD_TIMESTAMP", "?");
      buildNumber = getInteger("BUILD_NUMBER", Integer.MIN_VALUE);
      testLevel = getInteger("TEST_LEVEL", 0);
      debugLevel = getInteger("DEBUG_LEVEL", 0);
   }
}
