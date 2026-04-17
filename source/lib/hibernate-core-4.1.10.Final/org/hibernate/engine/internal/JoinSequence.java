package org.hibernate.engine.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.persister.collection.QueryableCollection;
import org.hibernate.persister.entity.Joinable;
import org.hibernate.sql.JoinFragment;
import org.hibernate.sql.JoinType;
import org.hibernate.sql.QueryJoinFragment;
import org.hibernate.type.AssociationType;

public class JoinSequence {
   private final SessionFactoryImplementor factory;
   private final List joins = new ArrayList();
   private boolean useThetaStyle = false;
   private final StringBuilder conditions = new StringBuilder();
   private String rootAlias;
   private Joinable rootJoinable;
   private Selector selector;
   private JoinSequence next;
   private boolean isFromPart = false;

   public String toString() {
      StringBuilder buf = new StringBuilder();
      buf.append("JoinSequence{");
      if (this.rootJoinable != null) {
         buf.append(this.rootJoinable).append('[').append(this.rootAlias).append(']');
      }

      for(int i = 0; i < this.joins.size(); ++i) {
         buf.append("->").append(this.joins.get(i));
      }

      return buf.append('}').toString();
   }

   public JoinSequence(SessionFactoryImplementor factory) {
      this.factory = factory;
   }

   public JoinSequence getFromPart() {
      JoinSequence fromPart = new JoinSequence(this.factory);
      fromPart.joins.addAll(this.joins);
      fromPart.useThetaStyle = this.useThetaStyle;
      fromPart.rootAlias = this.rootAlias;
      fromPart.rootJoinable = this.rootJoinable;
      fromPart.selector = this.selector;
      fromPart.next = this.next == null ? null : this.next.getFromPart();
      fromPart.isFromPart = true;
      return fromPart;
   }

   public JoinSequence copy() {
      JoinSequence copy = new JoinSequence(this.factory);
      copy.joins.addAll(this.joins);
      copy.useThetaStyle = this.useThetaStyle;
      copy.rootAlias = this.rootAlias;
      copy.rootJoinable = this.rootJoinable;
      copy.selector = this.selector;
      copy.next = this.next == null ? null : this.next.copy();
      copy.isFromPart = this.isFromPart;
      copy.conditions.append(this.conditions.toString());
      return copy;
   }

   public JoinSequence addJoin(AssociationType associationType, String alias, JoinType joinType, String[] referencingKey) throws MappingException {
      this.joins.add(new Join(associationType, alias, joinType, referencingKey));
      return this;
   }

   public JoinFragment toJoinFragment() throws MappingException {
      return this.toJoinFragment(java.util.Collections.EMPTY_MAP, true);
   }

   public JoinFragment toJoinFragment(Map enabledFilters, boolean includeExtraJoins) throws MappingException {
      return this.toJoinFragment(enabledFilters, includeExtraJoins, (String)null, (String)null);
   }

   public JoinFragment toJoinFragment(Map enabledFilters, boolean includeExtraJoins, String withClauseFragment, String withClauseJoinAlias) throws MappingException {
      QueryJoinFragment joinFragment = new QueryJoinFragment(this.factory.getDialect(), this.useThetaStyle);
      if (this.rootJoinable != null) {
         joinFragment.addCrossJoin(this.rootJoinable.getTableName(), this.rootAlias);
         String filterCondition = this.rootJoinable.filterFragment(this.rootAlias, enabledFilters);
         joinFragment.setHasFilterCondition(joinFragment.addCondition(filterCondition));
         if (includeExtraJoins) {
            this.addExtraJoins(joinFragment, this.rootAlias, this.rootJoinable, true);
         }
      }

      Joinable last = this.rootJoinable;

      for(Join join : this.joins) {
         String on = join.getAssociationType().getOnCondition(join.getAlias(), this.factory, enabledFilters);
         String condition = null;
         if (last != null && this.isManyToManyRoot(last) && ((QueryableCollection)last).getElementType() == join.getAssociationType()) {
            String manyToManyFilter = ((QueryableCollection)last).getManyToManyFilterFragment(join.getAlias(), enabledFilters);
            condition = "".equals(manyToManyFilter) ? on : ("".equals(on) ? manyToManyFilter : on + " and " + manyToManyFilter);
         } else {
            condition = on;
         }

         if (withClauseFragment != null && join.getAlias().equals(withClauseJoinAlias)) {
            condition = condition + " and " + withClauseFragment;
         }

         joinFragment.addJoin(join.getJoinable().getTableName(), join.getAlias(), join.getLHSColumns(), JoinHelper.getRHSColumnNames(join.getAssociationType(), this.factory), join.joinType, condition);
         if (includeExtraJoins) {
            this.addExtraJoins(joinFragment, join.getAlias(), join.getJoinable(), join.joinType == JoinType.INNER_JOIN);
         }

         last = join.getJoinable();
      }

      if (this.next != null) {
         joinFragment.addFragment(this.next.toJoinFragment(enabledFilters, includeExtraJoins));
      }

      joinFragment.addCondition(this.conditions.toString());
      if (this.isFromPart) {
         joinFragment.clearWherePart();
      }

      return joinFragment;
   }

