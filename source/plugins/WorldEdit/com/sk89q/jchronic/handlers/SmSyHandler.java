package com.sk89q.jchronic.handlers;

import com.sk89q.jchronic.Options;
import com.sk89q.jchronic.tags.ScalarMonth;
import com.sk89q.jchronic.tags.ScalarYear;
import com.sk89q.jchronic.utils.Span;
import com.sk89q.jchronic.utils.Time;
import com.sk89q.jchronic.utils.Token;
import java.util.Calendar;
import java.util.List;

public class SmSyHandler implements IHandler {
   public Span handle(List tokens, Options options) {
      int month = (Integer)((ScalarMonth)((Token)tokens.get(0)).getTag(ScalarMonth.class)).getType();
      int year = (Integer)((ScalarYear)((Token)tokens.get(1)).getTag(ScalarYear.class)).getType();

      Span span;
      try {
         Calendar start = Time.construct(year, month);
         Calendar end = Time.cloneAndAdd(start, 2, 1L);
         span = new Span(start, end);
      } catch (IllegalArgumentException e) {
         if (options.isDebug()) {
            e.printStackTrace(System.out);
         }

         span = null;
      }

      return span;
   }
}
