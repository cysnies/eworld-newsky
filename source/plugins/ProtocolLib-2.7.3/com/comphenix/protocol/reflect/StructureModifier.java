package com.comphenix.protocol.reflect;

import com.comphenix.protocol.reflect.compiler.BackgroundCompiler;
import com.comphenix.protocol.reflect.instances.BannedGenerator;
import com.comphenix.protocol.reflect.instances.DefaultInstances;
import com.comphenix.protocol.reflect.instances.InstanceProvider;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StructureModifier {
   protected Class targetType;
   protected Object target;
   protected EquivalentConverter converter;
   protected Class fieldType;
   protected List data;
   protected Map defaultFields;
   protected Map subtypeCache;
   protected boolean customConvertHandling;
   protected boolean useStructureCompiler;
   private static DefaultInstances DEFAULT_GENERATOR = getDefaultGenerator();

   private static DefaultInstances getDefaultGenerator() {
      List<InstanceProvider> providers = Lists.newArrayList();
      providers.add(new BannedGenerator(new Class[]{MinecraftReflection.getItemStackClass(), MinecraftReflection.getBlockClass()}));
      providers.addAll(DefaultInstances.DEFAULT.getRegistered());
      return DefaultInstances.fromCollection(providers);
   }

   public StructureModifier(Class targetType) {
      this(targetType, (Class)null, true);
   }

   public StructureModifier(Class targetType, boolean useStructureCompiler) {
      this(targetType, (Class)null, true, useStructureCompiler);
   }

   public StructureModifier(Class targetType, Class superclassExclude, boolean requireDefault) {
      this(targetType, superclassExclude, requireDefault, true);
   }

   public StructureModifier(Class targetType, Class superclassExclude, boolean requireDefault, boolean useStructureCompiler) {
      this.data = new ArrayList();
      List<Field> fields = getFields(targetType, superclassExclude);
      Map<Field, Integer> defaults = (Map<Field, Integer>)(requireDefault ? generateDefaultFields(fields) : new HashMap());
      this.initialize(targetType, Object.class, fields, defaults, (EquivalentConverter)null, new ConcurrentHashMap(), useStructureCompiler);
   }

   protected StructureModifier() {
      this.data = new ArrayList();
   }

   protected void initialize(StructureModifier other) {
      this.initialize(other.targetType, other.fieldType, other.data, other.defaultFields, other.converter, other.subtypeCache, other.useStructureCompiler);
   }

   protected void initialize(Class targetType, Class fieldType, List data, Map defaultFields, EquivalentConverter converter, Map subTypeCache) {
      this.initialize(targetType, fieldType, data, defaultFields, converter, subTypeCache, true);
   }

   protected void initialize(Class targetType, Class fieldType, List data, Map defaultFields, EquivalentConverter converter, Map subTypeCache, boolean useStructureCompiler) {
      this.targetType = targetType;
      this.fieldType = fieldType;
      this.data = data;
      this.defaultFields = defaultFields;
      this.converter = converter;
      this.subtypeCache = subTypeCache;
      this.useStructureCompiler = useStructureCompiler;
   }

   public Object read(int fieldIndex) throws FieldAccessException {
      if (fieldIndex >= 0 && fieldIndex < this.data.size()) {
         if (this.target == null) {
            throw new IllegalStateException("Cannot read from a NULL target.");
         } else {
            try {
               Object result = FieldUtils.readField((Field)this.data.get(fieldIndex), this.target, true);
               return this.needConversion() ? this.converter.getSpecific(result) : result;
            } catch (IllegalAccessException e) {
               throw new FieldAccessException("Cannot read field due to a security limitation.", e);
            }
         }
      } else {
         throw new FieldAccessException("Field index must be within 0 - count", new IndexOutOfBoundsException("Out of bounds"));
      }
   }

   public Object readSafely(int fieldIndex) throws FieldAccessException {
      return fieldIndex >= 0 && fieldIndex < this.data.size() ? this.read(fieldIndex) : null;
   }

   public boolean isReadOnly(int fieldIndex) {
      return Modifier.isFinal(this.getField(fieldIndex).getModifiers());
   }

   public boolean isPublic(int fieldIndex) {
      return Modifier.isPublic(this.getField(fieldIndex).getModifiers());
   }

   public void setReadOnly(int fieldIndex, boolean value) throws FieldAccessException {
      if (fieldIndex >= 0 && fieldIndex < this.data.size()) {
         try {
            setFinalState((Field)this.data.get(fieldIndex), value);
         } catch (IllegalAccessException e) {
            throw new FieldAccessException("Cannot write read only status due to a security limitation.", e);
         }
      } else {
         throw new IllegalArgumentException("Index parameter is not within [0 - " + this.data.size() + ")");
      }
   }

   protected static void setFinalState(Field field, boolean isReadOnly) throws IllegalAccessException {
      if (isReadOnly) {
         FieldUtils.writeField((Object)field, (String)"modifiers", field.getModifiers() | 16, true);
      } else {
         FieldUtils.writeField((Object)field, (String)"modifiers", field.getModifiers() & -17, true);
      }

   }

   public StructureModifier write(int fieldIndex, Object value) throws FieldAccessException {
      if (fieldIndex >= 0 && fieldIndex < this.data.size()) {
         if (this.target == null) {
            throw new IllegalStateException("Cannot write to a NULL target.");
         } else {
            Object obj = this.needConversion() ? this.converter.getGeneric(this.getFieldType(fieldIndex), value) : value;

            try {
               FieldUtils.writeField((Field)this.data.get(fieldIndex), this.target, obj, true);
               return this;
            } catch (IllegalAccessException e) {
               throw new FieldAccessException("Cannot read field due to a security limitation.", e);
            }
         }
      } else {
         throw new FieldAccessException("Field index must be within 0 - count", new IndexOutOfBoundsException("Out of bounds"));
      }
   }

   protected Class getFieldType(int index) {
      return ((Field)this.data.get(index)).getType();
   }

   private final boolean needConversion() {
      return this.converter != null && !this.customConvertHandling;
   }

   public StructureModifier writeSafely(int fieldIndex, Object value) throws FieldAccessException {
      if (fieldIndex >= 0 && fieldIndex < this.data.size()) {
         this.write(fieldIndex, value);
      }

      return this;
   }

   public StructureModifier modify(int fieldIndex, Function select) throws FieldAccessException {
      TField value = (TField)this.read(fieldIndex);
      return this.write(fieldIndex, select.apply(value));
   }

   public StructureModifier withType(Class fieldType) {
      return this.withType(fieldType, (EquivalentConverter)null);
   }

   public StructureModifier writeDefaults() throws FieldAccessException {
      DefaultInstances generator = DefaultInstances.DEFAULT;

      for(Field field : this.defaultFields.keySet()) {
         try {
            FieldUtils.writeField(field, this.target, generator.getDefault(field.getType()), true);
         } catch (IllegalAccessException e) {
            throw new FieldAccessException("Cannot write to field due to a security limitation.", e);
         }
      }

      return this;
   }

   public StructureModifier withType(Class fieldType, EquivalentConverter converter) {
      StructureModifier<T> result = (StructureModifier)this.subtypeCache.get(fieldType);
      if (result == null) {
         List<Field> filtered = new ArrayList();
         Map<Field, Integer> defaults = new HashMap();
         int index = 0;

         for(Field field : this.data) {
            if (fieldType != null && fieldType.isAssignableFrom(field.getType())) {
               filtered.add(field);
               if (this.defaultFields.containsKey(field)) {
                  defaults.put(field, index);
               }
            }

            ++index;
         }

         result = this.withFieldType(fieldType, filtered, defaults, converter);
         if (fieldType != null) {
            this.subtypeCache.put(fieldType, result);
            if (this.useStructureCompiler && BackgroundCompiler.getInstance() != null) {
               BackgroundCompiler.getInstance().scheduleCompilation(this.subtypeCache, fieldType);
            }
         }
      }

      result = result.withTarget(this.target);
      if (!Objects.equal(result.converter, converter)) {
         result = result.withConverter(converter);
      }

      return result;
   }

   public Class getFieldType() {
      return this.fieldType;
   }

   public Class getTargetType() {
      return this.targetType;
   }

   public Object getTarget() {
      return this.target;
   }

   public int size() {
      return this.data.size();
   }

   protected StructureModifier withFieldType(Class fieldType, List filtered, Map defaults, EquivalentConverter converter) {
      StructureModifier<T> result = new StructureModifier();
      result.initialize(this.targetType, fieldType, filtered, defaults, converter, new ConcurrentHashMap(), this.useStructureCompiler);
      return result;
   }

   public StructureModifier withTarget(Object target) {
      StructureModifier<TField> copy = new StructureModifier();
      copy.initialize(this);
      copy.target = target;
      return copy;
   }

   private StructureModifier withConverter(EquivalentConverter converter) {
      StructureModifier copy = this.withTarget(this.target);
      copy.setConverter(converter);
      return copy;
   }

   protected void setConverter(EquivalentConverter converter) {
      this.converter = converter;
   }

   public List getFields() {
      return ImmutableList.copyOf(this.data);
   }

   public Field getField(int fieldIndex) {
      if (fieldIndex >= 0 && fieldIndex < this.data.size()) {
         return (Field)this.data.get(fieldIndex);
      } else {
         throw new IllegalArgumentException("Index parameter is not within [0 - " + this.data.size() + ")");
      }
   }

   public List getValues() throws FieldAccessException {
      List<TField> values = new ArrayList();

      for(int i = 0; i < this.size(); ++i) {
         values.add(this.read(i));
      }

      return values;
   }

   private static Map generateDefaultFields(List fields) {
      Map<Field, Integer> requireDefaults = new HashMap();
      DefaultInstances generator = DEFAULT_GENERATOR;
      int index = 0;

      for(Field field : fields) {
         Class<?> type = field.getType();
         int modifier = field.getModifiers();
         if (!type.isPrimitive() && !Modifier.isFinal(modifier) && generator.getDefault(type) != null) {
            requireDefaults.put(field, index);
         }

         ++index;
      }

      return requireDefaults;
   }

   private static List getFields(Class type, Class superclassExclude) {
      List<Field> result = new ArrayList();

      for(Field field : FuzzyReflection.fromClass(type, true).getFields()) {
         int mod = field.getModifiers();
         if (!Modifier.isStatic(mod) && (superclassExclude == null || !field.getDeclaringClass().equals(superclassExclude))) {
            result.add(field);
         }
      }

      return result;
   }
}
