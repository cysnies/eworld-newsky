package org.hibernate.criterion;

import org.hibernate.transform.AliasToEntityMapResultTransformer;
import org.hibernate.transform.DistinctRootEntityResultTransformer;
import org.hibernate.transform.PassThroughResultTransformer;
import org.hibernate.transform.ResultTransformer;
import org.hibernate.transform.RootEntityResultTransformer;

public interface CriteriaSpecification {
   String ROOT_ALIAS = "this";
   ResultTransformer ALIAS_TO_ENTITY_MAP = AliasToEntityMapResultTransformer.INSTANCE;
   ResultTransformer ROOT_ENTITY = RootEntityResultTransformer.INSTANCE;
   ResultTransformer DISTINCT_ROOT_ENTITY = DistinctRootEntityResultTransformer.INSTANCE;
   ResultTransformer PROJECTION = PassThroughResultTransformer.INSTANCE;
   /** @deprecated */
   @Deprecated
   int INNER_JOIN = 0;
   /** @deprecated */
   @Deprecated
   int FULL_JOIN = 4;
   /** @deprecated */
   @Deprecated
   int LEFT_JOIN = 1;
}
