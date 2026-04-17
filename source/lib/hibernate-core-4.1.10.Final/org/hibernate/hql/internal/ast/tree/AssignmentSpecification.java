package org.hibernate.hql.internal.ast.tree;

import antlr.collections.AST;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.hibernate.QueryException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.hql.internal.ast.SqlGenerator;
import org.hibernate.hql.internal.ast.util.ASTUtil;
import org.hibernate.param.ParameterSpecification;
import org.hibernate.persister.entity.Queryable;
import org.hibernate.persister.entity.UnionSubclassEntityPersister;

public class AssignmentSpecification {
   private final Set tableNames;
   private final ParameterSpecification[] hqlParameters;
   private final AST eq;
   private final SessionFactoryImplementor factory;
   private String sqlAssignmentString;

   public AssignmentSpecification(AST eq, Queryable persister) {
      if (eq.getType() != 102) {
         throw new QueryException("assignment in set-clause not associated with equals");
      } else {
         this.eq = eq;
         this.factory = persister.getFactory();
         DotNode lhs = (DotNode)eq.getFirstChild();
         SqlNode rhs = (SqlNode)lhs.getNextSibling();
         this.validateLhs(lhs);
         String propertyPath = lhs.getPropertyPath();
         Set temp = new HashSet();
         if (persister instanceof UnionSubclassEntityPersister) {
            UnionSubclassEntityPersister usep = (UnionSubclassEntityPersister)persister;
            String[] tables = persister.getConstraintOrderedTableNameClosure();
            int size = tables.length;

            for(int i = 0; i < size; ++i) {
               temp.add(tables[i]);
            }
         } else {
            temp.add(persister.getSubclassTableName(persister.getSubclassPropertyTableNumber(propertyPath)));
         }

         this.tableNames = Collections.unmodifiableSet(temp);
         if (rhs == null) {
            this.hqlParameters = new ParameterSpecification[0];
         } else if (isParam(rhs)) {
            this.hqlParameters = new ParameterSpecification[]{((ParameterNode)rhs).getHqlParameterSpecification()};
         } else {
            List parameterList = ASTUtil.collectChildren(rhs, new ASTUtil.IncludePredicate() {
               public boolean include(AST node) {
                  return AssignmentSpecification.isParam(node);
               }
            });
            this.hqlParameters = new ParameterSpecification[parameterList.size()];
            Iterator itr = parameterList.iterator();

            for(int i = 0; itr.hasNext(); this.hqlParameters[i++] = ((ParameterNode)itr.next()).getHqlParameterSpecification()) {
            }
         }

      }
   }

   public boolean affectsTable(String tableName) {
      return this.tableNames.contains(tableName);
   }

   public ParameterSpecification[] getParameters() {
      return this.hqlParameters;
   }

   public String getSqlAssignmentFragment() {
      if (this.sqlAssignmentString == null) {
         try {
            SqlGenerator sqlGenerator = new SqlGenerator(this.factory);
            sqlGenerator.comparisonExpr(this.eq, false);
            this.sqlAssignmentString = sqlGenerator.getSQL();
         } catch (Throwable var2) {
            throw new QueryException("cannot interpret set-clause assignment");
         }
      }

      return this.sqlAssignmentString;
   }

   private static boolean isParam(AST node) {
      return node.getType() == 123 || node.getType() == 148;
   }

   private void validateLhs(FromReferenceNode lhs) {
      if (!lhs.isResolved()) {
         throw new UnsupportedOperationException("cannot validate assignablity of unresolved node");
      } else if (lhs.getDataType().isCollectionType()) {
         throw new QueryException("collections not assignable in update statements");
      } else if (lhs.getDataType().isComponentType()) {
         throw new QueryException("Components currently not assignable in update statements");
      } else {
         if (lhs.getDataType().isEntityType()) {
         }

         if (lhs.getImpliedJoin() != null || lhs.getFromElement().isImplied()) {
            throw new QueryException("Implied join paths are not assignable in update statements");
         }
      }
   }
}
