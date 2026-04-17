package org.ibex.nestedvm.util;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ELF {
   private static final int ELF_MAGIC = 2135247942;
   public static final int ELFCLASSNONE = 0;
   public static final int ELFCLASS32 = 1;
   public static final int ELFCLASS64 = 2;
   public static final int ELFDATANONE = 0;
   public static final int ELFDATA2LSB = 1;
   public static final int ELFDATA2MSB = 2;
   public static final int SHT_SYMTAB = 2;
   public static final int SHT_STRTAB = 3;
   public static final int SHT_NOBITS = 8;
   public static final int SHF_WRITE = 1;
   public static final int SHF_ALLOC = 2;
   public static final int SHF_EXECINSTR = 4;
   public static final int PF_X = 1;
   public static final int PF_W = 2;
   public static final int PF_R = 4;
   public static final int PT_LOAD = 1;
   public static final short ET_EXEC = 2;
   public static final short EM_MIPS = 8;
   private Seekable data;
   public ELFIdent ident;
   public ELFHeader header;
   public PHeader[] pheaders;
   public SHeader[] sheaders;
   private byte[] stringTable;
   private boolean sectionReaderActive;
   private Symtab _symtab;

   private void readFully(byte[] var1) throws IOException {
      int var2 = var1.length;

      int var4;
      for(int var3 = 0; var2 > 0; var2 -= var4) {
         var4 = this.data.read(var1, var3, var2);
         if (var4 == -1) {
            throw new IOException("EOF");
         }

         var3 += var4;
      }

   }

   private int readIntBE() throws IOException {
      byte[] var1 = new byte[4];
      this.readFully(var1);
      return (var1[0] & 255) << 24 | (var1[1] & 255) << 16 | (var1[2] & 255) << 8 | (var1[3] & 255) << 0;
   }

   private int readInt() throws IOException {
      int var1 = this.readIntBE();
      if (this.ident != null && this.ident.data == 1) {
         var1 = var1 << 24 & -16777216 | var1 << 8 & 16711680 | var1 >>> 8 & '\uff00' | var1 >> 24 & 255;
      }

      return var1;
   }

   private short readShortBE() throws IOException {
      byte[] var1 = new byte[2];
      this.readFully(var1);
      return (short)((var1[0] & 255) << 8 | (var1[1] & 255) << 0);
   }

   private short readShort() throws IOException {
      short var1 = this.readShortBE();
      if (this.ident != null && this.ident.data == 1) {
         var1 = (short)((var1 << 8 & '\uff00' | var1 >> 8 & 255) & '\uffff');
      }

      return var1;
   }

   private byte readByte() throws IOException {
      byte[] var1 = new byte[1];
      this.readFully(var1);
      return var1[0];
   }

   public ELF(String var1) throws IOException, ELFException {
      this((Seekable)(new Seekable.File(var1, false)));
   }

   public ELF(Seekable var1) throws IOException, ELFException {
      this.data = var1;
      this.ident = new ELFIdent();
      this.header = new ELFHeader();
      this.pheaders = new PHeader[this.header.phnum];

      for(int var2 = 0; var2 < this.header.phnum; ++var2) {
         var1.seek(this.header.phoff + var2 * this.header.phentsize);
         this.pheaders[var2] = new PHeader();
      }

      this.sheaders = new SHeader[this.header.shnum];

      for(int var4 = 0; var4 < this.header.shnum; ++var4) {
         var1.seek(this.header.shoff + var4 * this.header.shentsize);
         this.sheaders[var4] = new SHeader();
      }

      if (this.header.shstrndx >= 0 && this.header.shstrndx < this.header.shnum) {
         var1.seek(this.sheaders[this.header.shstrndx].offset);
         this.stringTable = new byte[this.sheaders[this.header.shstrndx].size];
         this.readFully(this.stringTable);

         for(int var5 = 0; var5 < this.header.shnum; ++var5) {
            SHeader var3 = this.sheaders[var5];
            var3.name = this.getString(var3.nameidx);
         }

      } else {
         throw new ELFException("Bad shstrndx");
      }
   }

   private String getString(int var1) {
      return this.getString(var1, this.stringTable);
   }

   private String getString(int var1, byte[] var2) {
      StringBuffer var3 = new StringBuffer();
      if (var1 >= 0 && var1 < var2.length) {
         while(var1 >= 0 && var1 < var2.length && var2[var1] != 0) {
            var3.append((char)var2[var1++]);
         }

         return var3.toString();
      } else {
         return "<invalid strtab entry>";
      }
   }

   public SHeader sectionWithName(String var1) {
      for(int var2 = 0; var2 < this.sheaders.length; ++var2) {
         if (this.sheaders[var2].name.equals(var1)) {
            return this.sheaders[var2];
         }
      }

      return null;
   }

   public Symtab getSymtab() throws IOException {
      if (this._symtab != null) {
         return this._symtab;
      } else if (this.sectionReaderActive) {
         throw new ELFException("Can't read the symtab while a section reader is active");
      } else {
         SHeader var1 = this.sectionWithName(".symtab");
         if (var1 != null && var1.type == 2) {
            SHeader var2 = this.sectionWithName(".strtab");
            if (var2 != null && var2.type == 3) {
               byte[] var3 = new byte[var2.size];
               DataInputStream var4 = new DataInputStream(var2.getInputStream());
               var4.readFully(var3);
               var4.close();
               return this._symtab = new Symtab(var1.offset, var1.size, var3);
            } else {
               return null;
            }
         } else {
            return null;
         }
      }
   }

   private static String toHex(int var0) {
      return "0x" + Long.toString((long)var0 & 4294967295L, 16);
   }

   public class ELFIdent {
      public byte klass;
      public byte data;
      public byte osabi;
      public byte abiversion;

      ELFIdent() throws IOException {
         if (ELF.this.readIntBE() != 2135247942) {
            throw ELF.this.new ELFException("Bad Magic");
         } else {
            this.klass = ELF.this.readByte();
            if (this.klass != 1) {
               throw ELF.this.new ELFException("org.ibex.nestedvm.util.ELF does not suport 64-bit binaries");
            } else {
               this.data = ELF.this.readByte();
               if (this.data != 1 && this.data != 2) {
                  throw ELF.this.new ELFException("Unknown byte order");
               } else {
                  ELF.this.readByte();
                  this.osabi = ELF.this.readByte();
                  this.abiversion = ELF.this.readByte();

                  for(int var2 = 0; var2 < 7; ++var2) {
                     ELF.this.readByte();
                  }

               }
            }
         }
      }
   }

   public class ELFHeader {
      public short type = ELF.this.readShort();
      public short machine = ELF.this.readShort();
      public int version = ELF.this.readInt();
      public int entry;
      public int phoff;
      public int shoff;
      public int flags;
      public short ehsize;
      public short phentsize;
      public short phnum;
      public short shentsize;
      public short shnum;
      public short shstrndx;

      ELFHeader() throws IOException {
         if (this.version != 1) {
            throw ELF.this.new ELFException("version != 1");
         } else {
            this.entry = ELF.this.readInt();
            this.phoff = ELF.this.readInt();
            this.shoff = ELF.this.readInt();
            this.flags = ELF.this.readInt();
            this.ehsize = ELF.this.readShort();
            this.phentsize = ELF.this.readShort();
            this.phnum = ELF.this.readShort();
            this.shentsize = ELF.this.readShort();
            this.shnum = ELF.this.readShort();
            this.shstrndx = ELF.this.readShort();
         }
      }
   }

   public class PHeader {
      public int type = ELF.this.readInt();
      public int offset = ELF.this.readInt();
      public int vaddr = ELF.this.readInt();
      public int paddr = ELF.this.readInt();
      public int filesz = ELF.this.readInt();
      public int memsz = ELF.this.readInt();
      public int flags = ELF.this.readInt();
      public int align = ELF.this.readInt();

      PHeader() throws IOException {
         if (this.filesz > this.memsz) {
            throw ELF.this.new ELFException("ELF inconsistency: filesz > memsz (" + ELF.toHex(this.filesz) + " > " + ELF.toHex(this.memsz) + ")");
         }
      }

      public boolean writable() {
         return (this.flags & 2) != 0;
      }

      public InputStream getInputStream() throws IOException {
         return new BufferedInputStream(ELF.this.new SectionInputStream(this.offset, this.offset + this.filesz));
      }
   }

   public class SHeader {
      int nameidx = ELF.this.readInt();
      public String name;
      public int type = ELF.this.readInt();
      public int flags = ELF.this.readInt();
      public int addr = ELF.this.readInt();
      public int offset = ELF.this.readInt();
      public int size = ELF.this.readInt();
      public int link = ELF.this.readInt();
      public int info = ELF.this.readInt();
      public int addralign = ELF.this.readInt();
      public int entsize = ELF.this.readInt();

      SHeader() throws IOException {
      }

      public InputStream getInputStream() throws IOException {
         return new BufferedInputStream(ELF.this.new SectionInputStream(this.offset, this.type == 8 ? 0 : this.offset + this.size));
      }

      public boolean isText() {
         return this.name.equals(".text");
      }

      public boolean isData() {
         return this.name.equals(".data") || this.name.equals(".sdata") || this.name.equals(".rodata") || this.name.equals(".ctors") || this.name.equals(".dtors");
      }

      public boolean isBSS() {
         return this.name.equals(".bss") || this.name.equals(".sbss");
      }
   }

   public class ELFException extends IOException {
      ELFException(String var2) {
         super(var2);
      }
   }

   private class SectionInputStream extends InputStream {
      private int pos;
      private int maxpos;

      SectionInputStream(int var2, int var3) throws IOException {
         if (ELF.this.sectionReaderActive) {
            throw new IOException("Section reader already active");
         } else {
            ELF.this.sectionReaderActive = true;
            this.pos = var2;
            ELF.this.data.seek(this.pos);
            this.maxpos = var3;
         }
      }

      private int bytesLeft() {
         return this.maxpos - this.pos;
      }

      public int read() throws IOException {
         byte[] var1 = new byte[1];
         return this.read(var1, 0, 1) == -1 ? -1 : var1[0] & 255;
      }

      public int read(byte[] var1, int var2, int var3) throws IOException {
         int var4 = ELF.this.data.read(var1, var2, Math.min(var3, this.bytesLeft()));
         if (var4 > 0) {
            this.pos += var4;
         }

         return var4;
      }

      public void close() {
         ELF.this.sectionReaderActive = false;
      }
   }

   public class Symtab {
      public Symbol[] symbols;

      Symtab(int var2, int var3, byte[] var4) throws IOException {
         ELF.this.data.seek(var2);
         int var5 = var3 / 16;
         this.symbols = new Symbol[var5];

         for(int var6 = 0; var6 < var5; ++var6) {
            this.symbols[var6] = ELF.this.new Symbol(var4);
         }

      }

      public Symbol getSymbol(String var1) {
         Symbol var2 = null;

         for(int var3 = 0; var3 < this.symbols.length; ++var3) {
            if (this.symbols[var3].name.equals(var1)) {
               if (var2 == null) {
                  var2 = this.symbols[var3];
               } else {
                  System.err.println("WARNING: Multiple symbol matches for " + var1);
               }
            }
         }

         return var2;
      }

      public Symbol getGlobalSymbol(String var1) {
         for(int var2 = 0; var2 < this.symbols.length; ++var2) {
            if (this.symbols[var2].binding == 1 && this.symbols[var2].name.equals(var1)) {
               return this.symbols[var2];
            }
         }

         return null;
      }
   }

   public class Symbol {
      public String name;
      public int addr;
      public int size;
      public byte info;
      public byte type;
      public byte binding;
      public byte other;
      public short shndx;
      public SHeader sheader;
      public static final int STT_FUNC = 2;
      public static final int STB_GLOBAL = 1;

      Symbol(byte[] var2) throws IOException {
         this.name = ELF.this.getString(ELF.this.readInt(), var2);
         this.addr = ELF.this.readInt();
         this.size = ELF.this.readInt();
         this.info = ELF.this.readByte();
         this.type = (byte)(this.info & 15);
         this.binding = (byte)(this.info >> 4);
         this.other = ELF.this.readByte();
         this.shndx = ELF.this.readShort();
      }
   }
}
