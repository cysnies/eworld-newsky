package org.hibernate.context.spi;

import java.io.Serializable;
import org.hibernate.HibernateException;
import org.hibernate.Session;

public interface CurrentSessionContext extends Serializable {
   Session currentSession() throws HibernateException;
}
