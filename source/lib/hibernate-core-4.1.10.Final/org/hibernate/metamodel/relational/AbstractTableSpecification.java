package org.hibernate.metamodel.relational;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractTableSpecification implements TableSpecification {
   private static final AtomicInteger tableCounter = new AtomicInteger(0);
   private final int tableNumber;
   private final LinkedHashMap values = new LinkedHashMap();
   private final PrimaryKey primaryKey = new PrimaryKey(this);
   private final List foreignKeys = new ArrayList();

   public AbstractTableSpecification() {
      this.tableNumber = tableCounter.getAndIncrement();
   }

   public int getTableNumber() {
      return this.tableNumber;
   }

   public Iterable values() {
      return this.values.values();
   }

   public Column locateOrCreateColumn(String name) {
      if (this.values.containsKey(name)) {
         return (Column)this.values.get(name);
      } else {
         Column column = new Column(this, this.values.size(), name);
         this.values.put(name, column);
         return column;
      }
   }

   public DerivedValue locateOrCreateDerivedValue(String fragment) {
      if (this.values.containsKey(fragment)) {
         return (DerivedValue)this.values.get(fragment);
      } else {
         DerivedValue value = new DerivedValue(this, this.values.size(), fragment);
         this.values.put(fragment, value);
         return value;
      }
   }

   public Tuple createTuple(String name) {
      return new Tuple(this, name);
   }

   public Iterable getForeignKeys() {
      return this.foreignKeys;
   }

   public ForeignKey createForeignKey(TableSpecification targetTable, String name) {
      ForeignKey fk = new ForeignKey(this, targetTable, name);
      this.foreignKeys.add(fk);
      return fk;
   }

   public PrimaryKey getPrimaryKey() {
      return this.primaryKey;
   }
}
