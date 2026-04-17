package com.goncalomb.bukkit.nbteditor.nbt;

import com.goncalomb.bukkit.nbteditor.nbt.variable.IntegerVariable;
import com.goncalomb.bukkit.nbteditor.nbt.variable.NBTGenericVariableContainer;
import com.goncalomb.bukkit.reflect.NBTTagCompoundWrapper;
import com.goncalomb.bukkit.reflect.NBTTagListWrapper;
import java.util.ArrayList;
import java.util.List;

public class VillagerNBT extends BreedNBT {
   private ArrayList _offers;

   static {
      NBTGenericVariableContainer variables = new NBTGenericVariableContainer("Villager");
      variables.add("profession", new IntegerVariable("Profession", 0, 5));
      EntityNBTVariableManager.registerVariables(VillagerNBT.class, variables);
   }

   public void clearOffers() {
      this._data.remove("Offers");
      this._offers = null;
   }

   public void addOffer(VillagerNBTOffer offer) {
      NBTTagCompoundWrapper offers = this._data.getCompound("Offers");
      if (offers == null) {
         offers = new NBTTagCompoundWrapper();
         this._data.setCompound("Offers", offers);
      }

      NBTTagListWrapper recipes = offers.getList("Recipes");
      if (recipes == null) {
         recipes = new NBTTagListWrapper();
         offers.setList("Recipes", recipes);
      }

      recipes.add(offer.getCompound());
      if (this._offers != null) {
         this._offers.add(offer);
      }

   }

   public List getOffers() {
      if (this._offers == null) {
         this._offers = new ArrayList();
         if (this._data.hasKey("Offers")) {
            NBTTagCompoundWrapper offers = this._data.getCompound("Offers");
            if (offers.hasKey("Recipes")) {
               Object[] recipes = offers.getListAsArray("Recipes");

               for(Object recipe : recipes) {
                  this._offers.add(new VillagerNBTOffer((NBTTagCompoundWrapper)recipe));
               }
            }
         }
      }

      return this._offers;
   }
}
