package org.hibernate.hql.spi;

import java.util.Map;
import org.hibernate.MappingException;
import org.hibernate.QueryException;

public interface FilterTranslator extends QueryTranslator {
   void compile(String var1, Map var2, boolean var3) throws QueryException, MappingException;
}
