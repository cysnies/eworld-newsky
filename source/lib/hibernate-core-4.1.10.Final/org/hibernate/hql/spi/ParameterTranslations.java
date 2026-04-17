package org.hibernate.hql.spi;

import java.util.Set;
import org.hibernate.type.Type;

public interface ParameterTranslations {
   boolean supportsOrdinalParameterMetadata();

   int getOrdinalParameterCount();

   int getOrdinalParameterSqlLocation(int var1);

   Type getOrdinalParameterExpectedType(int var1);

   Set getNamedParameterNames();

   int[] getNamedParameterSqlLocations(String var1);

   Type getNamedParameterExpectedType(String var1);
}
