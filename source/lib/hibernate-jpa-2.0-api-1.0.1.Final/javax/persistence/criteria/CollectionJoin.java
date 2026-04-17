package javax.persistence.criteria;

import javax.persistence.metamodel.CollectionAttribute;

public interface CollectionJoin extends PluralJoin {
   CollectionAttribute getModel();
}
