package org.hibernate.internal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.hql.internal.HolderInstantiator;
import org.hibernate.loader.Loader;
import org.hibernate.type.Type;

public class FetchingScrollableResultsImpl extends AbstractScrollableResults {
   private Object[] currentRow = null;
   private int currentPosition = 0;
   private Integer maxPosition = null;

   public FetchingScrollableResultsImpl(ResultSet rs, PreparedStatement ps, SessionImplementor sess, Loader loader, QueryParameters queryParameters, Type[] types, HolderInstantiator holderInstantiator) throws MappingException {
      super(rs, ps, sess, loader, queryParameters, types, holderInstantiator);
   }

   protected Object[] getCurrentRow() {
      return this.currentRow;
   }

   public boolean next() throws HibernateException {
      if (this.maxPosition != null && this.maxPosition <= this.currentPosition) {
         this.currentRow = null;
         this.currentPosition = this.maxPosition + 1;
         return false;
      } else if (this.isResultSetEmpty()) {
         this.currentRow = null;
         this.currentPosition = 0;
         return false;
      } else {
         Object row = this.getLoader().loadSequentialRowsForward(this.getResultSet(), this.getSession(), this.getQueryParameters(), false);

         boolean afterLast;
         try {
            afterLast = this.getResultSet().isAfterLast();
         } catch (SQLException e) {
            throw this.getSession().getFactory().getSQLExceptionHelper().convert(e, "exception calling isAfterLast()");
         }

         ++this.currentPosition;
         this.currentRow = new Object[]{row};
         if (afterLast && this.maxPosition == null) {
            this.maxPosition = this.currentPosition;
         }

         this.afterScrollOperation();
         return true;
      }
   }

   public boolean previous() throws HibernateException {
      if (this.currentPosition <= 1) {
         this.currentPosition = 0;
         this.currentRow = null;
         return false;
      } else {
         Object loadResult = this.getLoader().loadSequentialRowsReverse(this.getResultSet(), this.getSession(), this.getQueryParameters(), false, this.maxPosition != null && this.currentPosition > this.maxPosition);
         this.currentRow = new Object[]{loadResult};
         --this.currentPosition;
         this.afterScrollOperation();
         return true;
      }
   }

   public boolean scroll(int positions) throws HibernateException {
      boolean more = false;
      if (positions > 0) {
         for(int i = 0; i < positions; ++i) {
            more = this.next();
            if (!more) {
               break;
            }
         }
      } else {
         if (positions >= 0) {
            throw new HibernateException("scroll(0) not valid");
         }

         for(int i = 0; i < 0 - positions; ++i) {
            more = this.previous();
            if (!more) {
               break;
            }
         }
      }

      this.afterScrollOperation();
      return more;
   }

   public boolean last() throws HibernateException {
      boolean more = false;
      if (this.maxPosition != null) {
         if (this.currentPosition > this.maxPosition) {
            more = this.previous();
         }

         for(int i = this.currentPosition; i < this.maxPosition; ++i) {
            more = this.next();
         }
      } else {
         try {
            label36: {
               if (!this.isResultSetEmpty() && !this.getResultSet().isAfterLast()) {
                  while(true) {
                     if (this.getResultSet().isAfterLast()) {
                        break label36;
                     }

                     more = this.next();
                  }
               }

               return false;
            }
         } catch (SQLException e) {
            throw this.getSession().getFactory().getSQLExceptionHelper().convert(e, "exception calling isAfterLast()");
         }
      }

      this.afterScrollOperation();
      return more;
   }

   public boolean first() throws HibernateException {
      this.beforeFirst();
      boolean more = this.next();
      this.afterScrollOperation();
      return more;
   }

   public void beforeFirst() throws HibernateException {
      try {
         this.getResultSet().beforeFirst();
      } catch (SQLException e) {
         throw this.getSession().getFactory().getSQLExceptionHelper().convert(e, "exception calling beforeFirst()");
      }

      this.currentRow = null;
      this.currentPosition = 0;
   }

   public void afterLast() throws HibernateException {
      this.last();
      this.next();
      this.afterScrollOperation();
   }

   public boolean isFirst() throws HibernateException {
      return this.currentPosition == 1;
   }

   public boolean isLast() throws HibernateException {
      if (this.maxPosition == null) {
         return false;
      } else {
         return this.currentPosition == this.maxPosition;
      }
   }

   public int getRowNumber() throws HibernateException {
      return this.currentPosition;
   }

   public boolean setRowNumber(int rowNumber) throws HibernateException {
      if (rowNumber == 1) {
         return this.first();
      } else if (rowNumber == -1) {
         return this.last();
      } else {
         return this.maxPosition != null && rowNumber == this.maxPosition ? this.last() : this.scroll(rowNumber - this.currentPosition);
      }
   }

   private boolean isResultSetEmpty() {
      try {
         return this.currentPosition == 0 && !this.getResultSet().isBeforeFirst() && !this.getResultSet().isAfterLast();
      } catch (SQLException e) {
         throw this.getSession().getFactory().getSQLExceptionHelper().convert(e, "Could not determine if resultset is empty due to exception calling isBeforeFirst or isAfterLast()");
      }
   }
}
