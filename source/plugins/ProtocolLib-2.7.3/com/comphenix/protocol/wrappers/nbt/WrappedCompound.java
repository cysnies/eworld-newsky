package com.comphenix.protocol.wrappers.nbt;

import com.comphenix.protocol.wrappers.collection.ConvertedMap;
import com.comphenix.protocol.wrappers.nbt.io.NbtBinarySerializer;
import java.io.DataOutput;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

class WrappedCompound implements NbtWrapper, Iterable, NbtCompound {
   private WrappedElement container;
   private ConvertedMap savedMap;

   public static WrappedCompound fromName(String name) {
      return (WrappedCompound)NbtFactory.ofWrapper(NbtType.TAG_COMPOUND, name);
   }

   public static NbtCompound fromList(String name, Collection list) {
      WrappedCompound copy = fromName(name);

      for(NbtBase base : list) {
         copy.getValue().put(base.getName(), base);
      }

      return copy;
   }

   public WrappedCompound(Object handle) {
      this.container = new WrappedElement(handle);
   }

   public boolean accept(NbtVisitor visitor) {
      if (visitor.visitEnter((NbtCompound)this)) {
         for(NbtBase node : this) {
            if (!node.accept(visitor)) {
               break;
            }
         }
      }

      return visitor.visitLeave((NbtCompound)this);
   }

   public Object getHandle() {
      return this.container.getHandle();
   }

   public NbtType getType() {
      return NbtType.TAG_COMPOUND;
   }

   public String getName() {
      return this.container.getName();
   }

   public void setName(String name) {
      this.container.setName(name);
   }

   public boolean containsKey(String key) {
      return this.getValue().containsKey(key);
   }

   public Set getKeys() {
      return this.getValue().keySet();
   }

   public Map getValue() {
      if (this.savedMap == null) {
         this.savedMap = new ConvertedMap((Map)this.container.getValue()) {
            protected Object toInner(NbtBase outer) {
               return outer == null ? null : NbtFactory.fromBase(outer).getHandle();
            }

            protected NbtBase toOuter(Object inner) {
               return inner == null ? null : NbtFactory.fromNMS(inner);
            }

            public String toString() {
               return WrappedCompound.this.toString();
            }
         };
      }

      return this.savedMap;
   }

   public void setValue(Map newValue) {
      for(Map.Entry entry : newValue.entrySet()) {
         Object value = entry.getValue();
         if (value instanceof NbtBase) {
            this.put((NbtBase)entry.getValue());
         } else {
            this.putObject((String)entry.getKey(), entry.getValue());
         }
      }

   }

   public NbtBase getValue(String key) {
      return (NbtBase)this.getValue().get(key);
   }

   public NbtBase getValueOrDefault(String key, NbtType type) {
      NbtBase<?> nbt = this.getValue(key);
      if (nbt == null) {
         this.put(nbt = NbtFactory.ofWrapper(type, key));
      } else if (nbt.getType() != type) {
         throw new IllegalArgumentException("Cannot get tag " + nbt + ": Not a " + type);
      }

      return nbt;
   }

   private NbtBase getValueExact(String key) {
      NbtBase<T> value = this.getValue(key);
      if (value != null) {
         return value;
      } else {
         throw new IllegalArgumentException("Cannot find key " + key);
      }
   }

   public NbtBase deepClone() {
      return this.container.deepClone();
   }

   public NbtCompound put(NbtBase entry) {
      if (entry == null) {
         throw new IllegalArgumentException("Entry cannot be NULL.");
      } else {
         this.getValue().put(entry.getName(), entry);
         return this;
      }
   }

   public String getString(String key) {
      return (String)this.getValueExact(key).getValue();
   }

   public String getStringOrDefault(String key) {
      return (String)this.getValueOrDefault(key, NbtType.TAG_STRING).getValue();
   }

   public NbtCompound put(String key, String value) {
      this.getValue().put(key, NbtFactory.of(key, value));
      return this;
   }

   public NbtCompound putObject(String key, Object value) {
      if (value == null) {
         this.remove(key);
      } else if (value instanceof NbtBase) {
         this.put(key, (NbtBase)value);
      } else {
         NbtBase<?> base = new MemoryElement(key, value);
         this.put(base);
      }

      return this;
   }

   public Object getObject(String key) {
      NbtBase<?> base = this.getValue(key);
      return base != null && base.getType() != NbtType.TAG_LIST && base.getType() != NbtType.TAG_COMPOUND ? base.getValue() : base;
   }

   public byte getByte(String key) {
      return (Byte)this.getValueExact(key).getValue();
   }

