package org.hibernate.loader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.hibernate.FetchMode;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.MappingException;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.internal.JoinHelper;
import org.hibernate.engine.spi.CascadeStyle;
import org.hibernate.engine.spi.LoadQueryInfluencers;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.collection.QueryableCollection;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.entity.Joinable;
import org.hibernate.persister.entity.Loadable;
import org.hibernate.persister.entity.OuterJoinLoadable;
import org.hibernate.sql.ConditionFragment;
import org.hibernate.sql.DisjunctionFragment;
import org.hibernate.sql.InFragment;
import org.hibernate.sql.JoinFragment;
import org.hibernate.sql.JoinType;
import org.hibernate.type.AssociationType;
import org.hibernate.type.CompositeType;
import org.hibernate.type.EntityType;
import org.hibernate.type.ForeignKeyDirection;
import org.hibernate.type.Type;

public class JoinWalker {
   private final SessionFactoryImplementor factory;
   protected final List associations = new ArrayList();
   private final Set visitedAssociationKeys = new HashSet();
   private final LoadQueryInfluencers loadQueryInfluencers;
   protected String[] suffixes;
   protected String[] collectionSuffixes;
   protected Loadable[] persisters;
   protected int[] owners;
   protected EntityType[] ownerAssociationTypes;
   protected CollectionPersister[] collectionPersisters;
   protected int[] collectionOwners;
   protected String[] aliases;
   protected LockOptions lockOptions;
   protected LockMode[] lockModeArray;
   protected String sql;

   protected JoinWalker(SessionFactoryImplementor factory, LoadQueryInfluencers loadQueryInfluencers) {
      this.factory = factory;
      this.loadQueryInfluencers = loadQueryInfluencers;
   }

   public String[] getCollectionSuffixes() {
      return this.collectionSuffixes;
   }

   public void setCollectionSuffixes(String[] collectionSuffixes) {
      this.collectionSuffixes = collectionSuffixes;
   }

   public LockOptions getLockModeOptions() {
      return this.lockOptions;
   }

   public LockMode[] getLockModeArray() {
      return this.lockModeArray;
   }

   public String[] getSuffixes() {
      return this.suffixes;
   }

   public void setSuffixes(String[] suffixes) {
      this.suffixes = suffixes;
   }

   public String[] getAliases() {
      return this.aliases;
   }

   public void setAliases(String[] aliases) {
      this.aliases = aliases;
   }

   public int[] getCollectionOwners() {
      return this.collectionOwners;
   }

   public void setCollectionOwners(int[] collectionOwners) {
      this.collectionOwners = collectionOwners;
   }

   public CollectionPersister[] getCollectionPersisters() {
      return this.collectionPersisters;
   }

   public void setCollectionPersisters(CollectionPersister[] collectionPersisters) {
      this.collectionPersisters = collectionPersisters;
   }

   public EntityType[] getOwnerAssociationTypes() {
      return this.ownerAssociationTypes;
   }

   public void setOwnerAssociationTypes(EntityType[] ownerAssociationType) {
      this.ownerAssociationTypes = ownerAssociationType;
   }

   public int[] getOwners() {
      return this.owners;
   }

   public void setOwners(int[] owners) {
      this.owners = owners;
   }

   public Loadable[] getPersisters() {
      return this.persisters;
   }

   public void setPersisters(Loadable[] persisters) {
      this.persisters = persisters;
   }

   public String getSQLString() {
      return this.sql;
   }

   public void setSql(String sql) {
      this.sql = sql;
   }

   protected SessionFactoryImplementor getFactory() {
      return this.factory;
   }

   protected Dialect getDialect() {
      return this.factory.getDialect();
   }

   public LoadQueryInfluencers getLoadQueryInfluencers() {
      return this.loadQueryInfluencers;
   }

   private void addAssociationToJoinTreeIfNecessary(AssociationType type, String[] aliasedLhsColumns, String alias, PropertyPath path, int currentDepth, JoinType joinType) throws MappingException {
      if (joinType != JoinType.NONE) {
         this.addAssociationToJoinTree(type, aliasedLhsColumns, alias, path, currentDepth, joinType);
      }

   }

