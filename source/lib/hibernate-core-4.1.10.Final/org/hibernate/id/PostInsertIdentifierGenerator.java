package org.hibernate.id;

import org.hibernate.HibernateException;
import org.hibernate.dialect.Dialect;
import org.hibernate.id.insert.InsertGeneratedIdentifierDelegate;

public interface PostInsertIdentifierGenerator extends IdentifierGenerator {
   InsertGeneratedIdentifierDelegate getInsertGeneratedIdentifierDelegate(PostInsertIdentityPersister var1, Dialect var2, boolean var3) throws HibernateException;
}
