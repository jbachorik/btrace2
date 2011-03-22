# To be defined externally
#BUILDDIR=../build
#SRCDIR=../src
#CLASSPATH=classes
#ARCH=32
OUTDIR=${BUILDDIR}/objs

# FIXME: 64 bit option

all:
	${JAVA_HOME}/bin/javah -classpath ${CLASSPATH} -d ${OUTDIR} com.sun.btrace.BTraceRuntime
	mkdir -p ${OUTDIR}
	cc -c -I${JAVA_HOME}/include -I${JAVA_HOME}/include/solaris -I${OUTDIR} -o ${OUTDIR}/btrace.o ${SRCDIR}/btrace.c
	/usr/sbin/dtrace -G -${ARCH} -o /tmp/btraced.o -s ${SRCDIR}/btraced.d ${OUTDIR}/btrace.o
	cc -G ${OUTDIR}/btrace.o /tmp/btraced.o -o ${BUILDDIR}/libbtrace.so

clean:
	rm -rf ${OUTDIR}
