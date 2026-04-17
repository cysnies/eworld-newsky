package fr.neatmonster.nocheatplus.compat.blocks.init.vanilla;

import fr.neatmonster.nocheatplus.compat.blocks.BlockPropertiesSetup;
import fr.neatmonster.nocheatplus.compat.blocks.init.BlockInit;
import fr.neatmonster.nocheatplus.config.WorldConfigProvider;
import fr.neatmonster.nocheatplus.logging.LogUtil;
import fr.neatmonster.nocheatplus.utilities.BlockFlags;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;
import org.bukkit.Material;

public class BlocksMC1_5 implements BlockPropertiesSetup {
   public BlocksMC1_5() {
      BlockInit.assertMaterialNameMatch(152, "redstone", "block");
   }

   public void setupBlockProperties(WorldConfigProvider worldConfigProvider) {
      BlockInit.setAs(146, Material.CHEST);
      BlockInit.setAs(147, Material.STONE_PLATE);
      BlockInit.setAs(148, Material.STONE_PLATE);
      BlockInit.setAs(149, Material.DIODE_BLOCK_OFF);
      BlockInit.setAs(150, Material.DIODE_BLOCK_ON);
      BlockInit.setAs(151, Material.HUGE_MUSHROOM_1);
      BlockInit.setAs(152, Material.ENCHANTMENT_TABLE);
      BlockInit.setAs(153, Material.COAL_ORE);
      BlockInit.setAs(154, Material.COAL_ORE);
      BlockFlags.addFlags(154, 4104L);
      BlockInit.setAs(155, Material.SANDSTONE);
      BlockInit.setAs(156, Material.SANDSTONE_STAIRS);
      BlockInit.setAs(157, Material.DETECTOR_RAIL);
      BlockInit.setAs(158, Material.DISPENSER);
      BlockFlags.addFlags(78, 32768L);
      BlockFlags.removeFlags(78, 16384L);
      BlockProperties.setBlockProps(95, BlockProperties.instantType);
      LogUtil.logInfo("[NoCheatPlus] Added block-info for Minecraft 1.5 blocks.");
   }
}
