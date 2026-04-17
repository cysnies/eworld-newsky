package org.hibernate;

import org.hibernate.type.Type;

public interface SQLQuery extends Query {
   SQLQuery addSynchronizedQuerySpace(String var1);

   SQLQuery addSynchronizedEntityName(String var1) throws MappingException;

   SQLQuery addSynchronizedEntityClass(Class var1) throws MappingException;

   SQLQuery setResultSetMapping(String var1);

   SQLQuery addScalar(String var1);

   SQLQuery addScalar(String var1, Type var2);

   RootReturn addRoot(String var1, String var2);

   RootReturn addRoot(String var1, Class var2);

   SQLQuery addEntity(String var1);

   SQLQuery addEntity(String var1, String var2);

   SQLQuery addEntity(String var1, String var2, LockMode var3);

   SQLQuery addEntity(Class var1);

   SQLQuery addEntity(String var1, Class var2);

   SQLQuery addEntity(String var1, Class var2, LockMode var3);

   FetchReturn addFetch(String var1, String var2, String var3);

   SQLQuery addJoin(String var1, String var2);

   SQLQuery addJoin(String var1, String var2, String var3);

   SQLQuery addJoin(String var1, String var2, LockMode var3);

   public interface FetchReturn {
      FetchReturn setLockMode(LockMode var1);

      FetchReturn addProperty(String var1, String var2);

      ReturnProperty addProperty(String var1);
   }

   public interface ReturnProperty {
      ReturnProperty addColumnAlias(String var1);
   }

   public interface RootReturn {
      RootReturn setLockMode(LockMode var1);

      RootReturn setDiscriminatorAlias(String var1);

      RootReturn addProperty(String var1, String var2);

      ReturnProperty addProperty(String var1);
   }
}
