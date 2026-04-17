package org.hibernate.engine.jdbc;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;

public abstract class AbstractLobCreator implements LobCreator {
   public Blob wrap(Blob blob) {
      return SerializableBlobProxy.generateProxy(blob);
   }

   public Clob wrap(Clob clob) {
      return (Clob)(NClob.class.isInstance(clob) ? this.wrap((NClob)clob) : SerializableClobProxy.generateProxy(clob));
   }

   public NClob wrap(NClob nclob) {
      return SerializableNClobProxy.generateProxy(nclob);
   }
}
