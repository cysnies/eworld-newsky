package org.hibernate.metamodel.source.annotations.attribute;

import java.util.Map;
import org.hibernate.AssertionFailure;
import org.hibernate.metamodel.binding.IdGenerator;
import org.hibernate.metamodel.source.binder.IdentifierSource;
import org.hibernate.metamodel.source.binder.SimpleIdentifierSource;
import org.hibernate.metamodel.source.binder.SingularAttributeSource;

public class SimpleIdentifierSourceImpl implements SimpleIdentifierSource {
   private final BasicAttribute attribute;
   private final Map attributeOverrideMap;

   public SimpleIdentifierSourceImpl(BasicAttribute attribute, Map attributeOverrideMap) {
      if (!attribute.isId()) {
         throw new AssertionFailure(String.format("A non id attribute was passed to SimpleIdentifierSourceImpl: %s", attribute.toString()));
      } else {
         this.attribute = attribute;
         this.attributeOverrideMap = attributeOverrideMap;
      }
   }

   public IdentifierSource.Nature getNature() {
      return IdentifierSource.Nature.SIMPLE;
   }

   public SingularAttributeSource getIdentifierAttributeSource() {
      return new SingularAttributeSourceImpl(this.attribute);
   }

   public IdGenerator getIdentifierGeneratorDescriptor() {
      return this.attribute.getIdGenerator();
   }
}
