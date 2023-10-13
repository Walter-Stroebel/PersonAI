/*
 */
package nl.infcomtec.pubsub;

import java.util.Date;
import java.util.Random;

/**
 *
 * @author walter
 */
public class BlobPoolDemo {

    /**
     * Rename to main to demo.
     *
     * @param args
     */
    public static void main(String[] args) {
        long l1, l2, l3, l4;
        l1 = BlobPool.getUniqueTime();
        l2 = BlobPool.getUniqueTime();
        l3 = BlobPool.getUniqueTime();
        l4 = BlobPool.getUniqueTime();
        System.out.format("%g %g %g\n", (l2 - l1) * 1e-9, (l3 - l2) * 1e-9, (l4 - l3) * 1e-9);
        long realStart = Long.MIN_VALUE / BlobPool.millieNanos;
        System.out.println(new Date(realStart));
        long realEnd = Long.MAX_VALUE / BlobPool.millieNanos;
        System.out.println(new Date(realEnd));
        final String ironOre = "iron ore";
        final String copperOre = "copper ore";
        final String ironIngot = "iron ingot";
        final String copperIngot = "copper ingot";
        final String coil = "coil";
        final BlobPool bp = new BlobPool().withTopics(ironOre, copperOre, ironIngot, copperIngot, coil);
        final Random rnd = new Random();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    sleep(rnd.nextInt(1000));
                    bp.submit(new Blob(ironOre, bp, ironOre));
                    sleep(rnd.nextInt(1000));
                }
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    sleep(rnd.nextInt(1000));
                    bp.submit(new Blob(copperOre, bp,2));
                    sleep(rnd.nextInt(1000));
                }
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                long t = 0;
                while (true) {
                    BlobPool.Result wait = bp.waitForMessage(ironOre, t, 1000);
                    if (wait.found == BlobPool.Results.NewMessage) {
                        sleep(rnd.nextInt(1000));
                        bp.submit(new Blob(ironIngot, bp,"ingot"));
                        sleep(rnd.nextInt(1000));
                        t = wait.blob.nTime;
                    } else {
                        System.err.println("Iron smelter did not get iron ore " + wait);
                    }
                }
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                long t = 0;
                while (true) {
                    BlobPool.Result wait = bp.waitForMessage(copperOre, t, 1000);
                    if (wait.found == BlobPool.Results.NewMessage) {
                        sleep(rnd.nextInt(1000));
                        bp.submit(new Blob(copperIngot, bp,2));
                        sleep(rnd.nextInt(1000));
                        t = wait.blob.nTime;
                    } else {
                        System.err.println("Copper smelter did not get copper ore " + wait);
                    }
                }
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                long t1 = 0;
                long t2 = 0;
                int copper=0;
                while (true) {
                    BlobPool.Result wait = bp.waitForMessage(copperIngot, t1, 2000);
                    if (wait.found == BlobPool.Results.NewMessage) {
                        t1 = wait.blob.nTime;
                    } else {
                        System.err.println("Coil maker did not get any copper ingot " + wait);
                        continue;
                    }
                    copper+=(Integer) wait.blob.getData(bp);
                    wait = bp.waitForMessage(ironIngot, t2, 1000);
                    if (wait.found == BlobPool.Results.NewMessage) {
                        t2 = wait.blob.nTime;
                    } else {
                        System.err.println("Coil maker did not get iron ingot " + wait);
                        continue;
                    }
                    if(copper>=3){
                        copper-=3;
                        sleep(rnd.nextInt(100));
                        bp.submit(new Blob(coil, bp,1));
                        System.out.println("Coil produced");
                    }else{
                        System.err.println("Coil maker needs more copper ingots: "+copper+" of 3");
                    }
                }
            }
        }).start();
    }

    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            System.exit(0);
        }
    }
}
