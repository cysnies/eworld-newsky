package com.sk89q.jchronic.repeaters;

import com.sk89q.jchronic.tags.Pointer;
import com.sk89q.jchronic.utils.Span;
import com.sk89q.jchronic.utils.Time;
import java.util.Calendar;

public class RepeaterFortnight extends RepeaterUnit {
   public static final int FORTNIGHT_SECONDS = 1209600;
   private Calendar _currentFortnightStart;

   protected Span _nextSpan(Pointer.PointerType pointer) {
      if (this._currentFortnightStart == null) {
         if (pointer == Pointer.PointerType.FUTURE) {
            RepeaterDayName sundayRepeater = new RepeaterDayName(RepeaterDayName.DayName.SUNDAY);
            sundayRepeater.setStart(this.getNow());
            Span nextSundaySpan = sundayRepeater.nextSpan(Pointer.PointerType.FUTURE);
            this._currentFortnightStart = nextSundaySpan.getBeginCalendar();
         } else {
            if (pointer != Pointer.PointerType.PAST) {
               throw new IllegalArgumentException("Unable to handle pointer " + pointer + ".");
            }

            RepeaterDayName sundayRepeater = new RepeaterDayName(RepeaterDayName.DayName.SUNDAY);
            sundayRepeater.setStart(Time.cloneAndAdd(this.getNow(), 13, 86400L));
            sundayRepeater.nextSpan(Pointer.PointerType.PAST);
            sundayRepeater.nextSpan(Pointer.PointerType.PAST);
            Span lastSundaySpan = sundayRepeater.nextSpan(Pointer.PointerType.PAST);
            this._currentFortnightStart = lastSundaySpan.getBeginCalendar();
         }
      } else {
         int direction = pointer == Pointer.PointerType.FUTURE ? 1 : -1;
         this._currentFortnightStart.add(13, direction * 1209600);
      }

      return new Span(this._currentFortnightStart, 13, 1209600L);
   }

   protected Span _thisSpan(Pointer.PointerType pointer) {
      if (pointer == null) {
         pointer = Pointer.PointerType.FUTURE;
      }

      Span span;
      if (pointer == Pointer.PointerType.FUTURE) {
         Calendar thisFortnightStart = Time.cloneAndAdd(Time.ymdh(this.getNow()), 13, 3600L);
         RepeaterDayName sundayRepeater = new RepeaterDayName(RepeaterDayName.DayName.SUNDAY);
         sundayRepeater.setStart(this.getNow());
         sundayRepeater.thisSpan(Pointer.PointerType.FUTURE);
         Span thisSundaySpan = sundayRepeater.thisSpan(Pointer.PointerType.FUTURE);
         Calendar thisFortnightEnd = thisSundaySpan.getBeginCalendar();
         span = new Span(thisFortnightStart, thisFortnightEnd);
      } else {
         if (pointer != Pointer.PointerType.PAST) {
            throw new IllegalArgumentException("Unable to handle pointer " + pointer + ".");
         }

         Calendar thisFortnightEnd = Time.ymdh(this.getNow());
         RepeaterDayName sundayRepeater = new RepeaterDayName(RepeaterDayName.DayName.SUNDAY);
         sundayRepeater.setStart(this.getNow());
         Span lastSundaySpan = sundayRepeater.nextSpan(Pointer.PointerType.PAST);
         Calendar thisFortnightStart = lastSundaySpan.getBeginCalendar();
         span = new Span(thisFortnightStart, thisFortnightEnd);
      }

      return span;
   }

   public Span getOffset(Span span, int amount, Pointer.PointerType pointer) {
      int direction = pointer == Pointer.PointerType.FUTURE ? 1 : -1;
      Span offsetSpan = span.add((long)(direction * amount * 1209600));
      return offsetSpan;
   }

   public int getWidth() {
      return 1209600;
   }

   public String toString() {
      return super.toString() + "-fortnight";
   }
}
