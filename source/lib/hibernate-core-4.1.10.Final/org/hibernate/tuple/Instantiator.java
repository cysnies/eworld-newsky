package org.hibernate.tuple;

import java.io.Serializable;

public interface Instantiator extends Serializable {
   Object instantiate(Serializable var1);

   Object instantiate();

   boolean isInstance(Object var1);
}
