package com.comphenix.protocol.wrappers.nbt;

import com.comphenix.protocol.wrappers.collection.ConvertedList;
import com.comphenix.protocol.wrappers.nbt.io.NbtBinarySerializer;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import java.io.DataOutput;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

class WrappedList implements NbtWrapper, Iterable, NbtList {
   private WrappedElement container;
   private ConvertedList savedList;
   private NbtType elementType;

   public static NbtList fromName(String name) {
      return (NbtList)NbtFactory.ofWrapper(NbtType.TAG_LIST, name);
   }

   public static NbtList fromArray(String name, Object... elements) {
      NbtList<T> result = fromName(name);

      for(Object element : elements) {
         if (element == null) {
            throw new IllegalArgumentException("An NBT list cannot contain a null element!");
         }

         if (element instanceof NbtBase) {
            result.add((NbtBase)element);
         } else {
            result.add((NbtBase)NbtFactory.ofWrapper(element.getClass(), "", element));
         }
      }

      return result;
   }

   public static NbtList fromList(String name, Collection elements) {
      NbtList<T> result = fromName(name);

      for(Object element : elements) {
         if (element == null) {
            throw new IllegalArgumentException("An NBT list cannot contain a null element!");
         }

         if (element instanceof NbtBase) {
            result.add((NbtBase)element);
         } else {
            result.add((NbtBase)NbtFactory.ofWrapper(element.getClass(), "", element));
         }
      }

      return result;
   }

   public WrappedList(Object handle) {
      this.elementType = NbtType.TAG_END;
      this.container = new WrappedElement(handle);
      this.elementType = this.container.getSubType();
   }

   public boolean accept(NbtVisitor visitor) {
      if (visitor.visitEnter((NbtList)this)) {
         for(NbtBase node : this.getValue()) {
            if (!node.accept(visitor)) {
               break;
            }
         }
      }

      return visitor.visitLeave((NbtList)this);
   }

   public Object getHandle() {
      return this.container.getHandle();
   }

   public NbtType getType() {
      return NbtType.TAG_LIST;
   }

   public NbtType getElementType() {
      return this.elementType;
   }

   public void setElementType(NbtType type) {
      this.elementType = type;
      this.container.setSubType(type);
   }

   public String getName() {
      return this.container.getName();
   }

   public void setName(String name) {
      this.container.setName(name);
   }

   public List getValue() {
      // $FF: Couldn't be decompiled
   }

   public NbtBase deepClone() {
      return this.container.deepClone();
   }

   public void addClosest(Object value) {
      if (this.getElementType() == NbtType.TAG_END) {
         throw new IllegalStateException("This list has not been typed yet.");
      } else {
         if (value instanceof Number) {
            Number number = (Number)value;
            switch (this.getElementType()) {
               case TAG_BYTE:
                  this.add(number.byteValue());
                  break;
               case TAG_SHORT:
                  this.add(number.shortValue());
                  break;
               case TAG_INT:
                  this.add(number.intValue());
                  break;
               case TAG_LONG:
                  this.add(number.longValue());
                  break;
               case TAG_FLOAT:
                  this.add((double)number.floatValue());
                  break;
               case TAG_DOUBLE:
                  this.add(number.doubleValue());
                  break;
               case TAG_STRING:
                  this.add(number.toString());
                  break;
               default:
                  throw new IllegalArgumentException("Cannot convert " + value + " to " + this.getType());
            }
         } else if (value instanceof NbtBase) {
            this.add((NbtBase)value);
         } else {
            this.add((NbtBase)NbtFactory.ofWrapper(this.getElementType(), "", value));
         }

      }
   }

   public void add(NbtBase element) {
      this.getValue().add(element);
   }

   public void add(String value) {
      this.add(NbtFactory.of("", value));
   }

   public void add(byte value) {
      this.add(NbtFactory.of("", value));
   }

   public void add(short value) {
      this.add(NbtFactory.of("", value));
   }

   public void add(int value) {
      this.add(NbtFactory.of("", value));
   }

   public void add(long value) {
      this.add(NbtFactory.of("", value));
   }

   public void add(double value) {
      this.add(NbtFactory.of("", value));
   }

   public void add(byte[] value) {
      this.add(NbtFactory.of("", value));
   }

   public void add(int[] value) {
      this.add(NbtFactory.of("", value));
   }

   public int size() {
      return this.getValue().size();
   }

   public Object getValue(int index) {
      return ((NbtBase)this.getValue().get(index)).getValue();
   }

   public Collection asCollection() {
      return this.getValue();
   }

   public void setValue(List newValue) {
      NbtBase<TType> lastElement = null;
      List<Object> list = (List)this.container.getValue();
      list.clear();

      for(NbtBase type : newValue) {
         if (type != null) {
            lastElement = type;
            list.add(NbtFactory.fromBase(type).getHandle());
         } else {
            list.add((Object)null);
         }
      }

      if (lastElement != null) {
         this.container.setSubType(lastElement.getType());
      }

   }

   public void write(DataOutput destination) {
      NbtBinarySerializer.DEFAULT.serialize(this.container, destination);
   }

   public boolean equals(Object obj) {
      if (obj instanceof WrappedList) {
         WrappedList<TType> other = (WrappedList)obj;
         return this.container.equals(other.container);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.container.hashCode();
   }

   public Iterator iterator() {
      // $FF: Couldn't be decompiled
   }

   public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("{\"name\": \"" + this.getName() + "\", \"value\": [");
      if (this.size() > 0) {
         if (this.getElementType() == NbtType.TAG_STRING) {
            builder.append("\"" + Joiner.on("\", \"").join(this) + "\"");
         } else {
            builder.append(Joiner.on(", ").join(this));
         }
      }

      builder.append("]}");
      return builder.toString();
   }

   public void remove(Object remove) {
      this.getValue().remove(remove);
   }

   // $FF: synthetic method
   static WrappedElement access$000(WrappedList x0) {
      return x0.container;
   }
}
