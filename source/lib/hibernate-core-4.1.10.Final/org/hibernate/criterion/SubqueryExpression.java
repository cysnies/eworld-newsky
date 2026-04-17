package org.hibernate.criterion;

import org.hibernate.Criteria;
import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.TypedValue;
import org.hibernate.internal.CriteriaImpl;
import org.hibernate.loader.criteria.CriteriaJoinWalker;
import org.hibernate.loader.criteria.CriteriaQueryTranslator;
import org.hibernate.persister.entity.OuterJoinLoadable;
import org.hibernate.type.Type;

public abstract class SubqueryExpression implements Criterion {
   private CriteriaImpl criteriaImpl;
   private String quantifier;
   private String op;
   private QueryParameters params;
   private Type[] types;
   private CriteriaQueryTranslator innerQuery;

   protected Type[] getTypes() {
      return this.types;
   }

   protected SubqueryExpression(String op, String quantifier, DetachedCriteria dc) {
      this.criteriaImpl = dc.getCriteriaImpl();
      this.quantifier = quantifier;
      this.op = op;
   }

   protected abstract String toLeftSqlString(Criteria var1, CriteriaQuery var2);

   public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
      SessionFactoryImplementor factory = criteriaQuery.getFactory();
      OuterJoinLoadable persister = (OuterJoinLoadable)factory.getEntityPersister(this.criteriaImpl.getEntityOrClassName());
      this.createAndSetInnerQuery(criteriaQuery, factory);
      this.criteriaImpl.setSession(this.deriveRootSession(criteria));
      CriteriaJoinWalker walker = new CriteriaJoinWalker(persister, this.innerQuery, factory, this.criteriaImpl, this.criteriaImpl.getEntityOrClassName(), this.criteriaImpl.getSession().getLoadQueryInfluencers(), this.innerQuery.getRootSQLALias());
      String sql = walker.getSQLString();
      StringBuilder buf = new StringBuilder(this.toLeftSqlString(criteria, criteriaQuery));
      if (this.op != null) {
         buf.append(' ').append(this.op).append(' ');
      }

      if (this.quantifier != null) {
         buf.append(this.quantifier).append(' ');
      }

      return buf.append('(').append(sql).append(')').toString();
   }

   private SessionImplementor deriveRootSession(Criteria criteria) {
      if (criteria instanceof CriteriaImpl) {
         return ((CriteriaImpl)criteria).getSession();
      } else {
         return criteria instanceof CriteriaImpl.Subcriteria ? this.deriveRootSession(((CriteriaImpl.Subcriteria)criteria).getParent()) : null;
      }
   }

   public TypedValue[] getTypedValues(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
      SessionFactoryImplementor factory = criteriaQuery.getFactory();
      this.createAndSetInnerQuery(criteriaQuery, factory);
      Type[] ppTypes = this.params.getPositionalParameterTypes();
      Object[] ppValues = this.params.getPositionalParameterValues();
      TypedValue[] tv = new TypedValue[ppTypes.length];

      for(int i = 0; i < ppTypes.length; ++i) {
         tv[i] = new TypedValue(ppTypes[i], ppValues[i], EntityMode.POJO);
      }

      return tv;
   }

   private void createAndSetInnerQuery(CriteriaQuery criteriaQuery, SessionFactoryImplementor factory) {
      if (this.innerQuery == null) {
         String alias;
         if (this.criteriaImpl.getAlias() == null) {
            alias = criteriaQuery.generateSQLAlias();
         } else {
            alias = this.criteriaImpl.getAlias() + "_";
         }

         this.innerQuery = new CriteriaQueryTranslator(factory, this.criteriaImpl, this.criteriaImpl.getEntityOrClassName(), alias, criteriaQuery);
         this.params = this.innerQuery.getQueryParameters();
         this.types = this.innerQuery.getProjectedTypes();
      }

   }
}
