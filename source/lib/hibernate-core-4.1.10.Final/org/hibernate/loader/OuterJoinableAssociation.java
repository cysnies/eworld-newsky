package org.hibernate.loader;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.hibernate.MappingException;
import org.hibernate.engine.internal.JoinHelper;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.persister.collection.QueryableCollection;
import org.hibernate.persister.entity.Joinable;
import org.hibernate.sql.JoinFragment;
import org.hibernate.sql.JoinType;
import org.hibernate.type.AssociationType;
import org.hibernate.type.EntityType;

public final class OuterJoinableAssociation {
   private final PropertyPath propertyPath;
   private final AssociationType joinableType;
   private final Joinable joinable;
   private final String lhsAlias;
   private final String[] lhsColumns;
   private final String rhsAlias;
   private final String[] rhsColumns;
   private final JoinType joinType;
   private final String on;
   private final Map enabledFilters;
   private final boolean hasRestriction;

   public static OuterJoinableAssociation createRoot(AssociationType joinableType, String alias, SessionFactoryImplementor factory) {
      return new OuterJoinableAssociation(new PropertyPath(), joinableType, (String)null, (String[])null, alias, JoinType.LEFT_OUTER_JOIN, (String)null, false, factory, Collections.EMPTY_MAP);
   }

   public OuterJoinableAssociation(PropertyPath propertyPath, AssociationType joinableType, String lhsAlias, String[] lhsColumns, String rhsAlias, JoinType joinType, String withClause, boolean hasRestriction, SessionFactoryImplementor factory, Map enabledFilters) throws MappingException {
      this.propertyPath = propertyPath;
      this.joinableType = joinableType;
      this.lhsAlias = lhsAlias;
      this.lhsColumns = lhsColumns;
      this.rhsAlias = rhsAlias;
      this.joinType = joinType;
      this.joinable = joinableType.getAssociatedJoinable(factory);
      this.rhsColumns = JoinHelper.getRHSColumnNames(joinableType, factory);
      this.on = joinableType.getOnCondition(rhsAlias, factory, enabledFilters) + (withClause != null && withClause.trim().length() != 0 ? " and ( " + withClause + " )" : "");
      this.hasRestriction = hasRestriction;
      this.enabledFilters = enabledFilters;
   }

   public PropertyPath getPropertyPath() {
      return this.propertyPath;
   }

   public JoinType getJoinType() {
      return this.joinType;
   }

   public String getLhsAlias() {
      return this.lhsAlias;
   }

   public String getRHSAlias() {
      return this.rhsAlias;
   }

   public String getRhsAlias() {
      return this.rhsAlias;
   }

   private boolean isOneToOne() {
      if (this.joinableType.isEntityType()) {
         EntityType etype = (EntityType)this.joinableType;
         return etype.isOneToOne();
      } else {
         return false;
      }
   }

   public AssociationType getJoinableType() {
      return this.joinableType;
   }

   public String getRHSUniqueKeyName() {
      return this.joinableType.getRHSUniqueKeyPropertyName();
   }

   public boolean isCollection() {
      return this.joinableType.isCollectionType();
   }

   public Joinable getJoinable() {
      return this.joinable;
   }

   public boolean hasRestriction() {
      return this.hasRestriction;
   }

   public int getOwner(List associations) {
      return !this.isOneToOne() && !this.isCollection() ? -1 : getPosition(this.lhsAlias, associations);
   }

   private static int getPosition(String lhsAlias, List associations) {
      int result = 0;

      for(int i = 0; i < associations.size(); ++i) {
         OuterJoinableAssociation oj = (OuterJoinableAssociation)associations.get(i);
         if (oj.getJoinable().consumesEntityAlias()) {
            if (oj.rhsAlias.equals(lhsAlias)) {
               return result;
            }

            ++result;
         }
      }

      return -1;
   }

   public void addJoins(JoinFragment outerjoin) throws MappingException {
      outerjoin.addJoin(this.joinable.getTableName(), this.rhsAlias, this.lhsColumns, this.rhsColumns, this.joinType, this.on);
      outerjoin.addJoins(this.joinable.fromJoinFragment(this.rhsAlias, false, true), this.joinable.whereJoinFragment(this.rhsAlias, false, true));
   }

   public void validateJoin(String path) throws MappingException {
      if (this.rhsColumns == null || this.lhsColumns == null || this.lhsColumns.length != this.rhsColumns.length || this.lhsColumns.length == 0) {
         throw new MappingException("invalid join columns for association: " + path);
      }
   }

   public boolean isManyToManyWith(OuterJoinableAssociation other) {
      if (this.joinable.isCollection()) {
         QueryableCollection persister = (QueryableCollection)this.joinable;
         if (persister.isManyToMany()) {
            return persister.getElementType() == other.getJoinableType();
         }
      }

      return false;
   }

   public void addManyToManyJoin(JoinFragment outerjoin, QueryableCollection collection) throws MappingException {
      String manyToManyFilter = collection.getManyToManyFilterFragment(this.rhsAlias, this.enabledFilters);
      String condition = "".equals(manyToManyFilter) ? this.on : ("".equals(this.on) ? manyToManyFilter : this.on + " and " + manyToManyFilter);
      outerjoin.addJoin(this.joinable.getTableName(), this.rhsAlias, this.lhsColumns, this.rhsColumns, this.joinType, condition);
      outerjoin.addJoins(this.joinable.fromJoinFragment(this.rhsAlias, false, true), this.joinable.whereJoinFragment(this.rhsAlias, false, true));
   }
}
