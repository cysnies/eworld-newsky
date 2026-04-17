package org.hibernate.hql.internal.classic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import org.hibernate.MappingException;
import org.hibernate.QueryException;
import org.hibernate.engine.internal.JoinSequence;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.persister.entity.Queryable;
import org.hibernate.type.EntityType;
import org.hibernate.type.LiteralType;
import org.hibernate.type.Type;

public class WhereParser implements Parser {
   private final PathExpressionParser pathExpressionParser = new PathExpressionParser();
   private static final Set EXPRESSION_TERMINATORS = new HashSet();
   private static final Set EXPRESSION_OPENERS = new HashSet();
   private static final Set BOOLEAN_OPERATORS = new HashSet();
   private static final Map NEGATIONS = new HashMap();
   private boolean betweenSpecialCase;
   private boolean negated;
   private boolean inSubselect;
   private int bracketsSinceSelect;
   private StringBuilder subselect;
   private boolean expectingPathContinuation;
   private int expectingIndex;
   private LinkedList nots;
   private LinkedList joins;
   private LinkedList booleanTests;

   public WhereParser() {
      this.pathExpressionParser.setUseThetaStyleJoin(true);
      this.betweenSpecialCase = false;
      this.negated = false;
      this.inSubselect = false;
      this.bracketsSinceSelect = 0;
      this.expectingPathContinuation = false;
      this.expectingIndex = 0;
      this.nots = new LinkedList();
      this.joins = new LinkedList();
      this.booleanTests = new LinkedList();
   }

   private String getElementName(PathExpressionParser.CollectionElement element, QueryTranslatorImpl q) throws QueryException {
      String name;
      if (element.isOneToMany) {
         name = element.alias;
      } else {
         Type type = element.elementType;
         if (!type.isEntityType()) {
            throw new QueryException("illegally dereferenced collection element");
         }

         String entityName = ((EntityType)type).getAssociatedEntityName();
         name = this.pathExpressionParser.continueFromManyToMany(entityName, element.elementColumns, q);
      }

      return name;
   }

   public void token(String token, QueryTranslatorImpl q) throws QueryException {
      String lcToken = token.toLowerCase();
      if (token.equals("[") && !this.expectingPathContinuation) {
         this.expectingPathContinuation = false;
         if (this.expectingIndex == 0) {
            throw new QueryException("unexpected [");
         }
      } else if (token.equals("]")) {
         --this.expectingIndex;
         this.expectingPathContinuation = true;
      } else {
         if (this.expectingPathContinuation) {
            boolean pathExpressionContinuesFurther = this.continuePathExpression(token, q);
            if (pathExpressionContinuesFurther) {
               return;
            }
         }

         if (!this.inSubselect && (lcToken.equals("select") || lcToken.equals("from"))) {
            this.inSubselect = true;
            this.subselect = new StringBuilder(20);
         }

         if (this.inSubselect && token.equals(")")) {
            --this.bracketsSinceSelect;
            if (this.bracketsSinceSelect == -1) {
               QueryTranslatorImpl subq = new QueryTranslatorImpl(this.subselect.toString(), q.getEnabledFilters(), q.getFactory());

               try {
                  subq.compile(q);
               } catch (MappingException me) {
                  throw new QueryException("MappingException occurred compiling subquery", me);
               }

               this.appendToken(q, subq.getSQLString());
               this.inSubselect = false;
               this.bracketsSinceSelect = 0;
            }
         }

         if (this.inSubselect) {
            if (token.equals("(")) {
               ++this.bracketsSinceSelect;
            }

            this.subselect.append(token).append(' ');
         } else {
            this.specialCasesBefore(lcToken);
            if (!this.betweenSpecialCase && EXPRESSION_TERMINATORS.contains(lcToken)) {
               this.closeExpression(q, lcToken);
            }

            if (BOOLEAN_OPERATORS.contains(lcToken)) {
               this.booleanTests.removeLast();
               this.booleanTests.addLast(Boolean.TRUE);
            }

            if (lcToken.equals("not")) {
               this.nots.addLast(!(Boolean)this.nots.removeLast());
               this.negated = !this.negated;
            } else {
               this.doToken(token, q);
               if (!this.betweenSpecialCase && EXPRESSION_OPENERS.contains(lcToken)) {
                  this.openExpression(q, lcToken);
               }

               this.specialCasesAfter(lcToken);
            }
         }
      }
   }

   public void start(QueryTranslatorImpl q) throws QueryException {
      this.token("(", q);
   }

   public void end(QueryTranslatorImpl q) throws QueryException {
      if (this.expectingPathContinuation) {
         this.expectingPathContinuation = false;
         PathExpressionParser.CollectionElement element = this.pathExpressionParser.lastCollectionElement();
         if (element.elementColumns.length != 1) {
            throw new QueryException("path expression ended in composite collection element");
         }

         this.appendToken(q, element.elementColumns[0]);
         this.addToCurrentJoin(element);
      }

      this.token(")", q);
   }

