package io.lp0onfire.ssi.microcontroller;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import io.lp0onfire.ssi.microcontroller.peripherals.RAM;
import io.lp0onfire.ssi.microcontroller.peripherals.ROM;

// Tests that run code solely on the CPU, with no dependency on peripherals
// except ROM and RAM.
public class IntTestCodeExecutionCPUOnly {

  private static final int textMemoryBase = 0x00000000;
  private static final int textMemoryPages = 4;
  private static final int dataMemoryBase = 0x10000000;
  private static final int dataMemoryPages = 4;
  
  private RV32Core cpu;
  private ROM textMemory;
  private RAM dataMemory;
  
  @Before
  public void setup() {
    cpu = new RV32Core();
    textMemory = new ROM(textMemoryPages);
    cpu.getSystemBus().attachPeripheral(textMemory, textMemoryBase);
    dataMemory = new RAM(dataMemoryPages);
    cpu.getSystemBus().attachPeripheral(dataMemory, dataMemoryBase);
    // stack pointer initially goes to top of RAM, 16-byte aligned
    int dataMemoryTop = dataMemoryBase + (dataMemoryPages * 1024) - 1;
    cpu.setXRegister(2, dataMemoryTop & 0xFFFFFFF0);
  }
  
  private void loadProgram(int[] text) {
    if (text.length > textMemoryPages * (1024/4)) {
      throw new IllegalArgumentException("cannot load program, insufficient memory");
    }
    byte[] bText = new byte[text.length * 4];
    for (int tPtr = 0; tPtr < text.length; ++tPtr) {
      int insn = text[tPtr];
      int bPtr = 4*tPtr;
      bText[bPtr+0] = (byte)((insn & 0x000000FF));
      bText[bPtr+1] = (byte)((insn & 0x0000FF00) >>>  8);
      bText[bPtr+2] = (byte)((insn & 0x00FF0000) >>> 16);
      bText[bPtr+3] = (byte)((insn & 0xFF000000) >>> 24);
    }
    textMemory.setContents(bText);
  }
  
  private void run(int maxCycles) {
    // prime ra (x1) with a target return address
    int retTarget = 0xDDCCDDCC;
    cpu.setXRegister(1, retTarget);
    for (int i = 0; i < maxCycles; ++i) {
      cpu.step();
      if (cpu.getPC() == retTarget) {
        return;
      }
    }
    throw new IllegalStateException("CPU did not terminate after " + Integer.toString(maxCycles) + " steps");
  }
  
  @Test
  public void testReturnOnly() {
    int[] program = {
        0x00008067
    };
    loadProgram(program);
    run(1);
  }
  
  @Test
  public void testDoubleA0() {
    // copies the value in a0 to t0,
    // then adds a0 and t0 and places the result in a0
    int[] program = {
      0x00050293,
      0x00550533,
      0x00008067,
    };
    loadProgram(program);
    cpu.setXRegister(10, 21);
    run(3);
    assertEquals(42, cpu.getXRegister(10));
  }
  
  @Test
  public void testMaxS() {
    // puts the larger of a0 and a1 into a0 (signed compare)
    int[] program = {
        0x00a5c463,
        0x00058513,
        0x00008067,
    };
    
    int[] testData_a0 = {1, 12, 4,  -3};
    int[] testData_a1 = {0, 6,  23, -2};
    int[] expected_a0 = {1, 12, 23, -2};
    
    for (int i = 0; i < expected_a0.length; ++i) {
      setup();
      loadProgram(program);
      cpu.setXRegister(10, testData_a0[i]);
      cpu.setXRegister(11, testData_a1[i]);
      run(3);
      assertEquals("failed test case " + Integer.toString(i), 
          expected_a0[i], cpu.getXRegister(10));
    }
  }
  
  @Test
  public void testMaxU() {
    // puts the larger of a0 and a1 into a0 (unsigned compare)
    int[] program = {
        0x00a5e463,
        0x00058513,
        0x00008067,
    };
    
    int[] testData_a0 = {1, 12, 4,  0xFFFF0000};
    int[] testData_a1 = {0, 6,  23, 0x0000000F};
    int[] expected_a0 = {1, 12, 23, 0xFFFF0000};
    
    for (int i = 0; i < expected_a0.length; ++i) {
      setup();
      loadProgram(program);
      cpu.setXRegister(10, testData_a0[i]);
      cpu.setXRegister(11, testData_a1[i]);
      run(3);
      assertEquals("failed test case " + Integer.toString(i), 
          expected_a0[i], cpu.getXRegister(10));
    }
  }
  
  @Test
  public void testLastElementOfArray() throws AddressTrapException {
    // puts the last element of an array into a0,
    // or -1 if the array is empty
    int[] program = {
          0x00058c63,
          0xfff58293,
          0x00229293,
          0x00a282b3,
          0x0002a503, 
          0x00008067,
          0xfff00513,
          0x00008067,
    };
    
    int[][] testData = {
        {},
        {1},
        {1, 2, 3},
        {-4, -3, -2, -1},
        {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20},
    };
    
    for (int i = 0; i < testData.length; ++i) {
      setup();
      loadProgram(program);
      int[] testArray = testData[i];
      for (int aPtr = 0; aPtr < testArray.length; ++aPtr) {
        int address = dataMemoryBase + 4*aPtr;
        dataMemory.writeWord(address, testArray[aPtr]);
      }
      // put the address of the array into a0
      cpu.setXRegister(10, dataMemoryBase);
      // put the length of the array into a1
      cpu.setXRegister(11, testArray.length);
      run(6);
      if (testArray.length == 0) {
        assertEquals("failed test case " + Integer.toString(i), 
            -1, cpu.getXRegister(10));
      } else {
        assertEquals("failed test case " + Integer.toString(i), 
            testArray[testArray.length - 1], cpu.getXRegister(10));
      }
    }
    
  }
  
  // TODO comment this out and test these instructions separately
  @Test
  public void testFibonacciRecursive() {
    // call with n in a0, returns fib(n) in a0
    int[] program = {
        0xfe010113,
        0x00112e23,
        0x00812c23,
        0x00912a23,
        0x02010413,
        0xfea42623,
        0xfec42703,
        0x00100793,
        0x00e7c663,
        0xfec42783,
        0x0300006f,
        0xfec42783,
        0xfff78793,
        0x00078513,
        0xfc9ff0ef,
        0x00050493,
        0xfec42783,
        0xffe78793,
        0x00078513,
        0xfb5ff0ef,
        0x00050793,
        0x00f487b3,
        0x00078513,
        0x01c12083,
        0x01812403,
        0x01412483,
        0x02010113,
        0x00008067,
    };
    
    int[] fib = {0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144, 233, 377, 610};
    
    for (int i = 0; i < 16; ++i) {
      setup();
      loadProgram(program);
      int expected = fib[i];
      cpu.setXRegister(10, i);
      System.err.println("computing fib(" + i + ")");
      run(45000);
      assertEquals(expected, cpu.getXRegister(10));
    }
    
  }
  
}
