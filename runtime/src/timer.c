#include "system.h"
#include "timer.h"

void timer0_setPrescaler(unsigned int prescaler) {
  unsigned int val = read_word(TIMER0_BASE);
  val = val & ~(7<<4);
  val = val | prescaler;
  write_word(TIMER0_BASE, val);
}

void timer0_enableAutoReload() {
  unsigned int val = read_word(TIMER0_BASE);
  val = val | TIMER_CTRL_AUTORELOAD_ENABLE;
  write_word(TIMER0_BASE, val);
}

void timer0_disableAutoReload() {
  unsigned int val = read_word(TIMER0_BASE);
  val = val & ~TIMER_CTRL_AUTORELOAD_ENABLE;
  write_word(TIMER0_BASE, val);
}

void timer0_enablePrescaler() {
  unsigned int val = read_word(TIMER0_BASE);
  val = val | TIMER_CTRL_PRESCALER_ENABLE;
  write_word(TIMER0_BASE, val);
}

void timer0_disablePrescaler() {
  unsigned int val = read_word(TIMER0_BASE);
  val = val & ~TIMER_CTRL_PRESCALER_ENABLE;
  write_word(TIMER0_BASE, val);
}

void timer0_enableInterrupts() {
  unsigned int val = read_word(TIMER0_BASE);
  val = val | TIMER_CTRL_INTERRUPT_ENABLE;
  write_word(TIMER0_BASE, val);
}

void timer0_disableInterrupts() {
  unsigned int val = read_word(TIMER0_BASE);
  val = val & ~TIMER_CTRL_INTERRUPT_ENABLE;
  write_word(TIMER0_BASE, val);
}

void timer0_startTimer() {
  unsigned int val = read_word(TIMER0_BASE);
  val = val | TIMER_CTRL_START;
  write_word(TIMER0_BASE, val);
}

void timer0_stopTimer() {
  unsigned int val = read_word(TIMER0_BASE);
  val = val & ~TIMER_CTRL_START;
  write_word(TIMER0_BASE, val);
}

void timer0_setCounter(unsigned int ctr) {
  write_word(TIMER0_COUNT, ctr);
}

void timer0_setReload(unsigned int rel) {
  write_word(TIMER0_RELOAD, rel);
}

void timer0_setMatch(unsigned int mat) {
  write_word(TIMER0_MATCH, mat);
}

void timer0_enableIndividualInterrupts(unsigned int val) {
  write_word(TIMER0_IE, val);
}

unsigned int timer0_getInterruptsPending() {
  return read_word(TIMER0_IP);
}

void timer0_acknowledgeInterrupts(unsigned int val) {
  write_word(TIMER0_IA, val);
}
