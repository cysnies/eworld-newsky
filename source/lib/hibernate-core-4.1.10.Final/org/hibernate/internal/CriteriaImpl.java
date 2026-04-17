package org.hibernate.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.NaturalIdentifier;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.sql.JoinType;
import org.hibernate.transform.ResultTransformer;

public class CriteriaImpl implements Criteria, Serializable {
   private final String entityOrClassName;
   private transient SessionImplementor session;
   private final String rootAlias;
   private List criterionEntries;
   private List orderEntries;
   private Projection projection;
   private Criteria projectionCriteria;
   private List subcriteriaList;
   private Map fetchModes;
   private Map lockModes;
   private Integer maxResults;
   private Integer firstResult;
   private Integer timeout;
   private Integer fetchSize;
   private boolean cacheable;
   private String cacheRegion;
   private String comment;
   private FlushMode flushMode;
   private CacheMode cacheMode;
   private FlushMode sessionFlushMode;
   private CacheMode sessionCacheMode;
   private Boolean readOnly;
   private ResultTransformer resultTransformer;

   public CriteriaImpl(String entityOrClassName, SessionImplementor session) {
      this(entityOrClassName, "this", session);
   }

   public CriteriaImpl(String entityOrClassName, String alias, SessionImplementor session) {
      this.criterionEntries = new ArrayList();
      this.orderEntries = new ArrayList();
      this.subcriteriaList = new ArrayList();
      this.fetchModes = new HashMap();
      this.lockModes = new HashMap();
      this.resultTransformer = Criteria.ROOT_ENTITY;
      this.session = session;
      this.entityOrClassName = entityOrClassName;
      this.cacheable = false;
      this.rootAlias = alias;
   }

   public String toString() {
      return "CriteriaImpl(" + this.entityOrClassName + ":" + (this.rootAlias == null ? "" : this.rootAlias) + this.subcriteriaList.toString() + this.criterionEntries.toString() + (this.projection == null ? "" : this.projection.toString()) + ')';
   }

   public SessionImplementor getSession() {
      return this.session;
   }

   public void setSession(SessionImplementor session) {
      this.session = session;
   }

   public String getEntityOrClassName() {
      return this.entityOrClassName;
   }

   public Map getLockModes() {
      return this.lockModes;
   }

   public Criteria getProjectionCriteria() {
      return this.projectionCriteria;
   }

   public Iterator iterateSubcriteria() {
      return this.subcriteriaList.iterator();
   }

   public Iterator iterateExpressionEntries() {
      return this.criterionEntries.iterator();
   }

   public Iterator iterateOrderings() {
      return this.orderEntries.iterator();
   }

   public Criteria add(Criteria criteriaInst, Criterion expression) {
      this.criterionEntries.add(new CriterionEntry(expression, criteriaInst));
      return this;
   }

   public String getAlias() {
      return this.rootAlias;
   }

   public Projection getProjection() {
      return this.projection;
   }

   public Criteria setProjection(Projection projection) {
      this.projection = projection;
      this.projectionCriteria = this;
      this.setResultTransformer(PROJECTION);
      return this;
   }

   public Criteria add(Criterion expression) {
      this.add(this, expression);
      return this;
   }

   public Criteria addOrder(Order ordering) {
      this.orderEntries.add(new OrderEntry(ordering, this));
      return this;
   }

   public FetchMode getFetchMode(String path) {
      return (FetchMode)this.fetchModes.get(path);
   }

   public Criteria setFetchMode(String associationPath, FetchMode mode) {
      this.fetchModes.put(associationPath, mode);
      return this;
   }

   public Criteria setLockMode(LockMode lockMode) {
      return this.setLockMode(this.getAlias(), lockMode);
   }

   public Criteria setLockMode(String alias, LockMode lockMode) {
      this.lockModes.put(alias, lockMode);
      return this;
   }

   public Criteria createAlias(String associationPath, String alias) {
      return this.createAlias(associationPath, alias, JoinType.INNER_JOIN);
   }

   public Criteria createAlias(String associationPath, String alias, JoinType joinType) {
      new Subcriteria(this, associationPath, alias, joinType);
      return this;
   }

   public Criteria createAlias(String associationPath, String alias, int joinType) throws HibernateException {
      return this.createAlias(associationPath, alias, JoinType.parse(joinType));
   }

   public Criteria createAlias(String associationPath, String alias, JoinType joinType, Criterion withClause) {
      new Subcriteria(this, associationPath, alias, joinType, withClause);
      return this;
   }

