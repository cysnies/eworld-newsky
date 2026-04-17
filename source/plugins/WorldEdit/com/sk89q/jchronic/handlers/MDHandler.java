package com.sk89q.jchronic.handlers;

import com.sk89q.jchronic.Options;
import com.sk89q.jchronic.repeaters.Repeater;
import com.sk89q.jchronic.tags.Tag;
import com.sk89q.jchronic.utils.Span;
import com.sk89q.jchronic.utils.Time;
import com.sk89q.jchronic.utils.Token;
import java.util.Calendar;
import java.util.List;

public abstract class MDHandler implements IHandler {
   public Span handle(Repeater month, Tag day, List timeTokens, Options options) {
      month.setStart((Calendar)options.getNow().clone());
      Span span = month.thisSpan(options.getContext());
      Calendar dayStart = Time.construct(span.getBeginCalendar().get(1), span.getBeginCalendar().get(2) + 1, (Integer)day.getType());
      return Handler.dayOrTime(dayStart, timeTokens, options);
   }
}
