package org.hibernate.metamodel.binding;

import org.hibernate.AssertionFailure;
import org.hibernate.metamodel.relational.ForeignKey;
import org.hibernate.metamodel.relational.TableSpecification;

public class CollectionKey {
   private final AbstractPluralAttributeBinding pluralAttributeBinding;
   private ForeignKey foreignKey;
   private boolean inverse;
   private HibernateTypeDescriptor hibernateTypeDescriptor;

   public CollectionKey(AbstractPluralAttributeBinding pluralAttributeBinding) {
      this.pluralAttributeBinding = pluralAttributeBinding;
   }

   public AbstractPluralAttributeBinding getPluralAttributeBinding() {
      return this.pluralAttributeBinding;
   }

   public void prepareForeignKey(String foreignKeyName, String targetTableName) {
      if (this.foreignKey != null) {
         throw new AssertionFailure("Foreign key already initialized");
      } else {
         TableSpecification collectionTable = this.pluralAttributeBinding.getCollectionTable();
         if (collectionTable == null) {
            throw new AssertionFailure("Collection table not yet bound");
         } else {
            TableSpecification targetTable = this.pluralAttributeBinding.getContainer().seekEntityBinding().locateTable(targetTableName);
            this.foreignKey = collectionTable.createForeignKey(targetTable, foreignKeyName);
         }
      }
   }

   public ForeignKey getForeignKey() {
      return this.foreignKey;
   }
}
