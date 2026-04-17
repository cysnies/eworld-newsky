package com.sk89q.jnbt;

public final class IntTag extends Tag {
   private final int value;

   public IntTag(String name, int value) {
      super(name);
      this.value = value;
   }

   public Integer getValue() {
      return this.value;
   }

   public String toString() {
      String name = this.getName();
      String append = "";
      if (name != null && !name.equals("")) {
         append = "(\"" + this.getName() + "\")";
      }

      return "TAG_Int" + append + ": " + this.value;
   }
}
