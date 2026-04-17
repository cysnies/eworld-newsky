package org.hibernate.service.jdbc.dialect.spi;

import java.sql.Connection;
import java.util.Map;
import org.hibernate.HibernateException;
import org.hibernate.dialect.Dialect;
import org.hibernate.service.Service;

public interface DialectFactory extends Service {
   Dialect buildDialect(Map var1, Connection var2) throws HibernateException;
}
