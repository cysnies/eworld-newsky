package com.goncalomb.bukkit.nbteditor.nbt.attributes;

import com.goncalomb.bukkit.reflect.NBTTagCompoundWrapper;
import com.goncalomb.bukkit.reflect.NBTTagListWrapper;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;

public final class AttributeContainer {
   private LinkedHashMap _attributes = new LinkedHashMap();

   public static AttributeContainer fromNBT(NBTTagListWrapper data) {
      AttributeContainer container = new AttributeContainer();

      Object[] var5;
      for(Object attr : var5 = data.getAsArray()) {
         container.setAttribute(Attribute.fromNBT((NBTTagCompoundWrapper)attr));
      }

      return container;
   }

   public Attribute getAttribute(AttributeType type) {
      return (Attribute)this._attributes.get(type);
   }

   public void setAttribute(Attribute attribute) {
      this._attributes.put(attribute.getType(), attribute);
   }

   public Attribute removeAttribute(AttributeType type) {
      return (Attribute)this._attributes.remove(type);
   }

   public int size() {
      return this._attributes.size();
   }

   public Collection values() {
      return Collections.unmodifiableCollection(this._attributes.values());
   }

   public NBTTagListWrapper toNBT() {
      NBTTagListWrapper data = new NBTTagListWrapper();

      for(Attribute attribute : this._attributes.values()) {
         data.add(attribute.toNBT());
      }

      return data;
   }
}