   protected boolean hasRestriction(PropertyPath path) {
      return false;
   }

   protected String getWithClause(PropertyPath path) {
      return "";
   }

   private void addAssociationToJoinTree(AssociationType type, String[] aliasedLhsColumns, String alias, PropertyPath path, int currentDepth, JoinType joinType) throws MappingException {
      Joinable joinable = type.getAssociatedJoinable(this.getFactory());
      String subalias = this.generateTableAlias(this.associations.size() + 1, path, joinable);
      OuterJoinableAssociation assoc = new OuterJoinableAssociation(path, type, alias, aliasedLhsColumns, subalias, joinType, this.getWithClause(path), this.hasRestriction(path), this.getFactory(), this.loadQueryInfluencers.getEnabledFilters());
      assoc.validateJoin(path.getFullPath());
      this.associations.add(assoc);
      int nextDepth = currentDepth + 1;
      if (!joinable.isCollection()) {
         if (joinable instanceof OuterJoinLoadable) {
            this.walkEntityTree((OuterJoinLoadable)joinable, subalias, path, nextDepth);
         }
      } else if (joinable instanceof QueryableCollection) {
         this.walkCollectionTree((QueryableCollection)joinable, subalias, path, nextDepth);
      }

   }

   protected final void walkEntityTree(OuterJoinLoadable persister, String alias) throws MappingException {
      this.walkEntityTree(persister, alias, new PropertyPath(), 0);
   }

   protected final void walkCollectionTree(QueryableCollection persister, String alias) throws MappingException {
      this.walkCollectionTree(persister, alias, new PropertyPath(), 0);
   }

   private void walkCollectionTree(QueryableCollection persister, String alias, PropertyPath path, int currentDepth) throws MappingException {
      if (persister.isOneToMany()) {
         this.walkEntityTree((OuterJoinLoadable)persister.getElementPersister(), alias, path, currentDepth);
      } else {
         Type type = persister.getElementType();
         if (type.isAssociationType()) {
            AssociationType associationType = (AssociationType)type;
            String[] aliasedLhsColumns = persister.getElementColumnNames(alias);
            String[] lhsColumns = persister.getElementColumnNames();
            boolean useInnerJoin = currentDepth == 0;
            JoinType joinType = this.getJoinType(associationType, persister.getFetchMode(), path, persister.getTableName(), lhsColumns, !useInnerJoin, currentDepth - 1, (CascadeStyle)null);
            this.addAssociationToJoinTreeIfNecessary(associationType, aliasedLhsColumns, alias, path, currentDepth - 1, joinType);
         } else if (type.isComponentType()) {
            this.walkCompositeElementTree((CompositeType)type, persister.getElementColumnNames(), persister, alias, path, currentDepth);
         }
      }

   }

   private void walkEntityAssociationTree(AssociationType associationType, OuterJoinLoadable persister, int propertyNumber, String alias, PropertyPath path, boolean nullable, int currentDepth) throws MappingException {
      String[] aliasedLhsColumns = JoinHelper.getAliasedLHSColumnNames(associationType, alias, propertyNumber, persister, this.getFactory());
      String[] lhsColumns = JoinHelper.getLHSColumnNames(associationType, propertyNumber, persister, this.getFactory());
      String lhsTable = JoinHelper.getLHSTableName(associationType, propertyNumber, persister);
      PropertyPath subPath = path.append(persister.getSubclassPropertyName(propertyNumber));
      JoinType joinType = this.getJoinType(persister, subPath, propertyNumber, associationType, persister.getFetchMode(propertyNumber), persister.getCascadeStyle(propertyNumber), lhsTable, lhsColumns, nullable, currentDepth);
      this.addAssociationToJoinTreeIfNecessary(associationType, aliasedLhsColumns, alias, subPath, currentDepth, joinType);
   }

   protected JoinType getJoinType(OuterJoinLoadable persister, PropertyPath path, int propertyNumber, AssociationType associationType, FetchMode metadataFetchMode, CascadeStyle metadataCascadeStyle, String lhsTable, String[] lhsColumns, boolean nullable, int currentDepth) throws MappingException {
      return this.getJoinType(associationType, metadataFetchMode, path, lhsTable, lhsColumns, nullable, currentDepth, metadataCascadeStyle);
   }