   private void closeExpression(QueryTranslatorImpl q, String lcToken) {
      if ((Boolean)this.booleanTests.removeLast()) {
         if (this.booleanTests.size() > 0) {
            this.booleanTests.removeLast();
            this.booleanTests.addLast(Boolean.TRUE);
         }

         this.appendToken(q, this.joins.removeLast().toString());
      } else {
         StringBuilder join = (StringBuilder)this.joins.removeLast();
         ((StringBuilder)this.joins.getLast()).append(join.toString());
      }

      if ((Boolean)this.nots.removeLast()) {
         this.negated = !this.negated;
      }

      if (!")".equals(lcToken)) {
         this.appendToken(q, ")");
      }

   }

   private void openExpression(QueryTranslatorImpl q, String lcToken) {
      this.nots.addLast(Boolean.FALSE);
      this.booleanTests.addLast(Boolean.FALSE);
      this.joins.addLast(new StringBuilder());
      if (!"(".equals(lcToken)) {
         this.appendToken(q, "(");
      }

   }

   private void preprocess(String token, QueryTranslatorImpl q) throws QueryException {
      String[] tokens = StringHelper.split(".", token, true);
      if (tokens.length > 5 && ("elements".equals(tokens[tokens.length - 1]) || "indices".equals(tokens[tokens.length - 1]))) {
         this.pathExpressionParser.start(q);

         for(int i = 0; i < tokens.length - 3; ++i) {
            this.pathExpressionParser.token(tokens[i], q);
         }

         this.pathExpressionParser.token((String)null, q);
         this.pathExpressionParser.end(q);
         this.addJoin(this.pathExpressionParser.getWhereJoin(), q);
         this.pathExpressionParser.ignoreInitialJoin();
      }

   }

   private void doPathExpression(String token, QueryTranslatorImpl q) throws QueryException {
      this.preprocess(token, q);
      StringTokenizer tokens = new StringTokenizer(token, ".", true);
      this.pathExpressionParser.start(q);

      while(tokens.hasMoreTokens()) {
         this.pathExpressionParser.token(tokens.nextToken(), q);
      }

      this.pathExpressionParser.end(q);
      if (this.pathExpressionParser.isCollectionValued()) {
         this.openExpression(q, "");
         this.appendToken(q, this.pathExpressionParser.getCollectionSubquery(q.getEnabledFilters()));
         this.closeExpression(q, "");
         q.addQuerySpaces(q.getCollectionPersister(this.pathExpressionParser.getCollectionRole()).getCollectionSpaces());
      } else if (this.pathExpressionParser.isExpectingCollectionIndex()) {
         ++this.expectingIndex;
      } else {
         this.addJoin(this.pathExpressionParser.getWhereJoin(), q);
         this.appendToken(q, this.pathExpressionParser.getWhereColumn());
      }

   }

   private void addJoin(JoinSequence joinSequence, QueryTranslatorImpl q) throws QueryException {
      q.addFromJoinOnly(this.pathExpressionParser.getName(), joinSequence);

      try {
         this.addToCurrentJoin(joinSequence.toJoinFragment(q.getEnabledFilters(), true).toWhereFragmentString());
      } catch (MappingException me) {
         throw new QueryException(me);
      }
   }

   private void doToken(String token, QueryTranslatorImpl q) throws QueryException {
      if (q.isName(StringHelper.root(token))) {
         this.doPathExpression(q.unalias(token), q);
      } else if (token.startsWith(":")) {
         q.addNamedParameter(token.substring(1));
         this.appendToken(q, "?");
      } else {
         Queryable persister = q.getEntityPersisterUsingImports(token);
         if (persister != null) {
            String discrim = persister.getDiscriminatorSQLValue();
            if ("null".equals(discrim) || "not null".equals(discrim)) {
               throw new QueryException("subclass test not allowed for null or not null discriminator");
            }

            this.appendToken(q, discrim);
         } else {
            Object constant;
            if (token.indexOf(46) > -1 && (constant = ReflectHelper.getConstantValue(token)) != null) {
               Type type;
               try {
                  type = q.getFactory().getTypeResolver().heuristicType(constant.getClass().getName());
               } catch (MappingException me) {
                  throw new QueryException(me);
               }

               if (type == null) {
                  throw new QueryException("Could not determine type of: " + token);
               }

               try {
                  this.appendToken(q, ((LiteralType)type).objectToSQLString(constant, q.getFactory().getDialect()));
               } catch (Exception e) {
                  throw new QueryException("Could not format constant value to SQL literal: " + token, e);
               }
            } else {
               String negatedToken = this.negated ? (String)NEGATIONS.get(token.toLowerCase()) : null;
               if (negatedToken == null || this.betweenSpecialCase && "or".equals(negatedToken)) {
                  this.appendToken(q, token);
               } else {
                  this.appendToken(q, negatedToken);
               }
            }
         }
      }

   }

   private void addToCurrentJoin(String sql) {
      ((StringBuilder)this.joins.getLast()).append(sql);
   }

   private void addToCurrentJoin(PathExpressionParser.CollectionElement ce) throws QueryException {
      try {
         this.addToCurrentJoin(ce.joinSequence.toJoinFragment().toWhereFragmentString() + ce.indexValue.toString());
      } catch (MappingException me) {
         throw new QueryException(me);
      }
   }

