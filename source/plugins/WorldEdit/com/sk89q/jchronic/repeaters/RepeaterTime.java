package com.sk89q.jchronic.repeaters;

import com.sk89q.jchronic.Options;
import com.sk89q.jchronic.tags.Pointer;
import com.sk89q.jchronic.utils.Span;
import com.sk89q.jchronic.utils.StringUtils;
import com.sk89q.jchronic.utils.Tick;
import com.sk89q.jchronic.utils.Time;
import com.sk89q.jchronic.utils.Token;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class RepeaterTime extends Repeater {
   private static final Pattern TIME_PATTERN = Pattern.compile("^\\d{1,2}(:?\\d{2})?([\\.:]?\\d{2})?$");
   private Calendar _currentTime;

   public RepeaterTime(String time) {
      super((Object)null);
      String t = time.replaceAll(":", "");
      int length = t.length();
      Tick type;
      if (length <= 2) {
         int hours = Integer.parseInt(t);
         int hoursInSeconds = hours * 60 * 60;
         if (hours == 12) {
            type = new Tick(0, true);
         } else {
            type = new Tick(hoursInSeconds, true);
         }
      } else if (length == 3) {
         int hoursInSeconds = Integer.parseInt(t.substring(0, 1)) * 60 * 60;
         int minutesInSeconds = Integer.parseInt(t.substring(1)) * 60;
         type = new Tick(hoursInSeconds + minutesInSeconds, true);
      } else if (length == 4) {
         boolean ambiguous = time.contains(":") && Integer.parseInt(t.substring(0, 1)) != 0 && Integer.parseInt(t.substring(0, 2)) <= 12;
         int hours = Integer.parseInt(t.substring(0, 2));
         int hoursInSeconds = hours * 60 * 60;
         int minutesInSeconds = Integer.parseInt(t.substring(2)) * 60;
         if (hours == 12) {
            type = new Tick(0 + minutesInSeconds, ambiguous);
         } else {
            type = new Tick(hoursInSeconds + minutesInSeconds, ambiguous);
         }
      } else if (length == 5) {
         int hoursInSeconds = Integer.parseInt(t.substring(0, 1)) * 60 * 60;
         int minutesInSeconds = Integer.parseInt(t.substring(1, 3)) * 60;
         int seconds = Integer.parseInt(t.substring(3));
         type = new Tick(hoursInSeconds + minutesInSeconds + seconds, true);
      } else {
         if (length != 6) {
            throw new IllegalArgumentException("Time cannot exceed six digits");
         }

         boolean ambiguous = time.contains(":") && Integer.parseInt(t.substring(0, 1)) != 0 && Integer.parseInt(t.substring(0, 2)) <= 12;
         int hours = Integer.parseInt(t.substring(0, 2));
         int hoursInSeconds = hours * 60 * 60;
         int minutesInSeconds = Integer.parseInt(t.substring(2, 4)) * 60;
         int seconds = Integer.parseInt(t.substring(4, 6));
         if (hours == 12) {
            type = new Tick(0 + minutesInSeconds + seconds, ambiguous);
         } else {
            type = new Tick(hoursInSeconds + minutesInSeconds + seconds, ambiguous);
         }
      }

      this.setType(type);
   }

   protected Span _nextSpan(Pointer.PointerType pointer) {
      int halfDay = 43200;
      int fullDay = 86400;
      Calendar now = this.getNow();
      Tick tick = (Tick)this.getType();
      boolean first = false;
      if (this._currentTime == null) {
         first = true;
         Calendar midnight = Time.ymd(now);
         Calendar yesterdayMidnight = Time.cloneAndAdd(midnight, 13, (long)(-fullDay));
         Calendar tomorrowMidnight = Time.cloneAndAdd(midnight, 13, (long)fullDay);
         boolean done = false;
         if (pointer == Pointer.PointerType.FUTURE) {
            if (tick.isAmbiguous()) {
               List<Calendar> futureDates = new LinkedList();
               futureDates.add(Time.cloneAndAdd(midnight, 13, (long)tick.intValue()));
               futureDates.add(Time.cloneAndAdd(midnight, 13, (long)(halfDay + tick.intValue())));
               futureDates.add(Time.cloneAndAdd(tomorrowMidnight, 13, (long)tick.intValue()));

               for(Calendar futureDate : futureDates) {
                  if (futureDate.after(now) || futureDate.equals(now)) {
                     this._currentTime = futureDate;
                     done = true;
                     break;
                  }
               }
            } else {
               List<Calendar> futureDates = new LinkedList();
               futureDates.add(Time.cloneAndAdd(midnight, 13, (long)tick.intValue()));
               futureDates.add(Time.cloneAndAdd(tomorrowMidnight, 13, (long)tick.intValue()));

               for(Calendar futureDate : futureDates) {
                  if (futureDate.after(now) || futureDate.equals(now)) {
                     this._currentTime = futureDate;
                     done = true;
                     break;
                  }
               }
            }
         } else if (tick.isAmbiguous()) {
            List<Calendar> pastDates = new LinkedList();
            pastDates.add(Time.cloneAndAdd(midnight, 13, (long)(halfDay + tick.intValue())));
            pastDates.add(Time.cloneAndAdd(midnight, 13, (long)tick.intValue()));
            pastDates.add(Time.cloneAndAdd(yesterdayMidnight, 13, (long)(tick.intValue() * 2)));

            for(Calendar pastDate : pastDates) {
               if (pastDate.before(now) || pastDate.equals(now)) {
                  this._currentTime = pastDate;
                  done = true;
                  break;
               }
            }
         } else {
            List<Calendar> pastDates = new LinkedList();
            pastDates.add(Time.cloneAndAdd(midnight, 13, (long)tick.intValue()));
            pastDates.add(Time.cloneAndAdd(yesterdayMidnight, 13, (long)tick.intValue()));

            for(Calendar pastDate : pastDates) {
               if (pastDate.before(now) || pastDate.equals(now)) {
                  this._currentTime = pastDate;
                  done = true;
                  break;
               }
            }
         }

         if (!done && this._currentTime == null) {
            throw new IllegalStateException("Current time cannot be null at this point.");
         }
      }

      if (!first) {
         int increment = tick.isAmbiguous() ? halfDay : fullDay;
         int direction = pointer == Pointer.PointerType.FUTURE ? 1 : -1;
         this._currentTime.add(13, direction * increment);
      }

      return new Span(this._currentTime, Time.cloneAndAdd(this._currentTime, 13, (long)this.getWidth()));
   }

   protected Span _thisSpan(Pointer.PointerType pointer) {
      if (pointer == Pointer.PointerType.NONE) {
         pointer = Pointer.PointerType.FUTURE;
      }

      return this.nextSpan(pointer);
   }

   public Span getOffset(Span span, int amount, Pointer.PointerType pointer) {
      throw new IllegalStateException("Not implemented.");
   }

   public int getWidth() {
      return 1;
   }

   public String toString() {
      return super.toString() + "-time-" + this.getType();
   }

   public static RepeaterTime scan(Token token, List tokens, Options options) {
      if (TIME_PATTERN.matcher(token.getWord()).matches()) {
         return new RepeaterTime(token.getWord());
      } else {
         Integer intStrValue = StringUtils.integerValue(token.getWord());
         return intStrValue != null ? new RepeaterTime(intStrValue.toString()) : null;
      }
   }
}
