package org.hibernate.metamodel.source.binder;

public interface SimpleIdentifierSource extends IdentifierSource {
   SingularAttributeSource getIdentifierAttributeSource();
}
