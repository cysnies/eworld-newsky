package javax.persistence.criteria;

import java.util.Set;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

public interface From extends Path, FetchParent {
   Set getJoins();

   boolean isCorrelated();

   From getCorrelationParent();

   Join join(SingularAttribute var1);

   Join join(SingularAttribute var1, JoinType var2);

   CollectionJoin join(CollectionAttribute var1);

   SetJoin join(SetAttribute var1);

   ListJoin join(ListAttribute var1);

   MapJoin join(MapAttribute var1);

   CollectionJoin join(CollectionAttribute var1, JoinType var2);

   SetJoin join(SetAttribute var1, JoinType var2);

   ListJoin join(ListAttribute var1, JoinType var2);

   MapJoin join(MapAttribute var1, JoinType var2);

   Join join(String var1);

   CollectionJoin joinCollection(String var1);

   SetJoin joinSet(String var1);

   ListJoin joinList(String var1);

   MapJoin joinMap(String var1);

   Join join(String var1, JoinType var2);

   CollectionJoin joinCollection(String var1, JoinType var2);

   SetJoin joinSet(String var1, JoinType var2);

   ListJoin joinList(String var1, JoinType var2);

   MapJoin joinMap(String var1, JoinType var2);
}
