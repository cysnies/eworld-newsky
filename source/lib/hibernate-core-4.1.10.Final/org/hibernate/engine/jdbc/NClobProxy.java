package org.hibernate.engine.jdbc;

import java.io.Reader;
import java.lang.reflect.Proxy;
import java.sql.NClob;

public class NClobProxy extends ClobProxy {
   public static final Class[] PROXY_INTERFACES = new Class[]{NClob.class, NClobImplementer.class};

   protected NClobProxy(String string) {
      super(string);
   }

   protected NClobProxy(Reader reader, long length) {
      super(reader, length);
   }

   public static NClob generateProxy(String string) {
      return (NClob)Proxy.newProxyInstance(getProxyClassLoader(), PROXY_INTERFACES, new ClobProxy(string));
   }

   public static NClob generateProxy(Reader reader, long length) {
      return (NClob)Proxy.newProxyInstance(getProxyClassLoader(), PROXY_INTERFACES, new ClobProxy(reader, length));
   }

   protected static ClassLoader getProxyClassLoader() {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      if (cl == null) {
         cl = NClobImplementer.class.getClassLoader();
      }

      return cl;
   }
}
