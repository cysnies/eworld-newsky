package com.goncalomb.bukkit.nbteditor;

import com.goncalomb.bukkit.betterplugin.BetterPlugin;
import com.goncalomb.bukkit.customitems.api.CustomItemManager;
import com.goncalomb.bukkit.nbteditor.bos.BookOfSouls;
import com.goncalomb.bukkit.nbteditor.commands.CommandBOS;
import com.goncalomb.bukkit.nbteditor.commands.CommandNBTArmor;
import com.goncalomb.bukkit.nbteditor.commands.CommandNBTBook;
import com.goncalomb.bukkit.nbteditor.commands.CommandNBTEnchant;
import com.goncalomb.bukkit.nbteditor.commands.CommandNBTHead;
import com.goncalomb.bukkit.nbteditor.commands.CommandNBTItem;
import com.goncalomb.bukkit.nbteditor.commands.CommandNBTPotion;
import com.goncalomb.bukkit.nbteditor.commands.CommandNBTSpawner;
import com.goncalomb.bukkit.nbteditor.commands.CommandNBTTile;
import com.goncalomb.bukkit.nbteditor.tools.MobInspectorTool;
import com.goncalomb.bukkit.nbteditor.tools.MobRemoverTool;
import com.goncalomb.bukkit.reflect.NBTBaseWrapper;
import com.goncalomb.bukkit.reflect.WorldUtils;
import java.util.logging.Level;

public final class NBTEditor extends BetterPlugin {
   public void onBetterEnable() {
      try {
         NBTBaseWrapper.prepareReflection();
         WorldUtils.prepareReflection();
      } catch (Throwable e) {
         this.getLogger().log(Level.SEVERE, "Error preparing reflection objects. This means that this version of NBTEditor is not compatible with this version of Bukkit.", e);
         this.getLogger().warning("NBTEditor version not compatible with this version of Bukkit. Please install the apropriated version.");
         return;
      }

      this.registerCommand(new CommandBOS());
      this.registerCommand(new CommandNBTSpawner());
      this.registerCommand(new CommandNBTItem());
      this.registerCommand(new CommandNBTEnchant());
      this.registerCommand(new CommandNBTBook());
      this.registerCommand(new CommandNBTPotion());
      this.registerCommand(new CommandNBTArmor());
      this.registerCommand(new CommandNBTHead());
      this.registerCommand(new CommandNBTTile());
      CustomItemManager itemManager = CustomItemManager.getInstance(this);
      BookOfSouls.initialize(this, itemManager);
      itemManager.registerNew(new MobInspectorTool(), this);
      itemManager.registerNew(new MobRemoverTool(), this);
      this.getLogger().info("NBTEditor has been enabled.");
   }
}
