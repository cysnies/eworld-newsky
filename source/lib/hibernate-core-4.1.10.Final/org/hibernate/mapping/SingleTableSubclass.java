package org.hibernate.mapping;

import java.util.Iterator;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.internal.util.collections.JoinedIterator;

public class SingleTableSubclass extends Subclass {
   public SingleTableSubclass(PersistentClass superclass) {
      super(superclass);
   }

   protected Iterator getNonDuplicatedPropertyIterator() {
      return new JoinedIterator(this.getSuperclass().getUnjoinedPropertyIterator(), this.getUnjoinedPropertyIterator());
   }

   protected Iterator getDiscriminatorColumnIterator() {
      return this.isDiscriminatorInsertable() && !this.getDiscriminator().hasFormula() ? this.getDiscriminator().getColumnIterator() : super.getDiscriminatorColumnIterator();
   }

   public Object accept(PersistentClassVisitor mv) {
      return mv.accept(this);
   }

   public void validate(Mapping mapping) throws MappingException {
      if (this.getDiscriminator() == null) {
         throw new MappingException("No discriminator found for " + this.getEntityName() + ". Discriminator is needed when 'single-table-per-hierarchy' is used and a class has subclasses");
      } else {
         super.validate(mapping);
      }
   }
}
