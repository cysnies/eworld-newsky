package org.hibernate.hql.spi;

import java.util.Map;
import org.hibernate.engine.spi.SessionFactoryImplementor;

public interface QueryTranslatorFactory {
   QueryTranslator createQueryTranslator(String var1, String var2, Map var3, SessionFactoryImplementor var4);

   FilterTranslator createFilterTranslator(String var1, String var2, Map var3, SessionFactoryImplementor var4);
}
