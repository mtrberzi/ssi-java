package io.lp0onfire.ssi.microcontroller;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import io.lp0onfire.ssi.microcontroller.ELFImage.HeaderType;
import io.lp0onfire.ssi.microcontroller.peripherals.InterruptController;
import io.lp0onfire.ssi.microcontroller.peripherals.InventoryController;
import io.lp0onfire.ssi.microcontroller.peripherals.MiningLaserController;
import io.lp0onfire.ssi.microcontroller.peripherals.RAM;
import io.lp0onfire.ssi.microcontroller.peripherals.ROM;

public class Microcontroller {

  private static final int textMemoryBase = 0x00000000;
  private static final int dataMemoryBase = 0x10000000;
  private static final int dataMemoryTop  = 0x20000000;
  
  private RV32Core cpu;
  protected RV32Core getCPU() {
    return this.cpu;
  }
  private ROM textMemory;
  protected ROM getTextMemory() {
    return this.textMemory;
  }
  private RAM dataMemory;
  protected RAM getDataMemory() {
    return this.dataMemory;
  }
  private InterruptController interruptController;
  
  private List<SystemBusPeripheral> peripherals = new LinkedList<>();
  
  public Microcontroller(int textMemoryPages, int dataMemoryPages) {
    cpu = new RV32Core();
    textMemory = new ROM(textMemoryPages);
    cpu.getSystemBus().attachPeripheral(textMemory, textMemoryBase);
    dataMemory = new RAM(dataMemoryPages);
    cpu.getSystemBus().attachPeripheral(dataMemory, dataMemoryBase);
    // stack pointer initially goes to top of RAM, 16-byte aligned
    int dataMemoryTop = dataMemoryBase + (dataMemoryPages * 1024) - 1;
    cpu.setXRegister(2, dataMemoryTop & 0xFFFFFFF8);
    
    // interrupt controller at 0xEA001000
    interruptController = new InterruptController(cpu);
    cpu.getSystemBus().attachPeripheral(interruptController, 0xEA001000);
    
    // TODO attach timer 0 at 0xE9000000 and route interrupt
  }
  
  public void attachPeripheral(SystemBusPeripheral peripheral, int baseAddress) {
    peripherals.add(peripheral);
    cpu.getSystemBus().attachPeripheral(peripheral, baseAddress);
  }
  
  public void attachInventoryController(InventoryController controller) {
    attachPeripheral(controller, 0x4A001000);
  }
  
  public void attachMiningLaserController(MiningLaserController laserCtrl) {
    attachPeripheral(laserCtrl, 0x4A002000);
  }
  
  public void registerInterrupt(InterruptSource source, int irq) {
    interruptController.registerInterrupt(source, irq);
  }
  
  public void loadELF(ELFImage elf) {
    ArrayList<Byte> romData = new ArrayList<Byte>();
    ArrayList<Byte> ramData = new ArrayList<Byte>();
    for (ELFImage.ELFProgramHeader phdr : elf.getProgramHeaders()) {
      if (phdr.headerType != HeaderType.PT_LOAD) {
        continue;
      }
      int baseAddress = phdr.baseAddress;
      int topAddress = baseAddress + phdr.segmentData.length;
      if (baseAddress >= 0 && baseAddress < dataMemoryBase && topAddress >= 0 && topAddress < dataMemoryBase) {
        // segment fits completely within ROM
        int requiredSize = topAddress - textMemoryBase;
        romData.ensureCapacity(requiredSize);
        while (romData.size() < requiredSize) {
          romData.add((byte)0);
        }
        for (byte b : phdr.segmentData) {
          romData.set(baseAddress, b);
          baseAddress++;
        }
      } else if (baseAddress >= dataMemoryBase && baseAddress < dataMemoryTop && topAddress >= dataMemoryBase && topAddress < dataMemoryTop) {
        // segment fits completely within RAM
        int requiredSize = topAddress - dataMemoryBase;
        ramData.ensureCapacity(requiredSize);
        while (ramData.size() < requiredSize) {
          ramData.add((byte)0);
        }
        baseAddress -= dataMemoryBase;
        for (byte b : phdr.segmentData) {
          ramData.set(baseAddress, b);
          baseAddress++;
        }
      } else {
        // TODO may be split between two sections, or in a completely different part of memory
        throw new UnsupportedOperationException("not yet implemented");
      }
    }
    textMemory.setContents(ArrayUtils.toPrimitive(romData.toArray(new Byte[]{})));
    dataMemory.setContents(ArrayUtils.toPrimitive(ramData.toArray(new Byte[]{})));
  }
  
  public void reset() {
    // TODO reset CPU, peripherals, interrupt controller, etc.
    cpu.setPC(0x00000200); // move PC to reset vector
  }
  
  public void cycle() {
    cpu.step();
    for (SystemBusPeripheral p : peripherals) {
      p.cycle();
    }
    interruptController.cycle();
  }
  
  public void timestep() {
    for (SystemBusPeripheral p : peripherals) {
      p.timestep();
    }
    interruptController.timestep();
  }
  
}
