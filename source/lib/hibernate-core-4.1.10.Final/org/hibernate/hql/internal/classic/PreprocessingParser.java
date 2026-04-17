package org.hibernate.hql.internal.classic;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.hibernate.QueryException;
import org.hibernate.hql.internal.CollectionProperties;
import org.hibernate.internal.util.StringHelper;

public class PreprocessingParser implements Parser {
   private static final Set HQL_OPERATORS = new HashSet();
   private Map replacements;
   private boolean quoted;
   private StringBuilder quotedString;
   private ClauseParser parser = new ClauseParser();
   private String lastToken;
   private String currentCollectionProp;

   public PreprocessingParser(Map replacements) {
      this.replacements = replacements;
   }

   public void token(String token, QueryTranslatorImpl q) throws QueryException {
      if (this.quoted) {
         this.quotedString.append(token);
      }

      if ("'".equals(token)) {
         if (this.quoted) {
            token = this.quotedString.toString();
         } else {
            this.quotedString = (new StringBuilder(20)).append(token);
         }

         this.quoted = !this.quoted;
      }

      if (!this.quoted) {
         if (!ParserHelper.isWhitespace(token)) {
            String substoken = (String)this.replacements.get(token);
            token = substoken == null ? token : substoken;
            if (this.currentCollectionProp != null) {
               if ("(".equals(token)) {
                  return;
               }

               if (")".equals(token)) {
                  this.currentCollectionProp = null;
                  return;
               }

               token = StringHelper.qualify(token, this.currentCollectionProp);
            } else {
               String prop = CollectionProperties.getNormalizedPropertyName(token.toLowerCase());
               if (prop != null) {
                  this.currentCollectionProp = prop;
                  return;
               }
            }

            if (this.lastToken == null) {
               this.lastToken = token;
            } else {
               String doubleToken = token.length() > 1 ? this.lastToken + ' ' + token : this.lastToken + token;
               if (HQL_OPERATORS.contains(doubleToken.toLowerCase())) {
                  this.parser.token(doubleToken, q);
                  this.lastToken = null;
               } else {
                  this.parser.token(this.lastToken, q);
                  this.lastToken = token;
               }
            }

         }
      }
   }

   public void start(QueryTranslatorImpl q) throws QueryException {
      this.quoted = false;
      this.parser.start(q);
   }

   public void end(QueryTranslatorImpl q) throws QueryException {
      if (this.lastToken != null) {
         this.parser.token(this.lastToken, q);
      }

      this.parser.end(q);
      this.lastToken = null;
      this.currentCollectionProp = null;
   }

   static {
      HQL_OPERATORS.add("<=");
      HQL_OPERATORS.add(">=");
      HQL_OPERATORS.add("=>");
      HQL_OPERATORS.add("=<");
      HQL_OPERATORS.add("!=");
      HQL_OPERATORS.add("<>");
      HQL_OPERATORS.add("!#");
      HQL_OPERATORS.add("!~");
      HQL_OPERATORS.add("!<");
      HQL_OPERATORS.add("!>");
      HQL_OPERATORS.add("is not");
      HQL_OPERATORS.add("not like");
      HQL_OPERATORS.add("not in");
      HQL_OPERATORS.add("not between");
      HQL_OPERATORS.add("not exists");
   }
}
