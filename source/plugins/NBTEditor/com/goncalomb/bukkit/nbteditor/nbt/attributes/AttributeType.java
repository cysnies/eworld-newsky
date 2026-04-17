package com.goncalomb.bukkit.nbteditor.nbt.attributes;

import java.util.HashMap;

public enum AttributeType {
   MAX_HEALTH("MaxHealth", "generic.maxHealth", (double)0.0F, Double.MAX_VALUE),
   FOLLOW_RANGE("FollowRange", "generic.followRange", (double)0.0F, (double)2048.0F),
   KNOCKBACK_RESISTANCE("KnockbackResistance", "generic.knockbackResistance", (double)0.0F, (double)1.0F),
   MOVEMENT_SPEED("MovementSpeed", "generic.movementSpeed", (double)0.0F, Double.MAX_VALUE),
   ATTACK_DAMAGE("AttackDamage", "generic.attackDamage", (double)0.0F, Double.MAX_VALUE),
   JUMP_STRENGTH("JumpStrength", "horse.jumpStrength", (double)0.0F, (double)2.0F),
   SPAWN_REINFORCEMENTS("SpawnReinforcements", "zombie.spawnReinforcements", (double)0.0F, (double)1.0F);

   private static final HashMap _attributes = new HashMap();
   private static final HashMap _attributesInternal = new HashMap();
   private String _name;
   String _internalName;
   private double _min;
   private double _max;

   static {
      AttributeType[] var3;
      for(AttributeType type : var3 = values()) {
         _attributes.put(type._name.toLowerCase(), type);
         _attributesInternal.put(type._internalName, type);
      }

   }

   private AttributeType(String name, String internalName, double min, double max) {
      this._name = name;
      this._internalName = internalName;
      this._min = min;
      this._max = max;
   }

   public String getName() {
      return this._name;
   }

   public double getMin() {
      return this._min;
   }

   public double getMax() {
      return this._max;
   }

   public static AttributeType getByName(String name) {
      return (AttributeType)_attributes.get(name.toLowerCase());
   }

   static AttributeType getByInternalName(String name) {
      return (AttributeType)_attributesInternal.get(name);
   }

   public String toString() {
      return this._name;
   }
}
