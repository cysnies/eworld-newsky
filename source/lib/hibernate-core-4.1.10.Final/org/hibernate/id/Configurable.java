package org.hibernate.id;

import java.util.Properties;
import org.hibernate.MappingException;
import org.hibernate.dialect.Dialect;
import org.hibernate.type.Type;

public interface Configurable {
   void configure(Type var1, Properties var2, Dialect var3) throws MappingException;
}
