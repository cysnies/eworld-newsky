package org.hibernate.persister.collection;

import org.hibernate.QueryException;
import org.hibernate.persister.entity.PropertyMapping;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;

public class CollectionPropertyMapping implements PropertyMapping {
   private final QueryableCollection memberPersister;

   public CollectionPropertyMapping(QueryableCollection memberPersister) {
      this.memberPersister = memberPersister;
   }

   public Type toType(String propertyName) throws QueryException {
      if (propertyName.equals("elements")) {
         return this.memberPersister.getElementType();
      } else if (propertyName.equals("indices")) {
         if (!this.memberPersister.hasIndex()) {
            throw new QueryException("unindexed collection before indices()");
         } else {
            return this.memberPersister.getIndexType();
         }
      } else if (propertyName.equals("size")) {
         return StandardBasicTypes.INTEGER;
      } else if (propertyName.equals("maxIndex")) {
         return this.memberPersister.getIndexType();
      } else if (propertyName.equals("minIndex")) {
         return this.memberPersister.getIndexType();
      } else if (propertyName.equals("maxElement")) {
         return this.memberPersister.getElementType();
      } else if (propertyName.equals("minElement")) {
         return this.memberPersister.getElementType();
      } else {
         throw new QueryException("illegal syntax near collection: " + propertyName);
      }
   }

   public String[] toColumns(String alias, String propertyName) throws QueryException {
      if (propertyName.equals("elements")) {
         return this.memberPersister.getElementColumnNames(alias);
      } else if (propertyName.equals("indices")) {
         if (!this.memberPersister.hasIndex()) {
            throw new QueryException("unindexed collection in indices()");
         } else {
            return this.memberPersister.getIndexColumnNames(alias);
         }
      } else if (propertyName.equals("size")) {
         String[] cols = this.memberPersister.getKeyColumnNames();
         return new String[]{"count(" + alias + '.' + cols[0] + ')'};
      } else if (propertyName.equals("maxIndex")) {
         if (!this.memberPersister.hasIndex()) {
            throw new QueryException("unindexed collection in maxIndex()");
         } else {
            String[] cols = this.memberPersister.getIndexColumnNames(alias);
            if (cols.length != 1) {
               throw new QueryException("composite collection index in maxIndex()");
            } else {
               return new String[]{"max(" + cols[0] + ')'};
            }
         }
      } else if (propertyName.equals("minIndex")) {
         if (!this.memberPersister.hasIndex()) {
            throw new QueryException("unindexed collection in minIndex()");
         } else {
            String[] cols = this.memberPersister.getIndexColumnNames(alias);
            if (cols.length != 1) {
               throw new QueryException("composite collection index in minIndex()");
            } else {
               return new String[]{"min(" + cols[0] + ')'};
            }
         }
      } else if (propertyName.equals("maxElement")) {
         String[] cols = this.memberPersister.getElementColumnNames(alias);
         if (cols.length != 1) {
            throw new QueryException("composite collection element in maxElement()");
         } else {
            return new String[]{"max(" + cols[0] + ')'};
         }
      } else if (propertyName.equals("minElement")) {
         String[] cols = this.memberPersister.getElementColumnNames(alias);
         if (cols.length != 1) {
            throw new QueryException("composite collection element in minElement()");
         } else {
            return new String[]{"min(" + cols[0] + ')'};
         }
      } else {
         throw new QueryException("illegal syntax near collection: " + propertyName);
      }
   }

   public String[] toColumns(String propertyName) throws QueryException, UnsupportedOperationException {
      throw new UnsupportedOperationException("References to collections must be define a SQL alias");
   }

   public Type getType() {
      return this.memberPersister.getCollectionType();
   }
}
