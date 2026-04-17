package javax.persistence.criteria;

import java.util.List;
import java.util.Set;

public interface Subquery extends AbstractQuery, Expression {
   Subquery select(Expression var1);

   Subquery where(Expression var1);

   Subquery where(Predicate... var1);

   Subquery groupBy(Expression... var1);

   Subquery groupBy(List var1);

   Subquery having(Expression var1);

   Subquery having(Predicate... var1);

   Subquery distinct(boolean var1);

   Root correlate(Root var1);

   Join correlate(Join var1);

   CollectionJoin correlate(CollectionJoin var1);

   SetJoin correlate(SetJoin var1);

   ListJoin correlate(ListJoin var1);

   MapJoin correlate(MapJoin var1);

   AbstractQuery getParent();

   Expression getSelection();

   Set getCorrelatedJoins();
}
