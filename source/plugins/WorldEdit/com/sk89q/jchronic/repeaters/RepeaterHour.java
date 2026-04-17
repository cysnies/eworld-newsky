package com.sk89q.jchronic.repeaters;

import com.sk89q.jchronic.tags.Pointer;
import com.sk89q.jchronic.utils.Span;
import com.sk89q.jchronic.utils.Time;
import java.util.Calendar;

public class RepeaterHour extends RepeaterUnit {
   public static final int HOUR_SECONDS = 3600;
   private Calendar _currentDayStart;

   protected Span _nextSpan(Pointer.PointerType pointer) {
      if (this._currentDayStart == null) {
         if (pointer == Pointer.PointerType.FUTURE) {
            this._currentDayStart = Time.cloneAndAdd(Time.ymdh(this.getNow()), 10, 1L);
         } else {
            if (pointer != Pointer.PointerType.PAST) {
               throw new IllegalArgumentException("Unable to handle pointer " + pointer + ".");
            }

            this._currentDayStart = Time.cloneAndAdd(Time.ymdh(this.getNow()), 10, -1L);
         }
      } else {
         int direction = pointer == Pointer.PointerType.FUTURE ? 1 : -1;
         this._currentDayStart.add(10, direction);
      }

      return new Span(this._currentDayStart, 10, 1L);
   }

   protected Span _thisSpan(Pointer.PointerType pointer) {
      Calendar hourStart;
      Calendar hourEnd;
      if (pointer == Pointer.PointerType.FUTURE) {
         hourStart = Time.cloneAndAdd(Time.ymdhm(this.getNow()), 12, 1L);
         hourEnd = Time.cloneAndAdd(Time.ymdh(this.getNow()), 10, 1L);
      } else if (pointer == Pointer.PointerType.PAST) {
         hourStart = Time.ymdh(this.getNow());
         hourEnd = Time.ymdhm(this.getNow());
      } else {
         if (pointer != Pointer.PointerType.NONE) {
            throw new IllegalArgumentException("Unable to handle pointer " + pointer + ".");
         }

         hourStart = Time.ymdh(this.getNow());
         hourEnd = Time.cloneAndAdd(hourStart, 10, 1L);
      }

      return new Span(hourStart, hourEnd);
   }

   public Span getOffset(Span span, int amount, Pointer.PointerType pointer) {
      int direction = pointer == Pointer.PointerType.FUTURE ? 1 : -1;
      return span.add((long)(direction * amount * 3600));
   }

   public int getWidth() {
      return 3600;
   }

   public String toString() {
      return super.toString() + "-hour";
   }
}
