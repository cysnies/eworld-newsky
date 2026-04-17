package org.hibernate.internal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.ScrollableResults;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.hql.internal.HolderInstantiator;
import org.hibernate.loader.Loader;
import org.hibernate.type.Type;

public class ScrollableResultsImpl extends AbstractScrollableResults implements ScrollableResults {
   private Object[] currentRow;

   public ScrollableResultsImpl(ResultSet rs, PreparedStatement ps, SessionImplementor sess, Loader loader, QueryParameters queryParameters, Type[] types, HolderInstantiator holderInstantiator) throws MappingException {
      super(rs, ps, sess, loader, queryParameters, types, holderInstantiator);
   }

   protected Object[] getCurrentRow() {
      return this.currentRow;
   }

   public boolean scroll(int i) throws HibernateException {
      try {
         boolean result = this.getResultSet().relative(i);
         this.prepareCurrentRow(result);
         return result;
      } catch (SQLException sqle) {
         throw this.getSession().getFactory().getSQLExceptionHelper().convert(sqle, "could not advance using scroll()");
      }
   }

   public boolean first() throws HibernateException {
      try {
         boolean result = this.getResultSet().first();
         this.prepareCurrentRow(result);
         return result;
      } catch (SQLException sqle) {
         throw this.getSession().getFactory().getSQLExceptionHelper().convert(sqle, "could not advance using first()");
      }
   }

   public boolean last() throws HibernateException {
      try {
         boolean result = this.getResultSet().last();
         this.prepareCurrentRow(result);
         return result;
      } catch (SQLException sqle) {
         throw this.getSession().getFactory().getSQLExceptionHelper().convert(sqle, "could not advance using last()");
      }
   }

   public boolean next() throws HibernateException {
      try {
         boolean result = this.getResultSet().next();
         this.prepareCurrentRow(result);
         return result;
      } catch (SQLException sqle) {
         throw this.getSession().getFactory().getSQLExceptionHelper().convert(sqle, "could not advance using next()");
      }
   }

   public boolean previous() throws HibernateException {
      try {
         boolean result = this.getResultSet().previous();
         this.prepareCurrentRow(result);
         return result;
      } catch (SQLException sqle) {
         throw this.getSession().getFactory().getSQLExceptionHelper().convert(sqle, "could not advance using previous()");
      }
   }

   public void afterLast() throws HibernateException {
      try {
         this.getResultSet().afterLast();
      } catch (SQLException sqle) {
         throw this.getSession().getFactory().getSQLExceptionHelper().convert(sqle, "exception calling afterLast()");
      }
   }

   public void beforeFirst() throws HibernateException {
      try {
         this.getResultSet().beforeFirst();
      } catch (SQLException sqle) {
         throw this.getSession().getFactory().getSQLExceptionHelper().convert(sqle, "exception calling beforeFirst()");
      }
   }

   public boolean isFirst() throws HibernateException {
      try {
         return this.getResultSet().isFirst();
      } catch (SQLException sqle) {
         throw this.getSession().getFactory().getSQLExceptionHelper().convert(sqle, "exception calling isFirst()");
      }
   }

   public boolean isLast() throws HibernateException {
      try {
         return this.getResultSet().isLast();
      } catch (SQLException sqle) {
         throw this.getSession().getFactory().getSQLExceptionHelper().convert(sqle, "exception calling isLast()");
      }
   }

   public int getRowNumber() throws HibernateException {
      try {
         return this.getResultSet().getRow() - 1;
      } catch (SQLException sqle) {
         throw this.getSession().getFactory().getSQLExceptionHelper().convert(sqle, "exception calling getRow()");
      }
   }

   public boolean setRowNumber(int rowNumber) throws HibernateException {
      if (rowNumber >= 0) {
         ++rowNumber;
      }

      try {
         boolean result = this.getResultSet().absolute(rowNumber);
         this.prepareCurrentRow(result);
         return result;
      } catch (SQLException sqle) {
         throw this.getSession().getFactory().getSQLExceptionHelper().convert(sqle, "could not advance using absolute()");
      }
   }

   private void prepareCurrentRow(boolean underlyingScrollSuccessful) throws HibernateException {
      if (!underlyingScrollSuccessful) {
         this.currentRow = null;
      } else {
         Object result = this.getLoader().loadSingleRow(this.getResultSet(), this.getSession(), this.getQueryParameters(), false);
         if (result != null && result.getClass().isArray()) {
            this.currentRow = result;
         } else {
            this.currentRow = new Object[]{result};
         }

         if (this.getHolderInstantiator() != null) {
            this.currentRow = new Object[]{this.getHolderInstantiator().instantiate(this.currentRow)};
         }

         this.afterScrollOperation();
      }
   }
}
