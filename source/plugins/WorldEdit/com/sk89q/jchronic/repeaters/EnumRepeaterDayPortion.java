package com.sk89q.jchronic.repeaters;

import com.sk89q.jchronic.utils.Range;

public class EnumRepeaterDayPortion extends RepeaterDayPortion {
   private static final Range AM_RANGE = new Range(0L, 43200L);
   private static final Range PM_RANGE = new Range(43200L, 86399L);
   private static final Range MORNING_RANGE = new Range(21600L, 43200L);
   private static final Range AFTERNOON_RANGE = new Range(46800L, 61200L);
   private static final Range EVENING_RANGE = new Range(61200L, 72000L);
   private static final Range NIGHT_RANGE = new Range(72000L, 86400L);

   public EnumRepeaterDayPortion(RepeaterDayPortion.DayPortion type) {
      super(type);
   }

   protected Range createRange(RepeaterDayPortion.DayPortion type) {
      Range range;
      if (type == RepeaterDayPortion.DayPortion.AM) {
         range = AM_RANGE;
      } else if (type == RepeaterDayPortion.DayPortion.PM) {
         range = PM_RANGE;
      } else if (type == RepeaterDayPortion.DayPortion.MORNING) {
         range = MORNING_RANGE;
      } else if (type == RepeaterDayPortion.DayPortion.AFTERNOON) {
         range = AFTERNOON_RANGE;
      } else if (type == RepeaterDayPortion.DayPortion.EVENING) {
         range = EVENING_RANGE;
      } else {
         if (type != RepeaterDayPortion.DayPortion.NIGHT) {
            throw new IllegalArgumentException("Unknown day portion type " + type);
         }

         range = NIGHT_RANGE;
      }

      return range;
   }

   protected int _getWidth(Range range) {
      int width = (int)range.getWidth();
      return width;
   }
}
