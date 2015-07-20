	.text
	.align 6
user_trap_entry:	j user_trap_entry
	.align 6
supervisor_trap_entry:	j supervisor_trap_entry
	.align 6
hypervisor_trap_entry:	j hypervisor_trap_entry
	.align 6
machine_trap_entry:	j machine_trap_entry
	.align 6
	.globl _start
_start:
	ret
	
