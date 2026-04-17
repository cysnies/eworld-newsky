package com.comphenix.protocol.reflect.compiler;

import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.instances.DefaultInstances;
import com.google.common.collect.Sets;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

public abstract class CompiledStructureModifier extends StructureModifier {
   protected StructureCompiler compiler;
   private Set exempted;

   public CompiledStructureModifier() {
      this.customConvertHandling = true;
   }

   public void setReadOnly(int fieldIndex, boolean value) throws FieldAccessException {
      if (this.isReadOnly(fieldIndex) && !value) {
         if (this.exempted == null) {
            this.exempted = Sets.newHashSet();
         }

         this.exempted.add(fieldIndex);
      }

      if (this.isReadOnly(fieldIndex) || !value || this.exempted != null && this.exempted.contains(fieldIndex)) {
         super.setReadOnly(fieldIndex, value);
      } else {
         throw new IllegalStateException("Cannot make compiled field " + fieldIndex + " read only.");
      }
   }

   public StructureModifier writeDefaults() throws FieldAccessException {
      DefaultInstances generator = DefaultInstances.DEFAULT;

      for(Map.Entry entry : this.defaultFields.entrySet()) {
         Integer index = (Integer)entry.getValue();
         Field field = (Field)entry.getKey();
         this.write(index, generator.getDefault(field.getType()));
      }

      return this;
   }

   public final Object read(int fieldIndex) throws FieldAccessException {
      Object result = this.readGenerated(fieldIndex);
      return this.converter != null ? this.converter.getSpecific(result) : result;
   }

   protected Object readReflected(int index) throws FieldAccessException {
      return super.read(index);
   }

   protected abstract Object readGenerated(int var1) throws FieldAccessException;

   public StructureModifier write(int index, Object value) throws FieldAccessException {
      if (this.converter != null) {
         value = this.converter.getGeneric(this.getFieldType(index), value);
      }

      return this.writeGenerated(index, value);
   }

   protected void writeReflected(int index, Object value) throws FieldAccessException {
      super.write(index, value);
   }

   protected abstract StructureModifier writeGenerated(int var1, Object var2) throws FieldAccessException;

   public StructureModifier withTarget(Object target) {
      return this.compiler != null ? this.compiler.compile(super.withTarget(target)) : super.withTarget(target);
   }
}
