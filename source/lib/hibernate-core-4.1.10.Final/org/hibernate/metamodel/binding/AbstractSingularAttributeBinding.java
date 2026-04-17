package org.hibernate.metamodel.binding;

import java.util.ArrayList;
import java.util.List;
import org.hibernate.AssertionFailure;
import org.hibernate.metamodel.domain.SingularAttribute;
import org.hibernate.metamodel.relational.SimpleValue;
import org.hibernate.metamodel.relational.Tuple;
import org.hibernate.metamodel.relational.Value;

public abstract class AbstractSingularAttributeBinding extends AbstractAttributeBinding implements SingularAttributeBinding {
   private Value value;
   private List simpleValueBindings = new ArrayList();
   private boolean hasDerivedValue;
   private boolean isNullable = true;

   protected AbstractSingularAttributeBinding(AttributeBindingContainer container, SingularAttribute attribute) {
      super(container, attribute);
   }

   public SingularAttribute getAttribute() {
      return (SingularAttribute)super.getAttribute();
   }

   public Value getValue() {
      return this.value;
   }

   public void setSimpleValueBindings(Iterable simpleValueBindings) {
      List<SimpleValue> values = new ArrayList();

      for(SimpleValueBinding simpleValueBinding : simpleValueBindings) {
         this.simpleValueBindings.add(simpleValueBinding);
         values.add(simpleValueBinding.getSimpleValue());
         this.hasDerivedValue = this.hasDerivedValue || simpleValueBinding.isDerived();
         this.isNullable = this.isNullable && simpleValueBinding.isNullable();
      }

      if (values.size() == 1) {
         this.value = (Value)values.get(0);
      } else {
         Tuple tuple = ((SimpleValue)values.get(0)).getTable().createTuple(this.getRole());

         for(SimpleValue value : values) {
            tuple.addValue(value);
         }

         this.value = tuple;
      }

   }

   private String getRole() {
      return this.getContainer().getPathBase() + '.' + this.getAttribute().getName();
   }

   public int getSimpleValueSpan() {
      this.checkValueBinding();
      return this.simpleValueBindings.size();
   }

   protected void checkValueBinding() {
      if (this.value == null) {
         throw new AssertionFailure("No values yet bound!");
      }
   }

   public Iterable getSimpleValueBindings() {
      return this.simpleValueBindings;
   }

   public boolean hasDerivedValue() {
      this.checkValueBinding();
      return this.hasDerivedValue;
   }

   public boolean isNullable() {
      this.checkValueBinding();
      return this.isNullable;
   }
}
