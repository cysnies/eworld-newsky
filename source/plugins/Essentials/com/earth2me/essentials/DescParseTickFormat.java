package com.earth2me.essentials;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

public final class DescParseTickFormat {
   public static final Map nameToTicks = new LinkedHashMap();
   public static final Set resetAliases = new HashSet();
   public static final int ticksAtMidnight = 18000;
   public static final int ticksPerDay = 24000;
   public static final int ticksPerHour = 1000;
   public static final double ticksPerMinute = 16.666666666666668;
   public static final double ticksPerSecond = 0.2777777777777778;
   private static final SimpleDateFormat SDFTwentyFour;
   private static final SimpleDateFormat SDFTwelve;

   private DescParseTickFormat() {
   }

   public static long parse(String desc) throws NumberFormatException {
      desc = desc.toLowerCase(Locale.ENGLISH).replaceAll("[^A-Za-z0-9:]", "");

      try {
         return parseTicks(desc);
      } catch (Exception var5) {
         try {
            return parse24(desc);
         } catch (Exception var4) {
            try {
               return parse12(desc);
            } catch (Exception var3) {
               try {
                  return parseAlias(desc);
               } catch (Exception var2) {
                  throw new NumberFormatException();
               }
            }
         }
      }
   }

   public static long parseTicks(String desc) throws NumberFormatException {
      if (!desc.matches("^[0-9]+ti?c?k?s?$")) {
         throw new NumberFormatException();
      } else {
         desc = desc.replaceAll("[^0-9]", "");
         return Long.parseLong(desc) % 24000L;
      }
   }

   public static long parse24(String desc) throws NumberFormatException {
      if (!desc.matches("^[0-9]{2}[^0-9]?[0-9]{2}$")) {
         throw new NumberFormatException();
      } else {
         desc = desc.toLowerCase(Locale.ENGLISH).replaceAll("[^0-9]", "");
         if (desc.length() != 4) {
            throw new NumberFormatException();
         } else {
            int hours = Integer.parseInt(desc.substring(0, 2));
            int minutes = Integer.parseInt(desc.substring(2, 4));
            return hoursMinutesToTicks(hours, minutes);
         }
      }
   }

   public static long parse12(String desc) throws NumberFormatException {
      if (!desc.matches("^[0-9]{1,2}([^0-9]?[0-9]{2})?(pm|am)$")) {
         throw new NumberFormatException();
      } else {
         int hours = 0;
         int minutes = 0;
         desc = desc.toLowerCase(Locale.ENGLISH);
         String parsetime = desc.replaceAll("[^0-9]", "");
         if (parsetime.length() > 4) {
            throw new NumberFormatException();
         } else {
            if (parsetime.length() == 4) {
               hours += Integer.parseInt(parsetime.substring(0, 2));
               minutes += Integer.parseInt(parsetime.substring(2, 4));
            } else if (parsetime.length() == 3) {
               hours += Integer.parseInt(parsetime.substring(0, 1));
               minutes += Integer.parseInt(parsetime.substring(1, 3));
            } else if (parsetime.length() == 2) {
               hours += Integer.parseInt(parsetime.substring(0, 2));
            } else {
               if (parsetime.length() != 1) {
                  throw new NumberFormatException();
               }

               hours += Integer.parseInt(parsetime.substring(0, 1));
            }

            if (desc.endsWith("pm") && hours != 12) {
               hours += 12;
            }

            if (desc.endsWith("am") && hours == 12) {
               hours -= 12;
            }

            return hoursMinutesToTicks(hours, minutes);
         }
      }
   }

   public static long hoursMinutesToTicks(int hours, int minutes) {
      long ret = 18000L;
      ret += (long)(hours * 1000);
      ret = (long)((double)ret + (double)minutes / (double)60.0F * (double)1000.0F);
      ret %= 24000L;
      return ret;
   }

   public static long parseAlias(String desc) throws NumberFormatException {
      Integer ret = (Integer)nameToTicks.get(desc);
      if (ret == null) {
         throw new NumberFormatException();
      } else {
         return (long)ret;
      }
   }

   public static boolean meansReset(String desc) {
      return resetAliases.contains(desc);
   }

   public static String format(long ticks) {
      return I18n._("timeFormat", format24(ticks), format12(ticks), formatTicks(ticks));
   }

   public static String formatTicks(long ticks) {
      return ticks % 24000L + "ticks";
   }

   public static String format24(long ticks) {
      synchronized(SDFTwentyFour) {
         return formatDateFormat(ticks, SDFTwentyFour);
      }
   }

   public static String format12(long ticks) {
      synchronized(SDFTwelve) {
         return formatDateFormat(ticks, SDFTwelve);
      }
   }

   public static String formatDateFormat(long ticks, SimpleDateFormat format) {
      Date date = ticksToDate(ticks);
      return format.format(date);
   }

   public static Date ticksToDate(long ticks) {
      ticks = ticks - 18000L + 24000L;
      long days = ticks / 24000L;
      ticks -= days * 24000L;
      long hours = ticks / 1000L;
      ticks -= hours * 1000L;
      long minutes = (long)Math.floor((double)ticks / 16.666666666666668);
      double dticks = (double)ticks - (double)minutes * 16.666666666666668;
      long seconds = (long)Math.floor(dticks / 0.2777777777777778);
      Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.ENGLISH);
      cal.setLenient(true);
      cal.set(0, 0, 1, 0, 0, 0);
      cal.add(6, (int)days);
      cal.add(11, (int)hours);
      cal.add(12, (int)minutes);
      cal.add(13, (int)seconds + 1);
      return cal.getTime();
   }

   static {
      SDFTwentyFour = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
      SDFTwelve = new SimpleDateFormat("h:mmaa", Locale.ENGLISH);
      SDFTwentyFour.setTimeZone(TimeZone.getTimeZone("GMT"));
      SDFTwelve.setTimeZone(TimeZone.getTimeZone("GMT"));
      nameToTicks.put("sunrise", 23000);
      nameToTicks.put("dawn", 23000);
      nameToTicks.put("daystart", 0);
      nameToTicks.put("day", 0);
      nameToTicks.put("morning", 1000);
      nameToTicks.put("midday", 6000);
      nameToTicks.put("noon", 6000);
      nameToTicks.put("afternoon", 9000);
      nameToTicks.put("sunset", 12000);
      nameToTicks.put("dusk", 12000);
      nameToTicks.put("sundown", 12000);
      nameToTicks.put("nightfall", 12000);
      nameToTicks.put("nightstart", 14000);
      nameToTicks.put("night", 14000);
      nameToTicks.put("midnight", 18000);
      resetAliases.add("reset");
      resetAliases.add("normal");
      resetAliases.add("default");
   }
}
