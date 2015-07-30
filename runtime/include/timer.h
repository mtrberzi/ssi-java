#ifndef _TIMER_H_
#define _TIMER_H_

#define TIMER_CTRL_PRESCALER_2 (0<<4)
#define TIMER_CTRL_PRESCALER_4 (1<<4)
#define TIMER_CTRL_PRESCALER_8 (2<<4)
#define TIMER_CTRL_PRESCALER_16 (3<<4)
#define TIMER_CTRL_PRESCALER_32 (4<<4)
#define TIMER_CTRL_PRESCALER_64 (5<<4)
#define TIMER_CTRL_PRESCALER_128 (6<<4)
#define TIMER_CTRL_PRESCALER_256 (7<<4)

#define TIMER_CTRL_AUTORELOAD_ENABLE (1<<3)
#define TIMER_CTRL_PRESCALER_ENABLE (1<<2)
#define TIMER_CTRL_INTERRUPT_ENABLE (1<<1)
#define TIMER_CTRL_START (1<<0)

#define TIMER_INTERRUPT_MATCH (1<<1)
#define TIMER_INTERRUPT_OVERFLOW (1<<0)

#define TIMER0_BASE 0xE9000000
#define TIMER0_CTRL (TIMER0_BASE)
#define TIMER0_COUNT (TIMER0_BASE + 4)
#define TIMER0_RELOAD (TIMER0_BASE + 8)
#define TIMER0_MATCH (TIMER0_BASE + 12)
#define TIMER0_IE (TIMER0_BASE + 16)
#define TIMER0_IP (TIMER0_BASE + 20)
#define TIMER0_IA (TIMER0_BASE + 24)

void timer0_setPrescaler(unsigned int prescaler);

void timer0_enableAutoReload();
void timer0_disableAutoReload();

void timer0_enablePrescaler();
void timer0_disablePrescaler();

// enable/disable master interrupt bit
void timer0_enableInterrupts();
void timer0_disableInterrupts();

void timer0_startTimer();
void timer0_stopTimer();

void timer0_setCounter(unsigned int ctr);
void timer0_setReload(unsigned int rel);
void timer0_setMatch(unsigned int mat);
void timer0_enableIndividualInterrupts(unsigned int val);
unsigned int timer0_getInterruptsPending();
void timer0_acknowledgeInterrupts(unsigned int val);

#endif // _TIMER_H_
