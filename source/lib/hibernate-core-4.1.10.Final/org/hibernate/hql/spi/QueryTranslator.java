package org.hibernate.hql.spi;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.QueryException;
import org.hibernate.ScrollableResults;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.event.spi.EventSource;
import org.hibernate.type.Type;

public interface QueryTranslator {
   String ERROR_CANNOT_FETCH_WITH_ITERATE = "fetch may not be used with scroll() or iterate()";
   String ERROR_NAMED_PARAMETER_DOES_NOT_APPEAR = "Named parameter does not appear in Query: ";
   String ERROR_CANNOT_DETERMINE_TYPE = "Could not determine type of: ";
   String ERROR_CANNOT_FORMAT_LITERAL = "Could not format constant value to SQL literal: ";

   void compile(Map var1, boolean var2) throws QueryException, MappingException;

   List list(SessionImplementor var1, QueryParameters var2) throws HibernateException;

   Iterator iterate(QueryParameters var1, EventSource var2) throws HibernateException;

   ScrollableResults scroll(QueryParameters var1, SessionImplementor var2) throws HibernateException;

   int executeUpdate(QueryParameters var1, SessionImplementor var2) throws HibernateException;

   Set getQuerySpaces();

   String getQueryIdentifier();

   String getSQLString();

   List collectSqlStrings();

   String getQueryString();

   Map getEnabledFilters();

   Type[] getReturnTypes();

   String[] getReturnAliases();

   String[][] getColumnNames();

   ParameterTranslations getParameterTranslations();

   void validateScrollability() throws HibernateException;

   boolean containsCollectionFetches();

   boolean isManipulationStatement();

   Class getDynamicInstantiationResultType();
}
