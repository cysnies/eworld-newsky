package org.hibernate.loader.criteria;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import org.hibernate.Criteria;
import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.MappingException;
import org.hibernate.QueryException;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.EnhancedProjection;
import org.hibernate.criterion.Projection;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.RowSelection;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.TypedValue;
import org.hibernate.hql.internal.ast.util.SessionFactoryHelper;
import org.hibernate.internal.CriteriaImpl;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.persister.entity.Loadable;
import org.hibernate.persister.entity.PropertyMapping;
import org.hibernate.persister.entity.Queryable;
import org.hibernate.sql.JoinType;
import org.hibernate.type.AssociationType;
import org.hibernate.type.CollectionType;
import org.hibernate.type.StringRepresentableType;
import org.hibernate.type.Type;

public class CriteriaQueryTranslator implements CriteriaQuery {
   public static final String ROOT_SQL_ALIAS = "this_";
   private CriteriaQuery outerQueryTranslator;
   private final CriteriaImpl rootCriteria;
   private final String rootEntityName;
   private final String rootSQLAlias;
   private int aliasCount;
   private final Map criteriaInfoMap;
   private final Map nameCriteriaInfoMap;
   private final Map criteriaSQLAliasMap;
   private final Map aliasCriteriaMap;
   private final Map associationPathCriteriaMap;
   private final Map associationPathJoinTypesMap;
   private final Map withClauseMap;
   private final SessionFactoryImplementor sessionFactory;
   private final SessionFactoryHelper helper;

   public CriteriaQueryTranslator(SessionFactoryImplementor factory, CriteriaImpl criteria, String rootEntityName, String rootSQLAlias, CriteriaQuery outerQuery) throws HibernateException {
      this(factory, criteria, rootEntityName, rootSQLAlias);
      this.outerQueryTranslator = outerQuery;
   }

   public CriteriaQueryTranslator(SessionFactoryImplementor factory, CriteriaImpl criteria, String rootEntityName, String rootSQLAlias) throws HibernateException {
      this.aliasCount = 0;
      this.criteriaInfoMap = new LinkedHashMap();
      this.nameCriteriaInfoMap = new LinkedHashMap();
      this.criteriaSQLAliasMap = new HashMap();
      this.aliasCriteriaMap = new HashMap();
      this.associationPathCriteriaMap = new LinkedHashMap();
      this.associationPathJoinTypesMap = new LinkedHashMap();
      this.withClauseMap = new HashMap();
      this.rootCriteria = criteria;
      this.rootEntityName = rootEntityName;
      this.sessionFactory = factory;
      this.rootSQLAlias = rootSQLAlias;
      this.helper = new SessionFactoryHelper(factory);
      this.createAliasCriteriaMap();
      this.createAssociationPathCriteriaMap();
      this.createCriteriaEntityNameMap();
      this.createCriteriaSQLAliasMap();
   }

   public String generateSQLAlias() {
      return StringHelper.generateAlias("this", this.aliasCount) + '_';
   }

   public String getRootSQLALias() {
      return this.rootSQLAlias;
   }

   private Criteria getAliasedCriteria(String alias) {
      return (Criteria)this.aliasCriteriaMap.get(alias);
   }

   public boolean isJoin(String path) {
      return this.associationPathCriteriaMap.containsKey(path);
   }

   public JoinType getJoinType(String path) {
      JoinType result = (JoinType)this.associationPathJoinTypesMap.get(path);
      return result == null ? JoinType.INNER_JOIN : result;
   }

   public Criteria getCriteria(String path) {
      return (Criteria)this.associationPathCriteriaMap.get(path);
   }

   public Set getQuerySpaces() {
      Set result = new HashSet();

      for(CriteriaInfoProvider info : this.criteriaInfoMap.values()) {
         result.addAll(Arrays.asList(info.getSpaces()));
      }

      return result;
   }

