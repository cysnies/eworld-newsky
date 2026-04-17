package fr.neatmonster.nocheatplus.compat.blocks.init.vanilla;

import fr.neatmonster.nocheatplus.compat.blocks.BlockPropertiesSetup;
import fr.neatmonster.nocheatplus.config.WorldConfigProvider;
import fr.neatmonster.nocheatplus.logging.LogUtil;
import java.util.LinkedList;
import java.util.List;

public class VanillaBlocksFactory implements BlockPropertiesSetup {
   public void setupBlockProperties(WorldConfigProvider worldConfigProvider) {
      List<BlockPropertiesSetup> setups = new LinkedList();

      try {
         setups.add(new BlocksMC1_5());
         setups.add(new BlocksMC1_6_1());
      } catch (Throwable var6) {
      }

      for(BlockPropertiesSetup setup : setups) {
         try {
            setup.setupBlockProperties(worldConfigProvider);
         } catch (Throwable t) {
            LogUtil.logSevere("[NoCheatPlus] " + setup.getClass().getSimpleName() + ".setupBlockProperties could not execute properly: " + t.getClass().getSimpleName() + " - " + t.getMessage());
            LogUtil.logSevere(t);
            break;
         }
      }

   }
}
