package javax.persistence;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Query {
   List getResultList();

   Object getSingleResult();

   int executeUpdate();

   Query setMaxResults(int var1);

   int getMaxResults();

   Query setFirstResult(int var1);

   int getFirstResult();

   Query setHint(String var1, Object var2);

   Map getHints();

   Query setParameter(Parameter var1, Object var2);

   Query setParameter(Parameter var1, Calendar var2, TemporalType var3);

   Query setParameter(Parameter var1, Date var2, TemporalType var3);

   Query setParameter(String var1, Object var2);

   Query setParameter(String var1, Calendar var2, TemporalType var3);

   Query setParameter(String var1, Date var2, TemporalType var3);

   Query setParameter(int var1, Object var2);

   Query setParameter(int var1, Calendar var2, TemporalType var3);

   Query setParameter(int var1, Date var2, TemporalType var3);

   Set getParameters();

   Parameter getParameter(String var1);

   Parameter getParameter(String var1, Class var2);

   Parameter getParameter(int var1);

   Parameter getParameter(int var1, Class var2);

   boolean isBound(Parameter var1);

   Object getParameterValue(Parameter var1);

   Object getParameterValue(String var1);

   Object getParameterValue(int var1);

   Query setFlushMode(FlushModeType var1);

   FlushModeType getFlushMode();

   Query setLockMode(LockModeType var1);

   LockModeType getLockMode();

   Object unwrap(Class var1);
}
