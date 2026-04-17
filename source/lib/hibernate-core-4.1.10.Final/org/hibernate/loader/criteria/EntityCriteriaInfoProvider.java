package org.hibernate.loader.criteria;

import java.io.Serializable;
import org.hibernate.persister.entity.PropertyMapping;
import org.hibernate.persister.entity.Queryable;
import org.hibernate.type.Type;

class EntityCriteriaInfoProvider implements CriteriaInfoProvider {
   Queryable persister;

   EntityCriteriaInfoProvider(Queryable persister) {
      this.persister = persister;
   }

   public String getName() {
      return this.persister.getEntityName();
   }

   public Serializable[] getSpaces() {
      return this.persister.getQuerySpaces();
   }

   public PropertyMapping getPropertyMapping() {
      return this.persister;
   }

   public Type getType(String relativePath) {
      return this.persister.toType(relativePath);
   }
}
