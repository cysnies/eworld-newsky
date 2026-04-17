package org.hibernate.hql.internal.classic;

import org.hibernate.QueryException;
import org.hibernate.internal.util.StringHelper;

public class OrderByParser implements Parser {
   private final PathExpressionParser pathExpressionParser = new PathExpressionParser();

   public OrderByParser() {
      this.pathExpressionParser.setUseThetaStyleJoin(true);
   }

   public void token(String token, QueryTranslatorImpl q) throws QueryException {
      if (q.isName(StringHelper.root(token))) {
         ParserHelper.parse(this.pathExpressionParser, q.unalias(token), ".", q);
         q.appendOrderByToken(this.pathExpressionParser.getWhereColumn());
         this.pathExpressionParser.addAssociation(q);
      } else if (token.startsWith(":")) {
         q.addNamedParameter(token.substring(1));
         q.appendOrderByToken("?");
      } else {
         q.appendOrderByToken(token);
      }

   }

   public void start(QueryTranslatorImpl q) throws QueryException {
   }

   public void end(QueryTranslatorImpl q) throws QueryException {
   }
}
