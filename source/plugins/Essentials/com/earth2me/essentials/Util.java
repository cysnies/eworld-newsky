package com.earth2me.essentials;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

public class Util {
   private static final Logger logger = Logger.getLogger("Minecraft");
   private static final Pattern INVALIDFILECHARS = Pattern.compile("[^a-z0-9]");
   private static final Pattern INVALIDCHARS = Pattern.compile("[^\t\n\r -~\u0085 -\ud7ff\ue000-￼]");
   private static final Set AIR_MATERIALS = new HashSet();
   private static final HashSet AIR_MATERIALS_TARGET = new HashSet();
   public static final int RADIUS = 3;
   public static final Vector3D[] VOLUME;
   private static DecimalFormat dFormat;
   private static final transient Pattern URL_PATTERN;
   private static final transient Pattern VANILLA_PATTERN;
   private static final transient Pattern LOGCOLOR_PATTERN;
   private static final transient Pattern REPLACE_PATTERN;
   private static final transient Pattern VANILLA_COLOR_PATTERN;
   private static final transient Pattern VANILLA_MAGIC_PATTERN;
   private static final transient Pattern VANILLA_FORMAT_PATTERN;
   private static final transient Pattern REPLACE_COLOR_PATTERN;
   private static final transient Pattern REPLACE_MAGIC_PATTERN;
   private static final transient Pattern REPLACE_FORMAT_PATTERN;
   private static final Pattern IPPATTERN;

   private Util() {
   }

   public static String sanitizeFileName(String name) {
      return safeString(name);
   }

   public static String safeString(String string) {
      return INVALIDFILECHARS.matcher(string.toLowerCase(Locale.ENGLISH)).replaceAll("_");
   }

   public static String sanitizeString(String string) {
      return INVALIDCHARS.matcher(string).replaceAll("");
   }

   public static String formatDateDiff(long date) {
      Calendar c = new GregorianCalendar();
      c.setTimeInMillis(date);
      Calendar now = new GregorianCalendar();
      return formatDateDiff(now, c);
   }

   public static String formatDateDiff(Calendar fromDate, Calendar toDate) {
      boolean future = false;
      if (toDate.equals(fromDate)) {
         return I18n._("now");
      } else {
         if (toDate.after(fromDate)) {
            future = true;
         }

         StringBuilder sb = new StringBuilder();
         int[] types = new int[]{1, 2, 5, 11, 12, 13};
         String[] names = new String[]{I18n._("year"), I18n._("years"), I18n._("month"), I18n._("months"), I18n._("day"), I18n._("days"), I18n._("hour"), I18n._("hours"), I18n._("minute"), I18n._("minutes"), I18n._("second"), I18n._("seconds")};
         int accuracy = 0;

         for(int i = 0; i < types.length && accuracy <= 2; ++i) {
            int diff = dateDiff(types[i], fromDate, toDate, future);
            if (diff > 0) {
               ++accuracy;
               sb.append(" ").append(diff).append(" ").append(names[i * 2 + (diff > 1 ? 1 : 0)]);
            }
         }

         return sb.length() == 0 ? "now" : sb.toString().trim();
      }
   }

   private static int dateDiff(int type, Calendar fromDate, Calendar toDate, boolean future) {
      int diff = 0;

      long savedDate;
      for(savedDate = fromDate.getTimeInMillis(); future && !fromDate.after(toDate) || !future && !fromDate.before(toDate); ++diff) {
         savedDate = fromDate.getTimeInMillis();
         fromDate.add(type, future ? 1 : -1);
      }

      --diff;
      fromDate.setTimeInMillis(savedDate);
      return diff;
   }

