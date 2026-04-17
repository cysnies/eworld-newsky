package com.goncalomb.bukkit.nbteditor.nbt.attributes;

import com.goncalomb.bukkit.reflect.NBTTagCompoundWrapper;
import com.goncalomb.bukkit.reflect.NBTTagListWrapper;
import java.util.ArrayList;
import java.util.List;

public final class Attribute {
   private AttributeType _type;
   private double _base;
   private List _modifiers = new ArrayList();

   public static Attribute fromNBT(NBTTagCompoundWrapper data) {
      Attribute attribute = new Attribute(AttributeType.getByInternalName(data.getString("Name")), data.getDouble("Base"));
      if (data.hasKey("Modifiers")) {
         Object[] modifiersData = data.getListAsArray("Modifiers");
         attribute._modifiers = new ArrayList(modifiersData.length);

         for(Object mod : modifiersData) {
            attribute.addModifier(Modifier.fromNBT((NBTTagCompoundWrapper)mod));
         }
      }

      return attribute;
   }

   public Attribute(AttributeType type, double base) {
      this._type = type;
      this.setBase(base);
   }

   public AttributeType getType() {
      return this._type;
   }

   public double getMin() {
      return this._type.getMin();
   }

   public double getMax() {
      return this._type.getMax();
   }

   public double getBase() {
      return this._base;
   }

   public void setBase(double value) {
      this._base = Math.max(Math.min(value, this.getMax()), this.getMin());
   }

   public List getModifiers() {
      return new ArrayList(this._modifiers);
   }

   public void setModifiers(List modifiers) {
      this._modifiers.clear();
      if (modifiers != null) {
         this._modifiers.addAll(modifiers);
      }

   }

   public void addModifier(Modifier modifier) {
      this._modifiers.add(modifier);
   }

   public Modifier removeModifier(int index) {
      return (Modifier)this._modifiers.remove(index);
   }

   public NBTTagCompoundWrapper toNBT() {
      NBTTagCompoundWrapper data = new NBTTagCompoundWrapper();
      data.setString("Name", this._type._internalName);
      data.setDouble("Base", this._base);
      if (this._modifiers.size() > 0) {
         NBTTagListWrapper modifiersData = new NBTTagListWrapper();

         for(Modifier modifier : this._modifiers) {
            modifiersData.add(modifier.toNBT());
         }

         data.setList("Modifiers", modifiersData);
      }

      return data;
   }
}
