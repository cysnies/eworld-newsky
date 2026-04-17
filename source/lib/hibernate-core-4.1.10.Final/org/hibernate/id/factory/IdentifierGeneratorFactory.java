package org.hibernate.id.factory;

import java.util.Properties;
import org.hibernate.dialect.Dialect;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.type.Type;

public interface IdentifierGeneratorFactory {
   Dialect getDialect();

   /** @deprecated */
   void setDialect(Dialect var1);

   IdentifierGenerator createIdentifierGenerator(String var1, Type var2, Properties var3);

   Class getIdentifierGeneratorClass(String var1);
}
