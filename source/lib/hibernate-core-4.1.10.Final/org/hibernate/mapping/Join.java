package org.hibernate.mapping;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import org.hibernate.engine.spi.ExecuteUpdateResultCheckStyle;
import org.hibernate.sql.Alias;

public class Join implements Serializable {
   private static final Alias PK_ALIAS = new Alias(15, "PK");
   private ArrayList properties = new ArrayList();
   private ArrayList declaredProperties = new ArrayList();
   private Table table;
   private KeyValue key;
   private PersistentClass persistentClass;
   private boolean sequentialSelect;
   private boolean inverse;
   private boolean optional;
   private String customSQLInsert;
   private boolean customInsertCallable;
   private ExecuteUpdateResultCheckStyle insertCheckStyle;
   private String customSQLUpdate;
   private boolean customUpdateCallable;
   private ExecuteUpdateResultCheckStyle updateCheckStyle;
   private String customSQLDelete;
   private boolean customDeleteCallable;
   private ExecuteUpdateResultCheckStyle deleteCheckStyle;

   public void addProperty(Property prop) {
      this.properties.add(prop);
      this.declaredProperties.add(prop);
      prop.setPersistentClass(this.getPersistentClass());
   }

   public void addMappedsuperclassProperty(Property prop) {
      this.properties.add(prop);
      prop.setPersistentClass(this.getPersistentClass());
   }

   public Iterator getDeclaredPropertyIterator() {
      return this.declaredProperties.iterator();
   }

   public boolean containsProperty(Property prop) {
      return this.properties.contains(prop);
   }

   public Iterator getPropertyIterator() {
      return this.properties.iterator();
   }

   public Table getTable() {
      return this.table;
   }

   public void setTable(Table table) {
      this.table = table;
   }

   public KeyValue getKey() {
      return this.key;
   }

   public void setKey(KeyValue key) {
      this.key = key;
   }

   public PersistentClass getPersistentClass() {
      return this.persistentClass;
   }

   public void setPersistentClass(PersistentClass persistentClass) {
      this.persistentClass = persistentClass;
   }

   public void createForeignKey() {
      this.getKey().createForeignKeyOfEntity(this.persistentClass.getEntityName());
   }

   public void createPrimaryKey() {
      PrimaryKey pk = new PrimaryKey();
      pk.setTable(this.table);
      pk.setName(PK_ALIAS.toAliasString(this.table.getName()));
      this.table.setPrimaryKey(pk);
      pk.addColumns(this.getKey().getColumnIterator());
   }

   public int getPropertySpan() {
      return this.properties.size();
   }

   public void setCustomSQLInsert(String customSQLInsert, boolean callable, ExecuteUpdateResultCheckStyle checkStyle) {
      this.customSQLInsert = customSQLInsert;
      this.customInsertCallable = callable;
      this.insertCheckStyle = checkStyle;
   }

   public String getCustomSQLInsert() {
      return this.customSQLInsert;
   }

   public boolean isCustomInsertCallable() {
      return this.customInsertCallable;
   }

   public ExecuteUpdateResultCheckStyle getCustomSQLInsertCheckStyle() {
      return this.insertCheckStyle;
   }

   public void setCustomSQLUpdate(String customSQLUpdate, boolean callable, ExecuteUpdateResultCheckStyle checkStyle) {
      this.customSQLUpdate = customSQLUpdate;
      this.customUpdateCallable = callable;
      this.updateCheckStyle = checkStyle;
   }

   public String getCustomSQLUpdate() {
      return this.customSQLUpdate;
   }

   public boolean isCustomUpdateCallable() {
      return this.customUpdateCallable;
   }

   public ExecuteUpdateResultCheckStyle getCustomSQLUpdateCheckStyle() {
      return this.updateCheckStyle;
   }

   public void setCustomSQLDelete(String customSQLDelete, boolean callable, ExecuteUpdateResultCheckStyle checkStyle) {
      this.customSQLDelete = customSQLDelete;
      this.customDeleteCallable = callable;
      this.deleteCheckStyle = checkStyle;
   }

   public String getCustomSQLDelete() {
      return this.customSQLDelete;
   }

   public boolean isCustomDeleteCallable() {
      return this.customDeleteCallable;
   }

   public ExecuteUpdateResultCheckStyle getCustomSQLDeleteCheckStyle() {
      return this.deleteCheckStyle;
   }

   public boolean isSequentialSelect() {
      return this.sequentialSelect;
   }

   public void setSequentialSelect(boolean deferred) {
      this.sequentialSelect = deferred;
   }

   public boolean isInverse() {
      return this.inverse;
   }

   public void setInverse(boolean leftJoin) {
      this.inverse = leftJoin;
   }

   public String toString() {
      return this.getClass().getName() + '(' + this.table.toString() + ')';
   }

   public boolean isLazy() {
      Iterator iter = this.getPropertyIterator();

      while(iter.hasNext()) {
         Property prop = (Property)iter.next();
         if (!prop.isLazy()) {
            return false;
         }
      }

      return true;
   }

   public boolean isOptional() {
      return this.optional;
   }

   public void setOptional(boolean nullable) {
      this.optional = nullable;
   }
}
