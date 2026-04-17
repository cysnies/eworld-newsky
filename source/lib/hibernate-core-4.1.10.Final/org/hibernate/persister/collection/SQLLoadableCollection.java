package org.hibernate.persister.collection;

public interface SQLLoadableCollection extends QueryableCollection {
   String[] getCollectionPropertyColumnAliases(String var1, String var2);

   String getIdentifierColumnName();
}