   private boolean isManyToManyRoot(Joinable joinable) {
      if (joinable != null && joinable.isCollection()) {
         QueryableCollection persister = (QueryableCollection)joinable;
         return persister.isManyToMany();
      } else {
         return false;
      }
   }

   private boolean isIncluded(String alias) {
      return this.selector != null && this.selector.includeSubclasses(alias);
   }

   private void addExtraJoins(JoinFragment joinFragment, String alias, Joinable joinable, boolean innerJoin) {
      boolean include = this.isIncluded(alias);
      joinFragment.addJoins(joinable.fromJoinFragment(alias, innerJoin, include), joinable.whereJoinFragment(alias, innerJoin, include));
   }

   public JoinSequence addCondition(String condition) {
      if (condition.trim().length() != 0) {
         if (!condition.startsWith(" and ")) {
            this.conditions.append(" and ");
         }

         this.conditions.append(condition);
      }

      return this;
   }

   public JoinSequence addCondition(String alias, String[] columns, String condition) {
      for(int i = 0; i < columns.length; ++i) {
         this.conditions.append(" and ").append(alias).append('.').append(columns[i]).append(condition);
      }

      return this;
   }

   public JoinSequence setRoot(Joinable joinable, String alias) {
      this.rootAlias = alias;
      this.rootJoinable = joinable;
      return this;
   }

   public JoinSequence setNext(JoinSequence next) {
      this.next = next;
      return this;
   }

   public JoinSequence setSelector(Selector s) {
      this.selector = s;
      return this;
   }

   public JoinSequence setUseThetaStyle(boolean useThetaStyle) {
      this.useThetaStyle = useThetaStyle;
      return this;
   }

   public boolean isThetaStyle() {
      return this.useThetaStyle;
   }

   public int getJoinCount() {
      return this.joins.size();
   }

   public Iterator iterateJoins() {
      return this.joins.iterator();
   }

   public Join getFirstJoin() {
      return (Join)this.joins.get(0);
   }

   public final class Join {
      private final AssociationType associationType;
      private final Joinable joinable;
      private final JoinType joinType;
      private final String alias;
      private final String[] lhsColumns;

      Join(AssociationType associationType, String alias, JoinType joinType, String[] lhsColumns) throws MappingException {
         this.associationType = associationType;
         this.joinable = associationType.getAssociatedJoinable(JoinSequence.this.factory);
         this.alias = alias;
         this.joinType = joinType;
         this.lhsColumns = lhsColumns;
      }

      public String getAlias() {
         return this.alias;
      }

      public AssociationType getAssociationType() {
         return this.associationType;
      }

      public Joinable getJoinable() {
         return this.joinable;
      }

      public JoinType getJoinType() {
         return this.joinType;
      }

      public String[] getLHSColumns() {
         return this.lhsColumns;
      }

      public String toString() {
         return this.joinable.toString() + '[' + this.alias + ']';
      }
   }

   public interface Selector {
      boolean includeSubclasses(String var1);
   }
}
