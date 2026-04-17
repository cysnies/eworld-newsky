package com.comphenix.protocol.reflect.cloning;

import com.comphenix.protocol.reflect.instances.DefaultInstances;
import com.comphenix.protocol.reflect.instances.ExistingGenerator;
import com.comphenix.protocol.reflect.instances.InstanceProvider;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

public class AggregateCloner implements Cloner {
   public static final AggregateCloner DEFAULT;
   private List cloners;
   private WeakReference lastObject;
   private int lastResult;

   public static Builder newBuilder() {
      return new Builder();
   }

   private AggregateCloner() {
   }

   public List getCloners() {
      return Collections.unmodifiableList(this.cloners);
   }

   private void setCloners(Iterable cloners) {
      this.cloners = Lists.newArrayList(cloners);
   }

   public boolean canClone(Object source) {
      this.lastResult = this.getFirstCloner(source);
      this.lastObject = new WeakReference(source);
      return this.lastResult >= 0 && this.lastResult < this.cloners.size();
   }

   private int getFirstCloner(Object source) {
      for(int i = 0; i < this.cloners.size(); ++i) {
         if (((Cloner)this.cloners.get(i)).canClone(source)) {
            return i;
         }
      }

      return this.cloners.size();
   }

   public Object clone(Object source) {
      if (source == null) {
         throw new IllegalAccessError("source cannot be NULL.");
      } else {
         int index = 0;
         if (this.lastObject != null && this.lastObject.get() == source) {
            index = this.lastResult;
         } else {
            index = this.getFirstCloner(source);
         }

         if (index < this.cloners.size()) {
            return ((Cloner)this.cloners.get(index)).clone(source);
         } else {
            throw new IllegalArgumentException("Cannot clone " + source + ": No cloner is sutable.");
         }
      }
   }

   static {
      DEFAULT = newBuilder().instanceProvider(DefaultInstances.DEFAULT).andThen(BukkitCloner.class).andThen(ImmutableDetector.class).andThen(CollectionCloner.class).andThen(FieldCloner.class).build();
   }

   public static class BuilderParameters {
      private InstanceProvider instanceProvider;
      private Cloner aggregateCloner;
      private InstanceProvider typeConstructor;

      private BuilderParameters() {
      }

      public InstanceProvider getInstanceProvider() {
         return this.instanceProvider;
      }

      public Cloner getAggregateCloner() {
         return this.aggregateCloner;
      }
   }

   public static class Builder {
      private List factories = Lists.newArrayList();
      private BuilderParameters parameters = new BuilderParameters();

      public Builder instanceProvider(InstanceProvider provider) {
         this.parameters.instanceProvider = provider;
         return this;
      }

      public Builder andThen(final Class type) {
         return this.andThen(new Function() {
            public Cloner apply(@Nullable BuilderParameters param) {
               Object result = param.typeConstructor.create(type);
               if (result == null) {
                  throw new IllegalStateException("Constructed NULL instead of " + type);
               } else if (type.isAssignableFrom(result.getClass())) {
                  return (Cloner)result;
               } else {
                  throw new IllegalStateException("Constructed " + result.getClass() + " instead of " + type);
               }
            }
         });
      }

      public Builder andThen(Function factory) {
         this.factories.add(factory);
         return this;
      }

      public AggregateCloner build() {
         AggregateCloner newCloner = new AggregateCloner();
         Cloner paramCloner = new NullableCloner(newCloner);
         InstanceProvider paramProvider = this.parameters.instanceProvider;
         this.parameters.aggregateCloner = paramCloner;
         this.parameters.typeConstructor = DefaultInstances.fromArray(ExistingGenerator.fromObjectArray(new Object[]{paramCloner, paramProvider}));
         List<Cloner> cloners = Lists.newArrayList();

         for(int i = 0; i < this.factories.size(); ++i) {
            Cloner cloner = (Cloner)((Function)this.factories.get(i)).apply(this.parameters);
            if (cloner == null) {
               throw new IllegalArgumentException(String.format("Cannot create cloner from %s (%s)", this.factories.get(i), i));
            }

            cloners.add(cloner);
         }

         newCloner.setCloners(cloners);
         return newCloner;
      }
   }
}
