package org.hibernate.loader;

import java.util.Collections;
import java.util.Map;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.persister.entity.Loadable;

public class DefaultEntityAliases implements EntityAliases {
   private final String[] suffixedKeyColumns;
   private final String[] suffixedVersionColumn;
   private final String[][] suffixedPropertyColumns;
   private final String suffixedDiscriminatorColumn;
   private final String suffix;
   private final String rowIdAlias;
   private final Map userProvidedAliases;

   public DefaultEntityAliases(Map userProvidedAliases, Loadable persister, String suffix) {
      this.suffix = suffix;
      this.userProvidedAliases = userProvidedAliases;
      this.suffixedKeyColumns = this.determineKeyAlias(persister, suffix);
      this.suffixedPropertyColumns = this.determinePropertyAliases(persister);
      this.suffixedDiscriminatorColumn = this.determineDiscriminatorAlias(persister, suffix);
      this.suffixedVersionColumn = this.determineVersionAlias(persister);
      this.rowIdAlias = "rowid_" + suffix;
   }

   public DefaultEntityAliases(Loadable persister, String suffix) {
      this(Collections.EMPTY_MAP, persister, suffix);
   }

   private String[] determineKeyAlias(Loadable persister, String suffix) {
      String[] keyColumnsCandidates = this.getUserProvidedAliases(persister.getIdentifierPropertyName(), (String[])null);
      String[] aliases;
      if (keyColumnsCandidates == null) {
         aliases = this.getUserProvidedAliases("id", this.getIdentifierAliases(persister, suffix));
      } else {
         aliases = keyColumnsCandidates;
      }

      String[] rtn = StringHelper.unquote(aliases, persister.getFactory().getDialect());
      intern(rtn);
      return rtn;
   }

   private String[][] determinePropertyAliases(Loadable persister) {
      return this.getSuffixedPropertyAliases(persister);
   }

   private String determineDiscriminatorAlias(Loadable persister, String suffix) {
      String alias = this.getUserProvidedAlias("class", this.getDiscriminatorAlias(persister, suffix));
      return StringHelper.unquote(alias, persister.getFactory().getDialect());
   }

   private String[] determineVersionAlias(Loadable persister) {
      return persister.isVersioned() ? this.suffixedPropertyColumns[persister.getVersionProperty()] : null;
   }

   protected String getDiscriminatorAlias(Loadable persister, String suffix) {
      return persister.getDiscriminatorAlias(suffix);
   }

   protected String[] getIdentifierAliases(Loadable persister, String suffix) {
      return persister.getIdentifierAliases(suffix);
   }

   protected String[] getPropertyAliases(Loadable persister, int j) {
      return persister.getPropertyAliases(this.suffix, j);
   }

   private String[] getUserProvidedAliases(String propertyPath, String[] defaultAliases) {
      String[] result = (String[])this.userProvidedAliases.get(propertyPath);
      return result == null ? defaultAliases : result;
   }

   private String getUserProvidedAlias(String propertyPath, String defaultAlias) {
      String[] columns = (String[])this.userProvidedAliases.get(propertyPath);
      return columns == null ? defaultAlias : columns[0];
   }

   public String[][] getSuffixedPropertyAliases(Loadable persister) {
      int size = persister.getPropertyNames().length;
      String[][] suffixedPropertyAliases = new String[size][];

      for(int j = 0; j < size; ++j) {
         suffixedPropertyAliases[j] = this.getUserProvidedAliases(persister.getPropertyNames()[j], this.getPropertyAliases(persister, j));
         suffixedPropertyAliases[j] = StringHelper.unquote(suffixedPropertyAliases[j], persister.getFactory().getDialect());
         intern(suffixedPropertyAliases[j]);
      }

      return suffixedPropertyAliases;
   }

   public String[] getSuffixedVersionAliases() {
      return this.suffixedVersionColumn;
   }

   public String[][] getSuffixedPropertyAliases() {
      return this.suffixedPropertyColumns;
   }

   public String getSuffixedDiscriminatorAlias() {
      return this.suffixedDiscriminatorColumn;
   }

   public String[] getSuffixedKeyAliases() {
      return this.suffixedKeyColumns;
   }

   public String getRowIdAlias() {
      return this.rowIdAlias;
   }

   private static void intern(String[] strings) {
      for(int i = 0; i < strings.length; ++i) {
         strings[i] = strings[i].intern();
      }

   }
}
