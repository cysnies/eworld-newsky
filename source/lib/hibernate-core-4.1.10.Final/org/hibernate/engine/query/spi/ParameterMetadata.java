package org.hibernate.engine.query.spi;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.hibernate.QueryParameterException;
import org.hibernate.type.Type;

public class ParameterMetadata implements Serializable {
   private static final OrdinalParameterDescriptor[] EMPTY_ORDINALS = new OrdinalParameterDescriptor[0];
   private final OrdinalParameterDescriptor[] ordinalDescriptors;
   private final Map namedDescriptorMap;

   public ParameterMetadata(OrdinalParameterDescriptor[] ordinalDescriptors, Map namedDescriptorMap) {
      if (ordinalDescriptors == null) {
         this.ordinalDescriptors = EMPTY_ORDINALS;
      } else {
         OrdinalParameterDescriptor[] copy = new OrdinalParameterDescriptor[ordinalDescriptors.length];
         System.arraycopy(ordinalDescriptors, 0, copy, 0, ordinalDescriptors.length);
         this.ordinalDescriptors = copy;
      }

      if (namedDescriptorMap == null) {
         this.namedDescriptorMap = Collections.EMPTY_MAP;
      } else {
         int size = (int)((double)namedDescriptorMap.size() / (double)0.75F + (double)1.0F);
         Map copy = new HashMap(size);
         copy.putAll(namedDescriptorMap);
         this.namedDescriptorMap = Collections.unmodifiableMap(copy);
      }

   }

   public int getOrdinalParameterCount() {
      return this.ordinalDescriptors.length;
   }

   public OrdinalParameterDescriptor getOrdinalParameterDescriptor(int position) {
      if (position >= 1 && position <= this.ordinalDescriptors.length) {
         return this.ordinalDescriptors[position - 1];
      } else {
         String error = "Position beyond number of declared ordinal parameters. Remember that ordinal parameters are 1-based! Position: " + position;
         throw new QueryParameterException(error);
      }
   }

   public Type getOrdinalParameterExpectedType(int position) {
      return this.getOrdinalParameterDescriptor(position).getExpectedType();
   }

   public int getOrdinalParameterSourceLocation(int position) {
      return this.getOrdinalParameterDescriptor(position).getSourceLocation();
   }

   public Set getNamedParameterNames() {
      return this.namedDescriptorMap.keySet();
   }

   public NamedParameterDescriptor getNamedParameterDescriptor(String name) {
      NamedParameterDescriptor meta = (NamedParameterDescriptor)this.namedDescriptorMap.get(name);
      if (meta == null) {
         throw new QueryParameterException("could not locate named parameter [" + name + "]");
      } else {
         return meta;
      }
   }

   public Type getNamedParameterExpectedType(String name) {
      return this.getNamedParameterDescriptor(name).getExpectedType();
   }

   public int[] getNamedParameterSourceLocations(String name) {
      return this.getNamedParameterDescriptor(name).getSourceLocations();
   }
}
