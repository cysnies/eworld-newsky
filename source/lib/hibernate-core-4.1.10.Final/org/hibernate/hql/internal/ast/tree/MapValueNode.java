package org.hibernate.hql.internal.ast.tree;

import org.hibernate.persister.collection.QueryableCollection;
import org.hibernate.type.Type;

public class MapValueNode extends AbstractMapComponentNode {
   protected String expressionDescription() {
      return "value(*)";
   }

   protected String[] resolveColumns(QueryableCollection collectionPersister) {
      FromElement fromElement = this.getFromElement();
      return fromElement.toColumns(fromElement.getCollectionTableAlias(), "elements", this.getWalker().isInSelect());
   }

   protected Type resolveType(QueryableCollection collectionPersister) {
      return collectionPersister.getElementType();
   }
}
