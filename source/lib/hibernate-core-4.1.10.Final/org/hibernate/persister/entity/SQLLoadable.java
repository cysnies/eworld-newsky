package org.hibernate.persister.entity;

import org.hibernate.type.Type;

public interface SQLLoadable extends Loadable {
   String[] getSubclassPropertyColumnAliases(String var1, String var2);

   String[] getSubclassPropertyColumnNames(String var1);

   String selectFragment(String var1, String var2);

   Type getType();
}
