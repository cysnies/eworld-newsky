package org.hibernate.bytecode.buildtime.spi;

public interface FieldFilter {
   boolean shouldInstrumentField(String var1, String var2);

   boolean shouldTransformFieldAccess(String var1, String var2, String var3);
}
