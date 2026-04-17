package javax.persistence.criteria;

import javax.persistence.metamodel.EntityType;

public interface Root extends From {
   EntityType getModel();
}
