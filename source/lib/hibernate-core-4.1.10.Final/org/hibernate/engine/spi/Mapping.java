package org.hibernate.engine.spi;

import org.hibernate.MappingException;
import org.hibernate.id.factory.IdentifierGeneratorFactory;
import org.hibernate.type.Type;

public interface Mapping {
   /** @deprecated */
   IdentifierGeneratorFactory getIdentifierGeneratorFactory();

   Type getIdentifierType(String var1) throws MappingException;

   String getIdentifierPropertyName(String var1) throws MappingException;

   Type getReferencedPropertyType(String var1, String var2) throws MappingException;
}
