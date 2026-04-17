package com.sk89q.jchronic.repeaters;

import com.sk89q.jchronic.tags.Pointer;
import com.sk89q.jchronic.utils.Span;

public class RepeaterSeason extends RepeaterUnit {
   public static final int SEASON_SECONDS = 7862400;

   protected Span _nextSpan(Pointer.PointerType pointer) {
      throw new IllegalStateException("Not implemented.");
   }

   protected Span _thisSpan(Pointer.PointerType pointer) {
      throw new IllegalStateException("Not implemented.");
   }

   public Span getOffset(Span span, int amount, Pointer.PointerType pointer) {
      throw new IllegalStateException("Not implemented.");
   }

   public int getWidth() {
      return 7862400;
   }

   public String toString() {
      return super.toString() + "-season";
   }
}
