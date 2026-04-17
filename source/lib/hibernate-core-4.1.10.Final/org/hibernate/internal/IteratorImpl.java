package org.hibernate.internal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.Set;
import org.hibernate.HibernateException;
import org.hibernate.JDBCException;
import org.hibernate.engine.HibernateIterator;
import org.hibernate.event.spi.EventSource;
import org.hibernate.hql.internal.HolderInstantiator;
import org.hibernate.type.EntityType;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

public final class IteratorImpl implements HibernateIterator {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, IteratorImpl.class.getName());
   private ResultSet rs;
   private final EventSource session;
   private boolean readOnly;
   private final Type[] types;
   private final boolean single;
   private Object currentResult;
   private boolean hasNext;
   private final String[][] names;
   private PreparedStatement ps;
   private HolderInstantiator holderInstantiator;

   public IteratorImpl(ResultSet rs, PreparedStatement ps, EventSource sess, boolean readOnly, Type[] types, String[][] columnNames, HolderInstantiator holderInstantiator) throws HibernateException, SQLException {
      this.rs = rs;
      this.ps = ps;
      this.session = sess;
      this.readOnly = readOnly;
      this.types = types;
      this.names = columnNames;
      this.holderInstantiator = holderInstantiator;
      this.single = types.length == 1;
      this.postNext();
   }

   public void close() throws JDBCException {
      if (this.ps != null) {
         try {
            LOG.debug("Closing iterator");
            this.ps.close();
            this.ps = null;
            this.rs = null;
            this.hasNext = false;
         } catch (SQLException e) {
            LOG.unableToCloseIterator(e);
            throw this.session.getFactory().getSQLExceptionHelper().convert(e, "Unable to close iterator");
         } finally {
            try {
               this.session.getPersistenceContext().getLoadContexts().cleanup(this.rs);
            } catch (Throwable ignore) {
               LOG.debugf("Exception trying to cleanup load context : %s", ignore.getMessage());
            }

         }
      }

   }

   private void postNext() throws SQLException {
      LOG.debug("Attempting to retrieve next results");
      this.hasNext = this.rs.next();
      if (!this.hasNext) {
         LOG.debug("Exhausted results");
         this.close();
      } else {
         LOG.debug("Retrieved next results");
      }

   }

   public boolean hasNext() {
      return this.hasNext;
   }

   public Object next() throws HibernateException {
      if (!this.hasNext) {
         throw new NoSuchElementException("No more results");
      } else {
         boolean sessionDefaultReadOnlyOrig = this.session.isDefaultReadOnly();
         this.session.setDefaultReadOnly(this.readOnly);

         Object[] currentResults;
         try {
            boolean isHolder = this.holderInstantiator.isRequired();
            LOG.debugf("Assembling results", new Object[0]);
            if (this.single && !isHolder) {
               this.currentResult = this.types[0].nullSafeGet(this.rs, (String[])this.names[0], this.session, (Object)null);
            } else {
               currentResults = new Object[this.types.length];

               for(int i = 0; i < this.types.length; ++i) {
                  currentResults[i] = this.types[i].nullSafeGet(this.rs, (String[])this.names[i], this.session, (Object)null);
               }

               if (isHolder) {
                  this.currentResult = this.holderInstantiator.instantiate(currentResults);
               } else {
                  this.currentResult = currentResults;
               }
            }

            this.postNext();
            LOG.debugf("Returning current results", new Object[0]);
            currentResults = (Object[])this.currentResult;
         } catch (SQLException sqle) {
            throw this.session.getFactory().getSQLExceptionHelper().convert(sqle, "could not get next iterator result");
         } finally {
            this.session.setDefaultReadOnly(sessionDefaultReadOnlyOrig);
         }

         return currentResults;
      }
   }

   public void remove() {
      if (!this.single) {
         throw new UnsupportedOperationException("Not a single column hibernate query result set");
      } else if (this.currentResult == null) {
         throw new IllegalStateException("Called Iterator.remove() before next()");
      } else if (!(this.types[0] instanceof EntityType)) {
         throw new UnsupportedOperationException("Not an entity");
      } else {
         this.session.delete(((EntityType)this.types[0]).getAssociatedEntityName(), this.currentResult, false, (Set)null);
      }
   }
}
