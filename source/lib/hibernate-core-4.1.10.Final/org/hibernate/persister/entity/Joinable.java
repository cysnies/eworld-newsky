package org.hibernate.persister.entity;

import java.util.Map;
import org.hibernate.MappingException;

public interface Joinable {
   String getName();

   String getTableName();

   String selectFragment(Joinable var1, String var2, String var3, String var4, String var5, boolean var6);

   String whereJoinFragment(String var1, boolean var2, boolean var3);

   String fromJoinFragment(String var1, boolean var2, boolean var3);

   String[] getKeyColumnNames();

   String filterFragment(String var1, Map var2) throws MappingException;

   String oneToManyFilterFragment(String var1) throws MappingException;

   boolean isCollection();

   boolean consumesEntityAlias();

   boolean consumesCollectionAlias();
}