   private void createAliasCriteriaMap() {
      this.aliasCriteriaMap.put(this.rootCriteria.getAlias(), this.rootCriteria);
      Iterator iter = this.rootCriteria.iterateSubcriteria();

      while(iter.hasNext()) {
         Criteria subcriteria = (Criteria)iter.next();
         if (subcriteria.getAlias() != null) {
            Object old = this.aliasCriteriaMap.put(subcriteria.getAlias(), subcriteria);
            if (old != null) {
               throw new QueryException("duplicate alias: " + subcriteria.getAlias());
            }
         }
      }

   }

   private void createAssociationPathCriteriaMap() {
      Iterator iter = this.rootCriteria.iterateSubcriteria();

      while(iter.hasNext()) {
         CriteriaImpl.Subcriteria crit = (CriteriaImpl.Subcriteria)iter.next();
         String wholeAssociationPath = this.getWholeAssociationPath(crit);
         Object old = this.associationPathCriteriaMap.put(wholeAssociationPath, crit);
         if (old != null) {
            throw new QueryException("duplicate association path: " + wholeAssociationPath);
         }

         JoinType joinType = crit.getJoinType();
         old = this.associationPathJoinTypesMap.put(wholeAssociationPath, joinType);
         if (old != null) {
            throw new QueryException("duplicate association path: " + wholeAssociationPath);
         }

         if (crit.getWithClause() != null) {
            this.withClauseMap.put(wholeAssociationPath, crit.getWithClause());
         }
      }

   }

   private String getWholeAssociationPath(CriteriaImpl.Subcriteria subcriteria) {
      String path = subcriteria.getPath();
      Criteria parent = null;
      if (path.indexOf(46) > 0) {
         String testAlias = StringHelper.root(path);
         if (!testAlias.equals(subcriteria.getAlias())) {
            parent = (Criteria)this.aliasCriteriaMap.get(testAlias);
         }
      }

      if (parent == null) {
         parent = subcriteria.getParent();
      } else {
         path = StringHelper.unroot(path);
      }

      return parent.equals(this.rootCriteria) ? path : this.getWholeAssociationPath((CriteriaImpl.Subcriteria)parent) + '.' + path;
   }

   private void createCriteriaEntityNameMap() {
      CriteriaInfoProvider rootProvider = new EntityCriteriaInfoProvider((Queryable)this.sessionFactory.getEntityPersister(this.rootEntityName));
      this.criteriaInfoMap.put(this.rootCriteria, rootProvider);
      this.nameCriteriaInfoMap.put(rootProvider.getName(), rootProvider);

      for(Map.Entry me : this.associationPathCriteriaMap.entrySet()) {
         CriteriaInfoProvider info = this.getPathInfo((String)me.getKey());
         this.criteriaInfoMap.put(me.getValue(), info);
         this.nameCriteriaInfoMap.put(info.getName(), info);
      }

   }

   private CriteriaInfoProvider getPathInfo(String path) {
      StringTokenizer tokens = new StringTokenizer(path, ".");
      String componentPath = "";
      CriteriaInfoProvider provider = (CriteriaInfoProvider)this.nameCriteriaInfoMap.get(this.rootEntityName);

      while(tokens.hasMoreTokens()) {
         componentPath = componentPath + tokens.nextToken();
         Type type = provider.getType(componentPath);
         if (type.isAssociationType()) {
            AssociationType atype = (AssociationType)type;
            CollectionType ctype = type.isCollectionType() ? (CollectionType)type : null;
            Type elementType = ctype != null ? ctype.getElementType(this.sessionFactory) : null;
            if (ctype != null && elementType.isComponentType()) {
               provider = new ComponentCollectionCriteriaInfoProvider(this.helper.getCollectionPersister(ctype.getRole()));
            } else if (ctype != null && !elementType.isEntityType()) {
               provider = new ScalarCollectionCriteriaInfoProvider(this.helper, ctype.getRole());
            } else {
               provider = new EntityCriteriaInfoProvider((Queryable)this.sessionFactory.getEntityPersister(atype.getAssociatedEntityName(this.sessionFactory)));
            }

            componentPath = "";
         } else {
            if (!type.isComponentType()) {
               throw new QueryException("not an association: " + componentPath);
            }

            if (!tokens.hasMoreTokens()) {
               throw new QueryException("Criteria objects cannot be created directly on components.  Create a criteria on owning entity and use a dotted property to access component property: " + path);
            }

            componentPath = componentPath + '.';
         }
      }

      return provider;
   }

