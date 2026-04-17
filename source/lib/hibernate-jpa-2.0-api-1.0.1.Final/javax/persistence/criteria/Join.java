package javax.persistence.criteria;

import javax.persistence.metamodel.Attribute;

public interface Join extends From {
   Attribute getAttribute();

   From getParent();

   JoinType getJoinType();
}
