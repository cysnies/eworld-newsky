package org.hibernate.loader.collection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.hibernate.FetchMode;
import org.hibernate.LockMode;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.CascadeStyle;
import org.hibernate.engine.spi.LoadQueryInfluencers;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.loader.BasicLoader;
import org.hibernate.loader.OuterJoinableAssociation;
import org.hibernate.loader.PropertyPath;
import org.hibernate.persister.collection.QueryableCollection;
import org.hibernate.persister.entity.OuterJoinLoadable;
import org.hibernate.sql.JoinFragment;
import org.hibernate.sql.JoinType;
import org.hibernate.sql.Select;
import org.hibernate.type.AssociationType;

public class BasicCollectionJoinWalker extends CollectionJoinWalker {
   private final QueryableCollection collectionPersister;

   public BasicCollectionJoinWalker(QueryableCollection collectionPersister, int batchSize, String subquery, SessionFactoryImplementor factory, LoadQueryInfluencers loadQueryInfluencers) throws MappingException {
      super(factory, loadQueryInfluencers);
      this.collectionPersister = collectionPersister;
      String alias = this.generateRootAlias(collectionPersister.getRole());
      this.walkCollectionTree(collectionPersister, alias);
      List allAssociations = new ArrayList();
      allAssociations.addAll(this.associations);
      allAssociations.add(OuterJoinableAssociation.createRoot(collectionPersister.getCollectionType(), alias, this.getFactory()));
      this.initPersisters(allAssociations, LockMode.NONE);
      this.initStatementString(alias, batchSize, subquery);
   }

   private void initStatementString(String alias, int batchSize, String subquery) throws MappingException {
      int joins = countEntityPersisters(this.associations);
      int collectionJoins = countCollectionPersisters(this.associations) + 1;
      this.suffixes = BasicLoader.generateSuffixes(joins);
      this.collectionSuffixes = BasicLoader.generateSuffixes(joins, collectionJoins);
      StringBuilder whereString = this.whereString(alias, this.collectionPersister.getKeyColumnNames(), subquery, batchSize);
      String manyToManyOrderBy = "";
      String filter = this.collectionPersister.filterFragment(alias, this.getLoadQueryInfluencers().getEnabledFilters());
      if (this.collectionPersister.isManyToMany()) {
         Iterator itr = this.associations.iterator();
         AssociationType associationType = (AssociationType)this.collectionPersister.getElementType();

         while(itr.hasNext()) {
            OuterJoinableAssociation oja = (OuterJoinableAssociation)itr.next();
            if (oja.getJoinableType() == associationType) {
               filter = filter + this.collectionPersister.getManyToManyFilterFragment(oja.getRHSAlias(), this.getLoadQueryInfluencers().getEnabledFilters());
               manyToManyOrderBy = manyToManyOrderBy + this.collectionPersister.getManyToManyOrderByString(oja.getRHSAlias());
            }
         }
      }

      whereString.insert(0, StringHelper.moveAndToBeginning(filter));
      JoinFragment ojf = this.mergeOuterJoins(this.associations);
      Select select = (new Select(this.getDialect())).setSelectClause(this.collectionPersister.selectFragment(alias, this.collectionSuffixes[0]) + this.selectString(this.associations)).setFromClause(this.collectionPersister.getTableName(), alias).setWhereClause(whereString.toString()).setOuterJoins(ojf.toFromFragmentString(), ojf.toWhereFragmentString());
      select.setOrderByClause(this.orderBy(this.associations, mergeOrderings(this.collectionPersister.getSQLOrderByString(alias), manyToManyOrderBy)));
      if (this.getFactory().getSettings().isCommentsEnabled()) {
         select.setComment("load collection " + this.collectionPersister.getRole());
      }

      this.sql = select.toStatementString();
   }

   protected JoinType getJoinType(OuterJoinLoadable persister, PropertyPath path, int propertyNumber, AssociationType associationType, FetchMode metadataFetchMode, CascadeStyle metadataCascadeStyle, String lhsTable, String[] lhsColumns, boolean nullable, int currentDepth) throws MappingException {
      JoinType joinType = super.getJoinType(persister, path, propertyNumber, associationType, metadataFetchMode, metadataCascadeStyle, lhsTable, lhsColumns, nullable, currentDepth);
      if (joinType == JoinType.LEFT_OUTER_JOIN && path.isRoot()) {
         joinType = JoinType.INNER_JOIN;
      }

      return joinType;
   }

   public String toString() {
      return this.getClass().getName() + '(' + this.collectionPersister.getRole() + ')';
   }
}
