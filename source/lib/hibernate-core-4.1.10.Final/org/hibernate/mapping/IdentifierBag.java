package org.hibernate.mapping;

import org.hibernate.cfg.Mappings;
import org.hibernate.type.CollectionType;

public class IdentifierBag extends IdentifierCollection {
   public IdentifierBag(Mappings mappings, PersistentClass owner) {
      super(mappings, owner);
   }

   public CollectionType getDefaultCollectionType() {
      return this.getMappings().getTypeResolver().getTypeFactory().idbag(this.getRole(), this.getReferencedPropertyName());
   }

   public Object accept(ValueVisitor visitor) {
      return visitor.accept(this);
   }
}
