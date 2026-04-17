package com.sk89q.jchronic.utils;

import java.text.DateFormat;
import java.util.Calendar;

public class Span extends Range {
   public Span(Calendar begin, int field, long amount) {
      this(begin, Time.cloneAndAdd(begin, field, amount));
   }

   public Span(Calendar begin, Calendar end) {
      this(begin.getTimeInMillis() / 1000L, end.getTimeInMillis() / 1000L);
   }

   public Span(long begin, long end) {
      super(begin, end);
   }

   public Calendar getBeginCalendar() {
      Calendar cal = Calendar.getInstance(Time.getTimeZone());
      cal.setTimeInMillis(this.getBegin() * 1000L);
      return cal;
   }

   public Calendar getEndCalendar() {
      Calendar cal = Calendar.getInstance(Time.getTimeZone());
      cal.setTimeInMillis(this.getEnd() * 1000L);
      return cal;
   }

   public Span add(long seconds) {
      return new Span(this.getBegin() + seconds, this.getEnd() + seconds);
   }

   public Span subtract(long seconds) {
      return this.add(-seconds);
   }

   public String toString() {
      return "(" + DateFormat.getDateTimeInstance().format(this.getBeginCalendar().getTime()) + ".." + DateFormat.getDateTimeInstance().format(this.getEndCalendar().getTime()) + ")";
   }
}
