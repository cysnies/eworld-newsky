package com.sk89q.jchronic.repeaters;

import com.sk89q.jchronic.utils.Token;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public abstract class RepeaterUnit extends Repeater {
   private static final Pattern YEAR_PATTERN = Pattern.compile("^years?$");
   private static final Pattern SEASON_PATTERN = Pattern.compile("^seasons?$");
   private static final Pattern MONTH_PATTERN = Pattern.compile("^months?$");
   private static final Pattern FORTNIGHT_PATTERN = Pattern.compile("^fortnights?$");
   private static final Pattern WEEK_PATTERN = Pattern.compile("^weeks?$");
   private static final Pattern WEEKEND_PATTERN = Pattern.compile("^weekends?$");
   private static final Pattern DAY_PATTERN = Pattern.compile("^days?$");
   private static final Pattern HOUR_PATTERN = Pattern.compile("^hours?$");
   private static final Pattern MINUTE_PATTERN = Pattern.compile("^minutes?$");
   private static final Pattern SECOND_PATTERN = Pattern.compile("^seconds?$");

   public RepeaterUnit() {
      super((Object)null);
   }

   public static RepeaterUnit scan(Token token) {
      try {
         Map<Pattern, UnitName> scanner = new HashMap();
         scanner.put(YEAR_PATTERN, RepeaterUnit.UnitName.YEAR);
         scanner.put(SEASON_PATTERN, RepeaterUnit.UnitName.SEASON);
         scanner.put(MONTH_PATTERN, RepeaterUnit.UnitName.MONTH);
         scanner.put(FORTNIGHT_PATTERN, RepeaterUnit.UnitName.FORTNIGHT);
         scanner.put(WEEK_PATTERN, RepeaterUnit.UnitName.WEEK);
         scanner.put(WEEKEND_PATTERN, RepeaterUnit.UnitName.WEEKEND);
         scanner.put(DAY_PATTERN, RepeaterUnit.UnitName.DAY);
         scanner.put(HOUR_PATTERN, RepeaterUnit.UnitName.HOUR);
         scanner.put(MINUTE_PATTERN, RepeaterUnit.UnitName.MINUTE);
         scanner.put(SECOND_PATTERN, RepeaterUnit.UnitName.SECOND);

         for(Pattern scannerItem : scanner.keySet()) {
            if (scannerItem.matcher(token.getWord()).matches()) {
               UnitName unitNameEnum = (UnitName)scanner.get(scannerItem);
               String unitName = unitNameEnum.name();
               String capitalizedUnitName = unitName.substring(0, 1) + unitName.substring(1).toLowerCase();
               String repeaterClassName = "com.sk89q.jchronic.repeaters.Repeater" + capitalizedUnitName;
               RepeaterUnit repeater = (RepeaterUnit)Class.forName(repeaterClassName).asSubclass(RepeaterUnit.class).newInstance();
               return repeater;
            }
         }

         return null;
      } catch (Throwable t) {
         throw new RuntimeException("Failed to create RepeaterUnit.", t);
      }
   }

   public static enum UnitName {
      YEAR,
      SEASON,
      MONTH,
      FORTNIGHT,
      WEEK,
      WEEKEND,
      DAY,
      HOUR,
      MINUTE,
      SECOND;
   }
}
