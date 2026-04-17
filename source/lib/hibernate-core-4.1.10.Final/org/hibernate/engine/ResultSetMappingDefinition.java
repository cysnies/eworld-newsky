package org.hibernate.engine;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.engine.query.spi.sql.NativeSQLQueryReturn;

public class ResultSetMappingDefinition implements Serializable {
   private final String name;
   private final List queryReturns = new ArrayList();

   public ResultSetMappingDefinition(String name) {
      this.name = name;
   }

   public String getName() {
      return this.name;
   }

   public void addQueryReturn(NativeSQLQueryReturn queryReturn) {
      this.queryReturns.add(queryReturn);
   }

   public NativeSQLQueryReturn[] getQueryReturns() {
      return (NativeSQLQueryReturn[])this.queryReturns.toArray(new NativeSQLQueryReturn[this.queryReturns.size()]);
   }
}
