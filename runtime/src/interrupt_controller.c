#include "system.h"
#include "interrupt_controller.h"

irq_handler irq_handler_table[32];

static void default_irq_handler(void) {
  return;
}

void pic_init() {
  unsigned int i;
  for (i = 0; i < 32; ++i) {
    irq_handler_table[i] = default_irq_handler;
  }
}

void pic_register_handler(unsigned int irqno, irq_handler handler) {
  if (irqno >= 32) return;
  irq_handler_table[irqno] = handler;
}

void pic_master_enable() {
  unsigned int prev = read_word(PIC_MIS);
  prev |= (1 << 31);
  write_word(PIC_MIS, prev);
}

void pic_master_disable() {
  unsigned int prev = read_word(PIC_MIS);
  prev &= ~(1 << 31);
  write_word(PIC_MIS, prev);
}

void pic_enable_interrupt(unsigned int irqno) {
  if (irqno >= 32) return;
  unsigned int prev = read_word(PIC_IER);
  prev |= (1 << irqno);
  write_word(PIC_IER, prev);
}

void pic_disable_interrupt(unsigned int irqno) {
  if (irqno >= 32) return;
  unsigned int prev = read_word(PIC_IER);
  prev &= ~(1 << irqno);
  write_word(PIC_IER, prev);
}

void pic_set_priority(unsigned int irqno, unsigned int priority) {
  if (irqno >= 32) return;
  if (priority >= 32) priority = 31;
  unsigned int addr = PIC_PRIORITY_BASE + 4*irqno;
  write_word(addr, priority);
}

void handle_external_interrupt() {
  int interrupt_status = read_word(PIC_MIS);
  unsigned int current_interrupt = interrupt_status & 0x0000001F;
  irq_handler handler = irq_handler_table[current_interrupt];
  if (handler != 0) {
    handler();
  }
  // acknowledge interrupt
  unsigned int acknowledge = 1 << current_interrupt;
  write_word(PIC_IAR, acknowledge);
}
