package com.earth2me.essentials.storage;

import com.earth2me.essentials.Util;
import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;

public class BukkitConstructor extends CustomClassLoaderConstructor {
   private final transient Plugin plugin;

   public BukkitConstructor(Class clazz, Plugin plugin) {
      super(clazz, plugin.getClass().getClassLoader());
      this.plugin = plugin;
      this.yamlClassConstructors.put(NodeId.scalar, new ConstructBukkitScalar());
      this.yamlClassConstructors.put(NodeId.mapping, new ConstructBukkitMapping());
   }

   private class ConstructBukkitScalar extends Constructor.ConstructScalar {
      private ConstructBukkitScalar() {
         super(BukkitConstructor.this);
      }

      public Object construct(Node node) {
         if (node.getType().equals(Material.class)) {
            String val = (String)BukkitConstructor.this.constructScalar((ScalarNode)node);
            Material mat;
            if (Util.isInt(val)) {
               int typeId = Integer.parseInt(val);
               mat = Material.getMaterial(typeId);
            } else {
               mat = Material.matchMaterial(val);
            }

            return mat;
         } else if (node.getType().equals(MaterialData.class)) {
            String val = (String)BukkitConstructor.this.constructScalar((ScalarNode)node);
            if (val.isEmpty()) {
               return null;
            } else {
               String[] split = val.split("[:+',;.]", 2);
               if (split.length == 0) {
                  return null;
               } else {
                  Material mat;
                  if (Util.isInt(split[0])) {
                     int typeId = Integer.parseInt(split[0]);
                     mat = Material.getMaterial(typeId);
                  } else {
                     mat = Material.matchMaterial(split[0]);
                  }

                  if (mat == null) {
                     return null;
                  } else {
                     byte data = 0;
                     if (split.length == 2 && Util.isInt(split[1])) {
                        data = Byte.parseByte(split[1]);
                     }

                     return new MaterialData(mat, data);
                  }
               }
            }
         } else if (!node.getType().equals(ItemStack.class)) {
            if (node.getType().equals(EnchantmentLevel.class)) {
               String val = (String)BukkitConstructor.this.constructScalar((ScalarNode)node);
               if (val.isEmpty()) {
                  return null;
               } else {
                  String[] split = val.split("[:+',;.]", 2);
                  if (split.length == 0) {
                     return null;
                  } else {
                     Enchantment enchant;
                     if (Util.isInt(split[0])) {
                        int typeId = Integer.parseInt(split[0]);
                        enchant = Enchantment.getById(typeId);
                     } else {
                        enchant = Enchantment.getByName(split[0].toUpperCase(Locale.ENGLISH));
                     }

                     if (enchant == null) {
                        return null;
                     } else {
                        int level = enchant.getStartLevel();
                        if (split.length == 2 && Util.isInt(split[1])) {
                           level = Integer.parseInt(split[1]);
                        }

                        if (level < enchant.getStartLevel()) {
                           level = enchant.getStartLevel();
                        }

                        if (level > enchant.getMaxLevel()) {
                           level = enchant.getMaxLevel();
                        }

                        return new EnchantmentLevel(enchant, level);
                     }
                  }
               }
            } else {
               return super.construct(node);
            }
         } else {
            String val = (String)BukkitConstructor.this.constructScalar((ScalarNode)node);
            if (val.isEmpty()) {
               return null;
            } else {
               String[] split1 = val.split("\\W");
               if (split1.length == 0) {
                  return null;
               } else {
                  String[] split2 = split1[0].split("[:+',;.]", 2);
                  if (split2.length == 0) {
                     return null;
                  } else {
                     Material mat;
                     if (Util.isInt(split2[0])) {
                        int typeId = Integer.parseInt(split2[0]);
                        mat = Material.getMaterial(typeId);
                     } else {
                        mat = Material.matchMaterial(split2[0]);
                     }

                     if (mat == null) {
                        return null;
                     } else {
                        short data = 0;
                        if (split2.length == 2 && Util.isInt(split2[1])) {
                           data = Short.parseShort(split2[1]);
                        }

                        int size = mat.getMaxStackSize();
                        if (split1.length > 1 && Util.isInt(split1[1])) {
                           size = Integer.parseInt(split1[1]);
                        }

                        ItemStack stack = new ItemStack(mat, size, data);
                        if (split1.length > 2) {
                           for(int i = 2; i < split1.length; ++i) {
                              String[] split3 = split1[0].split("[:+',;.]", 2);
                              if (split3.length >= 1) {
                                 Enchantment enchantment;
                                 if (Util.isInt(split3[0])) {
                                    int enchantId = Integer.parseInt(split3[0]);
                                    enchantment = Enchantment.getById(enchantId);
                                 } else {
                                    enchantment = Enchantment.getByName(split3[0].toUpperCase(Locale.ENGLISH));
                                 }

                                 if (enchantment != null) {
                                    int level = enchantment.getStartLevel();
                                    if (split3.length == 2 && Util.isInt(split3[1])) {
                                       level = Integer.parseInt(split3[1]);
                                    }

                                    if (level < enchantment.getStartLevel()) {
                                       level = enchantment.getStartLevel();
                                    }

                                    if (level > enchantment.getMaxLevel()) {
                                       level = enchantment.getMaxLevel();
                                    }

                                    stack.addUnsafeEnchantment(enchantment, level);
                                 }
                              }
                           }
                        }

                        return stack;
                     }
                  }
               }
            }
         }
      }
   }

