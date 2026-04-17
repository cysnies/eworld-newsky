package com.sk89q.jchronic.repeaters;

import com.sk89q.jchronic.utils.Range;

public class IntegerRepeaterDayPortion extends RepeaterDayPortion {
   public IntegerRepeaterDayPortion(Integer type) {
      super(type);
   }

   protected Range createRange(Integer type) {
      Range range = new Range((long)(type * 60 * 60), (long)((type + 12) * 60 * 60));
      return range;
   }

   protected int _getWidth(Range range) {
      int width = 43200;
      return width;
   }
}
