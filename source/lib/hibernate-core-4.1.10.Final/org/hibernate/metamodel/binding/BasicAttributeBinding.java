package org.hibernate.metamodel.binding;

import java.util.Properties;
import org.hibernate.MappingException;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.factory.IdentifierGeneratorFactory;
import org.hibernate.mapping.PropertyGeneration;
import org.hibernate.metamodel.domain.SingularAttribute;
import org.hibernate.metamodel.relational.Column;
import org.hibernate.metamodel.relational.Schema;
import org.hibernate.metamodel.relational.SimpleValue;
import org.hibernate.metamodel.source.MetaAttributeContext;

public class BasicAttributeBinding extends AbstractSingularAttributeBinding implements KeyValueBinding {
   private String unsavedValue;
   private PropertyGeneration generation;
   private boolean includedInOptimisticLocking;
   private boolean forceNonNullable;
   private boolean forceUnique;
   private boolean keyCascadeDeleteEnabled;
   private MetaAttributeContext metaAttributeContext;

   BasicAttributeBinding(AttributeBindingContainer container, SingularAttribute attribute, boolean forceNonNullable, boolean forceUnique) {
      super(container, attribute);
      this.forceNonNullable = forceNonNullable;
      this.forceUnique = forceUnique;
   }

   public boolean isAssociation() {
      return false;
   }

   public String getUnsavedValue() {
      return this.unsavedValue;
   }

   public void setUnsavedValue(String unsavedValue) {
      this.unsavedValue = unsavedValue;
   }

   public PropertyGeneration getGeneration() {
      return this.generation;
   }

   public void setGeneration(PropertyGeneration generation) {
      this.generation = generation;
   }

   public boolean isIncludedInOptimisticLocking() {
      return this.includedInOptimisticLocking;
   }

   public void setIncludedInOptimisticLocking(boolean includedInOptimisticLocking) {
      this.includedInOptimisticLocking = includedInOptimisticLocking;
   }

   public boolean isKeyCascadeDeleteEnabled() {
      return this.keyCascadeDeleteEnabled;
   }

   public void setKeyCascadeDeleteEnabled(boolean keyCascadeDeleteEnabled) {
      this.keyCascadeDeleteEnabled = keyCascadeDeleteEnabled;
   }

   public boolean forceNonNullable() {
      return this.forceNonNullable;
   }

   public boolean forceUnique() {
      return this.forceUnique;
   }

   public MetaAttributeContext getMetaAttributeContext() {
      return this.metaAttributeContext;
   }

   public void setMetaAttributeContext(MetaAttributeContext metaAttributeContext) {
      this.metaAttributeContext = metaAttributeContext;
   }

   IdentifierGenerator createIdentifierGenerator(IdGenerator idGenerator, IdentifierGeneratorFactory identifierGeneratorFactory, Properties properties) {
      Properties params = new Properties();
      params.putAll(properties);
      Schema schema = this.getValue().getTable().getSchema();
      if (schema != null) {
         if (schema.getName().getSchema() != null) {
            params.setProperty("schema", schema.getName().getSchema().getName());
         }

         if (schema.getName().getCatalog() != null) {
            params.setProperty("catalog", schema.getName().getCatalog().getName());
         }
      }

      params.setProperty("entity_name", this.getContainer().seekEntityBinding().getEntity().getName());
      String tableName = this.getValue().getTable().getQualifiedName(identifierGeneratorFactory.getDialect());
      params.setProperty("target_table", tableName);
      if (this.getSimpleValueSpan() > 1) {
         throw new MappingException("A SimpleAttributeBinding used for an identifier has more than 1 Value: " + this.getAttribute().getName());
      } else {
         SimpleValue simpleValue = (SimpleValue)this.getValue();
         if (!Column.class.isInstance(simpleValue)) {
            throw new MappingException("Cannot create an IdentifierGenerator because the value is not a column: " + simpleValue.toLoggableString());
         } else {
            params.setProperty("target_column", ((Column)simpleValue).getColumnName().encloseInQuotesIfQuoted(identifierGeneratorFactory.getDialect()));
            params.setProperty("identity_tables", tableName);
            params.putAll(idGenerator.getParameters());
            return identifierGeneratorFactory.createIdentifierGenerator(idGenerator.getStrategy(), this.getHibernateTypeDescriptor().getResolvedTypeMapping(), params);
         }
      }
   }
}
