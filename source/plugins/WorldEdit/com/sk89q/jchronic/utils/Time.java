package com.sk89q.jchronic.utils;

import java.util.Calendar;
import java.util.TimeZone;

public class Time {
   protected static TimeZone tz = TimeZone.getDefault();

   public static void setTimeZone(TimeZone tz) {
      Time.tz = tz;
   }

   public static TimeZone getTimeZone() {
      return tz;
   }

   public static Calendar construct(int year, int month) {
      if (year <= 1900) {
         throw new IllegalArgumentException("Illegal year '" + year + "'");
      } else {
         Calendar cal = Calendar.getInstance(tz);
         cal.clear();
         cal.set(1, year);
         cal.set(2, month - 1);
         return cal;
      }
   }

   public static Calendar construct(int year, int month, int day) {
      Calendar cal = construct(year, month);
      cal.set(5, day);
      return cal;
   }

   public static Calendar construct(int year, int month, int day, int hour) {
      Calendar cal = construct(year, month, day);
      cal.set(11, hour);
      return cal;
   }

   public static Calendar construct(int year, int month, int day, int hour, int minute) {
      Calendar cal = construct(year, month, day, hour);
      cal.set(12, minute);
      return cal;
   }

   public static Calendar construct(int year, int month, int day, int hour, int minute, int second) {
      Calendar cal = construct(year, month, day, hour, minute);
      cal.set(13, second);
      return cal;
   }

   public static Calendar construct(int year, int month, int day, int hour, int minute, int second, int millisecond) {
      Calendar cal = construct(year, month, day, hour, minute, second);
      cal.set(14, millisecond);
      return cal;
   }

   public static Calendar y(Calendar basis) {
      Calendar clone = Calendar.getInstance(tz);
      clone.clear();
      clone.set(1, basis.get(1));
      return clone;
   }

   public static Calendar yJan1(Calendar basis) {
      Calendar clone = y(basis, 1, 1);
      return clone;
   }

   public static Calendar y(Calendar basis, int month) {
      Calendar clone = y(basis);
      clone.set(2, month - 1);
      return clone;
   }

   public static Calendar y(Calendar basis, int month, int day) {
      Calendar clone = y(basis, month);
      clone.set(5, day);
      return clone;
   }

   public static Calendar ym(Calendar basis) {
      Calendar clone = y(basis);
      clone.set(2, basis.get(2));
      return clone;
   }

   public static Calendar ymd(Calendar basis) {
      Calendar clone = ym(basis);
      clone.set(5, basis.get(5));
      return clone;
   }

   public static Calendar ymdh(Calendar basis) {
      Calendar clone = ymd(basis);
      clone.set(11, basis.get(11));
      return clone;
   }

   public static Calendar ymdhm(Calendar basis) {
      Calendar clone = ymdh(basis);
      clone.set(12, basis.get(12));
      return clone;
   }

   public static Calendar cloneAndAdd(Calendar basis, int field, long amount) {
      Calendar next = (Calendar)basis.clone();
      next.add(field, (int)amount);
      return next;
   }
}
