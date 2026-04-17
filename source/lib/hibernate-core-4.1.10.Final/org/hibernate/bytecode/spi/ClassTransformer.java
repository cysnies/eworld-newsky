package org.hibernate.bytecode.spi;

import java.security.ProtectionDomain;

public interface ClassTransformer {
   byte[] transform(ClassLoader var1, String var2, Class var3, ProtectionDomain var4, byte[] var5);
}
