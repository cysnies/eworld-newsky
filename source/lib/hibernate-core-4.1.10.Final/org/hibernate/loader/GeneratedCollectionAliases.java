package org.hibernate.loader;

import java.util.Collections;
import java.util.Map;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.persister.collection.CollectionPersister;

public class GeneratedCollectionAliases implements CollectionAliases {
   private final String suffix;
   private final String[] keyAliases;
   private final String[] indexAliases;
   private final String[] elementAliases;
   private final String identifierAlias;
   private Map userProvidedAliases;

   public GeneratedCollectionAliases(Map userProvidedAliases, CollectionPersister persister, String suffix) {
      this.suffix = suffix;
      this.userProvidedAliases = userProvidedAliases;
      this.keyAliases = this.getUserProvidedAliases("key", persister.getKeyColumnAliases(suffix));
      this.indexAliases = this.getUserProvidedAliases("index", persister.getIndexColumnAliases(suffix));
      this.elementAliases = this.getUserProvidedAliases("element", persister.getElementColumnAliases(suffix));
      this.identifierAlias = this.getUserProvidedAlias("id", persister.getIdentifierColumnAlias(suffix));
   }

   public GeneratedCollectionAliases(CollectionPersister persister, String string) {
      this(Collections.EMPTY_MAP, persister, string);
   }

   public String[] getSuffixedKeyAliases() {
      return this.keyAliases;
   }

   public String[] getSuffixedIndexAliases() {
      return this.indexAliases;
   }

   public String[] getSuffixedElementAliases() {
      return this.elementAliases;
   }

   public String getSuffixedIdentifierAlias() {
      return this.identifierAlias;
   }

   public String getSuffix() {
      return this.suffix;
   }

   public String toString() {
      return super.toString() + " [suffix=" + this.suffix + ", suffixedKeyAliases=[" + this.join(this.keyAliases) + "], suffixedIndexAliases=[" + this.join(this.indexAliases) + "], suffixedElementAliases=[" + this.join(this.elementAliases) + "], suffixedIdentifierAlias=[" + this.identifierAlias + "]]";
   }

   private String join(String[] aliases) {
      return aliases == null ? null : StringHelper.join(", ", aliases);
   }

   private String[] getUserProvidedAliases(String propertyPath, String[] defaultAliases) {
      String[] result = (String[])this.userProvidedAliases.get(propertyPath);
      return result == null ? defaultAliases : result;
   }

   private String getUserProvidedAlias(String propertyPath, String defaultAlias) {
      String[] columns = (String[])this.userProvidedAliases.get(propertyPath);
      return columns == null ? defaultAlias : columns[0];
   }
}
