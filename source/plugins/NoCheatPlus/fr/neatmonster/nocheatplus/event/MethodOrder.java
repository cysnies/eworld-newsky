package fr.neatmonster.nocheatplus.event;

public @interface MethodOrder {
   String tag() default "";

   String beforeTag() default "";
}
