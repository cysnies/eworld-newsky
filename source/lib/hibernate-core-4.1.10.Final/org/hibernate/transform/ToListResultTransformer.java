package org.hibernate.transform;

import java.util.Arrays;

public class ToListResultTransformer extends BasicTransformerAdapter {
   public static final ToListResultTransformer INSTANCE = new ToListResultTransformer();

   private ToListResultTransformer() {
   }

   public Object transformTuple(Object[] tuple, String[] aliases) {
      return Arrays.asList(tuple);
   }

   private Object readResolve() {
      return INSTANCE;
   }
}