   public Criteria createAlias(String associationPath, String alias, int joinType, Criterion withClause) throws HibernateException {
      return this.createAlias(associationPath, alias, JoinType.parse(joinType), withClause);
   }

   public Criteria createCriteria(String associationPath) {
      return this.createCriteria(associationPath, JoinType.INNER_JOIN);
   }

   public Criteria createCriteria(String associationPath, JoinType joinType) {
      return new Subcriteria(this, associationPath, joinType);
   }

   public Criteria createCriteria(String associationPath, int joinType) throws HibernateException {
      return this.createCriteria(associationPath, JoinType.parse(joinType));
   }

   public Criteria createCriteria(String associationPath, String alias) {
      return this.createCriteria(associationPath, alias, JoinType.INNER_JOIN);
   }

   public Criteria createCriteria(String associationPath, String alias, JoinType joinType) {
      return new Subcriteria(this, associationPath, alias, joinType);
   }

   public Criteria createCriteria(String associationPath, String alias, int joinType) throws HibernateException {
      return this.createCriteria(associationPath, alias, JoinType.parse(joinType));
   }

   public Criteria createCriteria(String associationPath, String alias, JoinType joinType, Criterion withClause) {
      return new Subcriteria(this, associationPath, alias, joinType, withClause);
   }

   public Criteria createCriteria(String associationPath, String alias, int joinType, Criterion withClause) throws HibernateException {
      return this.createCriteria(associationPath, alias, JoinType.parse(joinType), withClause);
   }

   public ResultTransformer getResultTransformer() {
      return this.resultTransformer;
   }

   public Criteria setResultTransformer(ResultTransformer tupleMapper) {
      this.resultTransformer = tupleMapper;
      return this;
   }

   public Integer getMaxResults() {
      return this.maxResults;
   }

   public Criteria setMaxResults(int maxResults) {
      this.maxResults = maxResults;
      return this;
   }

   public Integer getFirstResult() {
      return this.firstResult;
   }

   public Criteria setFirstResult(int firstResult) {
      this.firstResult = firstResult;
      return this;
   }

   public Integer getFetchSize() {
      return this.fetchSize;
   }

   public Criteria setFetchSize(int fetchSize) {
      this.fetchSize = fetchSize;
      return this;
   }

   public Integer getTimeout() {
      return this.timeout;
   }

   public Criteria setTimeout(int timeout) {
      this.timeout = timeout;
      return this;
   }

   public boolean isReadOnlyInitialized() {
      return this.readOnly != null;
   }

   public boolean isReadOnly() {
      if (!this.isReadOnlyInitialized() && this.getSession() == null) {
         throw new IllegalStateException("cannot determine readOnly/modifiable setting when it is not initialized and is not initialized and getSession() == null");
      } else {
         return this.isReadOnlyInitialized() ? this.readOnly : this.getSession().getPersistenceContext().isDefaultReadOnly();
      }
   }

   public Criteria setReadOnly(boolean readOnly) {
      this.readOnly = readOnly;
      return this;
   }

   public boolean getCacheable() {
      return this.cacheable;
   }

   public Criteria setCacheable(boolean cacheable) {
      this.cacheable = cacheable;
      return this;
   }

   public String getCacheRegion() {
      return this.cacheRegion;
   }

   public Criteria setCacheRegion(String cacheRegion) {
      this.cacheRegion = cacheRegion.trim();
      return this;
   }

   public String getComment() {
      return this.comment;
   }

   public Criteria setComment(String comment) {
      this.comment = comment;
      return this;
   }

   public Criteria setFlushMode(FlushMode flushMode) {
      this.flushMode = flushMode;
      return this;
   }

   public Criteria setCacheMode(CacheMode cacheMode) {
      this.cacheMode = cacheMode;
      return this;
   }

   public List list() throws HibernateException {
      this.before();

      List var1;
      try {
         var1 = this.session.list(this);
      } finally {
         this.after();
      }

      return var1;
   }

   public ScrollableResults scroll() {
      return this.scroll(ScrollMode.SCROLL_INSENSITIVE);
   }

   public ScrollableResults scroll(ScrollMode scrollMode) {
      this.before();

      ScrollableResults var2;
      try {
         var2 = this.session.scroll(this, scrollMode);
      } finally {
         this.after();
      }

      return var2;
   }

   public Object uniqueResult() throws HibernateException {
      return AbstractQueryImpl.uniqueElement(this.list());
   }

