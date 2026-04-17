package com.sk89q.jnbt;

import java.util.Collections;
import java.util.List;

public final class ListTag extends Tag {
   private final Class type;
   private final List value;

   public ListTag(String name, Class type, List value) {
      super(name);
      this.type = type;
      this.value = Collections.unmodifiableList(value);
   }

   public Class getType() {
      return this.type;
   }

   public List getValue() {
      return this.value;
   }

   public String toString() {
      String name = this.getName();
      String append = "";
      if (name != null && !name.equals("")) {
         append = "(\"" + this.getName() + "\")";
      }

      StringBuilder bldr = new StringBuilder();
      bldr.append("TAG_List" + append + ": " + this.value.size() + " entries of type " + NBTUtils.getTypeName(this.type) + "\r\n{\r\n");

      for(Tag t : this.value) {
         bldr.append("   " + t.toString().replaceAll("\r\n", "\r\n   ") + "\r\n");
      }

      bldr.append("}");
      return bldr.toString();
   }
}