   public static long parseDateDiff(String time, boolean future) throws Exception {
      Pattern timePattern = Pattern.compile("(?:([0-9]+)\\s*y[a-z]*[,\\s]*)?(?:([0-9]+)\\s*mo[a-z]*[,\\s]*)?(?:([0-9]+)\\s*w[a-z]*[,\\s]*)?(?:([0-9]+)\\s*d[a-z]*[,\\s]*)?(?:([0-9]+)\\s*h[a-z]*[,\\s]*)?(?:([0-9]+)\\s*m[a-z]*[,\\s]*)?(?:([0-9]+)\\s*(?:s[a-z]*)?)?", 2);
      Matcher m = timePattern.matcher(time);
      int years = 0;
      int months = 0;
      int weeks = 0;
      int days = 0;
      int hours = 0;
      int minutes = 0;
      int seconds = 0;
      boolean found = false;

      while(m.find()) {
         if (m.group() != null && !m.group().isEmpty()) {
            for(int i = 0; i < m.groupCount(); ++i) {
               if (m.group(i) != null && !m.group(i).isEmpty()) {
                  found = true;
                  break;
               }
            }

            if (found) {
               if (m.group(1) != null && !m.group(1).isEmpty()) {
                  years = Integer.parseInt(m.group(1));
               }

               if (m.group(2) != null && !m.group(2).isEmpty()) {
                  months = Integer.parseInt(m.group(2));
               }

               if (m.group(3) != null && !m.group(3).isEmpty()) {
                  weeks = Integer.parseInt(m.group(3));
               }

               if (m.group(4) != null && !m.group(4).isEmpty()) {
                  days = Integer.parseInt(m.group(4));
               }

               if (m.group(5) != null && !m.group(5).isEmpty()) {
                  hours = Integer.parseInt(m.group(5));
               }

               if (m.group(6) != null && !m.group(6).isEmpty()) {
                  minutes = Integer.parseInt(m.group(6));
               }

               if (m.group(7) != null && !m.group(7).isEmpty()) {
                  seconds = Integer.parseInt(m.group(7));
               }
               break;
            }
         }
      }

      if (!found) {
         throw new Exception(I18n._("illegalDate"));
      } else {
         Calendar c = new GregorianCalendar();
         if (years > 0) {
            c.add(1, years * (future ? 1 : -1));
         }

         if (months > 0) {
            c.add(2, months * (future ? 1 : -1));
         }

         if (weeks > 0) {
            c.add(3, weeks * (future ? 1 : -1));
         }

         if (days > 0) {
            c.add(5, days * (future ? 1 : -1));
         }

         if (hours > 0) {
            c.add(11, hours * (future ? 1 : -1));
         }

         if (minutes > 0) {
            c.add(12, minutes * (future ? 1 : -1));
         }

         if (seconds > 0) {
            c.add(13, seconds * (future ? 1 : -1));
         }

         Calendar max = new GregorianCalendar();
         max.add(1, 10);
         return c.after(max) ? max.getTimeInMillis() : c.getTimeInMillis();
      }
   }

   public static Location getTarget(LivingEntity entity) throws Exception {
      Block block = entity.getTargetBlock(AIR_MATERIALS_TARGET, 300);
      if (block == null) {
         throw new Exception("Not targeting a block");
      } else {
         return block.getLocation();
      }
   }

   public static Location getSafeDestination(Location loc) throws Exception {
      if (loc != null && loc.getWorld() != null) {
         World world = loc.getWorld();
         int x = loc.getBlockX();
         int y = (int)Math.round(loc.getY());
         int z = loc.getBlockZ();
         int origX = x;
         int origY = y;
         int origZ = z;

         while(isBlockAboveAir(world, x, y, z)) {
            --y;
            if (y < 0) {
               y = origY;
               break;
            }
         }

         for(int i = 0; isBlockUnsafe(world, x, y, z); z = origZ + VOLUME[i].z) {
            ++i;
            if (i >= VOLUME.length) {
               x = origX;
               y = origY + 3;
               z = origZ;
               break;
            }

            x = origX + VOLUME[i].x;
            y = origY + VOLUME[i].y;
         }

         while(isBlockUnsafe(world, x, y, z)) {
            ++y;
            if (y >= world.getMaxHeight()) {
               ++x;
               break;
            }
         }

         while(isBlockUnsafe(world, x, y, z)) {
            --y;
            if (y <= 1) {
               ++x;
               y = world.getHighestBlockYAt(x, z);
               if (x - 48 > loc.getBlockX()) {
                  throw new Exception(I18n._("holeInFloor"));
               }
            }
         }

         return new Location(world, (double)x + (double)0.5F, (double)y, (double)z + (double)0.5F, loc.getYaw(), loc.getPitch());
      } else {
         throw new Exception(I18n._("destinationNotSet"));
      }
   }

