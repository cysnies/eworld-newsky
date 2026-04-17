package com.sk89q.minecraft.util.commands;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Logging {
   LogMode value();

   public static enum LogMode {
      POSITION,
      REGION,
      ORIENTATION_REGION,
      PLACEMENT,
      ALL;
   }
}
