package com.sk89q.jchronic.handlers;

public class HandlerPattern {
   private boolean _optional;

   public HandlerPattern(boolean optional) {
      this._optional = optional;
   }

   public boolean isOptional() {
      return this._optional;
   }
}
