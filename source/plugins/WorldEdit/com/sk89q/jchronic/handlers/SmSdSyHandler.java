package com.sk89q.jchronic.handlers;

import com.sk89q.jchronic.Options;
import com.sk89q.jchronic.tags.ScalarDay;
import com.sk89q.jchronic.tags.ScalarMonth;
import com.sk89q.jchronic.tags.ScalarYear;
import com.sk89q.jchronic.utils.Span;
import com.sk89q.jchronic.utils.Time;
import com.sk89q.jchronic.utils.Token;
import java.util.Calendar;
import java.util.List;

public class SmSdSyHandler implements IHandler {
   public Span handle(List tokens, Options options) {
      int month = (Integer)((ScalarMonth)((Token)tokens.get(0)).getTag(ScalarMonth.class)).getType();
      int day = (Integer)((ScalarDay)((Token)tokens.get(1)).getTag(ScalarDay.class)).getType();
      int year = (Integer)((ScalarYear)((Token)tokens.get(2)).getTag(ScalarYear.class)).getType();

      Span span;
      try {
         List<Token> timeTokens = tokens.subList(3, tokens.size());
         Calendar dayStart = Time.construct(year, month, day);
         span = Handler.dayOrTime(dayStart, timeTokens, options);
      } catch (IllegalArgumentException e) {
         if (options.isDebug()) {
            e.printStackTrace(System.out);
         }

         span = null;
      }

      return span;
   }
}
