package com.sk89q.jchronic;

import com.sk89q.jchronic.tags.Pointer;
import java.util.Calendar;

public class Options {
   private Pointer.PointerType _context;
   private Calendar _now;
   private boolean _guess;
   private boolean _debug;
   private int _ambiguousTimeRange;
   private boolean _compatibilityMode;

   public Options() {
      this(Pointer.PointerType.FUTURE, Calendar.getInstance(), true, 6);
   }

   public Options(Calendar now) {
      this(Pointer.PointerType.FUTURE, now, true, 6);
   }

   public Options(Calendar now, boolean guess) {
      this(Pointer.PointerType.FUTURE, now, guess, 6);
   }

   public Options(Pointer.PointerType context) {
      this(context, Calendar.getInstance(), true, 6);
   }

   public Options(boolean guess) {
      this(Pointer.PointerType.FUTURE, Calendar.getInstance(), guess, 6);
   }

   public Options(int ambiguousTimeRange) {
      this(Pointer.PointerType.FUTURE, Calendar.getInstance(), true, ambiguousTimeRange);
   }

   public Options(Pointer.PointerType context, Calendar now, boolean guess, int ambiguousTimeRange) {
      this._context = context;
      this._now = now;
      this._guess = guess;
      this._ambiguousTimeRange = ambiguousTimeRange;
   }

   public void setDebug(boolean debug) {
      this._debug = debug;
   }

   public boolean isDebug() {
      return this._debug;
   }

   public void setCompatibilityMode(boolean compatibilityMode) {
      this._compatibilityMode = compatibilityMode;
   }

   public boolean isCompatibilityMode() {
      return this._compatibilityMode;
   }

   public void setContext(Pointer.PointerType context) {
      this._context = context;
   }

   public Pointer.PointerType getContext() {
      return this._context;
   }

   public void setNow(Calendar now) {
      this._now = now;
   }

   public Calendar getNow() {
      return this._now;
   }

   public void setGuess(boolean guess) {
      this._guess = guess;
   }

   public boolean isGuess() {
      return this._guess;
   }

   public void setAmbiguousTimeRange(int ambiguousTimeRange) {
      this._ambiguousTimeRange = ambiguousTimeRange;
   }

   public int getAmbiguousTimeRange() {
      return this._ambiguousTimeRange;
   }
}
