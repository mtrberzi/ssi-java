#include "system.h"
#include "interrupt_controller.h"

unsigned read_word(unsigned int address) {
  volatile unsigned int *reg = (unsigned int*)address;
  return *reg;
}

void write_word(unsigned int address, unsigned int value) {
  volatile unsigned int *reg = (unsigned int*)address;
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
      // skip over the ECALL instruction
      return epc+4;
    } else {
      // illegal operation
      while(1);
    }
  }
  return epc;
}
