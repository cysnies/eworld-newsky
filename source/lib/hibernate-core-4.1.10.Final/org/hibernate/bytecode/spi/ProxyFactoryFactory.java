package org.hibernate.bytecode.spi;

import org.hibernate.proxy.ProxyFactory;

public interface ProxyFactoryFactory {
   ProxyFactory buildProxyFactory();

   BasicProxyFactory buildBasicProxyFactory(Class var1, Class[] var2);
}
