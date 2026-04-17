package org.hibernate.hql.internal.ast.tree;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.hibernate.MappingException;
import org.hibernate.QueryException;
import org.hibernate.engine.internal.JoinSequence;
import org.hibernate.hql.internal.CollectionProperties;
import org.hibernate.hql.internal.CollectionSubqueryFactory;
import org.hibernate.hql.internal.NameGenerator;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.param.ParameterSpecification;
import org.hibernate.persister.collection.CollectionPropertyMapping;
import org.hibernate.persister.collection.QueryableCollection;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.entity.Joinable;
import org.hibernate.persister.entity.PropertyMapping;
import org.hibernate.persister.entity.Queryable;
import org.hibernate.type.EntityType;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

class FromElementType {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, FromElementType.class.getName());
   private FromElement fromElement;
   private EntityType entityType;
   private EntityPersister persister;
   private QueryableCollection queryableCollection;
   private CollectionPropertyMapping collectionPropertyMapping;
   private JoinSequence joinSequence;
   private String collectionSuffix;
   private ParameterSpecification indexCollectionSelectorParamSpec;
   private static final List SPECIAL_MANY2MANY_TREATMENT_FUNCTION_NAMES = Arrays.asList("index", "minIndex", "maxIndex");

   public FromElementType(FromElement fromElement, EntityPersister persister, EntityType entityType) {
      this.fromElement = fromElement;
      this.persister = persister;
      this.entityType = entityType;
      if (persister != null) {
         fromElement.setText(((Queryable)persister).getTableName() + " " + this.getTableAlias());
      }

   }

   protected FromElementType(FromElement fromElement) {
      this.fromElement = fromElement;
   }

   private String getTableAlias() {
      return this.fromElement.getTableAlias();
   }

   private String getCollectionTableAlias() {
      return this.fromElement.getCollectionTableAlias();
   }

   public String getCollectionSuffix() {
      return this.collectionSuffix;
   }

   public void setCollectionSuffix(String suffix) {
      this.collectionSuffix = suffix;
   }

   public EntityPersister getEntityPersister() {
      return this.persister;
   }

   public Type getDataType() {
      if (this.persister == null) {
         return this.queryableCollection == null ? null : this.queryableCollection.getType();
      } else {
         return this.entityType;
      }
   }

   public Type getSelectType() {
      if (this.entityType == null) {
         return null;
      } else {
         boolean shallow = this.fromElement.getFromClause().getWalker().isShallowQuery();
         return this.fromElement.getSessionFactoryHelper().getFactory().getTypeResolver().getTypeFactory().manyToOne(this.entityType.getAssociatedEntityName(), shallow);
      }
   }

   public Queryable getQueryable() {
      return this.persister instanceof Queryable ? (Queryable)this.persister : null;
   }

   String renderScalarIdentifierSelect(int i) {
      this.checkInitialized();
      String[] cols = this.getPropertyMapping("id").toColumns(this.getTableAlias(), "id");
      StringBuilder buf = new StringBuilder();

      for(int j = 0; j < cols.length; ++j) {
         String column = cols[j];
         if (j > 0) {
            buf.append(", ");
         }

         buf.append(column).append(" as ").append(NameGenerator.scalarName(i, j));
      }

      return buf.toString();
   }

   String renderIdentifierSelect(int size, int k) {
      this.checkInitialized();
      if (this.fromElement.getFromClause().isSubQuery()) {
         String[] idColumnNames = this.persister != null ? ((Queryable)this.persister).getIdentifierColumnNames() : new String[0];
         StringBuilder buf = new StringBuilder();

         for(int i = 0; i < idColumnNames.length; ++i) {
            buf.append(this.fromElement.getTableAlias()).append('.').append(idColumnNames[i]);
            if (i != idColumnNames.length - 1) {
               buf.append(", ");
            }
         }

         return buf.toString();
      } else if (this.persister == null) {
         throw new QueryException("not an entity");
      } else {
         String fragment = ((Queryable)this.persister).identifierSelectFragment(this.getTableAlias(), this.getSuffix(size, k));
         return trimLeadingCommaAndSpaces(fragment);
      }
   }

   private String getSuffix(int size, int sequence) {
      return generateSuffix(size, sequence);
   }

   private static String generateSuffix(int size, int k) {
      String suffix = size == 1 ? "" : Integer.toString(k) + '_';
      return suffix;
   }

   private void checkInitialized() {
      this.fromElement.checkInitialized();
   }

   String renderPropertySelect(int size, int k, boolean allProperties) {
      this.checkInitialized();
      if (this.persister == null) {
         return "";
      } else {
         String fragment = ((Queryable)this.persister).propertySelectFragment(this.getTableAlias(), this.getSuffix(size, k), allProperties);
         return trimLeadingCommaAndSpaces(fragment);
      }
   }

   String renderCollectionSelectFragment(int size, int k) {
      if (this.queryableCollection == null) {
         return "";
      } else {
         if (this.collectionSuffix == null) {
            this.collectionSuffix = generateSuffix(size, k);
         }

         String fragment = this.queryableCollection.selectFragment(this.getCollectionTableAlias(), this.collectionSuffix);
         return trimLeadingCommaAndSpaces(fragment);
      }
   }

   public String renderValueCollectionSelectFragment(int size, int k) {
      if (this.queryableCollection == null) {
         return "";
      } else {
         if (this.collectionSuffix == null) {
            this.collectionSuffix = generateSuffix(size, k);
         }

         String fragment = this.queryableCollection.selectFragment(this.getTableAlias(), this.collectionSuffix);
         return trimLeadingCommaAndSpaces(fragment);
      }
   }

   private static String trimLeadingCommaAndSpaces(String fragment) {
      if (fragment.length() > 0 && fragment.charAt(0) == ',') {
         fragment = fragment.substring(1);
      }

      fragment = fragment.trim();
      return fragment.trim();
   }

   public void setJoinSequence(JoinSequence joinSequence) {
      this.joinSequence = joinSequence;
   }

   public JoinSequence getJoinSequence() {
      if (this.joinSequence != null) {
         return this.joinSequence;
      } else if (this.persister instanceof Joinable) {
         Joinable joinable = (Joinable)this.persister;
         return this.fromElement.getSessionFactoryHelper().createJoinSequence().setRoot(joinable, this.getTableAlias());
      } else {
         return null;
      }
   }

   public void setQueryableCollection(QueryableCollection queryableCollection) {
      if (this.queryableCollection != null) {
         throw new IllegalStateException("QueryableCollection is already defined for " + this + "!");
      } else {
         this.queryableCollection = queryableCollection;
         if (!queryableCollection.isOneToMany()) {
            this.fromElement.setText(queryableCollection.getTableName() + " " + this.getTableAlias());
         }

      }
   }

   public QueryableCollection getQueryableCollection() {
      return this.queryableCollection;
   }

   public Type getPropertyType(String propertyName, String propertyPath) {
      this.checkInitialized();
      Type type = null;
      if (this.persister != null && propertyName.equals(propertyPath) && propertyName.equals(this.persister.getIdentifierPropertyName())) {
         type = this.persister.getIdentifierType();
      } else {
         PropertyMapping mapping = this.getPropertyMapping(propertyName);
         type = mapping.toType(propertyPath);
      }

      if (type == null) {
         throw new MappingException("Property " + propertyName + " does not exist in " + (this.queryableCollection == null ? "class" : "collection") + " " + (this.queryableCollection == null ? this.fromElement.getClassName() : this.queryableCollection.getRole()));
      } else {
         return type;
      }
   }

   String[] toColumns(String tableAlias, String path, boolean inSelect) {
      return this.toColumns(tableAlias, path, inSelect, false);
   }

   String[] toColumns(String tableAlias, String path, boolean inSelect, boolean forceAlias) {
      this.checkInitialized();
      PropertyMapping propertyMapping = this.getPropertyMapping(path);
      if (!inSelect && this.queryableCollection != null && CollectionProperties.isCollectionProperty(path)) {
         Map enabledFilters = this.fromElement.getWalker().getEnabledFilters();
         String subquery = CollectionSubqueryFactory.createCollectionSubquery(this.joinSequence.copy().setUseThetaStyle(true), enabledFilters, propertyMapping.toColumns(tableAlias, path));
         LOG.debugf("toColumns(%s,%s) : subquery = %s", tableAlias, path, subquery);
         return new String[]{"(" + subquery + ")"};
      } else if (forceAlias) {
         return propertyMapping.toColumns(tableAlias, path);
      } else if (this.fromElement.getWalker().getStatementType() == 45) {
         return propertyMapping.toColumns(tableAlias, path);
      } else if (this.fromElement.getWalker().getCurrentClauseType() == 45) {
         return propertyMapping.toColumns(tableAlias, path);
      } else if (this.fromElement.getWalker().isSubQuery()) {
         if (this.isCorrelation()) {
            return this.isMultiTable() ? propertyMapping.toColumns(tableAlias, path) : propertyMapping.toColumns(this.extractTableName(), path);
         } else {
            return propertyMapping.toColumns(tableAlias, path);
         }
      } else if (this.isManipulationQuery() && this.isMultiTable() && this.inWhereClause()) {
         return propertyMapping.toColumns(tableAlias, path);
      } else {
         String[] columns = propertyMapping.toColumns(path);
         LOG.tracev("Using non-qualified column reference [{0} -> ({1})]", path, ArrayHelper.toString(columns));
         return columns;
      }
   }

   private boolean isCorrelation() {
      FromClause top = this.fromElement.getWalker().getFinalFromClause();
      return this.fromElement.getFromClause() != this.fromElement.getWalker().getCurrentFromClause() && this.fromElement.getFromClause() == top;
   }

   private boolean isMultiTable() {
      return this.fromElement.getQueryable() != null && this.fromElement.getQueryable().isMultiTable();
   }

   private String extractTableName() {
      return this.fromElement.getQueryable().getTableName();
   }

   private boolean isManipulationQuery() {
      return this.fromElement.getWalker().getStatementType() == 51 || this.fromElement.getWalker().getStatementType() == 13;
   }

   private boolean inWhereClause() {
      return this.fromElement.getWalker().getCurrentTopLevelClauseType() == 53;
   }

   PropertyMapping getPropertyMapping(String propertyName) {
      this.checkInitialized();
      if (this.queryableCollection == null) {
         return (PropertyMapping)this.persister;
      } else if (this.queryableCollection.isManyToMany() && this.queryableCollection.hasIndex() && SPECIAL_MANY2MANY_TREATMENT_FUNCTION_NAMES.contains(propertyName)) {
         return new SpecialManyToManyCollectionPropertyMapping();
      } else if (CollectionProperties.isCollectionProperty(propertyName)) {
         if (this.collectionPropertyMapping == null) {
            this.collectionPropertyMapping = new CollectionPropertyMapping(this.queryableCollection);
         }

         return this.collectionPropertyMapping;
      } else if (this.queryableCollection.getElementType().isAnyType()) {
         return this.queryableCollection;
      } else {
         return (PropertyMapping)(this.queryableCollection.getElementType().isComponentType() && propertyName.equals("id") ? (PropertyMapping)this.queryableCollection.getOwnerEntityPersister() : this.queryableCollection);
      }
   }

   public boolean isCollectionOfValuesOrComponents() {
      return this.persister == null && this.queryableCollection != null && !this.queryableCollection.getElementType().isEntityType();
   }

   public boolean isEntity() {
      return this.persister != null;
   }

   public ParameterSpecification getIndexCollectionSelectorParamSpec() {
      return this.indexCollectionSelectorParamSpec;
   }

   public void setIndexCollectionSelectorParamSpec(ParameterSpecification indexCollectionSelectorParamSpec) {
      this.indexCollectionSelectorParamSpec = indexCollectionSelectorParamSpec;
   }

   private class SpecialManyToManyCollectionPropertyMapping implements PropertyMapping {
      private SpecialManyToManyCollectionPropertyMapping() {
      }

      public Type getType() {
         return FromElementType.this.queryableCollection.getCollectionType();
      }

      private void validate(String propertyName) {
         if (!"index".equals(propertyName) && !"maxIndex".equals(propertyName) && !"minIndex".equals(propertyName)) {
            throw new IllegalArgumentException("Expecting index-related function call");
         }
      }

      public Type toType(String propertyName) throws QueryException {
         this.validate(propertyName);
         return FromElementType.this.queryableCollection.getIndexType();
      }

      public String[] toColumns(String alias, String propertyName) throws QueryException {
         this.validate(propertyName);
         String joinTableAlias = FromElementType.this.joinSequence.getFirstJoin().getAlias();
         if ("index".equals(propertyName)) {
            return FromElementType.this.queryableCollection.toColumns(joinTableAlias, propertyName);
         } else {
            String[] cols = FromElementType.this.queryableCollection.getIndexColumnNames(joinTableAlias);
            if ("minIndex".equals(propertyName)) {
               if (cols.length != 1) {
                  throw new QueryException("composite collection index in minIndex()");
               } else {
                  return new String[]{"min(" + cols[0] + ')'};
               }
            } else if (cols.length != 1) {
               throw new QueryException("composite collection index in maxIndex()");
            } else {
               return new String[]{"max(" + cols[0] + ')'};
            }
         }
      }

      public String[] toColumns(String propertyName) throws QueryException, UnsupportedOperationException {
         this.validate(propertyName);
         return FromElementType.this.queryableCollection.toColumns(propertyName);
      }
   }
}