   protected JoinType getJoinType(AssociationType associationType, FetchMode config, PropertyPath path, String lhsTable, String[] lhsColumns, boolean nullable, int currentDepth, CascadeStyle cascadeStyle) throws MappingException {
      if (!this.isJoinedFetchEnabled(associationType, config, cascadeStyle)) {
         return JoinType.NONE;
      } else if (!this.isTooDeep(currentDepth) && (!associationType.isCollectionType() || !this.isTooManyCollections())) {
         return this.isDuplicateAssociation(lhsTable, lhsColumns, associationType) ? JoinType.NONE : this.getJoinType(nullable, currentDepth);
      } else {
         return JoinType.NONE;
      }
   }

   private void walkEntityTree(OuterJoinLoadable persister, String alias, PropertyPath path, int currentDepth) throws MappingException {
      int n = persister.countSubclassProperties();

      for(int i = 0; i < n; ++i) {
         Type type = persister.getSubclassPropertyType(i);
         if (type.isAssociationType()) {
            this.walkEntityAssociationTree((AssociationType)type, persister, i, alias, path, persister.isSubclassPropertyNullable(i), currentDepth);
         } else if (type.isComponentType()) {
            this.walkComponentTree((CompositeType)type, i, 0, persister, alias, path.append(persister.getSubclassPropertyName(i)), currentDepth);
         }
      }

      Type idType = persister.getIdentifierType();
      if (idType.isComponentType()) {
         CompositeType cidType = (CompositeType)idType;
         if (cidType.isEmbedded() && persister.getEntityMetamodel().getIdentifierProperty().isEmbedded()) {
            this.walkComponentTree(cidType, -1, 0, persister, alias, path, currentDepth);
         }
      }

   }

   private void walkComponentTree(CompositeType componentType, int propertyNumber, int begin, OuterJoinLoadable persister, String alias, PropertyPath path, int currentDepth) throws MappingException {
      Type[] types = componentType.getSubtypes();
      String[] propertyNames = componentType.getPropertyNames();

      for(int i = 0; i < types.length; ++i) {
         if (types[i].isAssociationType()) {
            AssociationType associationType = (AssociationType)types[i];
            String[] aliasedLhsColumns = JoinHelper.getAliasedLHSColumnNames(associationType, alias, propertyNumber, begin, persister, this.getFactory());
            String[] lhsColumns = JoinHelper.getLHSColumnNames(associationType, propertyNumber, begin, persister, this.getFactory());
            String lhsTable = JoinHelper.getLHSTableName(associationType, propertyNumber, persister);
            PropertyPath subPath = path.append(propertyNames[i]);
            boolean[] propertyNullability = componentType.getPropertyNullability();
            JoinType joinType = this.getJoinType(persister, subPath, propertyNumber, associationType, componentType.getFetchMode(i), componentType.getCascadeStyle(i), lhsTable, lhsColumns, propertyNullability == null || propertyNullability[i], currentDepth);
            this.addAssociationToJoinTreeIfNecessary(associationType, aliasedLhsColumns, alias, subPath, currentDepth, joinType);
         } else if (types[i].isComponentType()) {
            PropertyPath subPath = path.append(propertyNames[i]);
            this.walkComponentTree((CompositeType)types[i], propertyNumber, begin, persister, alias, subPath, currentDepth);
         }

         begin += types[i].getColumnSpan(this.getFactory());
      }

   }