   private static boolean isBlockAboveAir(World world, int x, int y, int z) {
      return AIR_MATERIALS.contains(world.getBlockAt(x, y - 1, z).getType().getId());
   }

   public static boolean isBlockUnsafe(World world, int x, int y, int z) {
      return isBlockDamaging(world, x, y, z) ? true : isBlockAboveAir(world, x, y, z);
   }

   public static boolean isBlockDamaging(World world, int x, int y, int z) {
      Block below = world.getBlockAt(x, y - 1, z);
      if (below.getType() != Material.LAVA && below.getType() != Material.STATIONARY_LAVA) {
         if (below.getType() == Material.FIRE) {
            return true;
         } else if (below.getType() == Material.BED_BLOCK) {
            return true;
         } else {
            return !AIR_MATERIALS.contains(world.getBlockAt(x, y, z).getType().getId()) || !AIR_MATERIALS.contains(world.getBlockAt(x, y + 1, z).getType().getId());
         }
      } else {
         return true;
      }
   }

   public static ItemStack convertBlockToItem(Block block) {
      ItemStack is = new ItemStack(block.getType(), 1, (short)0, block.getData());
      switch (is.getType()) {
         case WOODEN_DOOR:
            is.setType(Material.WOOD_DOOR);
            is.setDurability((short)0);
            break;
         case IRON_DOOR_BLOCK:
            is.setType(Material.IRON_DOOR);
            is.setDurability((short)0);
            break;
         case SIGN_POST:
         case WALL_SIGN:
            is.setType(Material.SIGN);
            is.setDurability((short)0);
            break;
         case CROPS:
            is.setType(Material.SEEDS);
            is.setDurability((short)0);
            break;
         case CAKE_BLOCK:
            is.setType(Material.CAKE);
            is.setDurability((short)0);
            break;
         case BED_BLOCK:
            is.setType(Material.BED);
            is.setDurability((short)0);
            break;
         case REDSTONE_WIRE:
            is.setType(Material.REDSTONE);
            is.setDurability((short)0);
            break;
         case REDSTONE_TORCH_OFF:
         case REDSTONE_TORCH_ON:
            is.setType(Material.REDSTONE_TORCH_ON);
            is.setDurability((short)0);
            break;
         case DIODE_BLOCK_OFF:
         case DIODE_BLOCK_ON:
            is.setType(Material.DIODE);
            is.setDurability((short)0);
            break;
         case DOUBLE_STEP:
            is.setType(Material.STEP);
            break;
         case TORCH:
         case RAILS:
         case LADDER:
         case WOOD_STAIRS:
         case COBBLESTONE_STAIRS:
         case LEVER:
         case STONE_BUTTON:
         case FURNACE:
         case DISPENSER:
         case PUMPKIN:
         case JACK_O_LANTERN:
         case WOOD_PLATE:
         case STONE_PLATE:
         case PISTON_STICKY_BASE:
         case PISTON_BASE:
         case IRON_FENCE:
         case THIN_GLASS:
         case TRAP_DOOR:
         case FENCE:
         case FENCE_GATE:
         case NETHER_FENCE:
            is.setDurability((short)0);
            break;
         case FIRE:
            return null;
         case PUMPKIN_STEM:
            is.setType(Material.PUMPKIN_SEEDS);
            break;
         case MELON_STEM:
            is.setType(Material.MELON_SEEDS);
      }

      return is;
   }

   public static String formatAsCurrency(double value) {
      String str = dFormat.format(value);
      if (str.endsWith(".00")) {
         str = str.substring(0, str.length() - 3);
      }

      return str;
   }

   public static String displayCurrency(double value, IEssentials ess) {
      return I18n._("currency", ess.getSettings().getCurrencySymbol(), formatAsCurrency(value));
   }

   public static String shortCurrency(double value, IEssentials ess) {
      return ess.getSettings().getCurrencySymbol() + formatAsCurrency(value);
   }

   public static double roundDouble(double d) {
      return (double)Math.round(d * (double)100.0F) / (double)100.0F;
   }