   public int getSQLAliasCount() {
      return this.criteriaSQLAliasMap.size();
   }

   private void createCriteriaSQLAliasMap() {
      int i = 0;

      for(Map.Entry me : this.criteriaInfoMap.entrySet()) {
         Criteria crit = (Criteria)me.getKey();
         String alias = crit.getAlias();
         if (alias == null) {
            alias = ((CriteriaInfoProvider)me.getValue()).getName();
         }

         this.criteriaSQLAliasMap.put(crit, StringHelper.generateAlias(alias, i++));
      }

      this.criteriaSQLAliasMap.put(this.rootCriteria, this.rootSQLAlias);
   }

   public CriteriaImpl getRootCriteria() {
      return this.rootCriteria;
   }

   public QueryParameters getQueryParameters() {
      LockOptions lockOptions = new LockOptions();
      RowSelection selection = new RowSelection();
      selection.setFirstRow(this.rootCriteria.getFirstResult());
      selection.setMaxRows(this.rootCriteria.getMaxResults());
      selection.setTimeout(this.rootCriteria.getTimeout());
      selection.setFetchSize(this.rootCriteria.getFetchSize());

      for(Map.Entry me : this.rootCriteria.getLockModes().entrySet()) {
         Criteria subcriteria = this.getAliasedCriteria((String)me.getKey());
         lockOptions.setAliasSpecificLockMode(this.getSQLAlias(subcriteria), (LockMode)me.getValue());
      }

      List values = new ArrayList();
      List types = new ArrayList();
      Iterator var10 = this.rootCriteria.iterateSubcriteria();

      while(var10.hasNext()) {
         CriteriaImpl.Subcriteria subcriteria = (CriteriaImpl.Subcriteria)var10.next();
         LockMode lm = subcriteria.getLockMode();
         if (lm != null) {
            lockOptions.setAliasSpecificLockMode(this.getSQLAlias(subcriteria), lm);
         }

         if (subcriteria.getWithClause() != null) {
            TypedValue[] tv = subcriteria.getWithClause().getTypedValues(subcriteria, this);

            for(int i = 0; i < tv.length; ++i) {
               values.add(tv[i].getValue());
               types.add(tv[i].getType());
            }
         }
      }

      var10 = this.rootCriteria.iterateExpressionEntries();

      while(var10.hasNext()) {
         CriteriaImpl.CriterionEntry ce = (CriteriaImpl.CriterionEntry)var10.next();
         TypedValue[] tv = ce.getCriterion().getTypedValues(ce.getCriteria(), this);

         for(int i = 0; i < tv.length; ++i) {
            values.add(tv[i].getValue());
            types.add(tv[i].getType());
         }
      }

      Object[] valueArray = values.toArray();
      Type[] typeArray = ArrayHelper.toTypeArray(types);
      return new QueryParameters(typeArray, valueArray, lockOptions, selection, this.rootCriteria.isReadOnlyInitialized(), this.rootCriteria.isReadOnlyInitialized() ? this.rootCriteria.isReadOnly() : false, this.rootCriteria.getCacheable(), this.rootCriteria.getCacheRegion(), this.rootCriteria.getComment(), this.rootCriteria.isLookupByNaturalKey(), this.rootCriteria.getResultTransformer());
   }

   public boolean hasProjection() {
      return this.rootCriteria.getProjection() != null;
   }

   public String getGroupBy() {
      return this.rootCriteria.getProjection().isGrouped() ? this.rootCriteria.getProjection().toGroupSqlString(this.rootCriteria.getProjectionCriteria(), this) : "";
   }

   public String getSelect() {
      return this.rootCriteria.getProjection().toSqlString(this.rootCriteria.getProjectionCriteria(), 0, this);
   }

   Type getResultType(Criteria criteria) {
      return this.getFactory().getTypeResolver().getTypeFactory().manyToOne(this.getEntityName(criteria));
   }

   public Type[] getProjectedTypes() {
      return this.rootCriteria.getProjection().getTypes(this.rootCriteria, this);
   }

