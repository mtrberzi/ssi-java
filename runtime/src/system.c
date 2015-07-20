#include "system.h"
#include "interrupt_controller.h"

int read_word(int address) {
  volatile int *reg = (int*)address;
  return *reg;
}

void write_word(int address, int value) {
  volatile int *reg = (int*)address;
  *reg = value;
}

int handle_syscall(int regs[32]) {
  return 0;
}

int handle_trap(int cause, int epc, int regs[32]) {
  if (cause < 0) {
    // interrupt
    int irqno = cause & 0x7FFFFFFF;
    switch (irqno) {
    case 15:
      // external interrupt
      handle_external_interrupt();
      break;
    default:
      // unhandled interrupt
      while(1);
    }
  } else {
    // trap
    if (cause == 11) {
      // environment call from machine mode
      int sys_ret = handle_syscall(regs);
      regs[10] = sys_ret;
    } else {
      // illegal operation
      while(1);
    }
  }
  return epc+4;
}
