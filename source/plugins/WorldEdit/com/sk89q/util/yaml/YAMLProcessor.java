package com.sk89q.util.yaml;

import com.sk89q.util.StringUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.DumperOptions.LineBreak;
import org.yaml.snakeyaml.DumperOptions.ScalarStyle;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.emitter.ScalarAnalysis;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.reader.UnicodeReader;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;

public class YAMLProcessor extends YAMLNode {
   public static final String LINE_BREAK = LineBreak.getPlatformLineBreak().getString();
   public static final char COMMENT_CHAR = '#';
   protected final Yaml yaml;
   protected final File file;
   protected String header;
   protected YAMLFormat format;
   private final Map comments;

   public YAMLProcessor(File file, boolean writeDefaults, YAMLFormat format) {
      super(new LinkedHashMap(), writeDefaults);
      this.header = null;
      this.comments = new HashMap();
      this.format = format;
      DumperOptions options = new FancyDumperOptions();
      options.setIndent(4);
      options.setDefaultFlowStyle(format.getStyle());
      Representer representer = new FancyRepresenter();
      representer.setDefaultFlowStyle(format.getStyle());
      this.yaml = new Yaml(new SafeConstructor(), representer, options);
      this.file = file;
   }

   public YAMLProcessor(File file, boolean writeDefaults) {
      this(file, writeDefaults, YAMLFormat.COMPACT);
   }

   public void load() throws IOException {
      InputStream stream = null;

      try {
         stream = this.getInputStream();
         if (stream == null) {
            throw new IOException("Stream is null!");
         }

         this.read(this.yaml.load(new UnicodeReader(stream)));
      } catch (YAMLProcessorException var11) {
         this.root = new LinkedHashMap();
      } finally {
         try {
            if (stream != null) {
               stream.close();
            }
         } catch (IOException var10) {
         }

      }

   }

   public void setHeader(String... headerLines) {
      StringBuilder header = new StringBuilder();

      for(String line : headerLines) {
         if (header.length() > 0) {
            header.append(LINE_BREAK);
         }

         header.append(line);
      }

      this.setHeader(header.toString());
   }

   public void setHeader(String header) {
      this.header = header;
   }

   public String getHeader() {
      return this.header;
   }

   public boolean save() {
      OutputStream stream = null;
      File parent = this.file.getParentFile();
      if (parent != null) {
         parent.mkdirs();
      }

      boolean var3;
      try {
         stream = this.getOutputStream();
         if (stream != null) {
            OutputStreamWriter writer = new OutputStreamWriter(stream, "UTF-8");
            if (this.header != null) {
               writer.append(this.header);
               writer.append(LINE_BREAK);
            }

            if (this.comments.size() != 0 && this.format == YAMLFormat.EXTENDED) {
               for(Map.Entry entry : this.root.entrySet()) {
                  String comment = (String)this.comments.get(entry.getKey());
                  if (comment != null) {
                     writer.append(LINE_BREAK);
                     writer.append(comment);
                     writer.append(LINE_BREAK);
                  }

                  this.yaml.dump(Collections.singletonMap(entry.getKey(), entry.getValue()), writer);
               }
            } else {
               this.yaml.dump(this.root, writer);
            }

            boolean i = true;
            return i;
         }

         var3 = false;
      } catch (IOException var16) {
         return false;
      } finally {
         try {
            if (stream != null) {
               stream.close();
            }
         } catch (IOException var15) {
         }

      }

      return var3;
   }

   private void read(Object input) throws YAMLProcessorException {
      try {
         if (null == input) {
            this.root = new LinkedHashMap();
         } else {
            this.root = new LinkedHashMap((Map)input);
         }

      } catch (ClassCastException var3) {
         throw new YAMLProcessorException("Root document must be an key-value structure");
      }
   }

   public InputStream getInputStream() throws IOException {
      return new FileInputStream(this.file);
   }

   public OutputStream getOutputStream() throws IOException {
      return new FileOutputStream(this.file);
   }

   public String getComment(String key) {
      return (String)this.comments.get(key);
   }

   public void setComment(String key, String comment) {
      if (comment != null) {
         this.setComment(key, comment.split("\\r?\\n"));
      } else {
         this.comments.remove(key);
      }

   }

   public void setComment(String key, String... comment) {
      if (comment != null && comment.length > 0) {
         for(int i = 0; i < comment.length; ++i) {
            if (!comment[i].matches("^# ?")) {
               comment[i] = "# " + comment[i];
            }
         }

         String s = StringUtil.joinString(comment, LINE_BREAK);
         this.comments.put(key, s);
      } else {
         this.comments.remove(key);
      }

   }

   public Map getComments() {
      return Collections.unmodifiableMap(this.comments);
   }

   public void setComments(Map comments) {
      this.comments.clear();
      if (comments != null) {
         this.comments.putAll(comments);
      }

   }

   public static YAMLNode getEmptyNode(boolean writeDefaults) {
      return new YAMLNode(new LinkedHashMap(), writeDefaults);
   }

   private class FancyDumperOptions extends DumperOptions {
      private FancyDumperOptions() {
      }

      public DumperOptions.ScalarStyle calculateScalarStyle(ScalarAnalysis analysis, DumperOptions.ScalarStyle style) {
         return YAMLProcessor.this.format != YAMLFormat.EXTENDED || !analysis.scalar.contains("\n") && !analysis.scalar.contains("\r") ? super.calculateScalarStyle(analysis, style) : ScalarStyle.LITERAL;
      }
   }

   private static class FancyRepresenter extends Representer {
      public FancyRepresenter() {
         this.nullRepresenter = new Represent() {
            public Node representData(Object o) {
               return FancyRepresenter.this.representScalar(Tag.NULL, "");
            }
         };
      }
   }
}
