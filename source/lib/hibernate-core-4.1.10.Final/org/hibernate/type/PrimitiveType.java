package org.hibernate.type;

import java.io.Serializable;

public interface PrimitiveType extends LiteralType {
   Class getPrimitiveClass();

   String toString(Object var1);

   Serializable getDefaultValue();
}