   private class ConstructBukkitMapping extends Constructor.ConstructMapping {
      private ConstructBukkitMapping() {
         super(BukkitConstructor.this);
      }

      public Object construct(Node node) {
         if (node.getType().equals(Location.class)) {
            MappingNode mnode = (MappingNode)node;
            String worldName = "";
            double x = (double)0.0F;
            double y = (double)0.0F;
            double z = (double)0.0F;
            float yaw = 0.0F;
            float pitch = 0.0F;
            if (mnode.getValue().size() < 4) {
               return null;
            } else {
               for(NodeTuple nodeTuple : mnode.getValue()) {
                  String key = (String)BukkitConstructor.this.constructScalar((ScalarNode)nodeTuple.getKeyNode());
                  ScalarNode snode = (ScalarNode)nodeTuple.getValueNode();
                  if (key.equalsIgnoreCase("world")) {
                     worldName = (String)BukkitConstructor.this.constructScalar(snode);
                  }

                  if (key.equalsIgnoreCase("x")) {
                     x = Double.parseDouble((String)BukkitConstructor.this.constructScalar(snode));
                  }

                  if (key.equalsIgnoreCase("y")) {
                     y = Double.parseDouble((String)BukkitConstructor.this.constructScalar(snode));
                  }

                  if (key.equalsIgnoreCase("z")) {
                     z = Double.parseDouble((String)BukkitConstructor.this.constructScalar(snode));
                  }

                  if (key.equalsIgnoreCase("yaw")) {
                     yaw = Float.parseFloat((String)BukkitConstructor.this.constructScalar(snode));
                  }

                  if (key.equalsIgnoreCase("pitch")) {
                     pitch = Float.parseFloat((String)BukkitConstructor.this.constructScalar(snode));
                  }
               }

               if (worldName != null && !worldName.isEmpty()) {
                  World world = Bukkit.getWorld(worldName);
                  if (world == null) {
                     return null;
                  } else {
                     return new Location(world, x, y, z, yaw, pitch);
                  }
               } else {
                  return null;
               }
            }
         } else {
            return super.construct(node);
         }
      }

      protected Object constructJavaBean2ndStep(MappingNode node, Object object) {
         Map<Class<? extends Object>, TypeDescription> typeDefinitions;
         try {
            Field typeDefField = Constructor.class.getDeclaredField("typeDefinitions");
            typeDefField.setAccessible(true);
            typeDefinitions = (Map)typeDefField.get(BukkitConstructor.this);
            if (typeDefinitions == null) {
               throw new NullPointerException();
            }
         } catch (Exception ex) {
            throw new YAMLException(ex);
         }

         BukkitConstructor.this.flattenMapping(node);
         Class<? extends Object> beanType = node.getType();

         for(NodeTuple tuple : node.getValue()) {
            if (!(tuple.getKeyNode() instanceof ScalarNode)) {
               throw new YAMLException("Keys must be scalars but found: " + tuple.getKeyNode());
            }

            ScalarNode keyNode = (ScalarNode)tuple.getKeyNode();
            Node valueNode = tuple.getValueNode();
            keyNode.setType(String.class);
            String key = (String)BukkitConstructor.this.constructObject(keyNode);

            try {
               Property property;
               try {
                  property = this.getProperty(beanType, key);
               } catch (YAMLException var18) {
                  continue;
               }

               valueNode.setType(property.getType());
               TypeDescription memberDescription = (TypeDescription)typeDefinitions.get(beanType);
               boolean typeDetected = false;
               if (memberDescription != null) {
                  switch (valueNode.getNodeId()) {
                     case sequence:
                        SequenceNode snode = (SequenceNode)valueNode;
                        Class<? extends Object> memberType = memberDescription.getListPropertyType(key);
                        if (memberType != null) {
                           snode.setListType(memberType);
                           typeDetected = true;
                        } else if (property.getType().isArray()) {
                           snode.setListType(property.getType().getComponentType());
                           typeDetected = true;
                        }
                        break;
                     case mapping:
                        MappingNode mnode = (MappingNode)valueNode;
                        Class<? extends Object> keyType = memberDescription.getMapKeyType(key);
                        if (keyType != null) {
                           mnode.setTypes(keyType, memberDescription.getMapValueType(key));
                           typeDetected = true;
                        }
                  }
               }

               if (!typeDetected && valueNode.getNodeId() != NodeId.scalar) {
                  Class<?>[] arguments = property.getActualTypeArguments();
                  if (arguments != null) {
                     if (valueNode.getNodeId() == NodeId.sequence) {
                        Class<?> t = arguments[0];
                        SequenceNode snode = (SequenceNode)valueNode;
                        snode.setListType(t);
                     } else if (valueNode.getTag().equals(Tag.SET)) {
                        Class<?> t = arguments[0];
                        MappingNode mnode = (MappingNode)valueNode;
                        mnode.setOnlyKeyType(t);
                        mnode.setUseClassConstructor(true);
                     } else if (property.getType().isAssignableFrom(Map.class)) {
                        Class<?> ketType = arguments[0];
                        Class<?> valueType = arguments[1];
                        MappingNode mnode = (MappingNode)valueNode;
                        mnode.setTypes(ketType, valueType);
                        mnode.setUseClassConstructor(true);
                     }
                  }
               }

               Object value = BukkitConstructor.this.constructObject(valueNode);
               property.set(object, value);
            } catch (Exception e) {
               throw new YAMLException("Cannot create property=" + key + " for JavaBean=" + object + "; " + e.getMessage(), e);
            }
         }

         return object;
      }
   }
}
