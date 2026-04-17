package org.hibernate.mapping;

import java.util.ArrayList;
import java.util.Iterator;
import org.hibernate.MappingException;
import org.hibernate.cfg.Mappings;
import org.hibernate.type.EntityType;
import org.hibernate.type.ForeignKeyDirection;
import org.hibernate.type.Type;

public class OneToOne extends ToOne {
   private boolean constrained;
   private ForeignKeyDirection foreignKeyType;
   private KeyValue identifier;
   private String propertyName;
   private String entityName;

   public OneToOne(Mappings mappings, Table table, PersistentClass owner) throws MappingException {
      super(mappings, table);
      this.identifier = owner.getKey();
      this.entityName = owner.getEntityName();
   }

   public String getPropertyName() {
      return this.propertyName;
   }

   public void setPropertyName(String propertyName) {
      this.propertyName = propertyName == null ? null : propertyName.intern();
   }

   public String getEntityName() {
      return this.entityName;
   }

   public void setEntityName(String propertyName) {
      this.entityName = this.entityName == null ? null : this.entityName.intern();
   }

   public Type getType() throws MappingException {
      return this.getColumnIterator().hasNext() ? this.getMappings().getTypeResolver().getTypeFactory().specialOneToOne(this.getReferencedEntityName(), this.foreignKeyType, this.referencedPropertyName, this.isLazy(), this.isUnwrapProxy(), this.entityName, this.propertyName) : this.getMappings().getTypeResolver().getTypeFactory().oneToOne(this.getReferencedEntityName(), this.foreignKeyType, this.referencedPropertyName, this.isLazy(), this.isUnwrapProxy(), this.entityName, this.propertyName);
   }

   public void createForeignKey() throws MappingException {
      if (this.constrained && this.referencedPropertyName == null) {
         this.createForeignKeyOfEntity(((EntityType)this.getType()).getAssociatedEntityName());
      }

   }

   public java.util.List getConstraintColumns() {
      ArrayList list = new ArrayList();
      Iterator iter = this.identifier.getColumnIterator();

      while(iter.hasNext()) {
         list.add(iter.next());
      }

      return list;
   }

   public boolean isConstrained() {
      return this.constrained;
   }

   public ForeignKeyDirection getForeignKeyType() {
      return this.foreignKeyType;
   }

   public KeyValue getIdentifier() {
      return this.identifier;
   }

   public void setConstrained(boolean constrained) {
      this.constrained = constrained;
   }

   public void setForeignKeyType(ForeignKeyDirection foreignKeyType) {
      this.foreignKeyType = foreignKeyType;
   }

   public void setIdentifier(KeyValue identifier) {
      this.identifier = identifier;
   }

   public boolean isNullable() {
      return !this.constrained;
   }

   public Object accept(ValueVisitor visitor) {
      return visitor.accept(this);
   }
}
