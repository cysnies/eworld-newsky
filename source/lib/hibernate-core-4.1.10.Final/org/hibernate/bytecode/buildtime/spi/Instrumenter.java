package org.hibernate.bytecode.buildtime.spi;

import java.util.Set;

public interface Instrumenter {
   void execute(Set var1);

   public interface Options {
      boolean performExtendedInstrumentation();
   }
}
