package org.hibernate.hql.internal.classic;

import java.util.HashMap;
import java.util.Map;
import org.hibernate.QueryException;
import org.hibernate.persister.entity.Queryable;
import org.hibernate.sql.JoinType;

public class FromParser implements Parser {
   private final PathExpressionParser peParser = new FromPathExpressionParser();
   private String entityName;
   private String alias;
   private boolean afterIn;
   private boolean afterAs;
   private boolean afterClass;
   private boolean expectingJoin;
   private boolean expectingIn;
   private boolean expectingAs;
   private boolean afterJoinType;
   private JoinType joinType;
   private boolean afterFetch;
   private boolean memberDeclarations;
   private boolean expectingPathExpression;
   private boolean afterMemberDeclarations;
   private String collectionName;
   private static final Map JOIN_TYPES = new HashMap();

   public FromParser() {
      this.joinType = JoinType.INNER_JOIN;
   }

   public void token(String token, QueryTranslatorImpl q) throws QueryException {
      String lcToken = token.toLowerCase();
      if (lcToken.equals(",")) {
         if (!(this.expectingJoin | this.expectingAs)) {
            throw new QueryException("unexpected token: ,");
         }

         this.expectingJoin = false;
         this.expectingAs = false;
      } else if (lcToken.equals("join")) {
         if (!this.afterJoinType) {
            if (!(this.expectingJoin | this.expectingAs)) {
               throw new QueryException("unexpected token: join");
            }

            this.joinType = JoinType.INNER_JOIN;
            this.expectingJoin = false;
            this.expectingAs = false;
         } else {
            this.afterJoinType = false;
         }
      } else if (lcToken.equals("fetch")) {
         if (q.isShallowQuery()) {
            throw new QueryException("fetch may not be used with scroll() or iterate()");
         }

         if (this.joinType == JoinType.NONE) {
            throw new QueryException("unexpected token: fetch");
         }

         if (this.joinType == JoinType.FULL_JOIN || this.joinType == JoinType.RIGHT_OUTER_JOIN) {
            throw new QueryException("fetch may only be used with inner join or left outer join");
         }

         this.afterFetch = true;
      } else if (lcToken.equals("outer")) {
         if (!this.afterJoinType || this.joinType != JoinType.LEFT_OUTER_JOIN && this.joinType != JoinType.RIGHT_OUTER_JOIN) {
            throw new QueryException("unexpected token: outer");
         }
      } else if (JOIN_TYPES.containsKey(lcToken)) {
         if (!(this.expectingJoin | this.expectingAs)) {
            throw new QueryException("unexpected token: " + token);
         }

         this.joinType = (JoinType)JOIN_TYPES.get(lcToken);
         this.afterJoinType = true;
         this.expectingJoin = false;
         this.expectingAs = false;
      } else if (lcToken.equals("class")) {
         if (!this.afterIn) {
            throw new QueryException("unexpected token: class");
         }

         if (this.joinType != JoinType.NONE) {
            throw new QueryException("outer or full join must be followed by path expression");
         }

         this.afterClass = true;
      } else if (lcToken.equals("in")) {
         if (this.alias == null) {
            this.memberDeclarations = true;
            this.afterMemberDeclarations = false;
         } else {
            if (!this.expectingIn) {
               throw new QueryException("unexpected token: in");
            }

            this.afterIn = true;
            this.expectingIn = false;
         }
      } else if (lcToken.equals("as")) {
         if (!this.expectingAs) {
            throw new QueryException("unexpected token: as");
         }

         this.afterAs = true;
         this.expectingAs = false;
      } else if ("(".equals(token)) {
         if (!this.memberDeclarations) {
            throw new QueryException("unexpected token: (");
         }

         this.expectingPathExpression = true;
      } else if (")".equals(token)) {
         this.afterMemberDeclarations = true;
      } else {
         if (this.afterJoinType) {
            throw new QueryException("join expected: " + token);
         }

         if (this.expectingJoin) {
            throw new QueryException("unexpected token: " + token);
         }

         if (this.expectingIn) {
            throw new QueryException("in expected: " + token);
         }

         if (!this.afterAs && !this.expectingAs) {
            if (this.afterIn) {
               if (this.alias == null) {
                  throw new QueryException("alias not specified for: " + token);
               }

               if (this.joinType != JoinType.NONE) {
                  throw new QueryException("outer or full join must be followed by path expression");
               }

               if (this.afterClass) {
                  Queryable p = q.getEntityPersisterUsingImports(token);
                  if (p == null) {
                     throw new QueryException("persister not found: " + token);
                  }

                  q.addFromClass(this.alias, p);
               } else {
                  this.peParser.setJoinType(JoinType.INNER_JOIN);
                  this.peParser.setUseThetaStyleJoin(true);
                  ParserHelper.parse(this.peParser, q.unalias(token), ".", q);
                  if (!this.peParser.isCollectionValued()) {
                     throw new QueryException("path expression did not resolve to collection: " + token);
                  }

                  String nm = this.peParser.addFromCollection(q);
                  q.setAliasName(this.alias, nm);
               }

               this.alias = null;
               this.afterIn = false;
               this.afterClass = false;
               this.expectingJoin = true;
            } else if (this.memberDeclarations && this.expectingPathExpression) {
               this.expectingAs = true;
               this.peParser.setJoinType(JoinType.INNER_JOIN);
               this.peParser.setUseThetaStyleJoin(false);
               ParserHelper.parse(this.peParser, q.unalias(token), ".", q);
               if (!this.peParser.isCollectionValued()) {
                  throw new QueryException("path expression did not resolve to collection: " + token);
               }

               this.collectionName = this.peParser.addFromCollection(q);
               this.expectingPathExpression = false;
               this.memberDeclarations = false;
            } else {
               Queryable p = q.getEntityPersisterUsingImports(token);
               if (p != null) {
                  if (this.joinType != JoinType.NONE) {
                     throw new QueryException("outer or full join must be followed by path expression");
                  }

                  this.entityName = q.createNameFor(p.getEntityName());
                  q.addFromClass(this.entityName, p);
                  this.expectingAs = true;
               } else if (token.indexOf(46) < 0) {
                  this.alias = token;
                  this.expectingIn = true;
               } else {
                  if (this.joinType != JoinType.NONE) {
                     this.peParser.setJoinType(this.joinType);
                  } else {
                     this.peParser.setJoinType(JoinType.INNER_JOIN);
                  }

                  this.peParser.setUseThetaStyleJoin(q.isSubquery());
                  ParserHelper.parse(this.peParser, q.unalias(token), ".", q);
                  this.entityName = this.peParser.addFromAssociation(q);
                  this.joinType = JoinType.NONE;
                  this.peParser.setJoinType(JoinType.INNER_JOIN);
                  if (this.afterFetch) {
                     this.peParser.fetch(q, this.entityName);
                     this.afterFetch = false;
                  }

                  this.expectingAs = true;
               }
            }
         } else {
            if (this.entityName != null) {
               q.setAliasName(token, this.entityName);
            } else {
               if (this.collectionName == null) {
                  throw new QueryException("unexpected: as " + token);
               }

               q.setAliasName(token, this.collectionName);
            }

            this.afterAs = false;
            this.expectingJoin = true;
            this.expectingAs = false;
            this.entityName = null;
            this.collectionName = null;
            this.memberDeclarations = false;
            this.expectingPathExpression = false;
            this.afterMemberDeclarations = false;
         }
      }

   }

   public void start(QueryTranslatorImpl q) {
      this.entityName = null;
      this.collectionName = null;
      this.alias = null;
      this.afterIn = false;
      this.afterAs = false;
      this.afterClass = false;
      this.expectingJoin = false;
      this.expectingIn = false;
      this.expectingAs = false;
      this.memberDeclarations = false;
      this.expectingPathExpression = false;
      this.afterMemberDeclarations = false;
      this.joinType = JoinType.NONE;
   }

   public void end(QueryTranslatorImpl q) {
      if (this.afterMemberDeclarations) {
         throw new QueryException("alias not specified for IN");
      }
   }

   static {
      JOIN_TYPES.put("left", JoinType.LEFT_OUTER_JOIN);
      JOIN_TYPES.put("right", JoinType.RIGHT_OUTER_JOIN);
      JOIN_TYPES.put("full", JoinType.FULL_JOIN);
      JOIN_TYPES.put("inner", JoinType.INNER_JOIN);
   }
}
