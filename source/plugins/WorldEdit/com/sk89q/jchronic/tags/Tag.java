package com.sk89q.jchronic.tags;

import java.util.Calendar;

public class Tag {
   private Object _type;
   private Calendar _now;

   public Tag(Object type) {
      this._type = type;
   }

   public Calendar getNow() {
      return this._now;
   }

   public void setType(Object type) {
      this._type = type;
   }

   public Object getType() {
      return this._type;
   }

   public void setStart(Calendar s) {
      this._now = s;
   }
}
