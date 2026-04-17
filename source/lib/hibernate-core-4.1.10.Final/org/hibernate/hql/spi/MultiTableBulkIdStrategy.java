package org.hibernate.hql.spi;

import java.util.Map;
import org.hibernate.cfg.Mappings;
import org.hibernate.engine.jdbc.spi.JdbcConnectionAccess;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.hql.internal.ast.HqlSqlWalker;
import org.hibernate.persister.entity.Queryable;

public interface MultiTableBulkIdStrategy {
   void prepare(JdbcServices var1, JdbcConnectionAccess var2, Mappings var3, Mapping var4, Map var5);

   void release(JdbcServices var1, JdbcConnectionAccess var2);

   UpdateHandler buildUpdateHandler(SessionFactoryImplementor var1, HqlSqlWalker var2);

   DeleteHandler buildDeleteHandler(SessionFactoryImplementor var1, HqlSqlWalker var2);

   public interface DeleteHandler {
      Queryable getTargetedQueryable();

      String[] getSqlStatements();

      int execute(SessionImplementor var1, QueryParameters var2);
   }

   public interface UpdateHandler {
      Queryable getTargetedQueryable();

      String[] getSqlStatements();

      int execute(SessionImplementor var1, QueryParameters var2);
   }
}