   private void walkCompositeElementTree(CompositeType compositeType, String[] cols, QueryableCollection persister, String alias, PropertyPath path, int currentDepth) throws MappingException {
      Type[] types = compositeType.getSubtypes();
      String[] propertyNames = compositeType.getPropertyNames();
      int begin = 0;

      for(int i = 0; i < types.length; ++i) {
         int length = types[i].getColumnSpan(this.getFactory());
         String[] lhsColumns = ArrayHelper.slice(cols, begin, length);
         if (types[i].isAssociationType()) {
            AssociationType associationType = (AssociationType)types[i];
            String[] aliasedLhsColumns = StringHelper.qualify(alias, lhsColumns);
            PropertyPath subPath = path.append(propertyNames[i]);
            boolean[] propertyNullability = compositeType.getPropertyNullability();
            JoinType joinType = this.getJoinType(associationType, compositeType.getFetchMode(i), subPath, persister.getTableName(), lhsColumns, propertyNullability == null || propertyNullability[i], currentDepth, compositeType.getCascadeStyle(i));
            this.addAssociationToJoinTreeIfNecessary(associationType, aliasedLhsColumns, alias, subPath, currentDepth, joinType);
         } else if (types[i].isComponentType()) {
            PropertyPath subPath = path.append(propertyNames[i]);
            this.walkCompositeElementTree((CompositeType)types[i], lhsColumns, persister, alias, subPath, currentDepth);
         }

         begin += length;
      }

   }

   protected JoinType getJoinType(boolean nullable, int currentDepth) {
      return !nullable && currentDepth <= 0 ? JoinType.INNER_JOIN : JoinType.LEFT_OUTER_JOIN;
   }

   protected boolean isTooDeep(int currentDepth) {
      Integer maxFetchDepth = this.getFactory().getSettings().getMaximumFetchDepth();
      return maxFetchDepth != null && currentDepth >= maxFetchDepth;
   }

   protected boolean isTooManyCollections() {
      return false;
   }

   protected boolean isJoinedFetchEnabledInMapping(FetchMode config, AssociationType type) throws MappingException {
      if (!type.isEntityType() && !type.isCollectionType()) {
         return false;
      } else if (config == FetchMode.JOIN) {
         return true;
      } else if (config == FetchMode.SELECT) {
         return false;
      } else if (type.isEntityType()) {
         EntityType entityType = (EntityType)type;
         EntityPersister persister = this.getFactory().getEntityPersister(entityType.getAssociatedEntityName());
         return !persister.hasProxy();
      } else {
         return false;
      }
   }

   protected boolean isJoinedFetchEnabled(AssociationType type, FetchMode config, CascadeStyle cascadeStyle) {
      return type.isEntityType() && this.isJoinedFetchEnabledInMapping(config, type);
   }

   protected String generateTableAlias(int n, PropertyPath path, Joinable joinable) {
      return StringHelper.generateAlias(joinable.getName(), n);
   }

   protected String generateRootAlias(String description) {
      return StringHelper.generateAlias(description, 0);
   }

   protected boolean isDuplicateAssociation(String foreignKeyTable, String[] foreignKeyColumns) {
      AssociationKey associationKey = new AssociationKey(foreignKeyColumns, foreignKeyTable);
      return !this.visitedAssociationKeys.add(associationKey);
   }

   protected boolean isDuplicateAssociation(String lhsTable, String[] lhsColumnNames, AssociationType type) {
      String foreignKeyTable;
      String[] foreignKeyColumns;
      if (type.getForeignKeyDirection() == ForeignKeyDirection.FOREIGN_KEY_FROM_PARENT) {
         foreignKeyTable = lhsTable;
         foreignKeyColumns = lhsColumnNames;
      } else {
         foreignKeyTable = type.getAssociatedJoinable(this.getFactory()).getTableName();
         foreignKeyColumns = JoinHelper.getRHSColumnNames(type, this.getFactory());
      }

      return this.isDuplicateAssociation(foreignKeyTable, foreignKeyColumns);
   }

   protected boolean isJoinable(JoinType joinType, Set visitedAssociationKeys, String lhsTable, String[] lhsColumnNames, AssociationType type, int depth) {
      if (joinType == JoinType.NONE) {
         return false;
      } else if (joinType == JoinType.INNER_JOIN) {
         return true;
      } else {
         Integer maxFetchDepth = this.getFactory().getSettings().getMaximumFetchDepth();
         boolean tooDeep = maxFetchDepth != null && depth >= maxFetchDepth;
         return !tooDeep && !this.isDuplicateAssociation(lhsTable, lhsColumnNames, type);
      }
   }

   protected String orderBy(List associations, String orderBy) {
      return mergeOrderings(orderBy(associations), orderBy);
   }

   protected static String mergeOrderings(String ordering1, String ordering2) {
      if (ordering1.length() == 0) {
         return ordering2;
      } else {
         return ordering2.length() == 0 ? ordering1 : ordering1 + ", " + ordering2;
      }
   }

