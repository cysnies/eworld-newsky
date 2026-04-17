package com.comphenix.protocol.wrappers.nbt.io;

import com.comphenix.protocol.wrappers.nbt.NbtBase;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.comphenix.protocol.wrappers.nbt.NbtList;
import com.comphenix.protocol.wrappers.nbt.NbtType;
import com.comphenix.protocol.wrappers.nbt.NbtVisitor;
import com.comphenix.protocol.wrappers.nbt.NbtWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class NbtConfigurationSerializer {
   public static final String TYPE_DELIMITER = "$";
   public static final NbtConfigurationSerializer DEFAULT = new NbtConfigurationSerializer();
   private String dataTypeDelimiter;

   public NbtConfigurationSerializer() {
      this.dataTypeDelimiter = "$";
   }

   public NbtConfigurationSerializer(String dataTypeDelimiter) {
      this.dataTypeDelimiter = dataTypeDelimiter;
   }

   public String getDataTypeDelimiter() {
      return this.dataTypeDelimiter;
   }

   public void serialize(NbtBase value, final ConfigurationSection destination) {
      value.accept(new NbtVisitor() {
         private ConfigurationSection current = destination;
         private List currentList;
         private Map workingIndex = Maps.newHashMap();

         public boolean visitEnter(NbtCompound compound) {
            this.current = this.current.createSection(compound.getName());
            return true;
         }

         public boolean visitEnter(NbtList list) {
            Integer listIndex = this.getNextIndex();
            String name = this.getEncodedName(list, listIndex);
            if (list.getElementType().isComposite()) {
               this.current = this.current.createSection(name);
               this.workingIndex.put(this.current, 0);
            } else {
               this.currentList = Lists.newArrayList();
               this.current.set(name, this.currentList);
            }

            return true;
         }

         public boolean visitLeave(NbtCompound compound) {
            this.current = this.current.getParent();
            return true;
         }

         public boolean visitLeave(NbtList list) {
            if (this.currentList != null) {
               this.currentList = null;
            } else {
               this.workingIndex.remove(this.current);
               this.current = this.current.getParent();
            }

            return true;
         }

         public boolean visit(NbtBase node) {
            if (this.currentList == null) {
               Integer listIndex = this.getNextIndex();
               String name = this.getEncodedName(node, listIndex);
               this.current.set(name, NbtConfigurationSerializer.this.fromNodeValue(node));
            } else {
               this.currentList.add(NbtConfigurationSerializer.this.fromNodeValue(node));
            }

            return true;
         }

         private Integer getNextIndex() {
            Integer listIndex = (Integer)this.workingIndex.get(this.current);
            return listIndex != null ? (Integer)this.workingIndex.put(this.current, listIndex + 1) : null;
         }

         private String getEncodedName(NbtBase node, Integer index) {
            return index != null ? index + NbtConfigurationSerializer.this.dataTypeDelimiter + node.getType().getRawID() : node.getName() + NbtConfigurationSerializer.this.dataTypeDelimiter + node.getType().getRawID();
         }

         private String getEncodedName(NbtList node, Integer index) {
            return index != null ? index + NbtConfigurationSerializer.this.dataTypeDelimiter + node.getElementType().getRawID() : node.getName() + NbtConfigurationSerializer.this.dataTypeDelimiter + node.getElementType().getRawID();
         }
      });
   }

   public NbtWrapper deserialize(ConfigurationSection root, String nodeName) {
      return this.readNode(root, nodeName);
   }

   public NbtCompound deserializeCompound(YamlConfiguration root, String nodeName) {
      return (NbtCompound)this.readNode(root, nodeName);
   }

   public NbtList deserializeList(YamlConfiguration root, String nodeName) {
      return (NbtList)this.readNode(root, nodeName);
   }

   private NbtWrapper readNode(ConfigurationSection parent, String name) {
      String[] decoded = getDecodedName(name);
      Object node = parent.get(name);
      NbtType type = NbtType.TAG_END;
      if (node == null) {
         for(String key : parent.getKeys(false)) {
            decoded = getDecodedName(key);
            if (decoded[0].equals(name)) {
               node = parent.get(decoded[0]);
               break;
            }
         }

         if (node == null) {
            throw new IllegalArgumentException("Unable to find node " + name + " in " + parent);
         }
      }

      if (decoded.length > 1) {
         type = NbtType.getTypeFromID(Integer.parseInt(decoded[1]));
      }

      if (node instanceof ConfigurationSection) {
         if (type != NbtType.TAG_END) {
            NbtList<Object> list = NbtFactory.ofList(decoded[0]);
            ConfigurationSection section = (ConfigurationSection)node;

            for(String key : this.sortSet(section.getKeys(false))) {
               NbtBase<Object> base = this.readNode(section, key.toString());
               base.setName("");
               ((List)list.getValue()).add(base);
            }

            return (NbtWrapper)list;
         } else {
            NbtCompound compound = NbtFactory.ofCompound(decoded[0]);
            ConfigurationSection section = (ConfigurationSection)node;

            for(String key : section.getKeys(false)) {
               compound.put((NbtBase)this.readNode(section, key));
            }

            return (NbtWrapper)compound;
         }
      } else if (type == NbtType.TAG_END) {
         throw new IllegalArgumentException("Cannot find encoded type of " + decoded[0] + " in " + name);
      } else if (!(node instanceof List)) {
         return NbtFactory.ofWrapper(type, decoded[0], this.toNodeValue(node, type));
      } else {
         NbtList<Object> list = NbtFactory.ofList(decoded[0]);
         list.setElementType(type);

         for(Object value : (List)node) {
            list.addClosest(this.toNodeValue(value, type));
         }

         return (NbtWrapper)list;
      }
   }

   private List sortSet(Set unsorted) {
      List<String> sorted = new ArrayList(unsorted);
      Collections.sort(sorted, new Comparator() {
         public int compare(String o1, String o2) {
            int index1 = Integer.parseInt(NbtConfigurationSerializer.getDecodedName(o1)[0]);
            int index2 = Integer.parseInt(NbtConfigurationSerializer.getDecodedName(o2)[0]);
            return Ints.compare(index1, index2);
         }
      });
      return sorted;
   }

   private Object fromNodeValue(NbtBase base) {
      return base.getType() == NbtType.TAG_INT_ARRAY ? toByteArray((int[])base.getValue()) : base.getValue();
   }

   public Object toNodeValue(Object value, NbtType type) {
      return type == NbtType.TAG_INT_ARRAY ? toIntegerArray((byte[])value) : value;
   }

   private static byte[] toByteArray(int[] data) {
      ByteBuffer byteBuffer = ByteBuffer.allocate(data.length * 4);
      IntBuffer intBuffer = byteBuffer.asIntBuffer();
      intBuffer.put(data);
      return byteBuffer.array();
   }

   private static int[] toIntegerArray(byte[] data) {
      IntBuffer source = ByteBuffer.wrap(data).asIntBuffer();
      IntBuffer copy = IntBuffer.allocate(source.capacity());
      copy.put(source);
      return copy.array();
   }

   private static String[] getDecodedName(String nodeName) {
      int delimiter = nodeName.lastIndexOf(36);
      return delimiter > 0 ? new String[]{nodeName.substring(0, delimiter), nodeName.substring(delimiter + 1)} : new String[]{nodeName};
   }
}
