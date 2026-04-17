package org.hibernate.mapping;

import java.util.Iterator;
import org.hibernate.FetchMode;
import org.hibernate.MappingException;
import org.hibernate.cfg.Mappings;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.type.EntityType;
import org.hibernate.type.Type;

public class OneToMany implements Value {
   private final Mappings mappings;
   private final Table referencingTable;
   private String referencedEntityName;
   private PersistentClass associatedClass;
   private boolean embedded;
   private boolean ignoreNotFound;

   private EntityType getEntityType() {
      return this.mappings.getTypeResolver().getTypeFactory().manyToOne(this.getReferencedEntityName(), (String)null, false, false, this.isIgnoreNotFound(), false);
   }

   public OneToMany(Mappings mappings, PersistentClass owner) throws MappingException {
      this.mappings = mappings;
      this.referencingTable = owner == null ? null : owner.getTable();
   }

   public PersistentClass getAssociatedClass() {
      return this.associatedClass;
   }

   public void setAssociatedClass(PersistentClass associatedClass) {
      this.associatedClass = associatedClass;
   }

   public void createForeignKey() {
   }

   public Iterator getColumnIterator() {
      return this.associatedClass.getKey().getColumnIterator();
   }

   public int getColumnSpan() {
      return this.associatedClass.getKey().getColumnSpan();
   }

   public FetchMode getFetchMode() {
      return FetchMode.JOIN;
   }

   public Table getTable() {
      return this.referencingTable;
   }

   public Type getType() {
      return this.getEntityType();
   }

   public boolean isNullable() {
      return false;
   }

   public boolean isSimpleValue() {
      return false;
   }

   public boolean isAlternateUniqueKey() {
      return false;
   }

   public boolean hasFormula() {
      return false;
   }

   public boolean isValid(Mapping mapping) throws MappingException {
      if (this.referencedEntityName == null) {
         throw new MappingException("one to many association must specify the referenced entity");
      } else {
         return true;
      }
   }

   public String getReferencedEntityName() {
      return this.referencedEntityName;
   }

   public void setReferencedEntityName(String referencedEntityName) {
      this.referencedEntityName = referencedEntityName == null ? null : referencedEntityName.intern();
   }

   public void setTypeUsingReflection(String className, String propertyName) {
   }

   public Object accept(ValueVisitor visitor) {
      return visitor.accept(this);
   }

   public boolean[] getColumnInsertability() {
      throw new UnsupportedOperationException();
   }

   public boolean[] getColumnUpdateability() {
      throw new UnsupportedOperationException();
   }

   /** @deprecated */
   @Deprecated
   public boolean isEmbedded() {
      return this.embedded;
   }

   /** @deprecated */
   @Deprecated
   public void setEmbedded(boolean embedded) {
      this.embedded = embedded;
   }

   public boolean isIgnoreNotFound() {
      return this.ignoreNotFound;
   }

   public void setIgnoreNotFound(boolean ignoreNotFound) {
      this.ignoreNotFound = ignoreNotFound;
   }
}