   protected final JoinFragment mergeOuterJoins(List associations) throws MappingException {
      JoinFragment outerjoin = this.getDialect().createOuterJoinFragment();
      Iterator iter = associations.iterator();

      OuterJoinableAssociation oj;
      for(OuterJoinableAssociation last = null; iter.hasNext(); last = oj) {
         oj = (OuterJoinableAssociation)iter.next();
         if (last != null && last.isManyToManyWith(oj)) {
            oj.addManyToManyJoin(outerjoin, (QueryableCollection)last.getJoinable());
         } else {
            oj.addJoins(outerjoin);
         }
      }

      OuterJoinableAssociation var6 = null;
      return outerjoin;
   }

   protected static final int countEntityPersisters(List associations) throws MappingException {
      int result = 0;

      for(OuterJoinableAssociation oj : associations) {
         if (oj.getJoinable().consumesEntityAlias()) {
            ++result;
         }
      }

      return result;
   }

   protected static final int countCollectionPersisters(List associations) throws MappingException {
      int result = 0;

      for(OuterJoinableAssociation oj : associations) {
         if (oj.getJoinType() == JoinType.LEFT_OUTER_JOIN && oj.getJoinable().isCollection() && !oj.hasRestriction()) {
            ++result;
         }
      }

      return result;
   }

   protected static final String orderBy(List associations) throws MappingException {
      StringBuilder buf = new StringBuilder();
      Iterator iter = associations.iterator();

      OuterJoinableAssociation oj;
      for(OuterJoinableAssociation last = null; iter.hasNext(); last = oj) {
         oj = (OuterJoinableAssociation)iter.next();
         if (oj.getJoinType() == JoinType.LEFT_OUTER_JOIN) {
            if (oj.getJoinable().isCollection()) {
               QueryableCollection queryableCollection = (QueryableCollection)oj.getJoinable();
               if (queryableCollection.hasOrdering()) {
                  String orderByString = queryableCollection.getSQLOrderByString(oj.getRHSAlias());
                  buf.append(orderByString).append(", ");
               }
            } else if (last != null && last.getJoinable().isCollection()) {
               QueryableCollection queryableCollection = (QueryableCollection)last.getJoinable();
               if (queryableCollection.isManyToMany() && last.isManyToManyWith(oj) && queryableCollection.hasManyToManyOrdering()) {
                  String orderByString = queryableCollection.getManyToManyOrderByString(oj.getRHSAlias());
                  buf.append(orderByString).append(", ");
               }
            }
         }
      }

      if (buf.length() > 0) {
         buf.setLength(buf.length() - 2);
      }

      return buf.toString();
   }

   protected StringBuilder whereString(String alias, String[] columnNames, int batchSize) {
      if (columnNames.length == 1) {
         InFragment in = (new InFragment()).setColumn(alias, columnNames[0]);

         for(int i = 0; i < batchSize; ++i) {
            in.addValue("?");
         }

         return new StringBuilder(in.toFragmentString());
      } else {
         ConditionFragment byId = (new ConditionFragment()).setTableAlias(alias).setCondition(columnNames, "?");
         StringBuilder whereString = new StringBuilder();
         if (batchSize == 1) {
            whereString.append(byId.toFragmentString());
         } else {
            whereString.append('(');
            DisjunctionFragment df = new DisjunctionFragment();

            for(int i = 0; i < batchSize; ++i) {
               df.addCondition(byId);
            }

            whereString.append(df.toFragmentString());
            whereString.append(')');
         }

         return whereString;
      }
   }

   protected void initPersisters(List associations, LockMode lockMode) throws MappingException {
      this.initPersisters(associations, new LockOptions(lockMode));
   }

   protected void initPersisters(List associations, LockOptions lockOptions) throws MappingException {
      this.initPersisters(associations, lockOptions, JoinWalker.AssociationInitCallback.NO_CALLBACK);
   }

