package org.hibernate.loader.criteria;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.LockOptions;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.CascadeStyle;
import org.hibernate.engine.spi.LoadQueryInfluencers;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.CriteriaImpl;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.loader.AbstractEntityJoinWalker;
import org.hibernate.loader.PropertyPath;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.entity.Joinable;
import org.hibernate.persister.entity.OuterJoinLoadable;
import org.hibernate.persister.entity.Queryable;
import org.hibernate.sql.JoinType;
import org.hibernate.type.AssociationType;
import org.hibernate.type.Type;

public class CriteriaJoinWalker extends AbstractEntityJoinWalker {
   private final CriteriaQueryTranslator translator;
   private final Set querySpaces;
   private final Type[] resultTypes;
   private final boolean[] includeInResultRow;
   private final String[] userAliases;
   private final List userAliasList;
   private final List resultTypeList;
   private final List includeInResultRowList;

   public Type[] getResultTypes() {
      return this.resultTypes;
   }

   public String[] getUserAliases() {
      return this.userAliases;
   }

   public boolean[] includeInResultRow() {
      return this.includeInResultRow;
   }

   public CriteriaJoinWalker(OuterJoinLoadable persister, CriteriaQueryTranslator translator, SessionFactoryImplementor factory, CriteriaImpl criteria, String rootEntityName, LoadQueryInfluencers loadQueryInfluencers) {
      this(persister, translator, factory, criteria, rootEntityName, loadQueryInfluencers, (String)null);
   }

   public CriteriaJoinWalker(OuterJoinLoadable persister, CriteriaQueryTranslator translator, SessionFactoryImplementor factory, CriteriaImpl criteria, String rootEntityName, LoadQueryInfluencers loadQueryInfluencers, String alias) {
      super(persister, factory, loadQueryInfluencers, alias);
      this.userAliasList = new ArrayList();
      this.resultTypeList = new ArrayList();
      this.includeInResultRowList = new ArrayList();
      this.translator = translator;
      this.querySpaces = translator.getQuerySpaces();
      if (translator.hasProjection()) {
         this.initProjection(translator.getSelect(), translator.getWhereCondition(), translator.getOrderBy(), translator.getGroupBy(), LockOptions.NONE);
         this.resultTypes = translator.getProjectedTypes();
         this.userAliases = translator.getProjectedAliases();
         this.includeInResultRow = new boolean[this.resultTypes.length];
         Arrays.fill(this.includeInResultRow, true);
      } else {
         this.initAll(translator.getWhereCondition(), translator.getOrderBy(), LockOptions.NONE);
         this.userAliasList.add(criteria.getAlias());
         this.resultTypeList.add(translator.getResultType(criteria));
         this.includeInResultRowList.add(true);
         this.userAliases = ArrayHelper.toStringArray((Collection)this.userAliasList);
         this.resultTypes = ArrayHelper.toTypeArray(this.resultTypeList);
         this.includeInResultRow = ArrayHelper.toBooleanArray(this.includeInResultRowList);
      }

   }

   protected JoinType getJoinType(OuterJoinLoadable persister, PropertyPath path, int propertyNumber, AssociationType associationType, FetchMode metadataFetchMode, CascadeStyle metadataCascadeStyle, String lhsTable, String[] lhsColumns, boolean nullable, int currentDepth) throws MappingException {
      JoinType resolvedJoinType;
      if (this.translator.isJoin(path.getFullPath())) {
         resolvedJoinType = this.translator.getJoinType(path.getFullPath());
      } else if (this.translator.hasProjection()) {
         resolvedJoinType = JoinType.NONE;
      } else {
         FetchMode fetchMode = this.translator.getRootCriteria().getFetchMode(path.getFullPath());
         if (isDefaultFetchMode(fetchMode)) {
            if (persister != null) {
               if (this.isJoinFetchEnabledByProfile(persister, path, propertyNumber)) {
                  if (this.isDuplicateAssociation(lhsTable, lhsColumns, associationType)) {
                     resolvedJoinType = JoinType.NONE;
                  } else if (!this.isTooDeep(currentDepth) && (!associationType.isCollectionType() || !this.isTooManyCollections())) {
                     resolvedJoinType = this.getJoinType(nullable, currentDepth);
                  } else {
                     resolvedJoinType = JoinType.NONE;
                  }
               } else {
                  resolvedJoinType = super.getJoinType(persister, path, propertyNumber, associationType, metadataFetchMode, metadataCascadeStyle, lhsTable, lhsColumns, nullable, currentDepth);
               }
            } else {
               resolvedJoinType = super.getJoinType(associationType, metadataFetchMode, path, lhsTable, lhsColumns, nullable, currentDepth, metadataCascadeStyle);
            }
         } else if (fetchMode == FetchMode.JOIN) {
            this.isDuplicateAssociation(lhsTable, lhsColumns, associationType);
            resolvedJoinType = this.getJoinType(nullable, currentDepth);
         } else {
            resolvedJoinType = JoinType.NONE;
         }
      }

      return resolvedJoinType;
   }

   protected JoinType getJoinType(AssociationType associationType, FetchMode config, PropertyPath path, String lhsTable, String[] lhsColumns, boolean nullable, int currentDepth, CascadeStyle cascadeStyle) throws MappingException {
      return this.getJoinType((OuterJoinLoadable)null, path, -1, associationType, config, cascadeStyle, lhsTable, lhsColumns, nullable, currentDepth);
   }

   private static boolean isDefaultFetchMode(FetchMode fetchMode) {
      return fetchMode == null || fetchMode == FetchMode.DEFAULT;
   }

   protected String getWhereFragment() throws MappingException {
      return super.getWhereFragment() + ((Queryable)this.getPersister()).filterFragment(this.getAlias(), this.getLoadQueryInfluencers().getEnabledFilters());
   }

   protected String generateTableAlias(int n, PropertyPath path, Joinable joinable) {
      boolean checkForSqlAlias = joinable.consumesEntityAlias();
      if (!checkForSqlAlias && joinable.isCollection()) {
         CollectionPersister collectionPersister = (CollectionPersister)joinable;
         Type elementType = collectionPersister.getElementType();
         if (elementType.isComponentType() || !elementType.isEntityType()) {
            checkForSqlAlias = true;
         }
      }

      String sqlAlias = null;
      if (checkForSqlAlias) {
         Criteria subcriteria = this.translator.getCriteria(path.getFullPath());
         sqlAlias = subcriteria == null ? null : this.translator.getSQLAlias(subcriteria);
         if (joinable.consumesEntityAlias() && !this.translator.hasProjection()) {
            this.includeInResultRowList.add(subcriteria != null && subcriteria.getAlias() != null);
            if (sqlAlias != null && subcriteria.getAlias() != null) {
               this.userAliasList.add(subcriteria.getAlias());
               this.resultTypeList.add(this.translator.getResultType(subcriteria));
            }
         }
      }

      if (sqlAlias == null) {
         sqlAlias = super.generateTableAlias(n + this.translator.getSQLAliasCount(), path, joinable);
      }

      return sqlAlias;
   }

   protected String generateRootAlias(String tableName) {
      return "this_";
   }

   public Set getQuerySpaces() {
      return this.querySpaces;
   }

   public String getComment() {
      return "criteria query";
   }

   protected String getWithClause(PropertyPath path) {
      return this.translator.getWithClause(path.getFullPath());
   }

   protected boolean hasRestriction(PropertyPath path) {
      return this.translator.hasRestriction(path.getFullPath());
   }
}
