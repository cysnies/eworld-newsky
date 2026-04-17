package javax.persistence.criteria;

import java.util.Set;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

public interface FetchParent {
   Set getFetches();

   Fetch fetch(SingularAttribute var1);

   Fetch fetch(SingularAttribute var1, JoinType var2);

   Fetch fetch(PluralAttribute var1);

   Fetch fetch(PluralAttribute var1, JoinType var2);

   Fetch fetch(String var1);

   Fetch fetch(String var1, JoinType var2);
}
