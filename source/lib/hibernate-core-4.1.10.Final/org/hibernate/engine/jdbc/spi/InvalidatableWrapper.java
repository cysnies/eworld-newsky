package org.hibernate.engine.jdbc.spi;

public interface InvalidatableWrapper extends JdbcWrapper {
   void invalidate();
}
