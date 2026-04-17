package com.sk89q.jchronic.utils;

import com.sk89q.jchronic.tags.Tag;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Token {
   private String _word;
   private List _tags;

   public Token(String word) {
      this._word = word;
      this._tags = new LinkedList();
   }

   public String getWord() {
      return this._word;
   }

   public void tag(Tag newTag) {
      this._tags.add(newTag);
   }

   public void untag(Class tagClass) {
      Iterator<Tag<?>> tagIter = this._tags.iterator();

      while(tagIter.hasNext()) {
         Tag<?> tag = (Tag)tagIter.next();
         if (tagClass.isInstance(tag)) {
            tagIter.remove();
         }
      }

   }

   public boolean isTagged() {
      return !this._tags.isEmpty();
   }

   public Tag getTag(Class tagClass) {
      List<T> matches = this.getTags(tagClass);
      T matchingTag = (T)null;
      if (matches.size() > 0) {
         matchingTag = (T)((Tag)matches.get(0));
      }

      return matchingTag;
   }

   public List getTags() {
      return this._tags;
   }

   public List getTags(Class tagClass) {
      List<T> matches = new LinkedList();

      for(Tag tag : this._tags) {
         if (tagClass.isInstance(tag)) {
            matches.add(tag);
         }
      }

      return matches;
   }

   public String toString() {
      return this._word + " " + this._tags;
   }
}
