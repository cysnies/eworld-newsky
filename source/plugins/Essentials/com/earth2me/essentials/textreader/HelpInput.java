package com.earth2me.essentials.textreader;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.User;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

public class HelpInput implements IText {
   private static final String DESCRIPTION = "description";
   private static final String PERMISSION = "permission";
   private static final String PERMISSIONS = "permissions";
   private final transient List lines = new ArrayList();
   private final transient List chapters = new ArrayList();
   private final transient Map bookmarks = new HashMap();
   private static final Logger logger = Logger.getLogger("Minecraft");

   public HelpInput(User user, String match, IEssentials ess) throws IOException {
      boolean reported = false;
      List<String> newLines = new ArrayList();
      String pluginName = "";
      String pluginNameLow = "";
      if (!match.equalsIgnoreCase("")) {
         this.lines.add(I18n._("helpMatching", match));
      }

      for(Plugin p : ess.getServer().getPluginManager().getPlugins()) {
         try {
            List<String> pluginLines = new ArrayList();
            PluginDescriptionFile desc = p.getDescription();
            Map<String, Map<String, Object>> cmds = desc.getCommands();
            pluginName = p.getDescription().getName();
            pluginNameLow = pluginName.toLowerCase(Locale.ENGLISH);
            if (pluginNameLow.equals(match)) {
               this.lines.clear();
               newLines.clear();
               this.lines.add(I18n._("helpFrom", p.getDescription().getName()));
            }

            boolean isOnWhitelist = user.isAuthorized("essentials.help." + pluginNameLow);

            for(Map.Entry k : cmds.entrySet()) {
               try {
                  if (match.equalsIgnoreCase("") || pluginNameLow.contains(match) || ((String)k.getKey()).toLowerCase(Locale.ENGLISH).contains(match) || ((Map)k.getValue()).get("description") instanceof String && ((String)((Map)k.getValue()).get("description")).toLowerCase(Locale.ENGLISH).contains(match)) {
                     if (pluginNameLow.contains("essentials")) {
                        String node = "essentials." + (String)k.getKey();
                        if (!ess.getSettings().isCommandDisabled((String)k.getKey()) && user.isAuthorized(node)) {
                           pluginLines.add(I18n._("helpLine", k.getKey(), ((Map)k.getValue()).get("description")));
                        }
                     } else if (ess.getSettings().showNonEssCommandsInHelp()) {
                        Map<String, Object> value = (Map)k.getValue();
                        Object permissions = null;
                        if (value.containsKey("permission")) {
                           permissions = value.get("permission");
                        } else if (value.containsKey("permissions")) {
                           permissions = value.get("permissions");
                        }

                        if (!isOnWhitelist && !user.isAuthorized("essentials.help." + pluginNameLow + "." + (String)k.getKey())) {
                           if (permissions instanceof List && !((List)permissions).isEmpty()) {
                              boolean enabled = false;

                              for(Object o : (List)permissions) {
                                 if (o instanceof String && user.isAuthorized(o.toString())) {
                                    enabled = true;
                                    break;
                                 }
                              }

                              if (enabled) {
                                 pluginLines.add(I18n._("helpLine", k.getKey(), value.get("description")));
                              }
                           } else if (permissions instanceof String && !"".equals(permissions)) {
                              if (user.isAuthorized(permissions.toString())) {
                                 pluginLines.add(I18n._("helpLine", k.getKey(), value.get("description")));
                              }
                           } else if (!ess.getSettings().hidePermissionlessHelp()) {
                              pluginLines.add(I18n._("helpLine", k.getKey(), value.get("description")));
                           }
                        } else {
                           pluginLines.add(I18n._("helpLine", k.getKey(), value.get("description")));
                        }
                     }
                  }
               } catch (NullPointerException var23) {
               }
            }

            if (!pluginLines.isEmpty()) {
               newLines.addAll(pluginLines);
               if (pluginNameLow.equals(match)) {
                  break;
               }

               if (match.equalsIgnoreCase("")) {
                  this.lines.add(I18n._("helpPlugin", pluginName, pluginNameLow));
               }
            }
         } catch (NullPointerException var24) {
         } catch (Exception ex) {
            if (!reported) {
               logger.log(Level.WARNING, I18n._("commandHelpFailedForPlugin", pluginNameLow), ex);
            }

            reported = true;
         }
      }

      this.lines.addAll(newLines);
   }

   public List getLines() {
      return this.lines;
   }

   public List getChapters() {
      return this.chapters;
   }

   public Map getBookmarks() {
      return this.bookmarks;
   }
}
