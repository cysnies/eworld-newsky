package org.hibernate.mapping;

import org.hibernate.MappingException;
import org.hibernate.dialect.Dialect;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.factory.IdentifierGeneratorFactory;

public interface KeyValue extends Value {
   IdentifierGenerator createIdentifierGenerator(IdentifierGeneratorFactory var1, Dialect var2, String var3, String var4, RootClass var5) throws MappingException;

   boolean isIdentityColumn(IdentifierGeneratorFactory var1, Dialect var2);

   void createForeignKeyOfEntity(String var1);

   boolean isCascadeDeleteEnabled();

   String getNullValue();

   boolean isUpdateable();
}
