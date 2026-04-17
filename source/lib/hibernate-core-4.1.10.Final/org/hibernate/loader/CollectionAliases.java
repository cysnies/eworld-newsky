package org.hibernate.loader;

public interface CollectionAliases {
   String[] getSuffixedKeyAliases();

   String[] getSuffixedIndexAliases();

   String[] getSuffixedElementAliases();

   String getSuffixedIdentifierAlias();

   String getSuffix();
}
