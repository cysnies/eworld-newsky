package org.hibernate.exception.internal;

import org.hibernate.exception.spi.ConversionContext;
import org.hibernate.exception.spi.SQLExceptionConverter;
import org.hibernate.exception.spi.ViolatedConstraintNameExtracter;

/** @deprecated */
@Deprecated
public class SQLStateConverter extends StandardSQLExceptionConverter implements SQLExceptionConverter {
   public SQLStateConverter(final ViolatedConstraintNameExtracter extracter) {
      ConversionContext conversionContext = new ConversionContext() {
         public ViolatedConstraintNameExtracter getViolatedConstraintNameExtracter() {
            return extracter;
         }
      };
      this.addDelegate(new SQLStateConversionDelegate(conversionContext));
   }
}
