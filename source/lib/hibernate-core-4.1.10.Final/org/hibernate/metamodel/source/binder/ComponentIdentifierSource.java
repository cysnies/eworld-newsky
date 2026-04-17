package org.hibernate.metamodel.source.binder;

public interface ComponentIdentifierSource extends IdentifierSource {
   ComponentAttributeSource getIdentifierAttributeSource();
}
