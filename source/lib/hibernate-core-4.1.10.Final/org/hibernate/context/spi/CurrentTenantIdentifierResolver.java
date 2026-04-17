package org.hibernate.context.spi;

public interface CurrentTenantIdentifierResolver {
   String resolveCurrentTenantIdentifier();

   boolean validateExistingCurrentSessions();
}