   public String[] getProjectedColumnAliases() {
      return this.rootCriteria.getProjection() instanceof EnhancedProjection ? ((EnhancedProjection)this.rootCriteria.getProjection()).getColumnAliases(0, this.rootCriteria, this) : this.rootCriteria.getProjection().getColumnAliases(0);
   }

   public String[] getProjectedAliases() {
      return this.rootCriteria.getProjection().getAliases();
   }

   public String getWhereCondition() {
      StringBuilder condition = new StringBuilder(30);
      Iterator criterionIterator = this.rootCriteria.iterateExpressionEntries();

      while(criterionIterator.hasNext()) {
         CriteriaImpl.CriterionEntry entry = (CriteriaImpl.CriterionEntry)criterionIterator.next();
         String sqlString = entry.getCriterion().toSqlString(entry.getCriteria(), this);
         condition.append(sqlString);
         if (criterionIterator.hasNext()) {
            condition.append(" and ");
         }
      }

      return condition.toString();
   }

   public String getOrderBy() {
      StringBuilder orderBy = new StringBuilder(30);
      Iterator criterionIterator = this.rootCriteria.iterateOrderings();

      while(criterionIterator.hasNext()) {
         CriteriaImpl.OrderEntry oe = (CriteriaImpl.OrderEntry)criterionIterator.next();
         orderBy.append(oe.getOrder().toSqlString(oe.getCriteria(), this));
         if (criterionIterator.hasNext()) {
            orderBy.append(", ");
         }
      }

      return orderBy.toString();
   }

   public SessionFactoryImplementor getFactory() {
      return this.sessionFactory;
   }

   public String getSQLAlias(Criteria criteria) {
      return (String)this.criteriaSQLAliasMap.get(criteria);
   }

   public String getEntityName(Criteria criteria) {
      CriteriaInfoProvider infoProvider = (CriteriaInfoProvider)this.criteriaInfoMap.get(criteria);
      return infoProvider != null ? infoProvider.getName() : null;
   }

   public String getColumn(Criteria criteria, String propertyName) {
      String[] cols = this.getColumns(propertyName, criteria);
      if (cols.length != 1) {
         throw new QueryException("property does not map to a single column: " + propertyName);
      } else {
         return cols[0];
      }
   }

   public String[] getColumnsUsingProjection(Criteria subcriteria, String propertyName) throws HibernateException {
      Projection projection = this.rootCriteria.getProjection();
      String[] projectionColumns = null;
      if (projection != null) {
         projectionColumns = projection instanceof EnhancedProjection ? ((EnhancedProjection)projection).getColumnAliases(propertyName, 0, this.rootCriteria, this) : projection.getColumnAliases(propertyName, 0);
      }

      if (projectionColumns == null) {
         try {
            return this.getColumns(propertyName, subcriteria);
         } catch (HibernateException he) {
            if (this.outerQueryTranslator != null) {
               return this.outerQueryTranslator.getColumnsUsingProjection(subcriteria, propertyName);
            } else {
               throw he;
            }
         }
      } else {
         return projectionColumns;
      }
   }

   public String[] getIdentifierColumns(Criteria subcriteria) {
      String[] idcols = ((Loadable)this.getPropertyMapping(this.getEntityName(subcriteria))).getIdentifierColumnNames();
      return StringHelper.qualify(this.getSQLAlias(subcriteria), idcols);
   }

   public Type getIdentifierType(Criteria subcriteria) {
      return ((Loadable)this.getPropertyMapping(this.getEntityName(subcriteria))).getIdentifierType();
   }

   public TypedValue getTypedIdentifierValue(Criteria subcriteria, Object value) {
      Loadable loadable = (Loadable)this.getPropertyMapping(this.getEntityName(subcriteria));
      return new TypedValue(loadable.getIdentifierType(), value, EntityMode.POJO);
   }

   public String[] getColumns(String propertyName, Criteria subcriteria) throws HibernateException {
      return this.getPropertyMapping(this.getEntityName(subcriteria, propertyName)).toColumns(this.getSQLAlias(subcriteria, propertyName), this.getPropertyName(propertyName));
   }

