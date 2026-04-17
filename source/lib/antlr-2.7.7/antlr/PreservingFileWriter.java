package antlr;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public class PreservingFileWriter extends FileWriter {
   protected File target_file;
   protected File tmp_file;

   public PreservingFileWriter(String var1) throws IOException {
      super(var1 + ".antlr.tmp");
      this.target_file = new File(var1);
      String var2 = this.target_file.getParent();
      if (var2 != null) {
         File var3 = new File(var2);
         if (!var3.exists()) {
            throw new IOException("destination directory of '" + var1 + "' doesn't exist");
         }

         if (!var3.canWrite()) {
            throw new IOException("destination directory of '" + var1 + "' isn't writeable");
         }
      }

      if (this.target_file.exists() && !this.target_file.canWrite()) {
         throw new IOException("cannot write to '" + var1 + "'");
      } else {
         this.tmp_file = new File(var1 + ".antlr.tmp");
      }
   }

   public void close() throws IOException {
      BufferedReader var1 = null;
      BufferedWriter var2 = null;

      try {
         super.close();
         char[] var3 = new char[1024];
         if (this.target_file.length() == this.tmp_file.length()) {
            char[] var6 = new char[1024];
            var1 = new BufferedReader(new FileReader(this.tmp_file));
            BufferedReader var5 = new BufferedReader(new FileReader(this.target_file));
            boolean var9 = true;

            while(var9) {
               int var7 = ((Reader)var1).read(var3, 0, 1024);
               int var8 = ((Reader)var5).read(var6, 0, 1024);
               if (var7 != var8) {
                  var9 = false;
                  break;
               }

               if (var7 == -1) {
                  break;
               }

               for(int var10 = 0; var10 < var7; ++var10) {
                  if (var3[var10] != var6[var10]) {
                     var9 = false;
                     break;
                  }
               }
            }

            ((Reader)var1).close();
            ((Reader)var5).close();
            Object var25 = null;
            var1 = null;
            if (var9) {
               return;
            }
         }

         var1 = new BufferedReader(new FileReader(this.tmp_file));
         var2 = new BufferedWriter(new FileWriter(this.target_file));

         while(true) {
            int var4 = ((Reader)var1).read(var3, 0, 1024);
            if (var4 == -1) {
               return;
            }

            ((Writer)var2).write(var3, 0, var4);
         }
      } finally {
         if (var1 != null) {
            try {
               ((Reader)var1).close();
            } catch (IOException var22) {
            }
         }

         if (var2 != null) {
            try {
               ((Writer)var2).close();
            } catch (IOException var21) {
            }
         }

         if (this.tmp_file != null && this.tmp_file.exists()) {
            this.tmp_file.delete();
            this.tmp_file = null;
         }

      }
   }
}
