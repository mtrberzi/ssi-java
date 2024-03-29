CC=riscv64-unknown-elf-gcc
AS=riscv64-unknown-elf-as
AR=riscv64-unknown-elf-ar
LD=riscv64-unknown-elf-ld

IDIR = include
ODIR = obj
SRCDIR = src

CFLAGS=-std=c99 -m32 -Os -I$(IDIR) -nostdlib -nostartfiles -static -Wall
ASFLAGS=-m32

_DEPS = system.h interrupt_controller.h timer.h
DEPS = $(patsubst %,$(IDIR)/%,$(_DEPS))

_OBJ = system.o interrupt_controller.o timer.o save-restore.o
OBJ = $(patsubst %,$(ODIR)/%,$(_OBJ))

all: $(ODIR)/ runtime.a deploy_tests

$(ODIR)/:
	mkdir -p $(ODIR)/

runtime.a: $(OBJ) $(ODIR)/startup.o
	$(AR) rcs runtime.a $(OBJ) $(ODIR)/startup.o

$(ODIR)/startup.o: $(SRCDIR)/startup.s
	$(AS) $(ASFLAGS) -o $@ $<

$(ODIR)/save-restore.o: $(SRCDIR)/save-restore.s
	$(AS) $(ASFLAGS) -o $@ $<

$(ODIR)/%.o: src/%.c $(DEPS)
	$(CC) $(CFLAGS) -c -o $@ $<

deploy_tests: tests
	cp test_timer_1.rv32 ../src/test/resources/programs/

tests: test_timer_1.rv32

test_timer_1.rv32: test/test_timer_1.c runtime.a
	$(CC) $(CFLAGS) -c -o obj/test_timer_1.o test/test_timer_1.c
	$(LD) -o $@ -T ssi.ld obj/test_timer_1.o runtime.a

.PHONY: clean
clean:
	rm *.a obj/* *.rv32