   public String[] findColumns(String propertyName, Criteria subcriteria) throws HibernateException {
      try {
         return this.getColumns(propertyName, subcriteria);
      } catch (HibernateException he) {
         if (this.outerQueryTranslator != null) {
            return this.outerQueryTranslator.findColumns(propertyName, subcriteria);
         } else {
            throw he;
         }
      }
   }

   public Type getTypeUsingProjection(Criteria subcriteria, String propertyName) throws HibernateException {
      Projection projection = this.rootCriteria.getProjection();
      Type[] projectionTypes = projection == null ? null : projection.getTypes(propertyName, subcriteria, this);
      if (projectionTypes == null) {
         try {
            return this.getType(subcriteria, propertyName);
         } catch (HibernateException he) {
            if (this.outerQueryTranslator != null) {
               return this.outerQueryTranslator.getType(subcriteria, propertyName);
            } else {
               throw he;
            }
         }
      } else if (projectionTypes.length != 1) {
         throw new QueryException("not a single-length projection: " + propertyName);
      } else {
         return projectionTypes[0];
      }
   }

   public Type getType(Criteria subcriteria, String propertyName) throws HibernateException {
      return this.getPropertyMapping(this.getEntityName(subcriteria, propertyName)).toType(this.getPropertyName(propertyName));
   }

   public TypedValue getTypedValue(Criteria subcriteria, String propertyName, Object value) throws HibernateException {
      if (value instanceof Class) {
         Class entityClass = (Class)value;
         Queryable q = SessionFactoryHelper.findQueryableUsingImports(this.sessionFactory, entityClass.getName());
         if (q != null) {
            Type type = q.getDiscriminatorType();
            String stringValue = q.getDiscriminatorSQLValue();
            if (stringValue != null && stringValue.length() > 2 && stringValue.startsWith("'") && stringValue.endsWith("'")) {
               stringValue = stringValue.substring(1, stringValue.length() - 1);
            }

            if (type instanceof StringRepresentableType) {
               StringRepresentableType nullableType = (StringRepresentableType)type;
               value = nullableType.fromStringValue(stringValue);
               return new TypedValue(type, value, EntityMode.POJO);
            }

            throw new QueryException("Unsupported discriminator type " + type);
         }
      }

      return new TypedValue(this.getTypeUsingProjection(subcriteria, propertyName), value, EntityMode.POJO);
   }

   private PropertyMapping getPropertyMapping(String entityName) throws MappingException {
      CriteriaInfoProvider info = (CriteriaInfoProvider)this.nameCriteriaInfoMap.get(entityName);
      if (info == null) {
         throw new HibernateException("Unknown entity: " + entityName);
      } else {
         return info.getPropertyMapping();
      }
   }

   public String getEntityName(Criteria subcriteria, String propertyName) {
      if (propertyName.indexOf(46) > 0) {
         String root = StringHelper.root(propertyName);
         Criteria crit = this.getAliasedCriteria(root);
         if (crit != null) {
            return this.getEntityName(crit);
         }
      }

      return this.getEntityName(subcriteria);
   }

   public String getSQLAlias(Criteria criteria, String propertyName) {
      if (propertyName.indexOf(46) > 0) {
         String root = StringHelper.root(propertyName);
         Criteria subcriteria = this.getAliasedCriteria(root);
         if (subcriteria != null) {
            return this.getSQLAlias(subcriteria);
         }
      }

      return this.getSQLAlias(criteria);
   }

   public String getPropertyName(String propertyName) {
      if (propertyName.indexOf(46) > 0) {
         String root = StringHelper.root(propertyName);
         Criteria crit = this.getAliasedCriteria(root);
         if (crit != null) {
            return propertyName.substring(root.length() + 1);
         }
      }

      return propertyName;
   }

   public String getWithClause(String path) {
      Criterion crit = (Criterion)this.withClauseMap.get(path);
      return crit == null ? null : crit.toSqlString(this.getCriteria(path), this);
   }

   public boolean hasRestriction(String path) {
      CriteriaImpl.Subcriteria crit = (CriteriaImpl.Subcriteria)this.getCriteria(path);
      return crit == null ? false : crit.hasRestriction();
   }
}
