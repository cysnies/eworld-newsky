package javax.persistence.criteria;

import javax.persistence.metamodel.PluralAttribute;

public interface PluralJoin extends Join {
   PluralAttribute getModel();
}
