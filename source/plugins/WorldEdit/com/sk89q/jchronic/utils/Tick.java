package com.sk89q.jchronic.utils;

public class Tick {
   private int _time;
   private boolean _ambiguous;

   public Tick(int time, boolean ambiguous) {
      this._time = time;
      this._ambiguous = ambiguous;
   }

   public boolean isAmbiguous() {
      return this._ambiguous;
   }

   public void setTime(int time) {
      this._time = time;
   }

   public Tick times(int other) {
      return new Tick(this._time * other, this._ambiguous);
   }

   public int intValue() {
      return this._time;
   }

   public float floatValue() {
      return (float)this._time;
   }

   public String toString() {
      return this._time + (this._ambiguous ? "?" : "");
   }
}
