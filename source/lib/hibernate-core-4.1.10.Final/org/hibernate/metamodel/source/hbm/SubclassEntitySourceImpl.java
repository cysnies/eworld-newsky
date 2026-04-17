package org.hibernate.metamodel.source.hbm;

import org.hibernate.internal.jaxb.mapping.hbm.EntityElement;
import org.hibernate.internal.jaxb.mapping.hbm.JaxbJoinedSubclassElement;
import org.hibernate.internal.jaxb.mapping.hbm.JaxbSubclassElement;
import org.hibernate.internal.jaxb.mapping.hbm.JaxbUnionSubclassElement;
import org.hibernate.metamodel.source.binder.SubclassEntitySource;
import org.hibernate.metamodel.source.binder.TableSource;

public class SubclassEntitySourceImpl extends AbstractEntitySourceImpl implements SubclassEntitySource {
   protected SubclassEntitySourceImpl(MappingDocument sourceMappingDocument, EntityElement entityElement) {
      super(sourceMappingDocument, entityElement);
   }

   public TableSource getPrimaryTable() {
      if (JaxbJoinedSubclassElement.class.isInstance(this.entityElement())) {
         return new TableSource() {
            public String getExplicitSchemaName() {
               return ((JaxbJoinedSubclassElement)SubclassEntitySourceImpl.this.entityElement()).getSchema();
            }

            public String getExplicitCatalogName() {
               return ((JaxbJoinedSubclassElement)SubclassEntitySourceImpl.this.entityElement()).getCatalog();
            }

            public String getExplicitTableName() {
               return ((JaxbJoinedSubclassElement)SubclassEntitySourceImpl.this.entityElement()).getTable();
            }

            public String getLogicalName() {
               return null;
            }
         };
      } else {
         return JaxbUnionSubclassElement.class.isInstance(this.entityElement()) ? new TableSource() {
            public String getExplicitSchemaName() {
               return ((JaxbUnionSubclassElement)SubclassEntitySourceImpl.this.entityElement()).getSchema();
            }

            public String getExplicitCatalogName() {
               return ((JaxbUnionSubclassElement)SubclassEntitySourceImpl.this.entityElement()).getCatalog();
            }

            public String getExplicitTableName() {
               return ((JaxbUnionSubclassElement)SubclassEntitySourceImpl.this.entityElement()).getTable();
            }

            public String getLogicalName() {
               return null;
            }
         } : null;
      }
   }

   public String getDiscriminatorMatchValue() {
      return JaxbSubclassElement.class.isInstance(this.entityElement()) ? ((JaxbSubclassElement)this.entityElement()).getDiscriminatorValue() : null;
   }
}