   public byte getByteOrDefault(String key) {
      return (Byte)this.getValueOrDefault(key, NbtType.TAG_BYTE).getValue();
   }

   public NbtCompound put(String key, byte value) {
      this.getValue().put(key, NbtFactory.of(key, value));
      return this;
   }

   public Short getShort(String key) {
      return (Short)this.getValueExact(key).getValue();
   }

   public short getShortOrDefault(String key) {
      return (Short)this.getValueOrDefault(key, NbtType.TAG_SHORT).getValue();
   }

   public NbtCompound put(String key, short value) {
      this.getValue().put(key, NbtFactory.of(key, value));
      return this;
   }

   public int getInteger(String key) {
      return (Integer)this.getValueExact(key).getValue();
   }

   public int getIntegerOrDefault(String key) {
      return (Integer)this.getValueOrDefault(key, NbtType.TAG_INT).getValue();
   }

   public NbtCompound put(String key, int value) {
      this.getValue().put(key, NbtFactory.of(key, value));
      return this;
   }

   public long getLong(String key) {
      return (Long)this.getValueExact(key).getValue();
   }

   public long getLongOrDefault(String key) {
      return (Long)this.getValueOrDefault(key, NbtType.TAG_LONG).getValue();
   }

   public NbtCompound put(String key, long value) {
      this.getValue().put(key, NbtFactory.of(key, value));
      return this;
   }

   public float getFloat(String key) {
      return (Float)this.getValueExact(key).getValue();
   }

   public float getFloatOrDefault(String key) {
      return (Float)this.getValueOrDefault(key, NbtType.TAG_FLOAT).getValue();
   }

   public NbtCompound put(String key, float value) {
      this.getValue().put(key, NbtFactory.of(key, value));
      return this;
   }

   public double getDouble(String key) {
      return (Double)this.getValueExact(key).getValue();
   }

   public double getDoubleOrDefault(String key) {
      return (Double)this.getValueOrDefault(key, NbtType.TAG_DOUBLE).getValue();
   }

   public NbtCompound put(String key, double value) {
      this.getValue().put(key, NbtFactory.of(key, value));
      return this;
   }

   public byte[] getByteArray(String key) {
      return (byte[])this.getValueExact(key).getValue();
   }

   public NbtCompound put(String key, byte[] value) {
      this.getValue().put(key, NbtFactory.of(key, value));
      return this;
   }

   public int[] getIntegerArray(String key) {
      return (int[])this.getValueExact(key).getValue();
   }

   public NbtCompound put(String key, int[] value) {
      this.getValue().put(key, NbtFactory.of(key, value));
      return this;
   }

   public NbtCompound getCompound(String key) {
      return (NbtCompound)this.getValueExact(key);
   }

   public NbtCompound getCompoundOrDefault(String key) {
      return (NbtCompound)this.getValueOrDefault(key, NbtType.TAG_COMPOUND);
   }

   public NbtCompound put(NbtCompound compound) {
      this.getValue().put(compound.getName(), compound);
      return this;
   }

   public NbtList getList(String key) {
      return (NbtList)this.getValueExact(key);
   }

   public NbtList getListOrDefault(String key) {
      return (NbtList)this.getValueOrDefault(key, NbtType.TAG_LIST);
   }

   public NbtCompound put(NbtList list) {
      this.getValue().put(list.getName(), list);
      return this;
   }

   public NbtCompound put(String key, NbtBase entry) {
      if (entry == null) {
         throw new IllegalArgumentException("Entry cannot be NULL.");
      } else {
         NbtBase<?> clone = entry.deepClone();
         clone.setName(key);
         return this.put(clone);
      }
   }

   public NbtCompound put(String key, Collection list) {
      return this.put(WrappedList.fromList(key, list));
   }

   public NbtBase remove(String key) {
      return (NbtBase)this.getValue().remove(key);
   }

   public void write(DataOutput destination) {
      NbtBinarySerializer.DEFAULT.serialize(this.container, destination);
   }

   public boolean equals(Object obj) {
      if (obj instanceof WrappedCompound) {
         WrappedCompound other = (WrappedCompound)obj;
         return this.container.equals(other.container);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.container.hashCode();
   }

   public Iterator iterator() {
      return this.getValue().values().iterator();
   }

   public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("{");
      builder.append("\"name\": \"" + this.getName() + "\"");

      for(NbtBase element : this) {
         builder.append(", ");
         if (element.getType() == NbtType.TAG_STRING) {
            builder.append("\"" + element.getName() + "\": \"" + element.getValue() + "\"");
         } else {
            builder.append("\"" + element.getName() + "\": " + element.getValue());
         }
      }

      builder.append("}");
      return builder.toString();
   }
}
