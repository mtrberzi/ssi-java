#include "system.h"
#include "interrupt_controller.h"

static irq_handler irq_handler_table[32];

static void default_irq_handler(void) {
  return;
}

void pic_init() {
  int i;
  for (i = 0; i < 32; ++i) {
    irq_handler_table[i] = default_irq_handler;
  }
}

void pic_register_handler(unsigned int irqno, irq_handler handler) {
  if (irqno > 32) return;
  irq_handler_table[irqno] = handler;
}

void handle_external_interrupt() {
  int interrupt_status = read_word(PIC_MIS);
  unsigned int current_interrupt = interrupt_status & 0x0000001F;
  irq_handler handler = irq_handler_table[current_interrupt];
  handler();
  // acknowledge interrupt
  unsigned int acknowledge = 1 << current_interrupt;
  write_word(PIC_IAR, acknowledge);
}
