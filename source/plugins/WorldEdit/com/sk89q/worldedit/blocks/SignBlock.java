package com.sk89q.worldedit.blocks;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.data.DataException;
import java.util.HashMap;
import java.util.Map;

public class SignBlock extends BaseBlock implements TileEntityBlock {
   private String[] text;

   public SignBlock(int type, int data) {
      super(type, data);
      this.text = new String[]{"", "", "", ""};
   }

   public SignBlock(int type, int data, String[] text) {
      super(type, data);
      if (text == null) {
         this.text = new String[]{"", "", "", ""};
      }

      this.text = text;
   }

   public String[] getText() {
      return this.text;
   }

   public void setText(String[] text) {
      if (text == null) {
         throw new IllegalArgumentException("Can't set null text for a sign");
      } else {
         this.text = text;
      }
   }

   public boolean hasNbtData() {
      return true;
   }

   public String getNbtId() {
      return "Sign";
   }

   public CompoundTag getNbtData() {
      Map<String, Tag> values = new HashMap();
      values.put("Text1", new StringTag("Text1", this.text[0]));
      values.put("Text2", new StringTag("Text2", this.text[1]));
      values.put("Text3", new StringTag("Text3", this.text[2]));
      values.put("Text4", new StringTag("Text4", this.text[3]));
      return new CompoundTag(this.getNbtId(), values);
   }

   public void setNbtData(CompoundTag rootTag) throws DataException {
      if (rootTag != null) {
         Map<String, Tag> values = rootTag.getValue();
         this.text = new String[]{"", "", "", ""};
         Tag t = (Tag)values.get("id");
         if (t instanceof StringTag && ((StringTag)t).getValue().equals("Sign")) {
            t = (Tag)values.get("Text1");
            if (t instanceof StringTag) {
               this.text[0] = ((StringTag)t).getValue();
            }

            t = (Tag)values.get("Text2");
            if (t instanceof StringTag) {
               this.text[1] = ((StringTag)t).getValue();
            }

            t = (Tag)values.get("Text3");
            if (t instanceof StringTag) {
               this.text[2] = ((StringTag)t).getValue();
            }

            t = (Tag)values.get("Text4");
            if (t instanceof StringTag) {
               this.text[3] = ((StringTag)t).getValue();
            }

         } else {
            throw new DataException("'Sign' tile entity expected");
         }
      }
   }
}
