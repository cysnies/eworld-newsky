package fr.neatmonster.nocheatplus.compat;

import fr.neatmonster.nocheatplus.compat.bukkit.MCAccessBukkit;
import fr.neatmonster.nocheatplus.compat.cb2511.MCAccessCB2511;
import fr.neatmonster.nocheatplus.compat.cb2512.MCAccessCB2512;
import fr.neatmonster.nocheatplus.compat.cb2545.MCAccessCB2545;
import fr.neatmonster.nocheatplus.compat.cb2602.MCAccessCB2602;
import fr.neatmonster.nocheatplus.compat.cb2645.MCAccessCB2645;
import fr.neatmonster.nocheatplus.compat.cb2691.MCAccessCB2691;
import fr.neatmonster.nocheatplus.compat.cb2763.MCAccessCB2763;
import fr.neatmonster.nocheatplus.compat.cb2794.MCAccessCB2794;
import fr.neatmonster.nocheatplus.compat.cbdev.MCAccessCBDev;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.logging.LogUtil;
import java.util.ArrayList;
import java.util.List;

public class MCAccessFactory {
   private final String[] updateLocs = new String[]{"[NoCheatPlus]  Check for updates at BukkitDev: http://dev.bukkit.org/server-mods/nocheatplus/", "[NoCheatPlus]  Development builds: http://ci.ecocitycraft.com/job/NoCheatPlus/"};

   public MCAccess getMCAccess() {
      return this.getMCAccess(ConfigManager.getConfigFile().getBoolean("compatibility.bukkitapionly"));
   }

   public MCAccess getMCAccess(boolean bukkitOnly) {
      List<Throwable> throwables = new ArrayList();
      if (!bukkitOnly) {
         try {
            return new MCAccessCBDev();
         } catch (Throwable t) {
            throwables.add(t);

            try {
               return new MCAccessCB2794();
            } catch (Throwable t) {
               throwables.add(t);

               try {
                  return new MCAccessCB2763();
               } catch (Throwable t) {
                  throwables.add(t);

                  try {
                     return new MCAccessCB2691();
                  } catch (Throwable t) {
                     throwables.add(t);

                     try {
                        return new MCAccessCB2645();
                     } catch (Throwable t) {
                        throwables.add(t);

                        try {
                           return new MCAccessCB2602();
                        } catch (Throwable t) {
                           throwables.add(t);

                           try {
                              return new MCAccessCB2545();
                           } catch (Throwable t) {
                              throwables.add(t);

                              try {
                                 return new MCAccessCB2512();
                              } catch (Throwable t) {
                                 throwables.add(t);

                                 try {
                                    return new MCAccessCB2511();
                                 } catch (Throwable t) {
                                    throwables.add(t);
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      try {
         String msg;
         if (bukkitOnly) {
            msg = "[NoCheatPlus] The plugin is configured for Bukkit-API-only access.";
         } else {
            msg = "[NoCheatPlus] Could not set up native access for your specific Minecraft + server-mod version.";
         }

         LogUtil.logWarning(msg);
         MCAccess mcAccess = new MCAccessBukkit();
         LogUtil.logWarning("[NoCheatPlus] API-only MCAccess: Some features will likely not function properly, performance might suffer.");

         for(String uMsg : this.updateLocs) {
            LogUtil.logWarning(uMsg);
         }

         return mcAccess;
      } catch (Throwable t) {
         throwables.add(t);
         LogUtil.logSevere("[NoCheatPlus] Your version of NoCheatPlus does not seem to provide support for either your Minecraft version or your specific server-mod.");

         for(String msg : this.updateLocs) {
            LogUtil.logSevere(msg);
         }

         LogUtil.logSevere("[NoCheatPlus] Could not set up MC version specific access.");

         for(Throwable t : throwables) {
            LogUtil.logSevere(t);
         }

         throw new RuntimeException("Could not set up access to Minecraft API.");
      }
   }
}
