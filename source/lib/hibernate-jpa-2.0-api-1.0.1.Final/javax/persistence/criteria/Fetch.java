package javax.persistence.criteria;

import javax.persistence.metamodel.Attribute;

public interface Fetch extends FetchParent {
   Attribute getAttribute();

   FetchParent getParent();

   JoinType getJoinType();
}
