package org.hibernate.id.insert;

import java.io.Serializable;
import org.hibernate.engine.spi.SessionImplementor;

public interface InsertGeneratedIdentifierDelegate {
   IdentifierGeneratingInsert prepareIdentifierGeneratingInsert();

   Serializable performInsert(String var1, SessionImplementor var2, Binder var3);
}