   protected void before() {
      if (this.flushMode != null) {
         this.sessionFlushMode = this.getSession().getFlushMode();
         this.getSession().setFlushMode(this.flushMode);
      }

      if (this.cacheMode != null) {
         this.sessionCacheMode = this.getSession().getCacheMode();
         this.getSession().setCacheMode(this.cacheMode);
      }

   }

   protected void after() {
      if (this.sessionFlushMode != null) {
         this.getSession().setFlushMode(this.sessionFlushMode);
         this.sessionFlushMode = null;
      }

      if (this.sessionCacheMode != null) {
         this.getSession().setCacheMode(this.sessionCacheMode);
         this.sessionCacheMode = null;
      }

   }

   public boolean isLookupByNaturalKey() {
      if (this.projection != null) {
         return false;
      } else if (this.subcriteriaList.size() > 0) {
         return false;
      } else if (this.criterionEntries.size() != 1) {
         return false;
      } else {
         CriterionEntry ce = (CriterionEntry)this.criterionEntries.get(0);
         return ce.getCriterion() instanceof NaturalIdentifier;
      }
   }

   public final class Subcriteria implements Criteria, Serializable {
      private String alias;
      private String path;
      private Criteria parent;
      private LockMode lockMode;
      private JoinType joinType;
      private Criterion withClause;
      private boolean hasRestriction;

      private Subcriteria(Criteria parent, String path, String alias, JoinType joinType, Criterion withClause) {
         this.joinType = JoinType.INNER_JOIN;
         this.alias = alias;
         this.path = path;
         this.parent = parent;
         this.joinType = joinType;
         this.withClause = withClause;
         this.hasRestriction = withClause != null;
         CriteriaImpl.this.subcriteriaList.add(this);
      }

      private Subcriteria(Criteria parent, String path, String alias, JoinType joinType) {
         this(parent, path, alias, joinType, (Criterion)null);
      }

      private Subcriteria(Criteria parent, String path, JoinType joinType) {
         this(parent, path, (String)null, (JoinType)joinType);
      }

      public String toString() {
         return "Subcriteria(" + this.path + ":" + (this.alias == null ? "" : this.alias) + ')';
      }

      public String getAlias() {
         return this.alias;
      }

      public void setAlias(String alias) {
         this.alias = alias;
      }

      public String getPath() {
         return this.path;
      }

      public Criteria getParent() {
         return this.parent;
      }

      public LockMode getLockMode() {
         return this.lockMode;
      }

      public Criteria setLockMode(LockMode lockMode) {
         this.lockMode = lockMode;
         return this;
      }

      public JoinType getJoinType() {
         return this.joinType;
      }

      public Criterion getWithClause() {
         return this.withClause;
      }

      public boolean hasRestriction() {
         return this.hasRestriction;
      }

      public Criteria add(Criterion expression) {
         this.hasRestriction = true;
         CriteriaImpl.this.add(this, expression);
         return this;
      }

      public Criteria addOrder(Order order) {
         CriteriaImpl.this.orderEntries.add(new OrderEntry(order, this));
         return this;
      }

      public Criteria createAlias(String associationPath, String alias) {
         return this.createAlias(associationPath, alias, JoinType.INNER_JOIN);
      }

      public Criteria createAlias(String associationPath, String alias, JoinType joinType) throws HibernateException {
         CriteriaImpl.this.new Subcriteria(this, associationPath, alias, joinType);
         return this;
      }

      public Criteria createAlias(String associationPath, String alias, int joinType) throws HibernateException {
         return this.createAlias(associationPath, alias, JoinType.parse(joinType));
      }

      public Criteria createAlias(String associationPath, String alias, JoinType joinType, Criterion withClause) throws HibernateException {
         CriteriaImpl.this.new Subcriteria(this, associationPath, alias, joinType, withClause);
         return this;
      }

      public Criteria createAlias(String associationPath, String alias, int joinType, Criterion withClause) throws HibernateException {
         return this.createAlias(associationPath, alias, JoinType.parse(joinType), withClause);
      }

      public Criteria createCriteria(String associationPath) {
         return this.createCriteria(associationPath, JoinType.INNER_JOIN);
      }

      public Criteria createCriteria(String associationPath, JoinType joinType) throws HibernateException {
         return CriteriaImpl.this.new Subcriteria(this, associationPath, joinType);
      }

      public Criteria createCriteria(String associationPath, int joinType) throws HibernateException {
         return this.createCriteria(associationPath, JoinType.parse(joinType));
      }

      public Criteria createCriteria(String associationPath, String alias) {
         return this.createCriteria(associationPath, alias, JoinType.INNER_JOIN);
      }

