package org.hibernate.engine.spi;

public interface UnsavedValueStrategy {
   Boolean isUnsaved(Object var1);

   Object getDefaultValue(Object var1);
}
