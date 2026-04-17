package org.hibernate.metamodel.binding;

import java.util.Properties;
import org.hibernate.AssertionFailure;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.factory.IdentifierGeneratorFactory;

public class EntityIdentifier {
   private final EntityBinding entityBinding;
   private BasicAttributeBinding attributeBinding;
   private IdentifierGenerator identifierGenerator;
   private IdGenerator idGenerator;
   private boolean isIdentifierMapper = false;

   public EntityIdentifier(EntityBinding entityBinding) {
      this.entityBinding = entityBinding;
   }

   public BasicAttributeBinding getValueBinding() {
      return this.attributeBinding;
   }

   public void setValueBinding(BasicAttributeBinding attributeBinding) {
      if (this.attributeBinding != null) {
         throw new AssertionFailure(String.format("Identifier value binding already existed for %s", this.entityBinding.getEntity().getName()));
      } else {
         this.attributeBinding = attributeBinding;
      }
   }

   public void setIdGenerator(IdGenerator idGenerator) {
      this.idGenerator = idGenerator;
   }

   public boolean isEmbedded() {
      return this.attributeBinding.getSimpleValueSpan() > 1;
   }

   public boolean isIdentifierMapper() {
      return this.isIdentifierMapper;
   }

   public IdentifierGenerator createIdentifierGenerator(IdentifierGeneratorFactory factory, Properties properties) {
      if (this.idGenerator != null) {
         this.identifierGenerator = this.attributeBinding.createIdentifierGenerator(this.idGenerator, factory, properties);
      }

      return this.identifierGenerator;
   }

   public IdentifierGenerator getIdentifierGenerator() {
      return this.identifierGenerator;
   }
}
