package com.sk89q.jchronic.repeaters;

import com.sk89q.jchronic.tags.Pointer;
import com.sk89q.jchronic.utils.Span;
import com.sk89q.jchronic.utils.Time;
import java.util.Calendar;

public class RepeaterMinute extends RepeaterUnit {
   public static final int MINUTE_SECONDS = 60;
   private Calendar _currentMinuteStart;

   protected Span _nextSpan(Pointer.PointerType pointer) {
      if (this._currentMinuteStart == null) {
         if (pointer == Pointer.PointerType.FUTURE) {
            this._currentMinuteStart = Time.cloneAndAdd(Time.ymdhm(this.getNow()), 12, 1L);
         } else {
            if (pointer != Pointer.PointerType.PAST) {
               throw new IllegalArgumentException("Unable to handle pointer " + pointer + ".");
            }

            this._currentMinuteStart = Time.cloneAndAdd(Time.ymdhm(this.getNow()), 12, -1L);
         }
      } else {
         int direction = pointer == Pointer.PointerType.FUTURE ? 1 : -1;
         this._currentMinuteStart.add(12, direction);
      }

      return new Span(this._currentMinuteStart, 13, 60L);
   }

   protected Span _thisSpan(Pointer.PointerType pointer) {
      Calendar minuteBegin;
      Calendar minuteEnd;
      if (pointer == Pointer.PointerType.FUTURE) {
         minuteBegin = this.getNow();
         minuteEnd = Time.ymdhm(this.getNow());
      } else if (pointer == Pointer.PointerType.PAST) {
         minuteBegin = Time.ymdhm(this.getNow());
         minuteEnd = this.getNow();
      } else {
         if (pointer != Pointer.PointerType.NONE) {
            throw new IllegalArgumentException("Unable to handle pointer " + pointer + ".");
         }

         minuteBegin = Time.ymdhm(this.getNow());
         minuteEnd = Time.cloneAndAdd(Time.ymdhm(this.getNow()), 13, 60L);
      }

      return new Span(minuteBegin, minuteEnd);
   }

   public Span getOffset(Span span, int amount, Pointer.PointerType pointer) {
      int direction = pointer == Pointer.PointerType.FUTURE ? 1 : -1;
      return span.add((long)(direction * amount * 60));
   }

   public int getWidth() {
      return 60;
   }

   public String toString() {
      return super.toString() + "-minute";
   }
}
