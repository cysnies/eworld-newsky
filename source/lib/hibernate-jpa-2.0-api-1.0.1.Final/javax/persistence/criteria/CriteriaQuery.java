package javax.persistence.criteria;

import java.util.List;
import java.util.Set;

public interface CriteriaQuery extends AbstractQuery {
   CriteriaQuery select(Selection var1);

   CriteriaQuery multiselect(Selection... var1);

   CriteriaQuery multiselect(List var1);

   CriteriaQuery where(Expression var1);

   CriteriaQuery where(Predicate... var1);

   CriteriaQuery groupBy(Expression... var1);

   CriteriaQuery groupBy(List var1);

   CriteriaQuery having(Expression var1);

   CriteriaQuery having(Predicate... var1);

   CriteriaQuery orderBy(Order... var1);

   CriteriaQuery orderBy(List var1);

   CriteriaQuery distinct(boolean var1);

   List getOrderList();

   Set getParameters();
}
