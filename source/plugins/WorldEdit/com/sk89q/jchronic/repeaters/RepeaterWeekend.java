package com.sk89q.jchronic.repeaters;

import com.sk89q.jchronic.tags.Pointer;
import com.sk89q.jchronic.utils.Span;
import com.sk89q.jchronic.utils.Time;
import java.util.Calendar;

public class RepeaterWeekend extends RepeaterUnit {
   public static final int WEEKEND_SECONDS = 172800;
   private Calendar _currentWeekStart;

   protected Span _nextSpan(Pointer.PointerType pointer) {
      if (this._currentWeekStart == null) {
         if (pointer == Pointer.PointerType.FUTURE) {
            RepeaterDayName saturdayRepeater = new RepeaterDayName(RepeaterDayName.DayName.SATURDAY);
            saturdayRepeater.setStart((Calendar)this.getNow().clone());
            Span nextSaturdaySpan = saturdayRepeater.nextSpan(Pointer.PointerType.FUTURE);
            this._currentWeekStart = nextSaturdaySpan.getBeginCalendar();
         } else if (pointer == Pointer.PointerType.PAST) {
            RepeaterDayName saturdayRepeater = new RepeaterDayName(RepeaterDayName.DayName.SATURDAY);
            saturdayRepeater.setStart(Time.cloneAndAdd(this.getNow(), 13, 86400L));
            Span lastSaturdaySpan = saturdayRepeater.nextSpan(Pointer.PointerType.PAST);
            this._currentWeekStart = lastSaturdaySpan.getBeginCalendar();
         }
      } else {
         int direction = pointer == Pointer.PointerType.FUTURE ? 1 : -1;
         this._currentWeekStart = Time.cloneAndAdd(this._currentWeekStart, 13, (long)(direction * 604800));
      }

      return new Span(this._currentWeekStart, Time.cloneAndAdd(this._currentWeekStart, 13, 172800L));
   }

   protected Span _thisSpan(Pointer.PointerType pointer) {
      Span thisSpan;
      if (pointer != Pointer.PointerType.FUTURE && pointer != Pointer.PointerType.NONE) {
         if (pointer != Pointer.PointerType.PAST) {
            throw new IllegalArgumentException("Unable to handle pointer " + pointer + ".");
         }

         RepeaterDayName saturdayRepeater = new RepeaterDayName(RepeaterDayName.DayName.SATURDAY);
         saturdayRepeater.setStart((Calendar)this.getNow().clone());
         Span lastSaturdaySpan = saturdayRepeater.nextSpan(Pointer.PointerType.PAST);
         thisSpan = new Span(lastSaturdaySpan.getBeginCalendar(), Time.cloneAndAdd(lastSaturdaySpan.getBeginCalendar(), 13, 172800L));
      } else {
         RepeaterDayName saturdayRepeater = new RepeaterDayName(RepeaterDayName.DayName.SATURDAY);
         saturdayRepeater.setStart((Calendar)this.getNow().clone());
         Span thisSaturdaySpan = saturdayRepeater.nextSpan(Pointer.PointerType.FUTURE);
         thisSpan = new Span(thisSaturdaySpan.getBeginCalendar(), Time.cloneAndAdd(thisSaturdaySpan.getBeginCalendar(), 13, 172800L));
      }

      return thisSpan;
   }

   public Span getOffset(Span span, int amount, Pointer.PointerType pointer) {
      int direction = pointer == Pointer.PointerType.FUTURE ? 1 : -1;
      RepeaterWeekend weekend = new RepeaterWeekend();
      weekend.setStart(span.getBeginCalendar());
      Calendar start = Time.cloneAndAdd(weekend.nextSpan(pointer).getBeginCalendar(), 13, (long)((amount - 1) * direction * 604800));
      return new Span(start, Time.cloneAndAdd(start, 13, span.getWidth()));
   }

   public int getWidth() {
      return 172800;
   }

   public String toString() {
      return super.toString() + "-weekend";
   }
}
