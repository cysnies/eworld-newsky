package fr.neatmonster.nocheatplus.utilities;

import org.bukkit.ChatColor;

public class ColorUtil {
   private static final String allColorChars = "0123456789AaBbCcDdEeFfKkLlMmNnOoRr";

   public static String removeColors(String text) {
      if (text.length() <= 1) {
         return text;
      } else {
         char[] chars = text.toCharArray();
         int srcIndex = 0;

         while(chars[srcIndex] != '&' || "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(chars[srcIndex + 1]) <= -1) {
            ++srcIndex;
            if (srcIndex >= chars.length - 1) {
               break;
            }
         }

         if (srcIndex >= chars.length - 1) {
            return text;
         } else {
            char[] newChars = new char[chars.length - 2];
            int tgtIndex = 0;

            for(tgtIndex = 0; tgtIndex < srcIndex; ++tgtIndex) {
               newChars[tgtIndex] = chars[tgtIndex];
            }

            for(int var5 = srcIndex + 2; var5 < chars.length; ++var5) {
               if (chars[var5] == '&' && var5 < chars.length - 1 && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(chars[var5 + 1]) > -1) {
                  ++var5;
               } else {
                  newChars[tgtIndex] = chars[var5];
                  ++tgtIndex;
               }
            }

            return new String(newChars, 0, tgtIndex);
         }
      }
   }

   public static String replaceColors(String text) {
      return ChatColor.translateAlternateColorCodes('&', text);
   }
}
