package org.hibernate.loader.collection;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.LoadQueryInfluencers;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.persister.collection.QueryableCollection;
import org.hibernate.type.Type;

public class SubselectOneToManyLoader extends OneToManyLoader {
   private final Serializable[] keys;
   private final Type[] types;
   private final Object[] values;
   private final Map namedParameters;
   private final Map namedParameterLocMap;

   public SubselectOneToManyLoader(QueryableCollection persister, String subquery, Collection entityKeys, QueryParameters queryParameters, Map namedParameterLocMap, SessionFactoryImplementor factory, LoadQueryInfluencers loadQueryInfluencers) throws MappingException {
      super(persister, 1, subquery, factory, loadQueryInfluencers);
      this.keys = new Serializable[entityKeys.size()];
      Iterator iter = entityKeys.iterator();

      for(int i = 0; iter.hasNext(); this.keys[i++] = ((EntityKey)iter.next()).getIdentifier()) {
      }

      this.namedParameters = queryParameters.getNamedParameters();
      this.types = queryParameters.getFilteredPositionalParameterTypes();
      this.values = queryParameters.getFilteredPositionalParameterValues();
      this.namedParameterLocMap = namedParameterLocMap;
   }

   public void initialize(Serializable id, SessionImplementor session) throws HibernateException {
      this.loadCollectionSubselect(session, this.keys, this.values, this.types, this.namedParameters, this.getKeyType());
   }

   public int[] getNamedParameterLocs(String name) {
      return (int[])this.namedParameterLocMap.get(name);
   }
}