   protected void initPersisters(List associations, LockOptions lockOptions, AssociationInitCallback callback) throws MappingException {
      int joins = countEntityPersisters(associations);
      int collections = countCollectionPersisters(associations);
      this.collectionOwners = collections == 0 ? null : new int[collections];
      this.collectionPersisters = collections == 0 ? null : new CollectionPersister[collections];
      this.collectionSuffixes = BasicLoader.generateSuffixes(joins + 1, collections);
      this.lockOptions = lockOptions;
      this.persisters = new Loadable[joins];
      this.aliases = new String[joins];
      this.owners = new int[joins];
      this.ownerAssociationTypes = new EntityType[joins];
      this.lockModeArray = ArrayHelper.fillArray(lockOptions.getLockMode(), joins);
      int i = 0;
      int j = 0;

      for(OuterJoinableAssociation oj : associations) {
         if (!oj.isCollection()) {
            this.persisters[i] = (Loadable)oj.getJoinable();
            this.aliases[i] = oj.getRHSAlias();
            this.owners[i] = oj.getOwner(associations);
            this.ownerAssociationTypes[i] = (EntityType)oj.getJoinableType();
            callback.associationProcessed(oj, i);
            ++i;
         } else {
            QueryableCollection collPersister = (QueryableCollection)oj.getJoinable();
            if (oj.getJoinType() == JoinType.LEFT_OUTER_JOIN && !oj.hasRestriction()) {
               this.collectionPersisters[j] = collPersister;
               this.collectionOwners[j] = oj.getOwner(associations);
               ++j;
            }

            if (collPersister.isOneToMany()) {
               this.persisters[i] = (Loadable)collPersister.getElementPersister();
               this.aliases[i] = oj.getRHSAlias();
               callback.associationProcessed(oj, i);
               ++i;
            }
         }
      }

      if (ArrayHelper.isAllNegative(this.owners)) {
         this.owners = null;
      }

      if (this.collectionOwners != null && ArrayHelper.isAllNegative(this.collectionOwners)) {
         this.collectionOwners = null;
      }

   }

   protected final String selectString(List associations) throws MappingException {
      if (associations.size() == 0) {
         return "";
      } else {
         StringBuilder buf = new StringBuilder(associations.size() * 100);
         int entityAliasCount = 0;
         int collectionAliasCount = 0;

         for(int i = 0; i < associations.size(); ++i) {
            OuterJoinableAssociation join = (OuterJoinableAssociation)associations.get(i);
            OuterJoinableAssociation next = i == associations.size() - 1 ? null : (OuterJoinableAssociation)associations.get(i + 1);
            Joinable joinable = join.getJoinable();
            String entitySuffix = this.suffixes != null && entityAliasCount < this.suffixes.length ? this.suffixes[entityAliasCount] : null;
            String collectionSuffix = this.collectionSuffixes != null && collectionAliasCount < this.collectionSuffixes.length ? this.collectionSuffixes[collectionAliasCount] : null;
            String selectFragment = joinable.selectFragment(next == null ? null : next.getJoinable(), next == null ? null : next.getRHSAlias(), join.getRHSAlias(), entitySuffix, collectionSuffix, join.getJoinType() == JoinType.LEFT_OUTER_JOIN);
            if (selectFragment.trim().length() > 0) {
               buf.append(", ").append(selectFragment);
            }

            if (joinable.consumesEntityAlias()) {
               ++entityAliasCount;
            }

            if (joinable.consumesCollectionAlias() && join.getJoinType() == JoinType.LEFT_OUTER_JOIN) {
               ++collectionAliasCount;
            }
         }

         return buf.toString();
      }
   }

   private static final class AssociationKey {
      private String[] columns;
      private String table;

      private AssociationKey(String[] columns, String table) {
         this.columns = columns;
         this.table = table;
      }

      public boolean equals(Object other) {
         AssociationKey that = (AssociationKey)other;
         return that.table.equals(this.table) && Arrays.equals(this.columns, that.columns);
      }

      public int hashCode() {
         return this.table.hashCode();
      }
   }

   protected interface AssociationInitCallback {
      AssociationInitCallback NO_CALLBACK = new AssociationInitCallback() {
         public void associationProcessed(OuterJoinableAssociation oja, int position) {
         }
      };

      void associationProcessed(OuterJoinableAssociation var1, int var2);
   }
}
