package org.hibernate.engine;

import java.util.Iterator;
import org.hibernate.JDBCException;

public interface HibernateIterator extends Iterator {
   void close() throws JDBCException;
}
