package com.sk89q.minecraft.util.commands;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
   String[] aliases();

   String usage() default "";

   String desc();

   int min() default 0;

   int max() default -1;

   String flags() default "";

   String help() default "";

   boolean anyFlags() default false;
}
