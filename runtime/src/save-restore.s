  .text

  .globl __riscv_save_12
  .globl __riscv_save_11
  .globl __riscv_save_10
  .globl __riscv_save_9
  .globl __riscv_save_8
  .globl __riscv_save_7
  .globl __riscv_save_6
  .globl __riscv_save_5
  .globl __riscv_save_4
  .globl __riscv_save_3
  .globl __riscv_save_2
  .globl __riscv_save_1
  .globl __riscv_save_0

  .globl __riscv_restore_12
  .globl __riscv_restore_11
  .globl __riscv_restore_10
  .globl __riscv_restore_9
  .globl __riscv_restore_8
  .globl __riscv_restore_7
  .globl __riscv_restore_6
  .globl __riscv_restore_5
  .globl __riscv_restore_4
  .globl __riscv_restore_3
  .globl __riscv_restore_2
  .globl __riscv_restore_1
  .globl __riscv_restore_0

__riscv_save_12:
  addi sp, sp, -64
  li t1, 0
  sw s11, 12(sp)
  j .Ls10

__riscv_save_11:
__riscv_save_10:
__riscv_save_9:
__riscv_save_8:
  addi sp, sp, -64
  li t1, -16
.Ls10:
  sw s10, 16(sp)
  sw s9, 20(sp)
  sw s8, 24(sp)
  sw s7, 28(sp)
  j .Ls6

__riscv_save_7:
__riscv_save_6:
__riscv_save_5:
__riscv_save_4:
  addi sp, sp, -64
  li t1, -32
.Ls6:
  sw s6, 32(sp)
  sw s5, 36(sp)
  sw s4, 40(sp)
  sw s3, 44(sp)
  sw s2, 48(sp)
  sw s1, 52(sp)
  sw s0, 56(sp)
  sw ra, 60(sp)
  sub sp, sp, t1
  jr t0

__riscv_save_3:
__riscv_save_2:
__riscv_save_1:
__riscv_save_0:
  addi sp, sp, -16
  sw s2, 0(sp)
  sw s1, 4(sp)
  sw s0, 8(sp)
  sw ra, 12(sp)
  jr t0

__riscv_restore_12:
  lw s11, 12(sp)
  addi sp, sp, 16

__riscv_restore_11:
__riscv_restore_10:
__riscv_restore_9:
__riscv_restore_8:
  lw s10, 0(sp)
  lw s9, 4(sp)
  lw s8, 8(sp)
  lw s7, 12(sp)
  addi sp, sp, 16

__riscv_restore_7:
__riscv_restore_6:
__riscv_restore_5:
__riscv_restore_4:
  lw s6, 0(sp)
  lw s5, 4(sp)
  lw s4, 8(sp)
  lw s3, 12(sp)
  addi sp, sp, 16

__riscv_restore_3:
__riscv_restore_2:
__riscv_restore_1:
__riscv_restore_0:
  lw s2, 0(sp)
  lw s1, 4(sp)
  lw s0, 8(sp)
  lw ra, 12(sp)
  addi sp, sp, 16
  ret
