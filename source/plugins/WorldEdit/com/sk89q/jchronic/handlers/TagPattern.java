package com.sk89q.jchronic.handlers;

import com.sk89q.jchronic.tags.Tag;

public class TagPattern extends HandlerPattern {
   private Class _tagClass;

   public TagPattern(Class tagClass) {
      this(tagClass, false);
   }

   public TagPattern(Class tagClass, boolean optional) {
      super(optional);
      this._tagClass = tagClass;
   }

   public Class getTagClass() {
      return this._tagClass;
   }

   public String toString() {
      return "[TagPattern: tagClass = " + this._tagClass + "]";
   }
}
