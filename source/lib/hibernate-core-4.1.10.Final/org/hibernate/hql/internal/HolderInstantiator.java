package org.hibernate.hql.internal;

import java.lang.reflect.Constructor;
import org.hibernate.transform.AliasToBeanConstructorResultTransformer;
import org.hibernate.transform.ResultTransformer;
import org.hibernate.transform.Transformers;

public final class HolderInstantiator {
   public static final HolderInstantiator NOOP_INSTANTIATOR = new HolderInstantiator((ResultTransformer)null, (String[])null);
   private final ResultTransformer transformer;
   private final String[] queryReturnAliases;

   public static HolderInstantiator getHolderInstantiator(ResultTransformer selectNewTransformer, ResultTransformer customTransformer, String[] queryReturnAliases) {
      return new HolderInstantiator(resolveResultTransformer(selectNewTransformer, customTransformer), queryReturnAliases);
   }

   public static ResultTransformer resolveResultTransformer(ResultTransformer selectNewTransformer, ResultTransformer customTransformer) {
      return selectNewTransformer != null ? selectNewTransformer : customTransformer;
   }

   public static ResultTransformer createSelectNewTransformer(Constructor constructor, boolean returnMaps, boolean returnLists) {
      if (constructor != null) {
         return new AliasToBeanConstructorResultTransformer(constructor);
      } else if (returnMaps) {
         return Transformers.ALIAS_TO_ENTITY_MAP;
      } else {
         return returnLists ? Transformers.TO_LIST : null;
      }
   }

   public static HolderInstantiator createClassicHolderInstantiator(Constructor constructor, ResultTransformer transformer) {
      return new HolderInstantiator(resolveClassicResultTransformer(constructor, transformer), (String[])null);
   }

   public static ResultTransformer resolveClassicResultTransformer(Constructor constructor, ResultTransformer transformer) {
      return (ResultTransformer)(constructor != null ? new AliasToBeanConstructorResultTransformer(constructor) : transformer);
   }

   public HolderInstantiator(ResultTransformer transformer, String[] queryReturnAliases) {
      this.transformer = transformer;
      this.queryReturnAliases = queryReturnAliases;
   }

   public boolean isRequired() {
      return this.transformer != null;
   }

   public Object instantiate(Object[] row) {
      return this.transformer == null ? row : this.transformer.transformTuple(row, this.queryReturnAliases);
   }

   public String[] getQueryReturnAliases() {
      return this.queryReturnAliases;
   }

   public ResultTransformer getResultTransformer() {
      return this.transformer;
   }
}
