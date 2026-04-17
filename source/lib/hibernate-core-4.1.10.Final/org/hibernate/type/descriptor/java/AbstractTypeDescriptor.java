package org.hibernate.type.descriptor.java;

import java.io.Serializable;
import java.util.Comparator;
import org.hibernate.HibernateException;
import org.hibernate.internal.util.compare.ComparableComparator;
import org.hibernate.internal.util.compare.EqualsHelper;

public abstract class AbstractTypeDescriptor implements JavaTypeDescriptor, Serializable {
   private final Class type;
   private final MutabilityPlan mutabilityPlan;
   private final Comparator comparator;

   protected AbstractTypeDescriptor(Class type) {
      this(type, ImmutableMutabilityPlan.INSTANCE);
   }

   protected AbstractTypeDescriptor(Class type, MutabilityPlan mutabilityPlan) {
      this.type = type;
      this.mutabilityPlan = mutabilityPlan;
      this.comparator = Comparable.class.isAssignableFrom(type) ? ComparableComparator.INSTANCE : null;
   }

   public MutabilityPlan getMutabilityPlan() {
      return this.mutabilityPlan;
   }

   public Class getJavaTypeClass() {
      return this.type;
   }

   public int extractHashCode(Object value) {
      return value.hashCode();
   }

   public boolean areEqual(Object one, Object another) {
      return EqualsHelper.equals(one, another);
   }

   public Comparator getComparator() {
      return this.comparator;
   }

   public String extractLoggableRepresentation(Object value) {
      return value == null ? "null" : value.toString();
   }

   protected HibernateException unknownUnwrap(Class conversionType) {
      throw new HibernateException("Unknown unwrap conversion requested: " + this.type.getName() + " to " + conversionType.getName());
   }

   protected HibernateException unknownWrap(Class conversionType) {
      throw new HibernateException("Unknown wrap conversion requested: " + conversionType.getName() + " to " + this.type.getName());
   }
}