   public static boolean isInt(String sInt) {
      try {
         Integer.parseInt(sInt);
         return true;
      } catch (NumberFormatException var2) {
         return false;
      }
   }

   public static String joinList(Object... list) {
      return joinList(", ", list);
   }

   public static String joinList(String seperator, Object... list) {
      StringBuilder buf = new StringBuilder();

      for(Object each : list) {
         if (buf.length() > 0) {
            buf.append(seperator);
         }

         if (each instanceof Collection) {
            buf.append(joinList(seperator, ((Collection)each).toArray()));
         } else {
            try {
               buf.append(each.toString());
            } catch (Exception var8) {
               buf.append(each.toString());
            }
         }
      }

      return buf.toString();
   }

   public static String lastCode(String input) {
      int pos = input.lastIndexOf("§");
      return pos != -1 && pos + 1 != input.length() ? input.substring(pos, pos + 2) : "";
   }

   public static String stripFormat(String input) {
      return input == null ? null : VANILLA_PATTERN.matcher(input).replaceAll("");
   }

   public static String stripLogColorFormat(String input) {
      return input == null ? null : LOGCOLOR_PATTERN.matcher(input).replaceAll("");
   }

   public static String replaceFormat(String input) {
      return input == null ? null : REPLACE_PATTERN.matcher(input).replaceAll("§$1");
   }

   public static String formatString(IUser user, String permBase, String input) {
      if (input == null) {
         return null;
      } else {
         String message;
         if (user.isAuthorized(permBase + ".color")) {
            message = replaceColor(input, REPLACE_COLOR_PATTERN);
         } else {
            message = stripColor(input, VANILLA_COLOR_PATTERN);
         }

         if (user.isAuthorized(permBase + ".magic")) {
            message = replaceColor(message, REPLACE_MAGIC_PATTERN);
         } else {
            message = stripColor(message, VANILLA_MAGIC_PATTERN);
         }

         if (user.isAuthorized(permBase + ".format")) {
            message = replaceColor(message, REPLACE_FORMAT_PATTERN);
         } else {
            message = stripColor(message, VANILLA_FORMAT_PATTERN);
         }

         return message;
      }
   }

   public static String formatMessage(IUser user, String permBase, String input) {
      if (input == null) {
         return null;
      } else {
         String message = formatString(user, permBase, input);
         if (!user.isAuthorized(permBase + ".url")) {
            message = blockURL(message);
         }

         return message;
      }
   }

   private static String blockURL(String input) {
      if (input == null) {
         return null;
      } else {
         String text;
         for(text = URL_PATTERN.matcher(input).replaceAll("$1 $2"); URL_PATTERN.matcher(text).find(); text = URL_PATTERN.matcher(text).replaceAll("$1 $2")) {
         }

         return text;
      }
   }

   private static String stripColor(String input, Pattern pattern) {
      return pattern.matcher(input).replaceAll("");
   }

   private static String replaceColor(String input, Pattern pattern) {
      return pattern.matcher(input).replaceAll("§$1");
   }

   public static boolean validIP(String ipAddress) {
      return IPPATTERN.matcher(ipAddress).matches();
   }

