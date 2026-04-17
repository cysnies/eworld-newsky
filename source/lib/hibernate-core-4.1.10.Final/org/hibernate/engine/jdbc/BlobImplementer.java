package org.hibernate.engine.jdbc;

public interface BlobImplementer {
   BinaryStream getUnderlyingStream();
}