      public Criteria createCriteria(String associationPath, String alias, JoinType joinType) throws HibernateException {
         return CriteriaImpl.this.new Subcriteria(this, associationPath, alias, joinType);
      }

      public Criteria createCriteria(String associationPath, String alias, int joinType) throws HibernateException {
         return this.createCriteria(associationPath, alias, JoinType.parse(joinType));
      }

      public Criteria createCriteria(String associationPath, String alias, JoinType joinType, Criterion withClause) throws HibernateException {
         return CriteriaImpl.this.new Subcriteria(this, associationPath, alias, joinType, withClause);
      }

      public Criteria createCriteria(String associationPath, String alias, int joinType, Criterion withClause) throws HibernateException {
         return this.createCriteria(associationPath, alias, JoinType.parse(joinType), withClause);
      }

      public boolean isReadOnly() {
         return CriteriaImpl.this.isReadOnly();
      }

      public boolean isReadOnlyInitialized() {
         return CriteriaImpl.this.isReadOnlyInitialized();
      }

      public Criteria setReadOnly(boolean readOnly) {
         CriteriaImpl.this.setReadOnly(readOnly);
         return this;
      }

      public Criteria setCacheable(boolean cacheable) {
         CriteriaImpl.this.setCacheable(cacheable);
         return this;
      }

      public Criteria setCacheRegion(String cacheRegion) {
         CriteriaImpl.this.setCacheRegion(cacheRegion);
         return this;
      }

      public List list() throws HibernateException {
         return CriteriaImpl.this.list();
      }

      public ScrollableResults scroll() throws HibernateException {
         return CriteriaImpl.this.scroll();
      }

      public ScrollableResults scroll(ScrollMode scrollMode) throws HibernateException {
         return CriteriaImpl.this.scroll(scrollMode);
      }

      public Object uniqueResult() throws HibernateException {
         return CriteriaImpl.this.uniqueResult();
      }

      public Criteria setFetchMode(String associationPath, FetchMode mode) {
         CriteriaImpl.this.setFetchMode(StringHelper.qualify(this.path, associationPath), mode);
         return this;
      }

      public Criteria setFlushMode(FlushMode flushMode) {
         CriteriaImpl.this.setFlushMode(flushMode);
         return this;
      }

      public Criteria setCacheMode(CacheMode cacheMode) {
         CriteriaImpl.this.setCacheMode(cacheMode);
         return this;
      }

      public Criteria setFirstResult(int firstResult) {
         CriteriaImpl.this.setFirstResult(firstResult);
         return this;
      }

      public Criteria setMaxResults(int maxResults) {
         CriteriaImpl.this.setMaxResults(maxResults);
         return this;
      }

      public Criteria setTimeout(int timeout) {
         CriteriaImpl.this.setTimeout(timeout);
         return this;
      }

      public Criteria setFetchSize(int fetchSize) {
         CriteriaImpl.this.setFetchSize(fetchSize);
         return this;
      }

      public Criteria setLockMode(String alias, LockMode lockMode) {
         CriteriaImpl.this.setLockMode(alias, lockMode);
         return this;
      }

      public Criteria setResultTransformer(ResultTransformer resultProcessor) {
         CriteriaImpl.this.setResultTransformer(resultProcessor);
         return this;
      }

      public Criteria setComment(String comment) {
         CriteriaImpl.this.setComment(comment);
         return this;
      }

      public Criteria setProjection(Projection projection) {
         CriteriaImpl.this.projection = projection;
         CriteriaImpl.this.projectionCriteria = this;
         this.setResultTransformer(PROJECTION);
         return this;
      }
   }

   public static final class CriterionEntry implements Serializable {
      private final Criterion criterion;
      private final Criteria criteria;

      private CriterionEntry(Criterion criterion, Criteria criteria) {
         this.criteria = criteria;
         this.criterion = criterion;
      }

      public Criterion getCriterion() {
         return this.criterion;
      }

      public Criteria getCriteria() {
         return this.criteria;
      }

      public String toString() {
         return this.criterion.toString();
      }
   }

   public static final class OrderEntry implements Serializable {
      private final Order order;
      private final Criteria criteria;

      private OrderEntry(Order order, Criteria criteria) {
         this.criteria = criteria;
         this.order = order;
      }

      public Order getOrder() {
         return this.order;
      }

      public Criteria getCriteria() {
         return this.criteria;
      }

      public String toString() {
         return this.order.toString();
      }
   }
}
