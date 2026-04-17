package org.hibernate.hql.internal.ast.tree;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.hibernate.QueryException;
import org.hibernate.engine.internal.JoinSequence;
import org.hibernate.hql.internal.CollectionProperties;
import org.hibernate.hql.internal.ast.TypeDiscriminatorMetadata;
import org.hibernate.hql.internal.ast.util.ASTUtil;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.param.ParameterSpecification;
import org.hibernate.persister.collection.QueryableCollection;
import org.hibernate.persister.entity.DiscriminatorMetadata;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.entity.PropertyMapping;
import org.hibernate.persister.entity.Queryable;
import org.hibernate.type.EntityType;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

public class FromElement extends HqlSqlWalkerNode implements DisplayableNode, ParameterContainer {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, FromElement.class.getName());
   private String className;
   private String classAlias;
   private String tableAlias;
   private String collectionTableAlias;
   private FromClause fromClause;
   private boolean includeSubclasses = true;
   private boolean collectionJoin = false;
   private FromElement origin;
   private String[] columns;
   private String role;
   private boolean fetch;
   private boolean isAllPropertyFetch;
   private boolean filter = false;
   private int sequence = -1;
   private boolean useFromFragment = false;
   private boolean initialized = false;
   private FromElementType elementType;
   private boolean useWhereFragment = true;
   private List destinations = new LinkedList();
   private boolean manyToMany = false;
   private String withClauseFragment = null;
   private String withClauseJoinAlias;
   private boolean dereferencedBySuperclassProperty;
   private boolean dereferencedBySubclassProperty;
   public static final String DISCRIMINATOR_PROPERTY_NAME = "class";
   private TypeDiscriminatorMetadata typeDiscriminatorMetadata;
   private List embeddedParameters;

   public FromElement() {
   }

   protected FromElement(FromClause fromClause, FromElement origin, String alias) {
      this.fromClause = fromClause;
      this.origin = origin;
      this.classAlias = alias;
      this.tableAlias = origin.getTableAlias();
      super.initialize(fromClause.getWalker());
   }

   protected void initializeComponentJoin(FromElementType elementType) {
      this.elementType = elementType;
      this.fromClause.registerFromElement(this);
      this.initialized = true;
   }

   public String getCollectionSuffix() {
      return this.elementType.getCollectionSuffix();
   }

   public void setCollectionSuffix(String suffix) {
      this.elementType.setCollectionSuffix(suffix);
   }

   public void initializeCollection(FromClause fromClause, String classAlias, String tableAlias) {
      this.doInitialize(fromClause, tableAlias, (String)null, classAlias, (EntityPersister)null, (EntityType)null);
      this.initialized = true;
   }

   public void initializeEntity(FromClause fromClause, String className, EntityPersister persister, EntityType type, String classAlias, String tableAlias) {
      this.doInitialize(fromClause, tableAlias, className, classAlias, persister, type);
      this.sequence = fromClause.nextFromElementCounter();
      this.initialized = true;
   }

   private void doInitialize(FromClause fromClause, String tableAlias, String className, String classAlias, EntityPersister persister, EntityType type) {
      if (this.initialized) {
         throw new IllegalStateException("Already initialized!!");
      } else {
         this.fromClause = fromClause;
         this.tableAlias = tableAlias;
         this.className = className;
         this.classAlias = classAlias;
         this.elementType = new FromElementType(this, persister, type);
         fromClause.registerFromElement(this);
         LOG.debugf("%s : %s (%s) -> %s", new Object[]{fromClause, className, classAlias == null ? "<no alias>" : classAlias, tableAlias});
      }
   }

   public EntityPersister getEntityPersister() {
      return this.elementType.getEntityPersister();
   }

   public Type getDataType() {
      return this.elementType.getDataType();
   }

   public Type getSelectType() {
      return this.elementType.getSelectType();
   }

   public Queryable getQueryable() {
      return this.elementType.getQueryable();
   }

   public String getClassName() {
      return this.className;
   }

   public String getClassAlias() {
      return this.classAlias;
   }

   private String getTableName() {
      Queryable queryable = this.getQueryable();
      return queryable != null ? queryable.getTableName() : "{none}";
   }

   public String getTableAlias() {
      return this.tableAlias;
   }

   String renderScalarIdentifierSelect(int i) {
      return this.elementType.renderScalarIdentifierSelect(i);
   }

   void checkInitialized() {
      if (!this.initialized) {
         throw new IllegalStateException("FromElement has not been initialized!");
      }
   }

   String renderIdentifierSelect(int size, int k) {
      return this.elementType.renderIdentifierSelect(size, k);
   }

   String renderPropertySelect(int size, int k) {
      return this.elementType.renderPropertySelect(size, k, this.isAllPropertyFetch);
   }

   String renderCollectionSelectFragment(int size, int k) {
      return this.elementType.renderCollectionSelectFragment(size, k);
   }

   String renderValueCollectionSelectFragment(int size, int k) {
      return this.elementType.renderValueCollectionSelectFragment(size, k);
   }

   public FromClause getFromClause() {
      return this.fromClause;
   }

   public boolean isImplied() {
      return false;
   }

   public String getDisplayText() {
      StringBuilder buf = new StringBuilder();
      buf.append("FromElement{");
      this.appendDisplayText(buf);
      buf.append("}");
      return buf.toString();
   }

   protected void appendDisplayText(StringBuilder buf) {
      buf.append(this.isImplied() ? (this.isImpliedInFromClause() ? "implied in FROM clause" : "implied") : "explicit");
      buf.append(",").append(this.isCollectionJoin() ? "collection join" : "not a collection join");
      buf.append(",").append(this.fetch ? "fetch join" : "not a fetch join");
      buf.append(",").append(this.isAllPropertyFetch ? "fetch all properties" : "fetch non-lazy properties");
      buf.append(",classAlias=").append(this.getClassAlias());
      buf.append(",role=").append(this.role);
      buf.append(",tableName=").append(this.getTableName());
      buf.append(",tableAlias=").append(this.getTableAlias());
      FromElement origin = this.getRealOrigin();
      buf.append(",origin=").append(origin == null ? "null" : origin.getText());
      buf.append(",columns={");
      if (this.columns != null) {
         for(int i = 0; i < this.columns.length; ++i) {
            buf.append(this.columns[i]);
            if (i < this.columns.length) {
               buf.append(" ");
            }
         }
      }

      buf.append(",className=").append(this.className);
      buf.append("}");
   }

   public int hashCode() {
      return super.hashCode();
   }

   public boolean equals(Object obj) {
      return super.equals(obj);
   }

   public void setJoinSequence(JoinSequence joinSequence) {
      this.elementType.setJoinSequence(joinSequence);
   }

   public JoinSequence getJoinSequence() {
      return this.elementType.getJoinSequence();
   }

   public void setIncludeSubclasses(boolean includeSubclasses) {
      if (LOG.isTraceEnabled() && this.isDereferencedBySuperclassOrSubclassProperty() && !includeSubclasses) {
         LOG.trace("Attempt to disable subclass-inclusions : ", new Exception("Stack-trace source"));
      }

      this.includeSubclasses = includeSubclasses;
   }

   public boolean isIncludeSubclasses() {
      return this.includeSubclasses;
   }

   public boolean isDereferencedBySuperclassOrSubclassProperty() {
      return this.dereferencedBySubclassProperty || this.dereferencedBySuperclassProperty;
   }

   public String getIdentityColumn() {
      String[] cols = this.getIdentityColumns();
      return cols.length == 1 ? cols[0] : "(" + StringHelper.join(", ", cols) + ")";
   }

   public String[] getIdentityColumns() {
      this.checkInitialized();
      String table = this.getTableAlias();
      if (table == null) {
         throw new IllegalStateException("No table alias for node " + this);
      } else {
         String propertyName;
         if (this.getEntityPersister() != null && this.getEntityPersister().getEntityMetamodel() != null && this.getEntityPersister().getEntityMetamodel().hasNonIdentifierPropertyNamedId()) {
            propertyName = this.getEntityPersister().getIdentifierPropertyName();
         } else {
            propertyName = "id";
         }

         return this.getWalker().getStatementType() == 45 ? this.getPropertyMapping(propertyName).toColumns(table, propertyName) : this.getPropertyMapping(propertyName).toColumns(propertyName);
      }
   }

   public void setCollectionJoin(boolean collectionJoin) {
      this.collectionJoin = collectionJoin;
   }

   public boolean isCollectionJoin() {
      return this.collectionJoin;
   }

   public void setRole(String role) {
      this.role = role;
   }

   public void setQueryableCollection(QueryableCollection queryableCollection) {
      this.elementType.setQueryableCollection(queryableCollection);
   }

   public QueryableCollection getQueryableCollection() {
      return this.elementType.getQueryableCollection();
   }

   public void setColumns(String[] columns) {
      this.columns = columns;
   }

   public void setOrigin(FromElement origin, boolean manyToMany) {
      this.origin = origin;
      this.manyToMany = manyToMany;
      origin.addDestination(this);
      if (origin.getFromClause() == this.getFromClause()) {
         if (manyToMany) {
            ASTUtil.appendSibling(origin, this);
         } else if (!this.getWalker().isInFrom() && !this.getWalker().isInSelect()) {
            this.getFromClause().addChild(this);
         } else {
            origin.addChild(this);
         }
      } else if (!this.getWalker().isInFrom()) {
         this.getFromClause().addChild(this);
      }

   }

   public boolean isManyToMany() {
      return this.manyToMany;
   }

   private void addDestination(FromElement fromElement) {
      this.destinations.add(fromElement);
   }

   public List getDestinations() {
      return this.destinations;
   }

   public FromElement getOrigin() {
      return this.origin;
   }

   public FromElement getRealOrigin() {
      if (this.origin == null) {
         return null;
      } else {
         return this.origin.getText() != null && !"".equals(this.origin.getText()) ? this.origin : this.origin.getRealOrigin();
      }
   }

   public TypeDiscriminatorMetadata getTypeDiscriminatorMetadata() {
      if (this.typeDiscriminatorMetadata == null) {
         this.typeDiscriminatorMetadata = this.buildTypeDiscriminatorMetadata();
      }

      return this.typeDiscriminatorMetadata;
   }

   private TypeDiscriminatorMetadata buildTypeDiscriminatorMetadata() {
      String aliasToUse = this.getTableAlias();
      Queryable queryable = this.getQueryable();
      if (queryable == null) {
         QueryableCollection collection = this.getQueryableCollection();
         if (!collection.getElementType().isEntityType()) {
            throw new QueryException("type discrimination cannot be applied to value collection [" + collection.getRole() + "]");
         }

         queryable = (Queryable)collection.getElementPersister();
      }

      this.handlePropertyBeingDereferenced(this.getDataType(), "class");
      return new TypeDiscriminatorMetadataImpl(queryable.getTypeDiscriminatorMetadata(), aliasToUse);
   }

   public Type getPropertyType(String propertyName, String propertyPath) {
      return this.elementType.getPropertyType(propertyName, propertyPath);
   }

   public String[] toColumns(String tableAlias, String path, boolean inSelect) {
      return this.elementType.toColumns(tableAlias, path, inSelect);
   }

   public String[] toColumns(String tableAlias, String path, boolean inSelect, boolean forceAlias) {
      return this.elementType.toColumns(tableAlias, path, inSelect, forceAlias);
   }

   public PropertyMapping getPropertyMapping(String propertyName) {
      return this.elementType.getPropertyMapping(propertyName);
   }

   public void setFetch(boolean fetch) {
      this.fetch = fetch;
      if (fetch && this.getWalker().isShallowQuery()) {
         throw new QueryException("fetch may not be used with scroll() or iterate()");
      }
   }

   public boolean isFetch() {
      return this.fetch;
   }

   public int getSequence() {
      return this.sequence;
   }

   public void setFilter(boolean b) {
      this.filter = b;
   }

   public boolean isFilter() {
      return this.filter;
   }

   public boolean useFromFragment() {
      this.checkInitialized();
      return !this.isImplied() || this.useFromFragment;
   }

   public void setUseFromFragment(boolean useFromFragment) {
      this.useFromFragment = useFromFragment;
   }

   public boolean useWhereFragment() {
      return this.useWhereFragment;
   }

   public void setUseWhereFragment(boolean b) {
      this.useWhereFragment = b;
   }

   public void setCollectionTableAlias(String collectionTableAlias) {
      this.collectionTableAlias = collectionTableAlias;
   }

   public String getCollectionTableAlias() {
      return this.collectionTableAlias;
   }

   public boolean isCollectionOfValuesOrComponents() {
      return this.elementType.isCollectionOfValuesOrComponents();
   }

   public boolean isEntity() {
      return this.elementType.isEntity();
   }

   public void setImpliedInFromClause(boolean flag) {
      throw new UnsupportedOperationException("Explicit FROM elements can't be implied in the FROM clause!");
   }

   public boolean isImpliedInFromClause() {
      return false;
   }

   public void setInProjectionList(boolean inProjectionList) {
   }

   public boolean inProjectionList() {
      return !this.isImplied() && this.isFromOrJoinFragment();
   }

   public boolean isFromOrJoinFragment() {
      return this.getType() == 134 || this.getType() == 136;
   }

   public boolean isAllPropertyFetch() {
      return this.isAllPropertyFetch;
   }

   public void setAllPropertyFetch(boolean fetch) {
      this.isAllPropertyFetch = fetch;
   }

   public String getWithClauseFragment() {
      return this.withClauseFragment;
   }

   public String getWithClauseJoinAlias() {
      return this.withClauseJoinAlias;
   }

   public void setWithClauseFragment(String withClauseJoinAlias, String withClauseFragment) {
      this.withClauseJoinAlias = withClauseJoinAlias;
      this.withClauseFragment = withClauseFragment;
   }

   public boolean hasCacheablePersister() {
      return this.getQueryableCollection() != null ? this.getQueryableCollection().hasCache() : this.getQueryable().hasCache();
   }

   public void handlePropertyBeingDereferenced(Type propertySource, String propertyName) {
      if (this.getQueryableCollection() == null || !CollectionProperties.isCollectionProperty(propertyName)) {
         if (!propertySource.isComponentType()) {
            Queryable persister = this.getQueryable();
            if (persister != null) {
               try {
                  Queryable.Declarer propertyDeclarer = persister.getSubclassPropertyDeclarer(propertyName);
                  if (LOG.isTraceEnabled()) {
                     LOG.tracev("Handling property dereference [{0} ({1}) -> {2} ({3})]", new Object[]{persister.getEntityName(), this.getClassAlias(), propertyName, propertyDeclarer});
                  }

                  if (propertyDeclarer == Queryable.Declarer.SUBCLASS) {
                     this.dereferencedBySubclassProperty = true;
                     this.includeSubclasses = true;
                  } else if (propertyDeclarer == Queryable.Declarer.SUPERCLASS) {
                     this.dereferencedBySuperclassProperty = true;
                  }
               } catch (QueryException var5) {
               }
            }

         }
      }
   }

   public boolean isDereferencedBySuperclassProperty() {
      return this.dereferencedBySuperclassProperty;
   }

   public boolean isDereferencedBySubclassProperty() {
      return this.dereferencedBySubclassProperty;
   }

   public void addEmbeddedParameter(ParameterSpecification specification) {
      if (this.embeddedParameters == null) {
         this.embeddedParameters = new ArrayList();
      }

      this.embeddedParameters.add(specification);
   }

   public boolean hasEmbeddedParameters() {
      return this.embeddedParameters != null && !this.embeddedParameters.isEmpty();
   }

   public ParameterSpecification[] getEmbeddedParameters() {
      return (ParameterSpecification[])this.embeddedParameters.toArray(new ParameterSpecification[this.embeddedParameters.size()]);
   }

   public ParameterSpecification getIndexCollectionSelectorParamSpec() {
      return this.elementType.getIndexCollectionSelectorParamSpec();
   }

   public void setIndexCollectionSelectorParamSpec(ParameterSpecification indexCollectionSelectorParamSpec) {
      if (indexCollectionSelectorParamSpec == null) {
         if (this.elementType.getIndexCollectionSelectorParamSpec() != null) {
            this.embeddedParameters.remove(this.elementType.getIndexCollectionSelectorParamSpec());
            this.elementType.setIndexCollectionSelectorParamSpec((ParameterSpecification)null);
         }
      } else {
         this.elementType.setIndexCollectionSelectorParamSpec(indexCollectionSelectorParamSpec);
         this.addEmbeddedParameter(indexCollectionSelectorParamSpec);
      }

   }

   private static class TypeDiscriminatorMetadataImpl implements TypeDiscriminatorMetadata {
      private final DiscriminatorMetadata persisterDiscriminatorMetadata;
      private final String alias;

      private TypeDiscriminatorMetadataImpl(DiscriminatorMetadata persisterDiscriminatorMetadata, String alias) {
         this.persisterDiscriminatorMetadata = persisterDiscriminatorMetadata;
         this.alias = alias;
      }

      public String getSqlFragment() {
         return this.persisterDiscriminatorMetadata.getSqlFragment(this.alias);
      }

      public Type getResolutionType() {
         return this.persisterDiscriminatorMetadata.getResolutionType();
      }
   }
}
