package javax.persistence.criteria;

import java.util.Collection;

public interface Expression extends Selection {
   Predicate isNull();

   Predicate isNotNull();

   Predicate in(Object... var1);

   Predicate in(Expression... var1);

   Predicate in(Collection var1);

   Predicate in(Expression var1);

   Expression as(Class var1);
}
