/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.java.btrace.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.CharBuffer;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;
import net.java.btrace.api.core.BTraceLogger;

/**
 *
 * @author Jaroslav Bachorik <jaroslav.bachorik at oracle.com>
 */
final public class ClientWriter extends PrintWriter {
    final private ExecutorService s = Executors.newSingleThreadExecutor();
    final private AtomicReference<PrintWriter> cacheWriterRef = new AtomicReference<PrintWriter>();
    final private PrintWriter directWriter;
    volatile private File cacheFile;
    
    public ClientWriter(OutputStream os) {
        super(os);
        directWriter = new PrintWriter(os);
        cacheWriterRef.set(directWriter);
    }
    
    public void park() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        s.submit(new Runnable() {

            @Override
            public void run() {
                try {
                    cacheFile = File.createTempFile("btrace-", ".cache");
                    PrintWriter pw = new PrintWriter(cacheFile);
                    
                    synchronized(lock) {
                        cacheWriterRef.set(pw);
                        latch.countDown();
                    }
                } catch (IOException e) {
                    BTraceLogger.debugPrint(e);
                }
            }
        });
        latch.await();
    }
    
    public void unpark() {
        s.submit(new Runnable() {

            @Override
            public void run() {
                synchronized(lock) {
                    Writer pw = cacheWriterRef.getAndSet(directWriter);
                    BufferedReader br = null;
                    try {
                        pw.close();

                        br = new BufferedReader(new FileReader(cacheFile));
                        String line = null;
                        while ((line = br.readLine()) != null) {
                            directWriter.println(line);
                        }
                        directWriter.flush();
                    } catch (IOException ex) {
                    } finally {
                        try {
                            if (br != null) {
                                br.close();
                            }
                        } catch (IOException ex) {}
                    }
                }
                cacheFile.delete();
                cacheFile = null;
            }
        });
    }

    public void flush() {
        synchronized(lock) {
            cacheWriterRef.get().flush();
        }
    }

    public void write(int c) {
        synchronized(lock) {
            cacheWriterRef.get().write(c);
        }
    }

    public void write(char[] buf, int off, int len) {
        synchronized(lock) {
            cacheWriterRef.get().write(buf, off, len);
        }
    }

    public void write(char[] buf) {
        synchronized(lock) {
            cacheWriterRef.get().write(buf);
        }
    }

    public void write(String s, int off, int len) {
        synchronized(lock) {
            cacheWriterRef.get().write(s, off, len);
        }
    }

    public void write(String s) {
        synchronized(lock) {
            cacheWriterRef.get().write(s);
        }
    }

    public void print(boolean b) {
        synchronized(lock) {
            cacheWriterRef.get().print(b);
        }
    }

    public void print(char c) {
        synchronized(lock) {
            cacheWriterRef.get().print(c);
        }
    }

    public void print(int i) {
        synchronized(lock) {
            cacheWriterRef.get().print(i);
        }
    }

    public void print(long l) {
        synchronized(lock) {
            cacheWriterRef.get().print(l);
        }
    }

    public void print(float f) {
        synchronized(lock) {
            cacheWriterRef.get().print(f);
        }
    }

    public void print(double d) {
        synchronized(lock) {
            cacheWriterRef.get().print(d);
        }
    }

    public void print(char[] s) {
        synchronized(lock) {
            cacheWriterRef.get().print(s);
        }
    }

    public void print(String s) {
        synchronized(lock) {
            cacheWriterRef.get().print(s);
        }
    }

    public void print(Object obj) {
        synchronized(lock) {
            cacheWriterRef.get().print(obj);
        }
    }

    public void println() {
        synchronized(lock) {
            cacheWriterRef.get().println();
        }
    }

    public void println(boolean x) {
        synchronized(lock) {
            cacheWriterRef.get().println(x);
        }
    }

    public void println(char x) {
        synchronized(lock) {
            cacheWriterRef.get().println(x);
        }
    }

    public void println(int x) {
        synchronized(lock) {
            cacheWriterRef.get().println(x);
        }
    }

    public void println(long x) {
        synchronized(lock) {
            cacheWriterRef.get().println(x);
        }
    }

    public void println(float x) {
        synchronized(lock) {
            cacheWriterRef.get().println(x);
        }
    }

    public void println(double x) {
        synchronized(lock) {
            cacheWriterRef.get().println(x);
        }
    }

    public void println(char[] x) {
        synchronized(lock) {
            cacheWriterRef.get().println(x);
        }
    }

    public void println(String x) {
        synchronized(lock) {
            cacheWriterRef.get().println(x);
        }
    }

    public void println(Object x) {
        synchronized(lock) {
            cacheWriterRef.get().println(x);
        }
    }

    public PrintWriter printf(String format, Object... args) {
        synchronized(lock) {
            return cacheWriterRef.get().printf(format, args);
        }
    }

    public PrintWriter printf(Locale l, String format, Object... args) {
        synchronized(lock) {
            return cacheWriterRef.get().printf(l, format, args);
        }
    }

    public PrintWriter format(String format, Object... args) {
        synchronized(lock) {
            return cacheWriterRef.get().format(format, args);
        }
    }

    public PrintWriter format(Locale l, String format, Object... args) {
        synchronized(lock) {
            return cacheWriterRef.get().format(l, format, args);
        }
    }

    public PrintWriter append(CharSequence csq) {
        synchronized(lock) {
            return cacheWriterRef.get().append(csq);
        }
    }

    public PrintWriter append(CharSequence csq, int start, int end) {
        return cacheWriterRef.get().append(csq, start, end);
    }

    public PrintWriter append(char c) {
        synchronized(lock) {
            return cacheWriterRef.get().append(c);
        }
    }

    public void close() {
        directWriter.close();
    }

    public boolean checkError() {
        return cacheWriterRef.get().checkError();
    }    
}
