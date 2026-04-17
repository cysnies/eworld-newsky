package org.hibernate.engine.jdbc;

import java.sql.Blob;

public interface WrappedBlob {
   Blob getWrappedBlob();
}
