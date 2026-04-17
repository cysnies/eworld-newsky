package org.hibernate.exception.spi;

public interface ConversionContext {
   ViolatedConstraintNameExtracter getViolatedConstraintNameExtracter();
}
