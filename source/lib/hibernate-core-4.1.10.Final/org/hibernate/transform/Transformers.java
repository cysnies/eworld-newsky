package org.hibernate.transform;

public final class Transformers {
   public static final AliasToEntityMapResultTransformer ALIAS_TO_ENTITY_MAP;
   public static final ToListResultTransformer TO_LIST;

   private Transformers() {
   }

   public static ResultTransformer aliasToBean(Class target) {
      return new AliasToBeanResultTransformer(target);
   }

   static {
      ALIAS_TO_ENTITY_MAP = AliasToEntityMapResultTransformer.INSTANCE;
      TO_LIST = ToListResultTransformer.INSTANCE;
   }
}
