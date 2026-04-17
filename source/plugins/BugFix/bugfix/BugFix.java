package bugfix;

import net.minecraft.server.v1_6_R2.MinecraftServer;
import org.bukkit.plugin.java.JavaPlugin;

public class BugFix extends JavaPlugin {
   public void onEnable() {
      if (MinecraftServer.currentTick < 1) {
         MinecraftServer.currentTick = 1;
      }

   }
}