   static {
      AIR_MATERIALS.add(Material.AIR.getId());
      AIR_MATERIALS.add(Material.SAPLING.getId());
      AIR_MATERIALS.add(Material.POWERED_RAIL.getId());
      AIR_MATERIALS.add(Material.DETECTOR_RAIL.getId());
      AIR_MATERIALS.add(Material.LONG_GRASS.getId());
      AIR_MATERIALS.add(Material.DEAD_BUSH.getId());
      AIR_MATERIALS.add(Material.YELLOW_FLOWER.getId());
      AIR_MATERIALS.add(Material.RED_ROSE.getId());
      AIR_MATERIALS.add(Material.BROWN_MUSHROOM.getId());
      AIR_MATERIALS.add(Material.RED_MUSHROOM.getId());
      AIR_MATERIALS.add(Material.TORCH.getId());
      AIR_MATERIALS.add(Material.REDSTONE_WIRE.getId());
      AIR_MATERIALS.add(Material.SEEDS.getId());
      AIR_MATERIALS.add(Material.SIGN_POST.getId());
      AIR_MATERIALS.add(Material.WOODEN_DOOR.getId());
      AIR_MATERIALS.add(Material.LADDER.getId());
      AIR_MATERIALS.add(Material.RAILS.getId());
      AIR_MATERIALS.add(Material.WALL_SIGN.getId());
      AIR_MATERIALS.add(Material.LEVER.getId());
      AIR_MATERIALS.add(Material.STONE_PLATE.getId());
      AIR_MATERIALS.add(Material.IRON_DOOR_BLOCK.getId());
      AIR_MATERIALS.add(Material.WOOD_PLATE.getId());
      AIR_MATERIALS.add(Material.REDSTONE_TORCH_OFF.getId());
      AIR_MATERIALS.add(Material.REDSTONE_TORCH_ON.getId());
      AIR_MATERIALS.add(Material.STONE_BUTTON.getId());
      AIR_MATERIALS.add(Material.SNOW.getId());
      AIR_MATERIALS.add(Material.SUGAR_CANE_BLOCK.getId());
      AIR_MATERIALS.add(Material.DIODE_BLOCK_OFF.getId());
      AIR_MATERIALS.add(Material.DIODE_BLOCK_ON.getId());
      AIR_MATERIALS.add(Material.TRAP_DOOR.getId());
      AIR_MATERIALS.add(Material.PUMPKIN_STEM.getId());
      AIR_MATERIALS.add(Material.MELON_STEM.getId());
      AIR_MATERIALS.add(Material.VINE.getId());
      AIR_MATERIALS.add(Material.FENCE_GATE.getId());
      AIR_MATERIALS.add(Material.WATER_LILY.getId());
      AIR_MATERIALS.add(Material.NETHER_FENCE.getId());
      AIR_MATERIALS.add(Material.NETHER_WARTS.getId());

      for(Integer integer : AIR_MATERIALS) {
         AIR_MATERIALS_TARGET.add(integer.byteValue());
      }

      AIR_MATERIALS_TARGET.add((byte)Material.WATER.getId());
      AIR_MATERIALS_TARGET.add((byte)Material.STATIONARY_WATER.getId());
      List<Vector3D> pos = new ArrayList();

      for(int x = -3; x <= 3; ++x) {
         for(int y = -3; y <= 3; ++y) {
            for(int z = -3; z <= 3; ++z) {
               pos.add(new Vector3D(x, y, z));
            }
         }
      }

      Collections.sort(pos, new Comparator() {
         public int compare(Vector3D a, Vector3D b) {
            return a.x * a.x + a.y * a.y + a.z * a.z - (b.x * b.x + b.y * b.y + b.z * b.z);
         }
      });
      VOLUME = (Vector3D[])pos.toArray(new Vector3D[0]);
      dFormat = new DecimalFormat("#0.00", DecimalFormatSymbols.getInstance(Locale.US));
      URL_PATTERN = Pattern.compile("((?:(?:https?)://)?[\\w-_\\.]{2,})\\.([a-z]{2,3}(?:/\\S+)?)");
      VANILLA_PATTERN = Pattern.compile("§+[0-9A-FK-ORa-fk-or]?");
      LOGCOLOR_PATTERN = Pattern.compile("\\x1B\\[([0-9]{1,2}(;[0-9]{1,2})?)?[m|K]");
      REPLACE_PATTERN = Pattern.compile("&([0-9a-fk-or])");
      VANILLA_COLOR_PATTERN = Pattern.compile("§+[0-9A-Fa-f]");
      VANILLA_MAGIC_PATTERN = Pattern.compile("§+[Kk]");
      VANILLA_FORMAT_PATTERN = Pattern.compile("§+[L-ORl-or]");
      REPLACE_COLOR_PATTERN = Pattern.compile("&([0-9a-f])");
      REPLACE_MAGIC_PATTERN = Pattern.compile("&(k)");
      REPLACE_FORMAT_PATTERN = Pattern.compile("&([l-or])");
      IPPATTERN = Pattern.compile("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
   }

   public static class Vector3D {
      public int x;
      public int y;
      public int z;

      public Vector3D(int x, int y, int z) {
         this.x = x;
         this.y = y;
         this.z = z;
      }
   }
}
