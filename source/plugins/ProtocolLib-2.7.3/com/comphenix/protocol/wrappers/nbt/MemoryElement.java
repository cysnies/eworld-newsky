package com.comphenix.protocol.wrappers.nbt;

class MemoryElement implements NbtBase {
   private String name;
   private Object value;
   private NbtType type;

   public MemoryElement(String name, Object value) {
      if (name == null) {
         throw new IllegalArgumentException("Name cannot be NULL.");
      } else if (value == null) {
         throw new IllegalArgumentException("Element cannot be NULL.");
      } else {
         this.name = name;
         this.value = value;
         this.type = NbtType.getTypeFromClass(value.getClass());
      }
   }

   public MemoryElement(String name, Object value, NbtType type) {
      if (name == null) {
         throw new IllegalArgumentException("Name cannot be NULL.");
      } else if (type == null) {
         throw new IllegalArgumentException("Type cannot be NULL.");
      } else {
         this.name = name;
         this.value = value;
         this.type = type;
      }
   }

   public boolean accept(NbtVisitor visitor) {
      return visitor.visit(this);
   }

   public NbtType getType() {
      return this.type;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public Object getValue() {
      return this.value;
   }

   public void setValue(Object newValue) {
      this.value = newValue;
   }

   public NbtBase deepClone() {
      return new MemoryElement(this.name, this.value, this.type);
   }
}
