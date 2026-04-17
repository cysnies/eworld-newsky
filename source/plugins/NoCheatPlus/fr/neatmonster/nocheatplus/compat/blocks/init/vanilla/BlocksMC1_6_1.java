package fr.neatmonster.nocheatplus.compat.blocks.init.vanilla;

import fr.neatmonster.nocheatplus.compat.blocks.BlockPropertiesSetup;
import fr.neatmonster.nocheatplus.compat.blocks.init.BlockInit;
import fr.neatmonster.nocheatplus.config.WorldConfigProvider;
import fr.neatmonster.nocheatplus.logging.LogUtil;
import fr.neatmonster.nocheatplus.utilities.BlockFlags;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;
import org.bukkit.Material;

public class BlocksMC1_6_1 implements BlockPropertiesSetup {
   public BlocksMC1_6_1() {
      BlockInit.assertMaterialNameMatch(173, "coal", "block");
   }

   public void setupBlockProperties(WorldConfigProvider worldConfigProvider) {
      BlockInit.setAs(173, 152);
      BlockProperties.setBlockProps(172, new BlockProperties.BlockProps(BlockProperties.woodPickaxe, 1.25F, BlockProperties.secToMs((double)6.25F, 0.95, (double)0.5F, 0.35, (double)0.25F, 0.2)));
      BlockFlags.setFlagsAs(172, Material.STONE);
      BlockInit.setAs(159, 172);
      BlockInit.setPropsAs(170, Material.STONE_BUTTON);
      BlockFlags.setFlagsAs(170, Material.STONE);
      BlockProperties.setBlockProps(171, new BlockProperties.BlockProps(BlockProperties.noTool, 0.1F, BlockProperties.secToMs(0.15)));
      BlockProperties.setBlockFlags(171, 8L);
      LogUtil.logInfo("[NoCheatPlus] Added block-info for Minecraft 1.6.1 blocks.");
   }
}
