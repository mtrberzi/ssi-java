package io.lp0onfire.ssi.microcontroller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class ELFImage {

  private int entryPoint;
  public int getEntryPoint() {
    return this.entryPoint;
  }
  private List<ELFProgramHeader> programHeaders = new ArrayList<ELFProgramHeader>();
  public List<ELFProgramHeader> getProgramHeaders() {
    return this.programHeaders;
  }
  
  public ELFImage(File elfFile) throws IOException {
    readelf(elfFile);
  }
  
  private void readelf(File elfFile) throws IOException, IllegalArgumentException {
    try(FileInputStream is = new FileInputStream(elfFile)) {
      byte[] elfBytes = new byte[(int)elfFile.length()];
      is.read(elfBytes);
      ByteBuffer elf = ByteBuffer.wrap(elfBytes);
      // read elf identification header
      byte[] e_ident = new byte[16];
      elf.get(e_ident);

      if (e_ident[0] != (byte)(0x7f) || e_ident[1] != 'E' || e_ident[2] != 'L' || e_ident[3] != 'F') {
        throw new IllegalArgumentException("file is not an ELF image");
      }
      byte ei_class = e_ident[4];
      if (ei_class != 1) {
        throw new IllegalArgumentException("file has wrong ELFCLASS");
      }
      byte ei_data = e_ident[5];
      if (ei_data == 1) {
        // use little-endian
        elf.order(ByteOrder.LITTLE_ENDIAN);
      } else if (ei_data == 2) {
        // use big-endian
        elf.order(ByteOrder.BIG_ENDIAN);
      } else {
        throw new IllegalArgumentException("file has invalid EI_DATA");
      }
      byte ei_version = e_ident[6];
      if (ei_version != 1) {
        throw new IllegalArgumentException("file has wrong ELF version");
      }
      short e_type = elf.getShort();
      if (e_type != 2) {
        throw new IllegalArgumentException("not an executable ELF image");
      }
      short e_machine = elf.getShort();
      // this should be correct
      if (e_machine != 0x00f3) {
        throw new IllegalArgumentException("not a RISC-V executable");
      }
      int e_version = elf.getInt();
      if (e_version != 1) {
        throw new IllegalArgumentException("file has wrong ELF version");
      }
      int e_entry = elf.getInt();
      this.entryPoint = e_entry;
      int e_phoff = elf.getInt();
      int e_shoff = elf.getInt();
      int e_flags = elf.getInt();
      short e_ehsize = elf.getShort();
      short e_phentsize = elf.getShort();
      short e_phnum = elf.getShort();
      short e_shentsize = elf.getShort();
      short e_shnum = elf.getShort();
      short e_shstrndx = elf.getShort();
      
      // now seek to program header offset
      elf.position(e_phoff);
      for (short i = 0; i < e_phnum; ++i) {
        byte[] phdrBytes = new byte[(int)e_phentsize];
        elf.get(phdrBytes);
        ByteBuffer phdr = ByteBuffer.wrap(phdrBytes);
        ELFProgramHeader headerData = new ELFProgramHeader(phdr, elfBytes);
        programHeaders.add(headerData);
      }
    }
  }
  
  enum HeaderType {
    PT_NULL,
    PT_LOAD,
    PT_DYNAMIC,
    PT_INTERP,
    PT_NOTE,
    PT_SHLIB,
    PT_PHDR,
  }
  
  class ELFProgramHeader {
    
    public final HeaderType headerType;
    public final int baseAddress;
    public final byte[] segmentData;
    
    public ELFProgramHeader(ByteBuffer programHeader, byte[] elfBytes) {
      int p_type = programHeader.getInt();
      switch(p_type) {
      case 0:
        headerType = HeaderType.PT_NULL; break;
      case 1:
        headerType = HeaderType.PT_LOAD; break;
      case 2:
        headerType = HeaderType.PT_DYNAMIC; break;
      case 3:
        headerType = HeaderType.PT_INTERP; break;
      case 4:
        headerType = HeaderType.PT_NOTE; break;
      case 5:
        headerType = HeaderType.PT_SHLIB; break;
      case 6:
        headerType = HeaderType.PT_PHDR; break;
        default:
          throw new IllegalArgumentException("unrecognized program header type " + p_type);
      }
      int p_offset = programHeader.getInt();
      int p_vaddr = programHeader.getInt();
      baseAddress = p_vaddr;
      int p_paddr = programHeader.getInt();
      int p_filesz = programHeader.getInt();
      int p_memsz = programHeader.getInt();
      int p_flags = programHeader.getInt();
      int p_align = programHeader.getInt();
      
      byte[] fileData = new byte[p_memsz];
      if (p_filesz > 0) {
        System.arraycopy(elfBytes, p_offset, fileData, 0, p_filesz);
      }
      
      segmentData = fileData;
    }
  }
  
}
