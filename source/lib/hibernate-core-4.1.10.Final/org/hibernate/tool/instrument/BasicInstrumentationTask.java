package org.hibernate.tool.instrument;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.hibernate.bytecode.buildtime.spi.Instrumenter;
import org.hibernate.bytecode.buildtime.spi.Logger;

public abstract class BasicInstrumentationTask extends Task implements Instrumenter.Options {
   private final LoggerBridge logger = new LoggerBridge();
   private List filesets = new ArrayList();
   private boolean extended;
   private boolean verbose;

   public void addFileset(FileSet set) {
      this.filesets.add(set);
   }

   protected final Iterator filesets() {
      return this.filesets.iterator();
   }

   public boolean isExtended() {
      return this.extended;
   }

   public void setExtended(boolean extended) {
      this.extended = extended;
   }

   public boolean isVerbose() {
      return this.verbose;
   }

   public void setVerbose(boolean verbose) {
      this.verbose = verbose;
   }

   public final boolean performExtendedInstrumentation() {
      return this.isExtended();
   }

   protected abstract Instrumenter buildInstrumenter(Logger var1, Instrumenter.Options var2);

   public void execute() throws BuildException {
      try {
         this.buildInstrumenter(this.logger, this).execute(this.collectSpecifiedFiles());
      } catch (Throwable t) {
         throw new BuildException(t);
      }
   }

   private Set collectSpecifiedFiles() {
      HashSet files = new HashSet();
      Project project = this.getProject();
      Iterator filesets = this.filesets();

      while(filesets.hasNext()) {
         FileSet fs = (FileSet)filesets.next();
         DirectoryScanner ds = fs.getDirectoryScanner(project);
         String[] includedFiles = ds.getIncludedFiles();
         File d = fs.getDir(project);

         for(int i = 0; i < includedFiles.length; ++i) {
            files.add(new File(d, includedFiles[i]));
         }
      }

      return files;
   }

   protected class LoggerBridge implements Logger {
      public void trace(String message) {
         BasicInstrumentationTask.this.log(message, 3);
      }

      public void debug(String message) {
         BasicInstrumentationTask.this.log(message, 4);
      }

      public void info(String message) {
         BasicInstrumentationTask.this.log(message, 2);
      }

      public void warn(String message) {
         BasicInstrumentationTask.this.log(message, 1);
      }

      public void error(String message) {
         BasicInstrumentationTask.this.log(message, 0);
      }
   }
}
