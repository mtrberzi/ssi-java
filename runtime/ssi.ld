OUTPUT_FORMAT("elf32-littleriscv")
OUTPUT_ARCH("riscv")
ENTRY("_start")

SECTIONS
{
/* text segment */
	. = 0x100;
	.text :
	{
		startup.o(.text)
		*(.text)
	}
/* data segment */
	. = 0x10000000;
	.data : { *(.data) }
	. = ALIGN(4);
	bss_start = .;
	.bss : { *(.bss) }
	bss_end = .;
	. = ALIGN(4);
	. = . + 0x1000; /* allocate 4kb of stack */
	stack_top = .;
}
