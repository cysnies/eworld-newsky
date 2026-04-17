package org.hibernate.bytecode.buildtime.spi;

public interface Logger {
   void trace(String var1);

   void debug(String var1);

   void info(String var1);

   void warn(String var1);

   void error(String var1);
}
