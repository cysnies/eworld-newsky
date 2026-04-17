package org.hibernate.bytecode.buildtime.spi;

public interface ClassDescriptor {
   String getName();

   boolean isInstrumented();

   byte[] getBytes();
}
