package org.hibernate.tool.hbm2ddl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.FileSet;
import org.hibernate.HibernateException;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.NamingStrategy;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.internal.util.collections.ArrayHelper;

public class SchemaUpdateTask extends MatchingTask {
   private List fileSets = new LinkedList();
   private File propertiesFile = null;
   private File configurationFile = null;
   private File outputFile = null;
   private boolean quiet = false;
   private boolean text = true;
   private boolean haltOnError = false;
   private String delimiter = null;
   private String namingStrategy = null;

   public void addFileset(FileSet set) {
      this.fileSets.add(set);
   }

   public void setProperties(File propertiesFile) {
      if (!propertiesFile.exists()) {
         throw new BuildException("Properties file: " + propertiesFile + " does not exist.");
      } else {
         this.log("Using properties file " + propertiesFile, 4);
         this.propertiesFile = propertiesFile;
      }
   }

   public void setConfig(File configurationFile) {
      this.configurationFile = configurationFile;
   }

   public void setText(boolean text) {
      this.text = text;
   }

   public void setQuiet(boolean quiet) {
      this.quiet = quiet;
   }

   public void execute() throws BuildException {
      try {
         this.log("Running Hibernate Core SchemaUpdate.");
         this.log("This is an Ant task supporting only mapping files, if you want to use annotations see http://tools.hibernate.org.");
         Configuration cfg = this.getConfiguration();
         this.getSchemaUpdate(cfg).execute(!this.quiet, !this.text);
      } catch (HibernateException e) {
         throw new BuildException("Schema text failed: " + e.getMessage(), e);
      } catch (FileNotFoundException e) {
         throw new BuildException("File not found: " + e.getMessage(), e);
      } catch (IOException e) {
         throw new BuildException("IOException : " + e.getMessage(), e);
      } catch (Exception e) {
         throw new BuildException(e);
      }
   }

   private String[] getFiles() {
      List files = new LinkedList();

      for(FileSet fs : this.fileSets) {
         DirectoryScanner ds = fs.getDirectoryScanner(this.getProject());
         String[] dsFiles = ds.getIncludedFiles();

         for(int j = 0; j < dsFiles.length; ++j) {
            File f = new File(dsFiles[j]);
            if (!f.isFile()) {
               f = new File(ds.getBasedir(), dsFiles[j]);
            }

            files.add(f.getAbsolutePath());
         }
      }

      return ArrayHelper.toStringArray((Collection)files);
   }

   private Configuration getConfiguration() throws Exception {
      Configuration cfg = new Configuration();
      if (this.namingStrategy != null) {
         cfg.setNamingStrategy((NamingStrategy)ReflectHelper.classForName(this.namingStrategy).newInstance());
      }

      if (this.configurationFile != null) {
         cfg.configure(this.configurationFile);
      }

      String[] files = this.getFiles();

      for(int i = 0; i < files.length; ++i) {
         String filename = files[i];
         if (filename.endsWith(".jar")) {
            cfg.addJar(new File(filename));
         } else {
            cfg.addFile(filename);
         }
      }

      return cfg;
   }

   private SchemaUpdate getSchemaUpdate(Configuration cfg) throws HibernateException, IOException {
      Properties properties = new Properties();
      properties.putAll(cfg.getProperties());
      if (this.propertiesFile == null) {
         properties.putAll(this.getProject().getProperties());
      } else {
         properties.load(new FileInputStream(this.propertiesFile));
      }

      cfg.setProperties(properties);
      SchemaUpdate su = new SchemaUpdate(cfg);
      su.setOutputFile(this.outputFile.getPath());
      su.setDelimiter(this.delimiter);
      su.setHaltOnError(this.haltOnError);
      return su;
   }

   public void setNamingStrategy(String namingStrategy) {
      this.namingStrategy = namingStrategy;
   }

   public File getOutputFile() {
      return this.outputFile;
   }

   public void setOutputFile(File outputFile) {
      this.outputFile = outputFile;
   }

   public boolean isHaltOnError() {
      return this.haltOnError;
   }

   public void setHaltOnError(boolean haltOnError) {
      this.haltOnError = haltOnError;
   }

   public String getDelimiter() {
      return this.delimiter;
   }

   public void setDelimiter(String delimiter) {
      this.delimiter = delimiter;
   }
}
