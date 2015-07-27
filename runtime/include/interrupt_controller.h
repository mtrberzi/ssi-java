#ifndef _INTERRUPT_CONTROLLER_H_
#define _INTERRUPT_CONTROLLER_H_

#define PIC_BASE 0xEA001000
#define PIC_MIS PIC_BASE
#define PIC_IER (PIC_BASE + 4)
#define PIC_IPR (PIC_BASE + 8)
#define PIC_IAR (PIC_BASE + 12)
#define PIC_PRIORITY_BASE (PIC_BASE + 16)
// PIC_I(n)R = PIC_PRIORITY_BASE + 4*n

typedef void (*irq_handler)(void);

void pic_init();
void pic_register_handler(unsigned int irqno, irq_handler handler);
void pic_master_enable();
void pic_master_disable();
void pic_enable_interrupt(unsigned int irqno);
void pic_disable_interrupt(unsigned int irqno);
void pic_set_priority(unsigned int irqno, unsigned int priority);

void handle_external_interrupt();

#endif // _INTERRUPT_CONTROLLER_H_
