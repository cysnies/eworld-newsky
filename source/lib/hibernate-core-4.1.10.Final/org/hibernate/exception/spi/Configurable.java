package org.hibernate.exception.spi;

import java.util.Properties;
import org.hibernate.HibernateException;

public interface Configurable {
   void configure(Properties var1) throws HibernateException;
}
