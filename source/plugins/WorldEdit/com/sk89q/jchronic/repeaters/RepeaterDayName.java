package com.sk89q.jchronic.repeaters;

import com.sk89q.jchronic.tags.Pointer;
import com.sk89q.jchronic.utils.Span;
import com.sk89q.jchronic.utils.Time;
import com.sk89q.jchronic.utils.Token;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class RepeaterDayName extends Repeater {
   private static final Pattern MON_PATTERN = Pattern.compile("^m[ou]n(day)?$");
   private static final Pattern TUE_PATTERN = Pattern.compile("^t(ue|eu|oo|u|)s(day)?$");
   private static final Pattern TUE_PATTERN_1 = Pattern.compile("^tue$");
   private static final Pattern WED_PATTERN_1 = Pattern.compile("^we(dnes|nds|nns)day$");
   private static final Pattern WED_PATTERN_2 = Pattern.compile("^wed$");
   private static final Pattern THU_PATTERN_1 = Pattern.compile("^th(urs|ers)day$");
   private static final Pattern THU_PATTERN_2 = Pattern.compile("^thu$");
   private static final Pattern FRI_PATTERN = Pattern.compile("^fr[iy](day)?$");
   private static final Pattern SAT_PATTERN = Pattern.compile("^sat(t?[ue]rday)?$");
   private static final Pattern SUN_PATTERN = Pattern.compile("^su[nm](day)?$");
   public static final int DAY_SECONDS = 86400;
   private Calendar _currentDayStart;

   public RepeaterDayName(DayName type) {
      super(type);
   }

   protected Span _nextSpan(Pointer.PointerType pointer) {
      int direction = pointer == Pointer.PointerType.FUTURE ? 1 : -1;
      if (this._currentDayStart == null) {
         this._currentDayStart = Time.ymd(this.getNow());
         this._currentDayStart.add(5, direction);
         int dayNum = ((DayName)this.getType()).ordinal();

         while(this._currentDayStart.get(7) - 1 != dayNum) {
            this._currentDayStart.add(5, direction);
         }
      } else {
         this._currentDayStart.add(5, direction * 7);
      }

      return new Span(this._currentDayStart, 5, 1L);
   }

   protected Span _thisSpan(Pointer.PointerType pointer) {
      if (pointer == Pointer.PointerType.NONE) {
         pointer = Pointer.PointerType.FUTURE;
      }

      return super.nextSpan(pointer);
   }

   public Span getOffset(Span span, int amount, Pointer.PointerType pointer) {
      throw new IllegalStateException("Not implemented.");
   }

   public int getWidth() {
      return 86400;
   }

   public String toString() {
      return super.toString() + "-dayname-" + this.getType();
   }

   public static RepeaterDayName scan(Token token) {
      Map<Pattern, DayName> scanner = new HashMap();
      scanner.put(MON_PATTERN, RepeaterDayName.DayName.MONDAY);
      scanner.put(TUE_PATTERN, RepeaterDayName.DayName.TUESDAY);
      scanner.put(TUE_PATTERN_1, RepeaterDayName.DayName.TUESDAY);
      scanner.put(WED_PATTERN_1, RepeaterDayName.DayName.WEDNESDAY);
      scanner.put(WED_PATTERN_2, RepeaterDayName.DayName.WEDNESDAY);
      scanner.put(THU_PATTERN_1, RepeaterDayName.DayName.THURSDAY);
      scanner.put(THU_PATTERN_2, RepeaterDayName.DayName.THURSDAY);
      scanner.put(FRI_PATTERN, RepeaterDayName.DayName.FRIDAY);
      scanner.put(SAT_PATTERN, RepeaterDayName.DayName.SATURDAY);
      scanner.put(SUN_PATTERN, RepeaterDayName.DayName.SUNDAY);

      for(Pattern scannerItem : scanner.keySet()) {
         if (scannerItem.matcher(token.getWord()).matches()) {
            return new RepeaterDayName((DayName)scanner.get(scannerItem));
         }
      }

      return null;
   }

   public static enum DayName {
      SUNDAY,
      MONDAY,
      TUESDAY,
      WEDNESDAY,
      THURSDAY,
      FRIDAY,
      SATURDAY;
   }
}
