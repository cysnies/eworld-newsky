package org.hibernate.id;

import java.io.Serializable;
import java.sql.ResultSet;

public interface ResultSetIdentifierConsumer {
   Serializable consumeIdentifier(ResultSet var1);
}
