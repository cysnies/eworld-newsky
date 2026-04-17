package org.hibernate.engine.spi;

public enum Status {
   MANAGED,
   READ_ONLY,
   DELETED,
   GONE,
   LOADING,
   SAVING;
}
