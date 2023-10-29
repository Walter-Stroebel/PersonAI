package nl.infcomtec.personai;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TraceLogger {

    public static RandomAccessFile raf = null;
    private FileChannel channel = null;

    public void log(String message) throws IOException {
        if (raf == null) {
            return; // Do nothing if raf is not initialized
        }

        if (channel == null) {
            channel = raf.getChannel();
        }

        FileLock lock = null;
        try {
            lock = channel.lock();
            raf.seek(raf.length()); // Move to end of file
            String logEntry = String.format("%1$tFT%1$tT.%1$tL: ~",
                    System.currentTimeMillis()) + message + "~\n";
            raf.write(logEntry.getBytes(StandardCharsets.UTF_8));
            raf.getFD().sync(); // Flush changes immediately to disk
        } finally {
            if (lock != null) {
                lock.release();
            }
        }
    }

    public static void traceMe(String myFile) {
        File trace = new File("/tmp", myFile);
        if (trace.exists()) {
            String nn = UUID.randomUUID().toString();
            trace.renameTo(new File("/tmp", nn));
        }
        try {
            raf = new RandomAccessFile(trace.getAbsoluteFile(), "rw");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TraceLogger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void close() throws IOException {
        if (channel != null) {
            channel.close();
        }
        if (raf != null) {
            raf.close();
        }
    }

}
