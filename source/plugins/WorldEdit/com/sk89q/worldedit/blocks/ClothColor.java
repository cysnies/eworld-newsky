package com.sk89q.worldedit.blocks;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum ClothColor {
   WHITE(0, "White", "white"),
   ORANGE(1, "Orange", "orange"),
   MAGENTA(2, "Magenta", "magenta"),
   LIGHT_BLUE(3, "Light blue", "lightblue"),
   YELLOW(4, "Yellow", "yellow"),
   LIGHT_GREEN(5, "Light green", "lightgreen"),
   PINK(6, "Pink", new String[]{"pink", "lightred"}),
   GRAY(7, "Gray", new String[]{"grey", "gray"}),
   LIGHT_GRAY(8, "Light gray", new String[]{"lightgrey", "lightgray"}),
   CYAN(9, "Cyan", new String[]{"cyan", "turquoise"}),
   PURPLE(10, "Purple", new String[]{"purple", "violet"}),
   BLUE(11, "Blue", "blue"),
   BROWN(12, "Brown", new String[]{"brown", "cocoa", "coffee"}),
   DARK_GREEN(13, "Dark green", new String[]{"green", "darkgreen", "cactusgreen", "cactigreen"}),
   RED(14, "Red", "red"),
   BLACK(15, "Black", "black");

   private static final Map ids = new HashMap();
   private static final Map lookup = new HashMap();
   private final int id;
   private final String name;
   private final String[] lookupKeys;

   private ClothColor(int id, String name, String lookupKey) {
      this.id = id;
      this.name = name;
      this.lookupKeys = new String[]{lookupKey};
   }

   private ClothColor(int id, String name, String[] lookupKeys) {
      this.id = id;
      this.name = name;
      this.lookupKeys = lookupKeys;
   }

   public static ClothColor fromID(int id) {
      return (ClothColor)ids.get(id);
   }

   public static ClothColor lookup(String name) {
      return (ClothColor)lookup.get(name.toLowerCase());
   }

   public int getID() {
      return this.id;
   }

   public String getName() {
      return this.name;
   }

   static {
      for(ClothColor type : EnumSet.allOf(ClothColor.class)) {
         ids.put(type.id, type);

         for(String key : type.lookupKeys) {
            lookup.put(key, type);
         }
      }

   }

   public static final class ID {
      public static final int WHITE = 0;
      public static final int ORANGE = 1;
      public static final int MAGENTA = 2;
      public static final int LIGHT_BLUE = 3;
      public static final int YELLOW = 4;
      public static final int LIGHT_GREEN = 5;
      public static final int PINK = 6;
      public static final int GRAY = 7;
      public static final int LIGHT_GRAY = 8;
      public static final int CYAN = 9;
      public static final int PURPLE = 10;
      public static final int BLUE = 11;
      public static final int BROWN = 12;
      public static final int DARK_GREEN = 13;
      public static final int RED = 14;
      public static final int BLACK = 15;
   }
}
