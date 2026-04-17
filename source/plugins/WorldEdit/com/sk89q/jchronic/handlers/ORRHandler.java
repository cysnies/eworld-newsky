package com.sk89q.jchronic.handlers;

import com.sk89q.jchronic.Options;
import com.sk89q.jchronic.repeaters.Repeater;
import com.sk89q.jchronic.tags.Ordinal;
import com.sk89q.jchronic.tags.Pointer;
import com.sk89q.jchronic.utils.Span;
import com.sk89q.jchronic.utils.Time;
import com.sk89q.jchronic.utils.Token;
import java.util.List;

public abstract class ORRHandler implements IHandler {
   public Span handle(List tokens, Span outerSpan, Options options) {
      Repeater<?> repeater = (Repeater)((Token)tokens.get(1)).getTag(Repeater.class);
      repeater.setStart(Time.cloneAndAdd(outerSpan.getBeginCalendar(), 13, -1L));
      Integer ordinalValue = (Integer)((Ordinal)((Token)tokens.get(0)).getTag(Ordinal.class)).getType();
      Span span = null;

      for(int i = 0; i < ordinalValue; ++i) {
         span = repeater.nextSpan(Pointer.PointerType.FUTURE);
         if (span.getBegin() > outerSpan.getEnd()) {
            span = null;
            break;
         }
      }

      return span;
   }
}
