package org.anjocaido.groupmanager.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.List;
import org.anjocaido.groupmanager.GroupManager;
import org.anjocaido.groupmanager.data.Group;

public abstract class Tasks {
   public static String getStackTraceAsString(Exception exception) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      exception.printStackTrace(pw);
      return sw.toString();
   }

   public static void copy(InputStream src, File dst) throws IOException {
      InputStream in = src;
      OutputStream out = new FileOutputStream(dst);
      byte[] buf = new byte[1024];

      int len;
      while((len = in.read(buf)) > 0) {
         out.write(buf, 0, len);
      }

      out.close();

      try {
         in.close();
      } catch (Exception var7) {
      }

   }

   public static void copy(File src, File dst) throws IOException {
      InputStream in = new FileInputStream(src);
      copy(in, dst);
   }

   public static void appendStringToFile(String data, String file) throws IOException {
      FileWriter outStream = new FileWriter("." + System.getProperty("file.separator") + file, true);
      BufferedWriter out = new BufferedWriter(outStream);
      data.replaceAll("\n", System.getProperty("line.separator"));
      out.append((new SimpleDateFormat("yyyy-MM-dd HH-mm")).format(System.currentTimeMillis()));
      out.append(System.getProperty("line.separator"));
      out.append(data);
      out.append(System.getProperty("line.separator"));
      out.close();
   }

   public static void removeOldFiles(GroupManager gm, File folder) {
      if (folder.isDirectory()) {
         long oldTime = System.currentTimeMillis() - (long)gm.getGMConfig().getBackupDuration() * 60L * 60L * 1000L;

         for(File olds : folder.listFiles()) {
            if (olds.isFile() && olds.lastModified() < oldTime) {
               try {
                  olds.delete();
               } catch (Exception var9) {
               }
            }
         }
      }

   }

   public static String getDateString() {
      GregorianCalendar now = new GregorianCalendar();
      String date = "";
      date = date + now.get(5);
      date = date + "-";
      date = date + now.get(10);
      date = date + "-";
      date = date + now.get(12);
      return date;
   }

   public static String getStringListInString(List list) {
      if (list == null) {
         return "";
      } else {
         String result = "";

         for(int i = 0; i < list.size(); ++i) {
            result = result + (String)list.get(i);
            if (i < list.size() - 1) {
               result = result + ", ";
            }
         }

         return result;
      }
   }

   public static String getStringArrayInString(String[] list) {
      if (list == null) {
         return "";
      } else {
         String result = "";

         for(int i = 0; i < list.length; ++i) {
            result = result + list[i];
            if (i < list.length - 1) {
               result = result + ", ";
            }
         }

         return result;
      }
   }

   public static String getGroupListInString(List list) {
      if (list == null) {
         return "";
      } else {
         String result = "";

         for(int i = 0; i < list.size(); ++i) {
            result = result + ((Group)list.get(i)).getName();
            if (i < list.size() - 1) {
               result = result + ", ";
            }
         }

         return result;
      }
   }

   public static String join(String[] arr, String separator) {
      if (arr.length == 0) {
         return "";
      } else {
         String out = arr[0].toString();

         for(int i = 1; i < arr.length; ++i) {
            out = out + separator + arr[i];
         }

         return out;
      }
   }
}
