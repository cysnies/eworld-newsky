package com.sk89q.jchronic.repeaters;

import com.sk89q.jchronic.tags.Pointer;
import com.sk89q.jchronic.utils.Span;
import com.sk89q.jchronic.utils.Time;
import java.util.Calendar;

public class RepeaterSecond extends RepeaterUnit {
   public static final int SECOND_SECONDS = 1;
   private Calendar _secondStart;

   protected Span _nextSpan(Pointer.PointerType pointer) {
      int direction = pointer == Pointer.PointerType.FUTURE ? 1 : -1;
      if (this._secondStart == null) {
         this._secondStart = Time.cloneAndAdd(this.getNow(), 13, (long)direction);
      } else {
         this._secondStart.add(13, direction);
      }

      return new Span(this._secondStart, 13, 1L);
   }

   protected Span _thisSpan(Pointer.PointerType pointer) {
      return new Span(this.getNow(), 13, 1L);
   }

   public Span getOffset(Span span, int amount, Pointer.PointerType pointer) {
      int direction = pointer == Pointer.PointerType.FUTURE ? 1 : -1;
      return span.add((long)(direction * amount * 1));
   }

   public int getWidth() {
      return 1;
   }

   public String toString() {
      return super.toString() + "-second";
   }
}
