package javax.persistence.criteria;

import java.util.List;
import java.util.Set;
import javax.persistence.metamodel.EntityType;

public interface AbstractQuery {
   Root from(Class var1);

   Root from(EntityType var1);

   AbstractQuery where(Expression var1);

   AbstractQuery where(Predicate... var1);

   AbstractQuery groupBy(Expression... var1);

   AbstractQuery groupBy(List var1);

   AbstractQuery having(Expression var1);

   AbstractQuery having(Predicate... var1);

   AbstractQuery distinct(boolean var1);

   Subquery subquery(Class var1);

   Set getRoots();

   Selection getSelection();

   Predicate getRestriction();

   List getGroupList();

   Predicate getGroupRestriction();

   boolean isDistinct();

   Class getResultType();
}
