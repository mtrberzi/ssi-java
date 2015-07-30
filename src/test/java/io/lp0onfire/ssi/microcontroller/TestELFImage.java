package io.lp0onfire.ssi.microcontroller;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import org.junit.Test;

public class TestELFImage {

  private ELFImage loadELFResource(String res) throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    URL resUrl = classLoader.getResource(res);
    if (resUrl == null) {
      throw new FileNotFoundException(res);
    }
    File elfFile = new File(resUrl.getFile());
    return new ELFImage(elfFile);
  }
  
  @Test
  public void testLoadSimple_Headers() throws IOException {
    ELFImage elf = loadELFResource("programs/test.rv32");
    // check: entry point = 0x200
    assertEquals("wrong entry point", 0x00000200, elf.getEntryPoint());
    // check: one program header
    assertEquals("wrong number of program headers", 1, elf.getProgramHeaders().size());
    ELFImage.ELFProgramHeader phdr = elf.getProgramHeaders().get(0);
    // check: phdr is of type PT_LOAD
    assertEquals("wrong program header type", ELFImage.HeaderType.PT_LOAD, phdr.headerType);
    // check: phdr has 0x204 bytes of data
    assertEquals("wrong program header length", 0x204, phdr.segmentData.length);
    // check: phdr loaded at address 0x0
    assertEquals("wrong program header load address", 0x0, phdr.baseAddress);
  }
  
  @Test
  public void testLoadSimple_Data() throws IOException {
    ELFImage elf = loadELFResource("programs/test.rv32");
    ELFImage.ELFProgramHeader phdr = elf.getProgramHeaders().get(0);
    // at address 0x200 in the file, we expect to see the four bytes
    // 67 80 00 00 corresponding to the 'ret' instruction
    assertEquals((byte)0x67, phdr.segmentData[0x200]);
    assertEquals((byte)0x80, phdr.segmentData[0x201]);
    assertEquals((byte)0x00, phdr.segmentData[0x202]);
    assertEquals((byte)0x00, phdr.segmentData[0x203]);
  }
  
}
