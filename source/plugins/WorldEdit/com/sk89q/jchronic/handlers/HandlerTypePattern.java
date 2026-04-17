package com.sk89q.jchronic.handlers;

public class HandlerTypePattern extends HandlerPattern {
   private Handler.HandlerType _type;

   public HandlerTypePattern(Handler.HandlerType type) {
      this(type, false);
   }

   public HandlerTypePattern(Handler.HandlerType type, boolean optional) {
      super(optional);
      this._type = type;
   }

   public Handler.HandlerType getType() {
      return this._type;
   }
}
