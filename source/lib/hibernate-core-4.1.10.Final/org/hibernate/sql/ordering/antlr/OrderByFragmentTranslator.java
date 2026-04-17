package org.hibernate.sql.ordering.antlr;

import java.io.StringReader;
import java.util.Set;
import org.hibernate.HibernateException;
import org.hibernate.hql.internal.ast.util.ASTPrinter;
import org.jboss.logging.Logger;

public class OrderByFragmentTranslator {
   private static final Logger LOG = Logger.getLogger(OrderByFragmentTranslator.class.getName());

   public static OrderByTranslation translate(TranslationContext context, String fragment) {
      GeneratedOrderByLexer lexer = new GeneratedOrderByLexer(new StringReader(fragment));
      OrderByFragmentParser parser = new OrderByFragmentParser(lexer, context);

      try {
         parser.orderByFragment();
      } catch (HibernateException e) {
         throw e;
      } catch (Throwable t) {
         throw new HibernateException("Unable to parse order-by fragment", t);
      }

      if (LOG.isTraceEnabled()) {
         ASTPrinter printer = new ASTPrinter(OrderByTemplateTokenTypes.class);
         LOG.trace(printer.showAsString(parser.getAST(), "--- {order-by fragment} ---"));
      }

      OrderByFragmentRenderer renderer = new OrderByFragmentRenderer();

      try {
         renderer.orderByFragment(parser.getAST());
      } catch (HibernateException e) {
         throw e;
      } catch (Throwable t) {
         throw new HibernateException("Unable to render parsed order-by fragment", t);
      }

      return new StandardOrderByTranslationImpl(renderer.getRenderedFragment(), parser.getColumnReferences());
   }

   public static class StandardOrderByTranslationImpl implements OrderByTranslation {
      private final String sqlTemplate;
      private final Set columnReferences;

      public StandardOrderByTranslationImpl(String sqlTemplate, Set columnReferences) {
         this.sqlTemplate = sqlTemplate;
         this.columnReferences = columnReferences;
      }

      public String injectAliases(OrderByAliasResolver aliasResolver) {
         String sql = this.sqlTemplate;

         for(String columnReference : this.columnReferences) {
            String replacementToken = "{" + columnReference + "}";
            sql = sql.replace(replacementToken, aliasResolver.resolveTableAlias(columnReference) + '.' + columnReference);
         }

         return sql;
      }
   }
}
