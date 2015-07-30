#ifndef _SYSTEM_H_
#define _SYSTEM_H_

unsigned int read_word(unsigned int address);
void write_word(unsigned int address, unsigned int value);

void enable_interrupts();
void disable_interrupts();

#endif // _SYSTEM_H_
