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
	.bss : { *(.bss) }
	_end = .;
}
