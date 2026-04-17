package org.hibernate.internal.util.xml;

import java.io.Serializable;

public interface Origin extends Serializable {
   String getType();

   String getName();
}
