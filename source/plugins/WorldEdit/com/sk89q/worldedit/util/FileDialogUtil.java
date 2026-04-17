package com.sk89q.worldedit.util;

import com.sk89q.util.StringUtil;
import java.awt.Component;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

public class FileDialogUtil {
   public static File showSaveDialog(String[] exts) {
      JFileChooser dialog = new JFileChooser();
      if (exts != null) {
         dialog.setFileFilter(new ExtensionFilter(exts));
      }

      int returnVal = dialog.showSaveDialog((Component)null);
      return returnVal == 0 ? dialog.getSelectedFile() : null;
   }

   public static File showOpenDialog(String[] exts) {
      JFileChooser dialog = new JFileChooser();
      if (exts != null) {
         dialog.setFileFilter(new ExtensionFilter(exts));
      }

      int returnVal = dialog.showOpenDialog((Component)null);
      return returnVal == 0 ? dialog.getSelectedFile() : null;
   }

   private static class ExtensionFilter extends FileFilter {
      private Set exts;
      private String desc;

      public ExtensionFilter(String[] exts) {
         this.exts = new HashSet(Arrays.asList(exts));
         this.desc = StringUtil.joinString(exts, ",");
      }

      public boolean accept(File f) {
         if (f.isDirectory()) {
            return true;
         } else {
            String path = f.getPath();
            int index = path.lastIndexOf(46);
            return index != -1 && index != path.length() - 1 ? this.exts.contains(path.indexOf(index + 1)) : false;
         }
      }

      public String getDescription() {
         return this.desc;
      }
   }
}
