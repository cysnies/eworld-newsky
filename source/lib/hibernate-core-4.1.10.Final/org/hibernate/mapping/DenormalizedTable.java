package org.hibernate.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.hibernate.internal.util.collections.JoinedIterator;

public class DenormalizedTable extends Table {
   private final Table includedTable;

   public DenormalizedTable(Table includedTable) {
      this.includedTable = includedTable;
      includedTable.setHasDenormalizedTables();
   }

   public void createForeignKeys() {
      this.includedTable.createForeignKeys();
      Iterator iter = this.includedTable.getForeignKeyIterator();

      while(iter.hasNext()) {
         ForeignKey fk = (ForeignKey)iter.next();
         this.createForeignKey(fk.getName() + Integer.toHexString(this.getName().hashCode()), fk.getColumns(), fk.getReferencedEntityName());
      }

   }

   public Column getColumn(Column column) {
      Column superColumn = super.getColumn(column);
      return superColumn != null ? superColumn : this.includedTable.getColumn(column);
   }

   public Iterator getColumnIterator() {
      return new JoinedIterator(this.includedTable.getColumnIterator(), super.getColumnIterator());
   }

   public boolean containsColumn(Column column) {
      return super.containsColumn(column) || this.includedTable.containsColumn(column);
   }

   public PrimaryKey getPrimaryKey() {
      return this.includedTable.getPrimaryKey();
   }

   public Iterator getUniqueKeyIterator() {
      java.util.Map uks = new HashMap();
      uks.putAll(this.getUniqueKeys());
      uks.putAll(this.includedTable.getUniqueKeys());
      return uks.values().iterator();
   }

   public Iterator getIndexIterator() {
      java.util.List indexes = new ArrayList();
      Iterator iter = this.includedTable.getIndexIterator();

      while(iter.hasNext()) {
         Index parentIndex = (Index)iter.next();
         Index index = new Index();
         index.setName(this.getName() + parentIndex.getName());
         index.setTable(this);
         index.addColumns(parentIndex.getColumnIterator());
         indexes.add(index);
      }

      return new JoinedIterator(indexes.iterator(), super.getIndexIterator());
   }
}
