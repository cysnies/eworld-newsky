package org.hibernate.transform;

import java.io.Serializable;
import java.util.List;

public interface ResultTransformer extends Serializable {
   Object transformTuple(Object[] var1, String[] var2);

   List transformList(List var1);
}
