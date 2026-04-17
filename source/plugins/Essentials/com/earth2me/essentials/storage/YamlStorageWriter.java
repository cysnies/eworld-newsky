package com.earth2me.essentials.storage;

import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.yaml.snakeyaml.Yaml;

public class YamlStorageWriter implements IStorageWriter {
   private static final transient Pattern NON_WORD_PATTERN = Pattern.compile("\\W");
   private final transient PrintWriter writer;
   private static final transient Yaml YAML = new Yaml();

   public YamlStorageWriter(PrintWriter writer) {
      this.writer = writer;
   }

   public void save(StorageObject object) {
      try {
         this.writeToFile(object, 0, object.getClass());
      } catch (IllegalArgumentException ex) {
         Logger.getLogger(YamlStorageWriter.class.getName()).log(Level.SEVERE, (String)null, ex);
      } catch (IllegalAccessException ex) {
         Logger.getLogger(YamlStorageWriter.class.getName()).log(Level.SEVERE, (String)null, ex);
      }

   }

   private void writeToFile(Object object, int depth, Class clazz) throws IllegalAccessException {
      for(Field field : clazz.getDeclaredFields()) {
         int modifier = field.getModifiers();
         if (Modifier.isPrivate(modifier) && !Modifier.isTransient(modifier) && !Modifier.isStatic(modifier)) {
            field.setAccessible(true);
            Object data = field.get(object);
            if (!this.writeKey(field, depth, data)) {
               if (data instanceof StorageObject) {
                  this.writer.println();
                  this.writeToFile(data, depth + 1, data.getClass());
               } else if (data instanceof Map) {
                  this.writeMap((Map)data, depth + 1);
               } else if (data instanceof Collection) {
                  this.writeCollection((Collection)data, depth + 1);
               } else if (data instanceof Location) {
                  this.writeLocation((Location)data, depth + 1);
               } else {
                  this.writeScalar(data);
                  this.writer.println();
               }
            }
         }
      }

   }

   private boolean writeKey(Field field, int depth, Object data) {
      boolean commentPresent = this.writeComment(field, depth);
      if (data == null && !commentPresent) {
         return true;
      } else {
         this.writeIndention(depth);
         if (data == null && commentPresent) {
            this.writer.print('#');
         }

         String name = field.getName();
         this.writer.print(name);
         this.writer.print(": ");
         if (data == null && commentPresent) {
            this.writer.println();
            this.writer.println();
            return true;
         } else {
            return false;
         }
      }
   }

   private boolean writeComment(Field field, int depth) {
      boolean commentPresent = field.isAnnotationPresent(Comment.class);
      if (commentPresent) {
         Comment comments = (Comment)field.getAnnotation(Comment.class);

         for(String comment : comments.value()) {
            String trimmed = comment.trim();
            if (!trimmed.isEmpty()) {
               this.writeIndention(depth);
               this.writer.print("# ");
               this.writer.print(trimmed);
               this.writer.println();
            }
         }
      }

      return commentPresent;
   }

   private void writeCollection(Collection data, int depth) throws IllegalAccessException {
      this.writer.println();
      if (data.isEmpty()) {
         this.writer.println();
      }

      for(Object entry : data) {
         if (entry != null) {
            this.writeIndention(depth);
            this.writer.print("- ");
            if (entry instanceof StorageObject) {
               this.writer.println();
               this.writeToFile(entry, depth + 1, entry.getClass());
            } else if (entry instanceof Location) {
               this.writeLocation((Location)entry, depth + 1);
            } else {
               this.writeScalar(entry);
            }
         }
      }

      this.writer.println();
   }

