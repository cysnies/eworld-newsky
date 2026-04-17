package javax.persistence;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public interface TypedQuery extends Query {
   List getResultList();

   Object getSingleResult();

   TypedQuery setMaxResults(int var1);

   TypedQuery setFirstResult(int var1);

   TypedQuery setHint(String var1, Object var2);

   TypedQuery setParameter(Parameter var1, Object var2);

   TypedQuery setParameter(Parameter var1, Calendar var2, TemporalType var3);

   TypedQuery setParameter(Parameter var1, Date var2, TemporalType var3);

   TypedQuery setParameter(String var1, Object var2);

   TypedQuery setParameter(String var1, Calendar var2, TemporalType var3);

   TypedQuery setParameter(String var1, Date var2, TemporalType var3);

   TypedQuery setParameter(int var1, Object var2);

   TypedQuery setParameter(int var1, Calendar var2, TemporalType var3);

   TypedQuery setParameter(int var1, Date var2, TemporalType var3);

   TypedQuery setFlushMode(FlushModeType var1);

   TypedQuery setLockMode(LockModeType var1);
}
