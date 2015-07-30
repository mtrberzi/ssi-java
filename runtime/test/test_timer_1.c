/*
 * - test_timer_1.c
 * Sets up an ISR to be called every 100 clock cycles.
 */

#include "system.h"
#include "interrupt_controller.h"
#include "timer.h"

unsigned int timer_count = 0;

void irq_timer0(void) {
  unsigned int irq_pending = timer0_getInterruptsPending();
  if (irq_pending & TIMER_INTERRUPT_MATCH) {
    // ignore
  }

  if (irq_pending & TIMER_INTERRUPT_OVERFLOW) {
    timer_count += 1;
  }

  timer0_acknowledgeInterrupts(irq_pending);
}

int main(void) {
  pic_init();

  unsigned int timer_period = 100;
  // adjust
  timer_period = (0xFFFFFFFF - timer_period) + 1;

  // initialize timer
  unsigned int timer0_irq = 0;
  timer0_disablePrescaler();
  timer0_enableAutoReload();
  timer0_setCounter(timer_period);
  timer0_setReload(timer_period);
  timer0_enableIndividualInterrupts(TIMER_INTERRUPT_OVERFLOW);
  timer0_enableInterrupts();

  pic_register_handler(timer0_irq, irq_timer0);
  pic_set_priority(timer0_irq, 0);
  pic_enable_interrupt(timer0_irq);
  pic_master_enable();
  enable_interrupts();

  timer0_startTimer();
  while(1) {}
  return 0;
}