   private void specialCasesBefore(String lcToken) {
      if (lcToken.equals("between") || lcToken.equals("not between")) {
         this.betweenSpecialCase = true;
      }

   }

   private void specialCasesAfter(String lcToken) {
      if (this.betweenSpecialCase && lcToken.equals("and")) {
         this.betweenSpecialCase = false;
      }

   }

   void appendToken(QueryTranslatorImpl q, String token) {
      if (this.expectingIndex > 0) {
         this.pathExpressionParser.setLastCollectionElementIndexValue(token);
      } else {
         q.appendWhereToken(token);
      }

   }

   private boolean continuePathExpression(String token, QueryTranslatorImpl q) throws QueryException {
      this.expectingPathContinuation = false;
      PathExpressionParser.CollectionElement element = this.pathExpressionParser.lastCollectionElement();
      if (token.startsWith(".")) {
         this.doPathExpression(this.getElementName(element, q) + token, q);
         this.addToCurrentJoin(element);
         return true;
      } else if (element.elementColumns.length != 1) {
         throw new QueryException("path expression ended in composite collection element");
      } else {
         this.appendToken(q, element.elementColumns[0]);
         this.addToCurrentJoin(element);
         return false;
      }
   }

   static {
      EXPRESSION_TERMINATORS.add("and");
      EXPRESSION_TERMINATORS.add("or");
      EXPRESSION_TERMINATORS.add(")");
      EXPRESSION_OPENERS.add("and");
      EXPRESSION_OPENERS.add("or");
      EXPRESSION_OPENERS.add("(");
      BOOLEAN_OPERATORS.add("<");
      BOOLEAN_OPERATORS.add("=");
      BOOLEAN_OPERATORS.add(">");
      BOOLEAN_OPERATORS.add("#");
      BOOLEAN_OPERATORS.add("~");
      BOOLEAN_OPERATORS.add("like");
      BOOLEAN_OPERATORS.add("ilike");
      BOOLEAN_OPERATORS.add("regexp");
      BOOLEAN_OPERATORS.add("rlike");
      BOOLEAN_OPERATORS.add("is");
      BOOLEAN_OPERATORS.add("in");
      BOOLEAN_OPERATORS.add("any");
      BOOLEAN_OPERATORS.add("some");
      BOOLEAN_OPERATORS.add("all");
      BOOLEAN_OPERATORS.add("exists");
      BOOLEAN_OPERATORS.add("between");
      BOOLEAN_OPERATORS.add("<=");
      BOOLEAN_OPERATORS.add(">=");
      BOOLEAN_OPERATORS.add("=>");
      BOOLEAN_OPERATORS.add("=<");
      BOOLEAN_OPERATORS.add("!=");
      BOOLEAN_OPERATORS.add("<>");
      BOOLEAN_OPERATORS.add("!#");
      BOOLEAN_OPERATORS.add("!~");
      BOOLEAN_OPERATORS.add("!<");
      BOOLEAN_OPERATORS.add("!>");
      BOOLEAN_OPERATORS.add("is not");
      BOOLEAN_OPERATORS.add("not like");
      BOOLEAN_OPERATORS.add("not ilike");
      BOOLEAN_OPERATORS.add("not regexp");
      BOOLEAN_OPERATORS.add("not rlike");
      BOOLEAN_OPERATORS.add("not in");
      BOOLEAN_OPERATORS.add("not between");
      BOOLEAN_OPERATORS.add("not exists");
      NEGATIONS.put("and", "or");
      NEGATIONS.put("or", "and");
      NEGATIONS.put("<", ">=");
      NEGATIONS.put("=", "<>");
      NEGATIONS.put(">", "<=");
      NEGATIONS.put("#", "!#");
      NEGATIONS.put("~", "!~");
      NEGATIONS.put("like", "not like");
      NEGATIONS.put("ilike", "not ilike");
      NEGATIONS.put("regexp", "not regexp");
      NEGATIONS.put("rlike", "not rlike");
      NEGATIONS.put("is", "is not");
      NEGATIONS.put("in", "not in");
      NEGATIONS.put("exists", "not exists");
      NEGATIONS.put("between", "not between");
      NEGATIONS.put("<=", ">");
      NEGATIONS.put(">=", "<");
      NEGATIONS.put("=>", "<");
      NEGATIONS.put("=<", ">");
      NEGATIONS.put("!=", "=");
      NEGATIONS.put("<>", "=");
      NEGATIONS.put("!#", "#");
      NEGATIONS.put("!~", "~");
      NEGATIONS.put("!<", "<");
      NEGATIONS.put("!>", ">");
      NEGATIONS.put("is not", "is");
      NEGATIONS.put("not like", "like");
      NEGATIONS.put("not ilike", "ilike");
      NEGATIONS.put("not regexp", "regexp");
      NEGATIONS.put("not rlike", "rlike");
      NEGATIONS.put("not in", "in");
      NEGATIONS.put("not between", "between");
      NEGATIONS.put("not exists", "exists");
   }
}
