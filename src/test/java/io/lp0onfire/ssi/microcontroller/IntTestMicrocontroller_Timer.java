package io.lp0onfire.ssi.microcontroller;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import io.lp0onfire.ssi.microcontroller.peripherals.Timer;

import org.junit.Before;
import org.junit.Test;

public class IntTestMicrocontroller_Timer {

  private ELFImage loadELFResource(String res) throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    URL resUrl = classLoader.getResource(res);
    if (resUrl == null) {
      throw new FileNotFoundException(res);
    }
    File elfFile = new File(resUrl.getFile());
    return new ELFImage(elfFile);
  }
  
  private static final int textMemoryPages = 4;
  private static final int dataMemoryPages = 5;
  private static final int timer0_IRQ = 0;
  
  private Microcontroller mcu;
  private Timer timer;
  
  @Before
  public void setup() {
    this.mcu = new Microcontroller(textMemoryPages, dataMemoryPages);
    timer = new Timer();
    mcu.attachPeripheral(timer, 0xE9000000);
    mcu.registerInterrupt(timer, timer0_IRQ);
  }
  
  @Test
  public void testTimer1() throws IOException, AddressTrapException {
    ELFImage elf = loadELFResource("programs/test_timer_1.rv32");
    mcu.loadELF(elf);
    mcu.reset();
    int cycleCount = 1000;
    for (int c = 0; c < cycleCount; ++c) {
      int pc = mcu.getCPU().getPC();
      StringBuilder statusLine = new StringBuilder();
      statusLine.append("cycle " + c + ": ");
      statusLine.append("pc=" + Integer.toHexString(pc) + " ");
      statusLine.append("msr=[");
      if (mcu.getCPU().mcause != 0) {
        statusLine.append("mcause=" + Integer.toHexString(mcu.getCPU().mcause));
      }
      statusLine.append("] ");
      statusLine.append("timer=[");
      statusLine.append("ctr=" + Integer.toHexString(timer.getCounter()));
      statusLine.append(" ");
      if (timer.getMasterInterruptEnable()) {
        statusLine.append("I");
      } else {
        statusLine.append("i");
      }
      if (timer.getMatchInterruptAsserted()) {
        statusLine.append("M");
      } else {
        statusLine.append("m");
      }
      if (timer.getOverflowInterruptAsserted()) {
        statusLine.append("O");
      } else {
        statusLine.append("o");
      }
      statusLine.append("] ");
      System.out.println(statusLine.toString());
      mcu.cycle();
    }
    // TODO get this information by reading the symbol table
    int timer_count = mcu.getDataMemory().readWord(0x10001080);
    if (timer_count == 0) {
      fail("timer_count did not increase");
    } else {
      System.out.println("timer_count = " + timer_count);
    }
  }
  
}
