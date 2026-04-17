package org.hibernate.engine.jdbc;

import java.lang.reflect.Proxy;
import java.sql.Clob;
import java.sql.NClob;

public class SerializableNClobProxy extends SerializableClobProxy {
   private static final Class[] PROXY_INTERFACES = new Class[]{NClob.class, WrappedNClob.class};

   public static boolean isNClob(Clob clob) {
      return NClob.class.isInstance(clob);
   }

   protected SerializableNClobProxy(Clob clob) {
      super(clob);
   }

   public static NClob generateProxy(NClob nclob) {
      return (NClob)Proxy.newProxyInstance(getProxyClassLoader(), PROXY_INTERFACES, new SerializableNClobProxy(nclob));
   }

   public static ClassLoader getProxyClassLoader() {
      return SerializableClobProxy.getProxyClassLoader();
   }
}
