package org.hibernate.loader;

import org.hibernate.persister.entity.Loadable;

public interface EntityAliases {
   String[] getSuffixedKeyAliases();

   String getSuffixedDiscriminatorAlias();

   String[] getSuffixedVersionAliases();

   String[][] getSuffixedPropertyAliases();

   String[][] getSuffixedPropertyAliases(Loadable var1);

   String getRowIdAlias();
}
