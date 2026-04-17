package org.hibernate.loader.criteria;

import java.io.Serializable;
import org.hibernate.hql.internal.ast.util.SessionFactoryHelper;
import org.hibernate.persister.collection.QueryableCollection;
import org.hibernate.persister.entity.PropertyMapping;
import org.hibernate.type.Type;

class ScalarCollectionCriteriaInfoProvider implements CriteriaInfoProvider {
   String role;
   QueryableCollection persister;
   SessionFactoryHelper helper;

   ScalarCollectionCriteriaInfoProvider(SessionFactoryHelper helper, String role) {
      this.role = role;
      this.helper = helper;
      this.persister = helper.requireQueryableCollection(role);
   }

   public String getName() {
      return this.role;
   }

   public Serializable[] getSpaces() {
      return this.persister.getCollectionSpaces();
   }

   public PropertyMapping getPropertyMapping() {
      return this.helper.getCollectionPropertyMapping(this.role);
   }

   public Type getType(String relativePath) {
      return this.getPropertyMapping().toType(relativePath);
   }
}
