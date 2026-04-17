package org.hibernate;

import java.util.Properties;
import org.hibernate.type.BasicType;
import org.hibernate.type.Type;

public interface TypeHelper {
   BasicType basic(String var1);

   BasicType basic(Class var1);

   Type heuristicType(String var1);

   Type entity(Class var1);

   Type entity(String var1);

   Type custom(Class var1);

   Type custom(Class var1, Properties var2);

   Type any(Type var1, Type var2);
}
