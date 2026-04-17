package javax.persistence.criteria;

import javax.persistence.metamodel.SetAttribute;

public interface SetJoin extends PluralJoin {
   SetAttribute getModel();
}
