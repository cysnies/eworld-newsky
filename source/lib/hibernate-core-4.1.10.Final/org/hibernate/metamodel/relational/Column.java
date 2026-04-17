package org.hibernate.metamodel.relational;

import org.hibernate.MappingException;
import org.hibernate.dialect.Dialect;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.metamodel.relational.state.ColumnRelationalState;

public class Column extends AbstractSimpleValue {
   private final Identifier columnName;
   private boolean nullable;
   private boolean unique;
   private String defaultValue;
   private String checkCondition;
   private String sqlType;
   private String readFragment;
   private String writeFragment;
   private String comment;
   private Size size;

   protected Column(TableSpecification table, int position, String name) {
      this(table, position, Identifier.toIdentifier(name));
   }

   protected Column(TableSpecification table, int position, Identifier name) {
      super(table, position);
      this.size = new Size();
      this.columnName = name;
   }

   public void initialize(ColumnRelationalState state, boolean forceNonNullable, boolean forceUnique) {
      this.size.initialize(state.getSize());
      this.nullable = !forceNonNullable && state.isNullable();
      this.unique = !forceUnique && state.isUnique();
      this.checkCondition = state.getCheckCondition();
      this.defaultValue = state.getDefault();
      this.sqlType = state.getSqlType();
      this.writeFragment = state.getCustomWriteFragment();
      this.readFragment = state.getCustomReadFragment();
      this.comment = state.getComment();

      for(String uniqueKey : state.getUniqueKeys()) {
         this.getTable().getOrCreateUniqueKey(uniqueKey).addColumn(this);
      }

      for(String index : state.getIndexes()) {
         this.getTable().getOrCreateIndex(index).addColumn(this);
      }

   }

   public Identifier getColumnName() {
      return this.columnName;
   }

   public boolean isNullable() {
      return this.nullable;
   }

   public void setNullable(boolean nullable) {
      this.nullable = nullable;
   }

   public boolean isUnique() {
      return this.unique;
   }

   public void setUnique(boolean unique) {
      this.unique = unique;
   }

   public String getDefaultValue() {
      return this.defaultValue;
   }

   public void setDefaultValue(String defaultValue) {
      this.defaultValue = defaultValue;
   }

   public String getCheckCondition() {
      return this.checkCondition;
   }

   public void setCheckCondition(String checkCondition) {
      this.checkCondition = checkCondition;
   }

   public String getSqlType() {
      return this.sqlType;
   }

   public void setSqlType(String sqlType) {
      this.sqlType = sqlType;
   }

   public String getReadFragment() {
      return this.readFragment;
   }

   public void setReadFragment(String readFragment) {
      this.readFragment = readFragment;
   }

   public String getWriteFragment() {
      return this.writeFragment;
   }

   public void setWriteFragment(String writeFragment) {
      this.writeFragment = writeFragment;
   }

   public String getComment() {
      return this.comment;
   }

   public void setComment(String comment) {
      this.comment = comment;
   }

   public Size getSize() {
      return this.size;
   }

   public void setSize(Size size) {
      this.size = size;
   }

   public String toLoggableString() {
      return this.getTable().getLoggableValueQualifier() + '.' + this.getColumnName();
   }

   public String getAlias(Dialect dialect) {
      String alias = this.columnName.getName();
      int lastLetter = StringHelper.lastIndexOfLetter(this.columnName.getName());
      if (lastLetter == -1) {
         alias = "column";
      }

      boolean useRawName = this.columnName.getName().equals(alias) && alias.length() <= dialect.getMaxAliasLength() && !this.columnName.isQuoted() && !this.columnName.getName().toLowerCase().equals("rowid");
      if (!useRawName) {
         String unique = "" + this.getPosition() + '_' + this.getTable().getTableNumber() + '_';
         if (unique.length() >= dialect.getMaxAliasLength()) {
            throw new MappingException("Unique suffix [" + unique + "] length must be less than maximum [" + dialect.getMaxAliasLength() + "]");
         }

         if (alias.length() + unique.length() > dialect.getMaxAliasLength()) {
            alias = alias.substring(0, dialect.getMaxAliasLength() - unique.length());
         }

         alias = alias + unique;
      }

      return alias;
   }
}
