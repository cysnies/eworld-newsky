package org.hibernate.type.descriptor;

import org.hibernate.engine.jdbc.LobCreator;
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor;

public interface WrapperOptions {
   boolean useStreamForLobBinding();

   LobCreator getLobCreator();

   SqlTypeDescriptor remapSqlTypeDescriptor(SqlTypeDescriptor var1);
}
