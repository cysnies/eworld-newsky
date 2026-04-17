package org.hibernate.persister.entity;

import org.hibernate.QueryException;
import org.hibernate.type.Type;

public interface PropertyMapping {
   Type toType(String var1) throws QueryException;

   String[] toColumns(String var1, String var2) throws QueryException;

   String[] toColumns(String var1) throws QueryException, UnsupportedOperationException;

   Type getType();
}
