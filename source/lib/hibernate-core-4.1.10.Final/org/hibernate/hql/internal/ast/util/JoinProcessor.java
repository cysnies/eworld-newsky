package org.hibernate.hql.internal.ast.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.StringTokenizer;
import org.hibernate.AssertionFailure;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.internal.JoinSequence;
import org.hibernate.engine.spi.LoadQueryInfluencers;
import org.hibernate.hql.internal.antlr.SqlTokenTypes;
import org.hibernate.hql.internal.ast.HqlSqlWalker;
import org.hibernate.hql.internal.ast.tree.DotNode;
import org.hibernate.hql.internal.ast.tree.FromClause;
import org.hibernate.hql.internal.ast.tree.FromElement;
import org.hibernate.hql.internal.ast.tree.ParameterContainer;
import org.hibernate.hql.internal.ast.tree.QueryNode;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.FilterImpl;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.param.DynamicFilterParameterSpecification;
import org.hibernate.sql.JoinFragment;
import org.hibernate.sql.JoinType;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

public class JoinProcessor implements SqlTokenTypes {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, JoinProcessor.class.getName());
   private final HqlSqlWalker walker;
   private final SyntheticAndFactory syntheticAndFactory;

   public JoinProcessor(HqlSqlWalker walker) {
      this.walker = walker;
      this.syntheticAndFactory = new SyntheticAndFactory(walker);
   }

   public static JoinType toHibernateJoinType(int astJoinType) {
      switch (astJoinType) {
         case 28:
            return JoinType.INNER_JOIN;
         case 138:
            return JoinType.LEFT_OUTER_JOIN;
         case 139:
            return JoinType.RIGHT_OUTER_JOIN;
         default:
            throw new AssertionFailure("undefined join type " + astJoinType);
      }
   }

   public void processJoins(QueryNode query) {
      final FromClause fromClause = query.getFromClause();
      List fromElements;
      if (DotNode.useThetaStyleImplicitJoins) {
         fromElements = new ArrayList();
         ListIterator liter = fromClause.getFromElements().listIterator(fromClause.getFromElements().size());

         while(liter.hasPrevious()) {
            fromElements.add(liter.previous());
         }
      } else {
         fromElements = fromClause.getFromElements();
      }

      for(final FromElement fromElement : fromElements) {
         JoinSequence join = fromElement.getJoinSequence();
         join.setSelector(new JoinSequence.Selector() {
            public boolean includeSubclasses(String alias) {
               boolean containsTableAlias = fromClause.containsTableAlias(alias);
               if (fromElement.isDereferencedBySubclassProperty()) {
                  JoinProcessor.LOG.tracev("Forcing inclusion of extra joins [alias={0}, containsTableAlias={1}]", alias, containsTableAlias);
                  return true;
               } else {
                  boolean shallowQuery = JoinProcessor.this.walker.isShallowQuery();
                  boolean includeSubclasses = fromElement.isIncludeSubclasses();
                  boolean subQuery = fromClause.isSubQuery();
                  return includeSubclasses && containsTableAlias && !subQuery && !shallowQuery;
               }
            }
         });
         this.addJoinNodes(query, join, fromElement);
      }

   }

   private void addJoinNodes(QueryNode query, JoinSequence join, FromElement fromElement) {
      JoinFragment joinFragment = join.toJoinFragment(this.walker.getEnabledFilters(), fromElement.useFromFragment() || fromElement.isDereferencedBySuperclassOrSubclassProperty(), fromElement.getWithClauseFragment(), fromElement.getWithClauseJoinAlias());
      String frag = joinFragment.toFromFragmentString();
      String whereFrag = joinFragment.toWhereFragmentString();
      if (fromElement.getType() == 136 && (join.isThetaStyle() || StringHelper.isNotEmpty(whereFrag))) {
         fromElement.setType(134);
         fromElement.getJoinSequence().setUseThetaStyle(true);
      }

      if (fromElement.useFromFragment()) {
         String fromFragment = this.processFromFragment(frag, join).trim();
         LOG.debugf("Using FROM fragment [%s]", fromFragment);
         processDynamicFilterParameters(fromFragment, fromElement, this.walker);
      }

      this.syntheticAndFactory.addWhereFragment(joinFragment, whereFrag, query, fromElement, this.walker);
   }

   private String processFromFragment(String frag, JoinSequence join) {
      String fromFragment = frag.trim();
      if (fromFragment.startsWith(", ")) {
         fromFragment = fromFragment.substring(2);
      }

      return fromFragment;
   }

   public static void processDynamicFilterParameters(String sqlFragment, ParameterContainer container, HqlSqlWalker walker) {
      if (!walker.getEnabledFilters().isEmpty() || hasDynamicFilterParam(sqlFragment) || hasCollectionFilterParam(sqlFragment)) {
         Dialect dialect = walker.getSessionFactoryHelper().getFactory().getDialect();
         String symbols = " \n\r\f\t,()=<>&|+-=/*'^![]#~\\" + dialect.openQuote() + dialect.closeQuote();
         StringTokenizer tokens = new StringTokenizer(sqlFragment, symbols, true);
         StringBuilder result = new StringBuilder();

         while(tokens.hasMoreTokens()) {
            String token = tokens.nextToken();
            if (token.startsWith(":")) {
               String filterParameterName = token.substring(1);
               String[] parts = LoadQueryInfluencers.parseFilterParameterName(filterParameterName);
               FilterImpl filter = (FilterImpl)walker.getEnabledFilters().get(parts[0]);
               Object value = filter.getParameter(parts[1]);
               Type type = filter.getFilterDefinition().getParameterType(parts[1]);
               String typeBindFragment = StringHelper.join(",", ArrayHelper.fillArray("?", type.getColumnSpan(walker.getSessionFactoryHelper().getFactory())));
               String bindFragment = value != null && Collection.class.isInstance(value) ? StringHelper.join(",", ArrayHelper.fillArray(typeBindFragment, ((Collection)value).size())) : typeBindFragment;
               result.append(bindFragment);
               container.addEmbeddedParameter(new DynamicFilterParameterSpecification(parts[0], parts[1], type));
            } else {
               result.append(token);
            }
         }

         container.setText(result.toString());
      }
   }

   private static boolean hasDynamicFilterParam(String sqlFragment) {
      return sqlFragment.indexOf(":") < 0;
   }

   private static boolean hasCollectionFilterParam(String sqlFragment) {
      return sqlFragment.indexOf("?") < 0;
   }
}
