package com.sk89q.jchronic.utils;

public class Range {
   private long _begin;
   private long _end;

   public Range(long begin, long end) {
      this._begin = begin;
      this._end = end;
   }

   public long getBegin() {
      return this._begin;
   }

   public long getEnd() {
      return this._end;
   }

   public long getWidth() {
      return this.getEnd() - this.getBegin();
   }

   public boolean isSingularity() {
      return this.getEnd() == this.getBegin();
   }

   public boolean contains(long value) {
      return this._begin <= value && this._end >= value;
   }

   public int hashCode() {
      return (int)(this._begin * this._end);
   }

   public boolean equals(Object obj) {
      return obj instanceof Range && ((Range)obj)._begin == this._begin && ((Range)obj)._end == this._end;
   }
}
