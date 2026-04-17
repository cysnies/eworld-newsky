package org.hibernate.loader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.hibernate.FetchMode;
import org.hibernate.LockOptions;
import org.hibernate.MappingException;
import org.hibernate.engine.profile.Fetch;
import org.hibernate.engine.profile.FetchProfile;
import org.hibernate.engine.spi.CascadeStyle;
import org.hibernate.engine.spi.LoadQueryInfluencers;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.persister.entity.Loadable;
import org.hibernate.persister.entity.OuterJoinLoadable;
import org.hibernate.sql.JoinFragment;
import org.hibernate.sql.Select;
import org.hibernate.type.AssociationType;

public abstract class AbstractEntityJoinWalker extends JoinWalker {
   private final OuterJoinLoadable persister;
   private final String alias;

   public AbstractEntityJoinWalker(OuterJoinLoadable persister, SessionFactoryImplementor factory, LoadQueryInfluencers loadQueryInfluencers) {
      this(persister, factory, loadQueryInfluencers, (String)null);
   }

   public AbstractEntityJoinWalker(OuterJoinLoadable persister, SessionFactoryImplementor factory, LoadQueryInfluencers loadQueryInfluencers, String alias) {
      super(factory, loadQueryInfluencers);
      this.persister = persister;
      this.alias = alias == null ? this.generateRootAlias(persister.getEntityName()) : alias;
   }

   protected final void initAll(String whereString, String orderByString, LockOptions lockOptions) throws MappingException {
      this.initAll(whereString, orderByString, lockOptions, JoinWalker.AssociationInitCallback.NO_CALLBACK);
   }

   protected final void initAll(String whereString, String orderByString, LockOptions lockOptions, JoinWalker.AssociationInitCallback callback) throws MappingException {
      this.walkEntityTree(this.persister, this.getAlias());
      List allAssociations = new ArrayList();
      allAssociations.addAll(this.associations);
      allAssociations.add(OuterJoinableAssociation.createRoot(this.persister.getEntityType(), this.alias, this.getFactory()));
      this.initPersisters(allAssociations, lockOptions, callback);
      this.initStatementString(whereString, orderByString, lockOptions);
   }

   protected final void initProjection(String projectionString, String whereString, String orderByString, String groupByString, LockOptions lockOptions) throws MappingException {
      this.walkEntityTree(this.persister, this.getAlias());
      this.persisters = new Loadable[0];
      this.initStatementString(projectionString, whereString, orderByString, groupByString, lockOptions);
   }

   private void initStatementString(String condition, String orderBy, LockOptions lockOptions) throws MappingException {
      this.initStatementString((String)null, condition, orderBy, "", lockOptions);
   }

   private void initStatementString(String projection, String condition, String orderBy, String groupBy, LockOptions lockOptions) throws MappingException {
      int joins = countEntityPersisters(this.associations);
      this.suffixes = BasicLoader.generateSuffixes(joins + 1);
      JoinFragment ojf = this.mergeOuterJoins(this.associations);
      Select select = (new Select(this.getDialect())).setLockOptions(lockOptions).setSelectClause(projection == null ? this.persister.selectFragment(this.alias, this.suffixes[joins]) + this.selectString(this.associations) : projection).setFromClause(this.getDialect().appendLockHint(lockOptions, this.persister.fromTableFragment(this.alias)) + this.persister.fromJoinFragment(this.alias, true, true)).setWhereClause(condition).setOuterJoins(ojf.toFromFragmentString(), ojf.toWhereFragmentString() + this.getWhereFragment()).setOrderByClause(this.orderBy(this.associations, orderBy)).setGroupByClause(groupBy);
      if (this.getFactory().getSettings().isCommentsEnabled()) {
         select.setComment(this.getComment());
      }

      this.sql = select.toStatementString();
   }

   protected String getWhereFragment() throws MappingException {
      return this.persister.whereJoinFragment(this.alias, true, true);
   }

   protected boolean isJoinedFetchEnabled(AssociationType type, FetchMode config, CascadeStyle cascadeStyle) {
      return this.isJoinedFetchEnabledInMapping(config, type);
   }

   protected final boolean isJoinFetchEnabledByProfile(OuterJoinLoadable persister, PropertyPath path, int propertyNumber) {
      if (!this.getLoadQueryInfluencers().hasEnabledFetchProfiles()) {
         return false;
      } else {
         String fullPath = path.getFullPath();
         String rootPropertyName = persister.getSubclassPropertyName(propertyNumber);
         int pos = fullPath.lastIndexOf(rootPropertyName);
         String relativePropertyPath = pos >= 0 ? fullPath.substring(pos) : rootPropertyName;
         String fetchRole = persister.getEntityName() + "." + relativePropertyPath;

         for(String profileName : this.getLoadQueryInfluencers().getEnabledFetchProfileNames()) {
            FetchProfile profile = this.getFactory().getFetchProfile(profileName);
            Fetch fetch = profile.getFetchByRole(fetchRole);
            if (fetch != null && Fetch.Style.JOIN == fetch.getStyle()) {
               return true;
            }
         }

         return false;
      }
   }

   public abstract String getComment();

   protected boolean isDuplicateAssociation(String foreignKeyTable, String[] foreignKeyColumns) {
      boolean isSameJoin = this.persister.getTableName().equals(foreignKeyTable) && Arrays.equals(foreignKeyColumns, this.persister.getKeyColumnNames());
      return isSameJoin || super.isDuplicateAssociation(foreignKeyTable, foreignKeyColumns);
   }

   protected final Loadable getPersister() {
      return this.persister;
   }

   protected final String getAlias() {
      return this.alias;
   }

   public String toString() {
      return this.getClass().getName() + '(' + this.getPersister().getEntityName() + ')';
   }
}
