package com.goncalomb.bukkit.nbteditor.nbt;

import com.goncalomb.bukkit.nbteditor.nbt.variable.NBTGenericVariableContainer;
import com.goncalomb.bukkit.nbteditor.nbt.variable.NBTVariable;
import com.goncalomb.bukkit.nbteditor.nbt.variable.NBTVariableContainer;
import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.entity.EntityType;

public final class EntityNBTVariableManager {
   private static HashMap _entityVariables = new HashMap();
   private static HashMap _entityVariablesByType = new HashMap();

   static void registerVariables(Class entityClass, NBTGenericVariableContainer variables) {
      _entityVariables.put(entityClass, variables);
   }

   static void registerVariables(EntityType entityType, NBTGenericVariableContainer variables) {
      _entityVariablesByType.put(entityType, variables);
   }

   static NBTVariableContainer[] getAllVariables(EntityNBT entity) {
      ArrayList<NBTVariableContainer> list = new ArrayList(3);
      NBTGenericVariableContainer aux;
      if ((aux = (NBTGenericVariableContainer)_entityVariablesByType.get(entity.getEntityType())) != null) {
         list.add(aux.boundToData(entity._data));
      }

      for(Class<?> clazz = entity.getClass(); clazz != Object.class; clazz = clazz.getSuperclass()) {
         if ((aux = (NBTGenericVariableContainer)_entityVariables.get(clazz)) != null) {
            list.add(aux.boundToData(entity._data));
         }
      }

      return (NBTVariableContainer[])list.toArray(new NBTVariableContainer[0]);
   }

   static NBTVariable getVariable(EntityNBT entity, String name) {
      name = name.toLowerCase();
      NBTGenericVariableContainer aux;
      if ((aux = (NBTGenericVariableContainer)_entityVariablesByType.get(entity.getEntityType())) != null && aux.hasVariable(name)) {
         return aux.getVariable(name, entity._data);
      } else {
         for(Class<?> clazz = entity.getClass(); clazz != Object.class; clazz = clazz.getSuperclass()) {
            if ((aux = (NBTGenericVariableContainer)_entityVariables.get(clazz)) != null && aux.hasVariable(name)) {
               return aux.getVariable(name, entity._data);
            }
         }

         return null;
      }
   }
}
