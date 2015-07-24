CC=riscv64-unknown-elf-gcc
AS=riscv64-unknown-elf-as
AR=riscv64-unknown-elf-ar

IDIR = include
ODIR = obj
SRCDIR = src

CFLAGS=-std=c99 -m32 -Os -I$(IDIR) -nostdlib -nostartfiles -static -Wall
ASFLAGS=-m32

_DEPS = system.h interrupt_controller.h timer.h
DEPS = $(patsubst %,$(IDIR)/%,$(_DEPS))

_OBJ = system.o interrupt_controller.o timer.o
OBJ = $(patsubst %,$(ODIR)/%,$(_OBJ))

all: runtime.a

runtime.a: $(OBJ) $(ODIR)/startup.o
	$(AR) rcs runtime.a $(OBJ) $(ODIR)/startup.o

$(ODIR)/startup.o: $(SRCDIR)/startup.s
	$(AS) $(ASFLAGS) -o $@ $<

$(ODIR)/%.o: src/%.c $(DEPS)
	$(CC) $(CFLAGS) -c -o $@ $<

.PHONY: clean
clean:
	rm *.a obj/*