   private void writeMap(Map data, int depth) throws IllegalArgumentException, IllegalAccessException {
      this.writer.println();
      if (data.isEmpty()) {
         this.writer.println();
      }

      for(Map.Entry entry : data.entrySet()) {
         Object value = entry.getValue();
         if (value != null) {
            this.writeIndention(depth);
            this.writeKey(entry.getKey());
            this.writer.print(": ");
            if (value instanceof StorageObject) {
               this.writer.println();
               this.writeToFile(value, depth + 1, value.getClass());
            } else if (value instanceof Collection) {
               this.writeCollection((Collection)value, depth + 1);
            } else if (value instanceof Location) {
               this.writeLocation((Location)value, depth + 1);
            } else {
               this.writeScalar(value);
               this.writer.println();
            }
         }
      }

   }

   private void writeIndention(int depth) {
      for(int i = 0; i < depth; ++i) {
         this.writer.print("  ");
      }

   }

   private void writeScalar(Object data) {
      if (!(data instanceof String) && !(data instanceof Boolean) && !(data instanceof Number)) {
         if (data instanceof Material) {
            this.writeMaterial(data);
            this.writer.println();
         } else if (data instanceof MaterialData) {
            this.writeMaterialData(data);
            this.writer.println();
         } else if (data instanceof ItemStack) {
            this.writeItemStack(data);
            this.writer.println();
         } else {
            if (!(data instanceof EnchantmentLevel)) {
               throw new UnsupportedOperationException();
            }

            this.writeEnchantmentLevel(data);
            this.writer.println();
         }
      } else {
         synchronized(YAML) {
            YAML.dumpAll(Collections.singletonList(data).iterator(), this.writer);
         }
      }

   }

   private void writeKey(Object data) {
      if (!(data instanceof String) && !(data instanceof Boolean) && !(data instanceof Number)) {
         if (data instanceof Material) {
            this.writeMaterial(data);
         } else if (data instanceof MaterialData) {
            this.writeMaterialData(data);
         } else {
            if (!(data instanceof EnchantmentLevel)) {
               throw new UnsupportedOperationException();
            }

            this.writeEnchantmentLevel(data);
         }
      } else {
         String output = data.toString();
         if (NON_WORD_PATTERN.matcher(output).find()) {
            this.writer.print('"');
            this.writer.print(output.replace("\"", "\\\""));
            this.writer.print('"');
         } else {
            this.writer.print(output);
         }
      }

   }

   private void writeMaterial(Object data) {
      this.writer.print(data.toString().toLowerCase(Locale.ENGLISH));
   }

   private void writeMaterialData(Object data) {
      MaterialData matData = (MaterialData)data;
      this.writeMaterial(matData.getItemType());
      if (matData.getData() > 0) {
         this.writer.print(':');
         this.writer.print(matData.getData());
      }

   }

   private void writeItemStack(Object data) {
      ItemStack itemStack = (ItemStack)data;
      this.writeMaterialData(itemStack.getData());
      this.writer.print(' ');
      this.writer.print(itemStack.getAmount());

      for(Map.Entry entry : itemStack.getEnchantments().entrySet()) {
         this.writer.print(' ');
         this.writeEnchantmentLevel(entry);
      }

   }

   private void writeEnchantmentLevel(Object data) {
      Map.Entry<Enchantment, Integer> enchLevel = (Map.Entry)data;
      this.writer.print(((Enchantment)enchLevel.getKey()).getName().toLowerCase(Locale.ENGLISH));
      this.writer.print(':');
      this.writer.print(enchLevel.getValue());
   }

   private void writeLocation(Location entry, int depth) {
      this.writer.println();
      this.writeIndention(depth);
      this.writer.print("world: ");
      this.writeScalar(entry.getWorld().getName());
      this.writeIndention(depth);
      this.writer.print("x: ");
      this.writeScalar(entry.getX());
      this.writeIndention(depth);
      this.writer.print("y: ");
      this.writeScalar(entry.getY());
      this.writeIndention(depth);
      this.writer.print("z: ");
      this.writeScalar(entry.getZ());
      this.writeIndention(depth);
      this.writer.print("yaw: ");
      this.writeScalar(entry.getYaw());
      this.writeIndention(depth);
      this.writer.print("pitch: ");
      this.writeScalar(entry.getPitch());
   }
}
