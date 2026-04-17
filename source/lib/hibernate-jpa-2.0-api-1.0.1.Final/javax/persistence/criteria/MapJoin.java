package javax.persistence.criteria;

import javax.persistence.metamodel.MapAttribute;

public interface MapJoin extends PluralJoin {
   MapAttribute getModel();

   Path key();

   Path value();

   Expression entry();
}
