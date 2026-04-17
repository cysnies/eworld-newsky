package org.hibernate.engine.internal;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.TypedValue;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

public class ParameterBinder {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, ParameterBinder.class.getName());

   private ParameterBinder() {
   }

   public static int bindQueryParameters(PreparedStatement st, QueryParameters queryParameters, int start, NamedParameterSource source, SessionImplementor session) throws SQLException, HibernateException {
      int col = start + bindPositionalParameters(st, queryParameters, start, session);
      col += bindNamedParameters(st, queryParameters, col, source, session);
      return col;
   }

   public static int bindPositionalParameters(PreparedStatement st, QueryParameters queryParameters, int start, SessionImplementor session) throws SQLException, HibernateException {
      return bindPositionalParameters(st, queryParameters.getPositionalParameterValues(), queryParameters.getPositionalParameterTypes(), start, session);
   }

   public static int bindPositionalParameters(PreparedStatement st, Object[] values, Type[] types, int start, SessionImplementor session) throws SQLException, HibernateException {
      int span = 0;

      for(int i = 0; i < values.length; ++i) {
         types[i].nullSafeSet(st, values[i], start + span, session);
         span += types[i].getColumnSpan(session.getFactory());
      }

      return span;
   }

   public static int bindNamedParameters(PreparedStatement ps, QueryParameters queryParameters, int start, NamedParameterSource source, SessionImplementor session) throws SQLException, HibernateException {
      return bindNamedParameters(ps, queryParameters.getNamedParameters(), start, source, session);
   }

   public static int bindNamedParameters(PreparedStatement ps, Map namedParams, int start, NamedParameterSource source, SessionImplementor session) throws SQLException, HibernateException {
      if (namedParams == null) {
         return 0;
      } else {
         Iterator iter = namedParams.entrySet().iterator();

         int result;
         int[] locations;
         for(result = 0; iter.hasNext(); result += locations.length) {
            Map.Entry e = (Map.Entry)iter.next();
            String name = (String)e.getKey();
            TypedValue typedval = (TypedValue)e.getValue();
            locations = source.getNamedParameterLocations(name);

            for(int i = 0; i < locations.length; ++i) {
               if (LOG.isDebugEnabled()) {
                  LOG.debugf("bindNamedParameters() %s -> %s [%s]", typedval.getValue(), name, locations[i] + start);
               }

               typedval.getType().nullSafeSet(ps, typedval.getValue(), locations[i] + start, session);
            }
         }

         return result;
      }
   }

   public interface NamedParameterSource {
      int[] getNamedParameterLocations(String var1);
   }
}
