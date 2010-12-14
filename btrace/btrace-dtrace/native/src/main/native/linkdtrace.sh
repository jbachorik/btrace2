#!/usr/bin/bash

OUTDIR=$BUILDDIR/objs

/usr/sbin/dtrace -G -o $OUTDIR/btraced.o -s $SRCDIR/btraced.d $OUTDIR/btrace.o
$CC -G $OUTDIR/btrace.o $OUTDIR/btraced.o -o $BUILDDIR/$LIBNAME-$ARCH.so