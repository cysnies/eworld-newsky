package org.hibernate.engine.jdbc.internal;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.hibernate.HibernateException;
import org.hibernate.engine.jdbc.spi.InvalidatableWrapper;
import org.hibernate.engine.jdbc.spi.JdbcResourceRegistry;
import org.hibernate.engine.jdbc.spi.JdbcWrapper;
import org.hibernate.engine.jdbc.spi.SqlExceptionHelper;
import org.hibernate.internal.CoreMessageLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;

public class JdbcResourceRegistryImpl implements JdbcResourceRegistry {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, JdbcResourceRegistryImpl.class.getName());
   private final HashMap xref = new HashMap();
   private final Set unassociatedResultSets = new HashSet();
   private final SqlExceptionHelper exceptionHelper;
   private Statement lastQuery;

   public JdbcResourceRegistryImpl(SqlExceptionHelper exceptionHelper) {
      this.exceptionHelper = exceptionHelper;
   }

   public void register(Statement statement) {
      LOG.tracev("Registering statement [{0}]", statement);
      if (this.xref.containsKey(statement)) {
         throw new HibernateException("statement already registered with JDBCContainer");
      } else {
         this.xref.put(statement, (Object)null);
      }
   }

   public void registerLastQuery(Statement statement) {
      LOG.tracev("Registering last query statement [{0}]", statement);
      if (statement instanceof JdbcWrapper) {
         JdbcWrapper<Statement> wrapper = (JdbcWrapper)statement;
         this.registerLastQuery((Statement)wrapper.getWrappedObject());
      } else {
         this.lastQuery = statement;
      }
   }

   public void cancelLastQuery() {
      try {
         if (this.lastQuery != null) {
            this.lastQuery.cancel();
         }
      } catch (SQLException sqle) {
         throw this.exceptionHelper.convert(sqle, "Cannot cancel query");
      } finally {
         this.lastQuery = null;
      }

   }

   public void release(Statement statement) {
      LOG.tracev("Releasing statement [{0}]", statement);
      Set<ResultSet> resultSets = (Set)this.xref.get(statement);
      if (resultSets != null) {
         for(ResultSet resultSet : resultSets) {
            this.close(resultSet);
         }

         resultSets.clear();
      }

      this.xref.remove(statement);
      this.close(statement);
   }

   public void register(ResultSet resultSet) {
      LOG.tracev("Registering result set [{0}]", resultSet);

      Statement statement;
      try {
         statement = resultSet.getStatement();
      } catch (SQLException e) {
         throw this.exceptionHelper.convert(e, "unable to access statement from resultset");
      }

      if (statement != null) {
         if (LOG.isEnabled(Level.WARN) && !this.xref.containsKey(statement)) {
            LOG.unregisteredStatement();
         }

         Set<ResultSet> resultSets = (Set)this.xref.get(statement);
         if (resultSets == null) {
            resultSets = new HashSet();
            this.xref.put(statement, resultSets);
         }

         resultSets.add(resultSet);
      } else {
         this.unassociatedResultSets.add(resultSet);
      }

   }

   public void release(ResultSet resultSet) {
      LOG.tracev("Releasing result set [{0}]", resultSet);

      Statement statement;
      try {
         statement = resultSet.getStatement();
      } catch (SQLException e) {
         throw this.exceptionHelper.convert(e, "unable to access statement from resultset");
      }

      if (statement != null) {
         if (LOG.isEnabled(Level.WARN) && !this.xref.containsKey(statement)) {
            LOG.unregisteredStatement();
         }

         Set<ResultSet> resultSets = (Set)this.xref.get(statement);
         if (resultSets != null) {
            resultSets.remove(resultSet);
            if (resultSets.isEmpty()) {
               this.xref.remove(statement);
            }
         }
      } else {
         boolean removed = this.unassociatedResultSets.remove(resultSet);
         if (!removed) {
            LOG.unregisteredResultSetWithoutStatement();
         }
      }

      this.close(resultSet);
   }

   public boolean hasRegisteredResources() {
      return !this.xref.isEmpty() || !this.unassociatedResultSets.isEmpty();
   }

   public void releaseResources() {
      LOG.tracev("Releasing JDBC container resources [{0}]", this);
      this.cleanup();
   }

   private void cleanup() {
      for(Map.Entry entry : this.xref.entrySet()) {
         if (entry.getValue() != null) {
            this.closeAll((Set)entry.getValue());
         }

         this.close((Statement)entry.getKey());
      }

      this.xref.clear();
      this.closeAll(this.unassociatedResultSets);
   }

   protected void closeAll(Set resultSets) {
      for(ResultSet resultSet : resultSets) {
         this.close(resultSet);
      }

      resultSets.clear();
   }

   public void close() {
      LOG.tracev("Closing JDBC container [{0}]", this);
      this.cleanup();
   }

   protected void close(Statement statement) {
      LOG.tracev("Closing prepared statement [{0}]", statement);
      if (statement instanceof InvalidatableWrapper) {
         InvalidatableWrapper<Statement> wrapper = (InvalidatableWrapper)statement;
         this.close((Statement)wrapper.getWrappedObject());
         wrapper.invalidate();
      } else {
         try {
            try {
               if (statement.getMaxRows() != 0) {
                  statement.setMaxRows(0);
               }

               if (statement.getQueryTimeout() != 0) {
                  statement.setQueryTimeout(0);
               }
            } catch (SQLException sqle) {
               if (LOG.isDebugEnabled()) {
                  LOG.debugf("Exception clearing maxRows/queryTimeout [%s]", sqle.getMessage());
               }

               return;
            }

            statement.close();
            if (this.lastQuery == statement) {
               this.lastQuery = null;
            }
         } catch (SQLException e) {
            LOG.debugf("Unable to release JDBC statement [%s]", e.getMessage());
         } catch (Exception e) {
            LOG.debugf("Unable to release JDBC statement [%s]", e.getMessage());
         }

      }
   }

   protected void close(ResultSet resultSet) {
      LOG.tracev("Closing result set [{0}]", resultSet);
      if (resultSet instanceof InvalidatableWrapper) {
         InvalidatableWrapper<ResultSet> wrapper = (InvalidatableWrapper)resultSet;
         this.close((ResultSet)wrapper.getWrappedObject());
         wrapper.invalidate();
      } else {
         try {
            resultSet.close();
         } catch (SQLException e) {
            LOG.debugf("Unable to release JDBC result set [%s]", e.getMessage());
         } catch (Exception e) {
            LOG.debugf("Unable to release JDBC result set [%s]", e.getMessage());
         }

      }
   }
}
