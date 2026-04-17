package javax.persistence.criteria;

import javax.persistence.metamodel.ListAttribute;

public interface ListJoin extends PluralJoin {
   ListAttribute getModel();

   Expression index();
}
