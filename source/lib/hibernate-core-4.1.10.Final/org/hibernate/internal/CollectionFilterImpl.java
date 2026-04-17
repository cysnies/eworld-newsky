package org.hibernate.internal;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.hibernate.HibernateException;
import org.hibernate.ScrollableResults;
import org.hibernate.engine.query.spi.ParameterMetadata;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.Type;

public class CollectionFilterImpl extends QueryImpl {
   private Object collection;

   public CollectionFilterImpl(String queryString, Object collection, SessionImplementor session, ParameterMetadata parameterMetadata) {
      super(queryString, session, parameterMetadata);
      this.collection = collection;
   }

   public Iterator iterate() throws HibernateException {
      this.verifyParameters();
      Map namedParams = this.getNamedParams();
      return this.getSession().iterateFilter(this.collection, this.expandParameterLists(namedParams), this.getQueryParameters(namedParams));
   }

   public List list() throws HibernateException {
      this.verifyParameters();
      Map namedParams = this.getNamedParams();
      return this.getSession().listFilter(this.collection, this.expandParameterLists(namedParams), this.getQueryParameters(namedParams));
   }

   public ScrollableResults scroll() throws HibernateException {
      throw new UnsupportedOperationException("Can't scroll filters");
   }

   public Type[] typeArray() {
      List typeList = this.getTypes();
      int size = typeList.size();
      Type[] result = new Type[size + 1];

      for(int i = 0; i < size; ++i) {
         result[i + 1] = (Type)typeList.get(i);
      }

      return result;
   }

   public Object[] valueArray() {
      List valueList = this.getValues();
      int size = valueList.size();
      Object[] result = new Object[size + 1];

      for(int i = 0; i < size; ++i) {
         result[i + 1] = valueList.get(i);
      }

      return result;
   }
}
