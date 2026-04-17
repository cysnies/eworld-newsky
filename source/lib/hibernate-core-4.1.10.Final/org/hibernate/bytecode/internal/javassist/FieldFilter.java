package org.hibernate.bytecode.internal.javassist;

public interface FieldFilter {
   boolean handleRead(String var1, String var2);

   boolean handleWrite(String var1, String var2);

   boolean handleReadAccess(String var1, String var2);

   boolean handleWriteAccess(String var1, String var2);
}
