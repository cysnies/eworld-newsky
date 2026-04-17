package org.maxgamer.QuickShop.Util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import net.minecraft.server.NBTCompressedStreamTools;
import net.minecraft.server.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class NMS {
   private static ArrayList dependents = new ArrayList();
   private static int nextId = 0;
   private static NMSDependent nms;

   static {
      NMSDependent dep = new NMSDependent("") {
         public void safeGuard(Item item) {
            ItemStack iStack = item.getItemStack();
            net.minecraft.server.ItemStack nmsI = CraftItemStack.asNMSCopy(iStack);
            nmsI.count = 0;
            iStack = CraftItemStack.asBukkitCopy(nmsI);
            item.setItemStack(iStack);
         }

         public byte[] getNBTBytes(ItemStack iStack) {
            net.minecraft.server.ItemStack is = CraftItemStack.asNMSCopy(iStack);
            NBTTagCompound itemCompound = new NBTTagCompound();
            itemCompound = is.save(itemCompound);
            return NBTCompressedStreamTools.a(itemCompound);
         }

         public ItemStack getItemStack(byte[] bytes) {
            NBTTagCompound c = NBTCompressedStreamTools.a(bytes);
            net.minecraft.server.ItemStack is = net.minecraft.server.ItemStack.createStack(c);
            return CraftItemStack.asBukkitCopy(is);
         }
      };
      dependents.add(dep);
      dep = new NMSDependent("v1_4_5") {
         public void safeGuard(Item item) {
            ItemStack iStack = item.getItemStack();
            net.minecraft.server.v1_4_5.ItemStack nmsI = org.bukkit.craftbukkit.v1_4_5.inventory.CraftItemStack.createNMSItemStack(iStack);
            nmsI.count = 0;
            iStack = org.bukkit.craftbukkit.v1_4_5.inventory.CraftItemStack.asBukkitStack(nmsI);
            item.setItemStack(iStack);
         }

         public byte[] getNBTBytes(ItemStack iStack) {
            net.minecraft.server.v1_4_5.ItemStack is = org.bukkit.craftbukkit.v1_4_5.inventory.CraftItemStack.createNMSItemStack(iStack);
            net.minecraft.server.v1_4_5.NBTTagCompound itemCompound = new net.minecraft.server.v1_4_5.NBTTagCompound();
            itemCompound = is.save(itemCompound);
            return net.minecraft.server.v1_4_5.NBTCompressedStreamTools.a(itemCompound);
         }

         public ItemStack getItemStack(byte[] bytes) {
            net.minecraft.server.v1_4_5.NBTTagCompound c = net.minecraft.server.v1_4_5.NBTCompressedStreamTools.a(bytes);
            net.minecraft.server.v1_4_5.ItemStack is = net.minecraft.server.v1_4_5.ItemStack.a(c);
            return org.bukkit.craftbukkit.v1_4_5.inventory.CraftItemStack.asBukkitStack(is);
         }
      };
      dependents.add(dep);
      dep = new NMSDependent("v1_4_6") {
         public void safeGuard(Item item) {
            ItemStack iStack = item.getItemStack();
            net.minecraft.server.v1_4_6.ItemStack nmsI = org.bukkit.craftbukkit.v1_4_6.inventory.CraftItemStack.asNMSCopy(iStack);
            nmsI.count = 0;
            iStack = org.bukkit.craftbukkit.v1_4_6.inventory.CraftItemStack.asBukkitCopy(nmsI);
            item.setItemStack(iStack);
         }

         public byte[] getNBTBytes(ItemStack iStack) {
            net.minecraft.server.v1_4_6.ItemStack is = org.bukkit.craftbukkit.v1_4_6.inventory.CraftItemStack.asNMSCopy(iStack);
            net.minecraft.server.v1_4_6.NBTTagCompound itemCompound = new net.minecraft.server.v1_4_6.NBTTagCompound();
            itemCompound = is.save(itemCompound);
            return net.minecraft.server.v1_4_6.NBTCompressedStreamTools.a(itemCompound);
         }

         public ItemStack getItemStack(byte[] bytes) {
            net.minecraft.server.v1_4_6.NBTTagCompound c = net.minecraft.server.v1_4_6.NBTCompressedStreamTools.a(bytes);
            net.minecraft.server.v1_4_6.ItemStack is = net.minecraft.server.v1_4_6.ItemStack.a(c);
            return org.bukkit.craftbukkit.v1_4_6.inventory.CraftItemStack.asBukkitCopy(is);
         }
      };
      dependents.add(dep);
      dep = new NMSDependent("v1_4_R1") {
         public void safeGuard(Item item) {
            ItemStack iStack = item.getItemStack();
            net.minecraft.server.v1_4_R1.ItemStack nmsI = org.bukkit.craftbukkit.v1_4_R1.inventory.CraftItemStack.asNMSCopy(iStack);
            nmsI.count = 0;
            iStack = org.bukkit.craftbukkit.v1_4_R1.inventory.CraftItemStack.asBukkitCopy(nmsI);
            item.setItemStack(iStack);
         }

         public byte[] getNBTBytes(ItemStack iStack) {
            net.minecraft.server.v1_4_R1.ItemStack is = org.bukkit.craftbukkit.v1_4_R1.inventory.CraftItemStack.asNMSCopy(iStack);
            net.minecraft.server.v1_4_R1.NBTTagCompound itemCompound = new net.minecraft.server.v1_4_R1.NBTTagCompound();
            itemCompound = is.save(itemCompound);
            return net.minecraft.server.v1_4_R1.NBTCompressedStreamTools.a(itemCompound);
         }

         public ItemStack getItemStack(byte[] bytes) {
            net.minecraft.server.v1_4_R1.NBTTagCompound c = net.minecraft.server.v1_4_R1.NBTCompressedStreamTools.a(bytes);
            net.minecraft.server.v1_4_R1.ItemStack is = net.minecraft.server.v1_4_R1.ItemStack.createStack(c);
            return org.bukkit.craftbukkit.v1_4_R1.inventory.CraftItemStack.asBukkitCopy(is);
         }
      };
      dependents.add(dep);
      dep = new NMSDependent("v1_5_R1") {
         public void safeGuard(Item item) {
            ItemStack iStack = item.getItemStack();
            net.minecraft.server.v1_5_R1.ItemStack nmsI = org.bukkit.craftbukkit.v1_5_R1.inventory.CraftItemStack.asNMSCopy(iStack);
            nmsI.count = 0;
            iStack = org.bukkit.craftbukkit.v1_5_R1.inventory.CraftItemStack.asBukkitCopy(nmsI);
            item.setItemStack(iStack);
         }

         public byte[] getNBTBytes(ItemStack iStack) {
            net.minecraft.server.v1_5_R1.ItemStack is = org.bukkit.craftbukkit.v1_5_R1.inventory.CraftItemStack.asNMSCopy(iStack);
            net.minecraft.server.v1_5_R1.NBTTagCompound itemCompound = new net.minecraft.server.v1_5_R1.NBTTagCompound();
            itemCompound = is.save(itemCompound);
            return net.minecraft.server.v1_5_R1.NBTCompressedStreamTools.a(itemCompound);
         }

         public ItemStack getItemStack(byte[] bytes) {
            net.minecraft.server.v1_5_R1.NBTTagCompound c = net.minecraft.server.v1_5_R1.NBTCompressedStreamTools.a(bytes);
            net.minecraft.server.v1_5_R1.ItemStack is = net.minecraft.server.v1_5_R1.ItemStack.createStack(c);
            return org.bukkit.craftbukkit.v1_5_R1.inventory.CraftItemStack.asBukkitCopy(is);
         }
      };
      dependents.add(dep);
      dep = new NMSDependent("v1_5_R2") {
         public void safeGuard(Item item) {
            ItemStack iStack = item.getItemStack();
            net.minecraft.server.v1_5_R2.ItemStack nmsI = org.bukkit.craftbukkit.v1_5_R2.inventory.CraftItemStack.asNMSCopy(iStack);
            nmsI.count = 0;
            iStack = org.bukkit.craftbukkit.v1_5_R2.inventory.CraftItemStack.asBukkitCopy(nmsI);
            item.setItemStack(iStack);
         }

         public byte[] getNBTBytes(ItemStack iStack) {
            net.minecraft.server.v1_5_R2.ItemStack is = org.bukkit.craftbukkit.v1_5_R2.inventory.CraftItemStack.asNMSCopy(iStack);
            net.minecraft.server.v1_5_R2.NBTTagCompound itemCompound = new net.minecraft.server.v1_5_R2.NBTTagCompound();
            itemCompound = is.save(itemCompound);
            return net.minecraft.server.v1_5_R2.NBTCompressedStreamTools.a(itemCompound);
         }

         public ItemStack getItemStack(byte[] bytes) {
            net.minecraft.server.v1_5_R2.NBTTagCompound c = net.minecraft.server.v1_5_R2.NBTCompressedStreamTools.a(bytes);
            net.minecraft.server.v1_5_R2.ItemStack is = net.minecraft.server.v1_5_R2.ItemStack.createStack(c);
            return org.bukkit.craftbukkit.v1_5_R2.inventory.CraftItemStack.asBukkitCopy(is);
         }
      };
      dependents.add(dep);
      dep = new NMSDependent("v1_5_R3") {
         public void safeGuard(Item item) {
            ItemStack iStack = item.getItemStack();
            net.minecraft.server.v1_5_R3.ItemStack nmsI = org.bukkit.craftbukkit.v1_5_R3.inventory.CraftItemStack.asNMSCopy(iStack);
            nmsI.count = 0;
            iStack = org.bukkit.craftbukkit.v1_5_R3.inventory.CraftItemStack.asBukkitCopy(nmsI);
            item.setItemStack(iStack);
         }

         public byte[] getNBTBytes(ItemStack iStack) {
            net.minecraft.server.v1_5_R3.ItemStack is = org.bukkit.craftbukkit.v1_5_R3.inventory.CraftItemStack.asNMSCopy(iStack);
            net.minecraft.server.v1_5_R3.NBTTagCompound itemCompound = new net.minecraft.server.v1_5_R3.NBTTagCompound();
            itemCompound = is.save(itemCompound);
            return net.minecraft.server.v1_5_R3.NBTCompressedStreamTools.a(itemCompound);
         }

         public ItemStack getItemStack(byte[] bytes) {
            net.minecraft.server.v1_5_R3.NBTTagCompound c = net.minecraft.server.v1_5_R3.NBTCompressedStreamTools.a(bytes);
            net.minecraft.server.v1_5_R3.ItemStack is = net.minecraft.server.v1_5_R3.ItemStack.createStack(c);
            return org.bukkit.craftbukkit.v1_5_R3.inventory.CraftItemStack.asBukkitCopy(is);
         }
      };
      dependents.add(dep);
   }

   public static void safeGuard(Item item) throws ClassNotFoundException {
      rename(item.getItemStack());
      protect(item);
      item.setPickupDelay(Integer.MAX_VALUE);
   }

   private static void rename(ItemStack iStack) {
      ItemMeta meta = iStack.getItemMeta();
      meta.setDisplayName(ChatColor.RED + "QuickShop " + Util.getName(iStack) + " " + nextId++);
      iStack.setItemMeta(meta);
   }

   public static byte[] getNBTBytes(ItemStack iStack) throws ClassNotFoundException {
      validate();
      return nms.getNBTBytes(iStack);
   }

   public static ItemStack getItemStack(byte[] bytes) throws ClassNotFoundException {
      validate();
      return nms.getItemStack(bytes);
   }

   private static void protect(Item item) {
      try {
         Field itemField = item.getClass().getDeclaredField("item");
         itemField.setAccessible(true);
         Object nmsEntityItem = itemField.get(item);

         Method getItemStack;
         try {
            getItemStack = nmsEntityItem.getClass().getMethod("getItemStack");
         } catch (NoSuchMethodException var8) {
            getItemStack = nmsEntityItem.getClass().getMethod("d");
         }

         Object itemStack = getItemStack.invoke(nmsEntityItem);

         Field countField;
         try {
            countField = itemStack.getClass().getDeclaredField("count");
         } catch (NoSuchFieldException var7) {
            countField = itemStack.getClass().getDeclaredField("a");
         }

         countField.setAccessible(true);
         countField.set(itemStack, 0);
      } catch (NoSuchFieldException e) {
         e.printStackTrace();
         System.out.println("[QuickShop] Could not protect item from pickup properly! Dupes are now possible.");
      } catch (IllegalArgumentException e) {
         e.printStackTrace();
      } catch (IllegalAccessException e) {
         e.printStackTrace();
      } catch (SecurityException e) {
         e.printStackTrace();
      } catch (InvocationTargetException e) {
         e.printStackTrace();
      } catch (NoSuchMethodException e) {
         e.printStackTrace();
      }

   }

   private static void validate() throws ClassNotFoundException {
      if (nms == null) {
         String packageName = Bukkit.getServer().getClass().getPackage().getName();
         packageName = packageName.substring(packageName.lastIndexOf(".") + 1);
         System.out.println("Package: " + packageName);

         for(NMSDependent dep : dependents) {
            if (dep.getVersion().equals(packageName) || dep.getVersion().isEmpty() && (packageName.equals("bukkit") || packageName.equals("craftbukkit"))) {
               nms = dep;
               return;
            }
         }

         throw new ClassNotFoundException("This version of QuickShop is incompatible.");
      }
   }

   private abstract static class NMSDependent {
      private String version;

      public String getVersion() {
         return this.version;
      }

      public NMSDependent(String version) {
         this.version = version;
      }

      public abstract void safeGuard(Item var1);

      public abstract byte[] getNBTBytes(ItemStack var1);

      public abstract ItemStack getItemStack(byte[] var1);
   }
}
