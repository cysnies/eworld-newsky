package org.hibernate.hql.internal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.hql.internal.classic.ParserHelper;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.StringHelper;
import org.jboss.logging.Logger;

public final class QuerySplitter {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, QuerySplitter.class.getName());
   private static final Set BEFORE_CLASS_TOKENS = new HashSet();
   private static final Set NOT_AFTER_CLASS_TOKENS = new HashSet();

   private QuerySplitter() {
   }

   public static String[] concreteQueries(String query, SessionFactoryImplementor factory) throws MappingException {
      String[] tokens = StringHelper.split(" \n\r\f\t(),", query, true);
      if (tokens.length == 0) {
         return new String[]{query};
      } else {
         ArrayList placeholders = new ArrayList();
         ArrayList replacements = new ArrayList();
         StringBuilder templateQuery = new StringBuilder(40);
         int start = getStartingPositionFor(tokens, templateQuery);
         int count = 0;
         String next = null;
         String last = tokens[start - 1].toLowerCase();

         for(int i = start; i < tokens.length; ++i) {
            String token = tokens[i];
            if (ParserHelper.isWhitespace(token)) {
               templateQuery.append(token);
            } else {
               next = nextNonWhite(tokens, i).toLowerCase();
               boolean process = isJavaIdentifier(token) && isPossiblyClassName(last, next);
               last = token.toLowerCase();
               if (process) {
                  String importedClassName = getImportedClass(token, factory);
                  if (importedClassName != null) {
                     String[] implementors = factory.getImplementors(importedClassName);
                     token = "$clazz" + count++ + "$";
                     if (implementors != null) {
                        placeholders.add(token);
                        replacements.add(implementors);
                     }
                  }
               }

               templateQuery.append(token);
            }
         }

         String[] results = StringHelper.multiply(templateQuery.toString(), placeholders.iterator(), replacements.iterator());
         if (results.length == 0) {
            LOG.noPersistentClassesFound(query);
         }

         return results;
      }
   }

   private static String nextNonWhite(String[] tokens, int start) {
      for(int i = start + 1; i < tokens.length; ++i) {
         if (!ParserHelper.isWhitespace(tokens[i])) {
            return tokens[i];
         }
      }

      return tokens[tokens.length - 1];
   }

   private static int getStartingPositionFor(String[] tokens, StringBuilder templateQuery) {
      templateQuery.append(tokens[0]);
      if (!"select".equals(tokens[0].toLowerCase())) {
         return 1;
      } else {
         for(int i = 1; i < tokens.length; ++i) {
            if ("from".equals(tokens[i].toLowerCase())) {
               return i;
            }

            templateQuery.append(tokens[i]);
         }

         return tokens.length;
      }
   }

   private static boolean isPossiblyClassName(String last, String next) {
      return "class".equals(last) || BEFORE_CLASS_TOKENS.contains(last) && !NOT_AFTER_CLASS_TOKENS.contains(next);
   }

   private static boolean isJavaIdentifier(String token) {
      return Character.isJavaIdentifierStart(token.charAt(0));
   }

   public static String getImportedClass(String name, SessionFactoryImplementor factory) {
      return factory.getImportedClassName(name);
   }

   static {
      BEFORE_CLASS_TOKENS.add("from");
      BEFORE_CLASS_TOKENS.add("delete");
      BEFORE_CLASS_TOKENS.add("update");
      BEFORE_CLASS_TOKENS.add(",");
      NOT_AFTER_CLASS_TOKENS.add("in");
      NOT_AFTER_CLASS_TOKENS.add("from");
      NOT_AFTER_CLASS_TOKENS.add(")");
   }
}
