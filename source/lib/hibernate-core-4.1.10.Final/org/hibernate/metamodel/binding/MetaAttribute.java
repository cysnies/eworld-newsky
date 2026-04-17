package org.hibernate.metamodel.binding;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MetaAttribute implements Serializable {
   private final String name;
   private List values = new ArrayList();

   public MetaAttribute(String name) {
      this.name = name;
   }

   public String getName() {
      return this.name;
   }

   public List getValues() {
      return Collections.unmodifiableList(this.values);
   }

   public void addValue(String value) {
      this.values.add(value);
   }

   public String getValue() {
      if (this.values.size() != 1) {
         throw new IllegalStateException("no unique value");
      } else {
         return (String)this.values.get(0);
      }
   }

   public boolean isMultiValued() {
      return this.values.size() > 1;
   }

   public String toString() {
      return "[" + this.name + "=" + this.values + "]";
   }
}
