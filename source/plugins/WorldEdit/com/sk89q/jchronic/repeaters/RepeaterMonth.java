package com.sk89q.jchronic.repeaters;

import com.sk89q.jchronic.tags.Pointer;
import com.sk89q.jchronic.utils.Span;
import com.sk89q.jchronic.utils.Time;
import java.util.Calendar;

public class RepeaterMonth extends RepeaterUnit {
   private static final int MONTH_SECONDS = 2592000;
   private Calendar _currentMonthStart;

   protected Span _nextSpan(Pointer.PointerType pointer) {
      int direction = pointer == Pointer.PointerType.FUTURE ? 1 : -1;
      if (this._currentMonthStart == null) {
         this._currentMonthStart = Time.cloneAndAdd(Time.ym(this.getNow()), 2, (long)direction);
      } else {
         this._currentMonthStart = Time.cloneAndAdd(this._currentMonthStart, 2, (long)direction);
      }

      return new Span(this._currentMonthStart, 2, 1L);
   }

   public Span getOffset(Span span, int amount, Pointer.PointerType pointer) {
      int direction = pointer == Pointer.PointerType.FUTURE ? 1 : -1;
      return new Span(Time.cloneAndAdd(span.getBeginCalendar(), 2, (long)(amount * direction)), Time.cloneAndAdd(span.getEndCalendar(), 2, (long)(amount * direction)));
   }

   protected Span _thisSpan(Pointer.PointerType pointer) {
      Calendar monthStart;
      Calendar monthEnd;
      if (pointer == Pointer.PointerType.FUTURE) {
         monthStart = Time.cloneAndAdd(Time.ymd(this.getNow()), 5, 1L);
         monthEnd = Time.cloneAndAdd(Time.ym(this.getNow()), 2, 1L);
      } else if (pointer == Pointer.PointerType.PAST) {
         monthStart = Time.ym(this.getNow());
         monthEnd = Time.ymd(this.getNow());
      } else {
         if (pointer != Pointer.PointerType.NONE) {
            throw new IllegalArgumentException("Unable to handle pointer " + pointer + ".");
         }

         monthStart = Time.ym(this.getNow());
         monthEnd = Time.cloneAndAdd(Time.ym(this.getNow()), 2, 1L);
      }

      return new Span(monthStart, monthEnd);
   }

   public int getWidth() {
      return 2592000;
   }

   public String toString() {
      return super.toString() + "-month";
   }
}
