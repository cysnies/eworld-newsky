package com.sk89q.jnbt;

public final class StringTag extends Tag {
   private final String value;

   public StringTag(String name, String value) {
      super(name);
      this.value = value;
   }

   public String getValue() {
      return this.value;
   }

   public String toString() {
      String name = this.getName();
      String append = "";
      if (name != null && !name.equals("")) {
         append = "(\"" + this.getName() + "\")";
      }

      return "TAG_String" + append + ": " + this.value;
   }
}
