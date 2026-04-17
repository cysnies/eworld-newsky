package com.comphenix.protocol.reflect.cloning;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class SerializableCloner implements Cloner {
   public boolean canClone(Object source) {
      return source == null ? false : source instanceof Serializable;
   }

   public Object clone(Object source) {
      return this.clone(source);
   }

   public static Serializable clone(Serializable obj) {
      try {
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         ObjectOutputStream oout = new ObjectOutputStream(out);
         oout.writeObject(obj);
         ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(out.toByteArray()));
         return (Serializable)in.readObject();
      } catch (Exception e) {
         throw new RuntimeException("Unable to clone object " + obj, e);
      }
   }
}
