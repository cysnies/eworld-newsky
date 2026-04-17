package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

public class Commandrecipe extends EssentialsCommand {
   public Commandrecipe() {
      super("recipe");
   }

   public void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         ItemStack itemType = this.ess.getItemDb().get(args[0]);
         int recipeNo = 0;
         if (args.length > 1) {
            if (!Util.isInt(args[1])) {
               throw new Exception(I18n._("invalidNumber"));
            }

            recipeNo = Integer.parseInt(args[1]) - 1;
         }

         List<Recipe> recipesOfType = this.ess.getServer().getRecipesFor(itemType);
         if (recipesOfType.size() < 1) {
            throw new Exception(I18n._("recipeNone", this.getMaterialName(itemType)));
         } else if (recipeNo >= 0 && recipeNo < recipesOfType.size()) {
            Recipe selectedRecipe = (Recipe)recipesOfType.get(recipeNo);
            sender.sendMessage(I18n._("recipe", this.getMaterialName(itemType), recipeNo + 1, recipesOfType.size()));
            if (selectedRecipe instanceof FurnaceRecipe) {
               this.furnaceRecipe(sender, (FurnaceRecipe)selectedRecipe);
            } else if (selectedRecipe instanceof ShapedRecipe) {
               this.shapedRecipe(sender, (ShapedRecipe)selectedRecipe);
            } else if (selectedRecipe instanceof ShapelessRecipe) {
               this.shapelessRecipe(sender, (ShapelessRecipe)selectedRecipe);
            }

            if (recipesOfType.size() > 1 && args.length == 1) {
               sender.sendMessage(I18n._("recipeMore", commandLabel, args[0], this.getMaterialName(itemType)));
            }

         } else {
            throw new Exception(I18n._("recipeBadIndex"));
         }
      }
   }

   public void furnaceRecipe(CommandSender sender, FurnaceRecipe recipe) {
      sender.sendMessage(I18n._("recipeFurnace", this.getMaterialName(recipe.getInput())));
   }

   public void shapedRecipe(CommandSender sender, ShapedRecipe recipe) {
      Map<Character, ItemStack> recipeMap = recipe.getIngredientMap();
      if (sender instanceof Player) {
         User user = this.ess.getUser(sender);
         user.setRecipeSee(true);
         InventoryView view = user.openWorkbench((Location)null, true);
         String[] recipeShape = recipe.getShape();
         Map<Character, ItemStack> ingredientMap = recipe.getIngredientMap();

         for(int j = 0; j < recipeShape.length; ++j) {
            for(int k = 0; k < recipeShape[j].length(); ++k) {
               ItemStack item = (ItemStack)ingredientMap.get(recipeShape[j].toCharArray()[k]);
               if (item != null) {
                  item.setAmount(0);
                  view.getTopInventory().setItem(j * 3 + k + 1, item);
               }
            }
         }
      } else {
         HashMap<Material, String> colorMap = new HashMap();
         int i = 1;
         char[] arr$ = "abcdefghi".toCharArray();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Character c = arr$[i$];
            ItemStack item = (ItemStack)recipeMap.get(c);
            if (!colorMap.containsKey(item == null ? null : item.getType())) {
               colorMap.put(item == null ? null : item.getType(), String.valueOf(i++));
            }
         }

         Material[][] materials = new Material[3][3];

         for(int j = 0; j < recipe.getShape().length; ++j) {
            for(int k = 0; k < recipe.getShape()[j].length(); ++k) {
               ItemStack item = (ItemStack)recipe.getIngredientMap().get(recipe.getShape()[j].toCharArray()[k]);
               materials[j][k] = item == null ? null : item.getType();
            }
         }

         sender.sendMessage(I18n._("recipeGrid", colorMap.get(materials[0][0]), colorMap.get(materials[0][1]), colorMap.get(materials[0][2])));
         sender.sendMessage(I18n._("recipeGrid", colorMap.get(materials[1][0]), colorMap.get(materials[1][1]), colorMap.get(materials[1][2])));
         sender.sendMessage(I18n._("recipeGrid", colorMap.get(materials[2][0]), colorMap.get(materials[2][1]), colorMap.get(materials[2][2])));
         StringBuilder s = new StringBuilder();

         for(Material items : (Material[])colorMap.keySet().toArray(new Material[colorMap.size()])) {
            s.append(I18n._("recipeGridItem", colorMap.get(items), this.getMaterialName(items)));
         }

         sender.sendMessage(I18n._("recipeWhere", s.toString()));
      }

   }

   public void shapelessRecipe(CommandSender sender, ShapelessRecipe recipe) {
      List<ItemStack> ingredients = recipe.getIngredientList();
      if (sender instanceof Player) {
         User user = this.ess.getUser(sender);
         user.setRecipeSee(true);
         InventoryView view = user.openWorkbench((Location)null, true);

         for(int i = 0; i < ingredients.size(); ++i) {
            view.setItem(i + 1, (ItemStack)ingredients.get(i));
         }
      } else {
         StringBuilder s = new StringBuilder();

         for(int i = 0; i < ingredients.size(); ++i) {
            s.append(this.getMaterialName((ItemStack)ingredients.get(i)));
            if (i != ingredients.size() - 1) {
               s.append(",");
            }

            s.append(" ");
         }

         sender.sendMessage(I18n._("recipeShapeless", s.toString()));
      }

   }

   public String getMaterialName(ItemStack stack) {
      return stack == null ? I18n._("recipeNothing") : this.getMaterialName(stack.getType());
   }

   public String getMaterialName(Material type) {
      return type == null ? I18n._("recipeNothing") : type.toString().replace("_", " ").toLowerCase(Locale.ENGLISH);
   }
}
