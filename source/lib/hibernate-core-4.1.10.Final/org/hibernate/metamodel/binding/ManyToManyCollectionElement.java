package org.hibernate.metamodel.binding;

import java.util.HashMap;
import java.util.Map;
import org.dom4j.Element;

public class ManyToManyCollectionElement extends AbstractCollectionElement {
   private final Map manyToManyFilters = new HashMap();
   private String manyToManyWhere;
   private String manyToManyOrderBy;

   ManyToManyCollectionElement(AbstractPluralAttributeBinding binding) {
      super(binding);
   }

   public CollectionElementNature getCollectionElementNature() {
      return CollectionElementNature.MANY_TO_MANY;
   }

   public void fromHbmXml(Element node) {
   }

   public String getManyToManyWhere() {
      return this.manyToManyWhere;
   }

   public void setManyToManyWhere(String manyToManyWhere) {
      this.manyToManyWhere = manyToManyWhere;
   }

   public String getManyToManyOrderBy() {
      return this.manyToManyOrderBy;
   }

   public void setManyToManyOrderBy(String manyToManyOrderBy) {
      this.manyToManyOrderBy = manyToManyOrderBy;
   }
}
