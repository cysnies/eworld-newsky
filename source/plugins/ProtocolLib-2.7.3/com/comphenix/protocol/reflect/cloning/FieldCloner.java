package com.comphenix.protocol.reflect.cloning;

import com.comphenix.protocol.reflect.ObjectWriter;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.instances.InstanceProvider;
import com.comphenix.protocol.reflect.instances.NotConstructableException;

public class FieldCloner implements Cloner {
   protected Cloner defaultCloner;
   protected InstanceProvider instanceProvider;
   protected ObjectWriter writer;

   public FieldCloner(Cloner defaultCloner, InstanceProvider instanceProvider) {
      this.defaultCloner = defaultCloner;
      this.instanceProvider = instanceProvider;
      this.writer = new ObjectWriter() {
         protected void transformField(StructureModifier modifierSource, StructureModifier modifierDest, int fieldIndex) {
            FieldCloner.this.defaultTransform(modifierDest, modifierDest, FieldCloner.this.getDefaultCloner(), fieldIndex);
         }
      };
   }

   protected void defaultTransform(StructureModifier modifierSource, StructureModifier modifierDest, Cloner defaultCloner, int fieldIndex) {
      Object value = modifierSource.read(fieldIndex);
      modifierDest.write(fieldIndex, defaultCloner.clone(value));
   }

   public boolean canClone(Object source) {
      if (source == null) {
         return false;
      } else {
         try {
            return this.instanceProvider.create(source.getClass()) != null;
         } catch (NotConstructableException var3) {
            return false;
         }
      }
   }

   public Object clone(Object source) {
      if (source == null) {
         throw new IllegalArgumentException("source cannot be NULL.");
      } else {
         Object copy = this.instanceProvider.create(source.getClass());
         this.writer.copyTo(source, copy, source.getClass());
         return copy;
      }
   }

   public Cloner getDefaultCloner() {
      return this.defaultCloner;
   }

   public InstanceProvider getInstanceProvider() {
      return this.instanceProvider;
   }
}
