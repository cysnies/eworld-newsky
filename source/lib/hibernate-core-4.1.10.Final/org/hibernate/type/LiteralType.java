package org.hibernate.type;

import org.hibernate.dialect.Dialect;

public interface LiteralType {
   String objectToSQLString(Object var1, Dialect var2) throws Exception;
}
