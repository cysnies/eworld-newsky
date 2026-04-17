package org.hibernate.service.spi;

public interface Wrapped {
   boolean isUnwrappableAs(Class var1);

   Object unwrap(Class var1);
}
