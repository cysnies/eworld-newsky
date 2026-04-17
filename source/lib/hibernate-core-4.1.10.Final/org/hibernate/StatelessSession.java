package org.hibernate;

import java.io.Serializable;
import java.sql.Connection;

public interface StatelessSession extends SharedSessionContract {
   void close();

   Serializable insert(Object var1);

   Serializable insert(String var1, Object var2);

   void update(Object var1);

   void update(String var1, Object var2);

   void delete(Object var1);

   void delete(String var1, Object var2);

   Object get(String var1, Serializable var2);

   Object get(Class var1, Serializable var2);

   Object get(String var1, Serializable var2, LockMode var3);

   Object get(Class var1, Serializable var2, LockMode var3);

   void refresh(Object var1);

   void refresh(String var1, Object var2);

   void refresh(Object var1, LockMode var2);

   void refresh(String var1, Object var2, LockMode var3);

   /** @deprecated */
   @Deprecated
   Connection connection();
}
