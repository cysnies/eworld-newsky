package com.goncalomb.bukkit.nbteditor.nbt.attributes;

import com.goncalomb.bukkit.reflect.NBTTagCompoundWrapper;
import java.util.UUID;

public class Modifier {
   private String _name;
   private double _amount;
   private int _operation;
   private UUID _uuid;

   public static Modifier fromNBT(NBTTagCompoundWrapper data) {
      return new Modifier(data.getString("Name"), data.getDouble("Amount"), data.getInt("Operation"), new UUID(data.getLong("UUIDMost"), data.getLong("UUIDLeast")));
   }

   public Modifier(String name, double amount, int operation) {
      this(name, amount, operation, UUID.randomUUID());
   }

   public Modifier(String name, double amount, int operation, UUID uuid) {
      this._name = name;
      this._amount = amount;
      this._operation = Math.max(Math.min(operation, 2), 0);
      this._uuid = uuid;
   }

   public final String getName() {
      return this._name;
   }

   public final double getAmount() {
      return this._amount;
   }

   public final int getOperation() {
      return this._operation;
   }

   public final UUID getUUID() {
      return this._uuid;
   }

   public NBTTagCompoundWrapper toNBT() {
      NBTTagCompoundWrapper data = new NBTTagCompoundWrapper();
      data.setString("Name", this._name);
      data.setDouble("Amount", this._amount);
      data.setInt("Operation", this._operation);
      data.setLong("UUIDMost", this._uuid.getMostSignificantBits());
      data.setLong("UUIDLeast", this._uuid.getLeastSignificantBits());
      return data;
   }
}
