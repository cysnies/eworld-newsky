package org.hibernate.hql.internal.ast.tree;

import org.hibernate.persister.collection.QueryableCollection;
import org.hibernate.type.Type;

public class MapKeyNode extends AbstractMapComponentNode {
   protected String expressionDescription() {
      return "key(*)";
   }

   protected String[] resolveColumns(QueryableCollection collectionPersister) {
      FromElement fromElement = this.getFromElement();
      return fromElement.toColumns(fromElement.getCollectionTableAlias(), "index", this.getWalker().isInSelect());
   }

   protected Type resolveType(QueryableCollection collectionPersister) {
      return collectionPersister.getIndexType();
   }
}
