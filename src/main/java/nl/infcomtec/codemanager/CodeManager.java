package nl.infcomtec.codemanager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Walter Stroebel
 */
public class CodeManager {

    public static final File HOME_DIR = new File(System.getProperty("user.home"));
    // If you are not on Linux, you might want to change this
    public static final File PROJ_DIR = new File(HOME_DIR, ".config/CodeManager");
    public static final File PROJ_CFG = new File(PROJ_DIR, "codeManager.json");
    public static final File CODE_DIR = new File(CodeManager.class.getProtectionDomain().getCodeSource().getLocation().getFile());
    private static Config config;
    public static final TreeMap<String, Project> packages = new TreeMap<>();

    public static void main(String[] args) throws Exception {
        PROJ_DIR.mkdirs();
        if (!PROJ_CFG.exists()) {
            File p = CODE_DIR.getParentFile();
            while (!p.getName().equals("CodeManager")) {
                p = p.getParentFile();
            }
            Config cfg = new Config();
            cfg.projectDirectories = new String[]{p.getParentFile().getAbsolutePath()};
            cfg.sourceDir = new File(p, MVN_SRC_LOC).getAbsolutePath();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try ( FileWriter fw = new FileWriter(PROJ_CFG)) {
                gson.toJson(cfg, fw);
                fw.write(System.lineSeparator());
            }
        }
        loadConfig();
        scan();
        System.out.println("Total lines: " + totalLines);
        System.out.println("Total chars: " + totalChars);
        System.out.println("Total words: " + totalWords);
    }

    private static void scan() throws InterruptedException {
        List<File> fs = config.getProjDirs();
        Collections.sort(fs);
        ExecutorService pool = Executors.newFixedThreadPool(8);
        for (final File ft : fs) {
            pool.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        Gson gson = new GsonBuilder().setPrettyPrinting().create();
                        File f = new File(ft, MVN_SRC_LOC);
                        final int ofs = f.getAbsolutePath().length() + 1;
                        final Project p = new Project(ft);
                        Files.walkFileTree(f.toPath(), new SimpleFileVisitor<Path>() {

                            @Override
                            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                File f = file.toFile();
                                if (f.getName().endsWith(".java")) {
                                    if (f.getParent().length() <= ofs) {
                                        return super.visitFile(file, attrs);
                                    }
                                    String pkg = f.getParent().substring(ofs).replace('/', '.');
                                    pkg = pkg.replace('\\', '.');
                                    List files = p.packages.get(pkg);
                                    if (null == files) {
                                        files = new LinkedList<>();
                                        p.packages.put(pkg, files);
                                    }
                                    files.add(f.getAbsolutePath());
                                }
                                return super.visitFile(file, attrs);
                            }
                        });
                        if (p.packages.isEmpty()) {
                            return;
                        }
                        synchronized (packages) {
                            for (Map.Entry<String, List<String>> pg : p.packages.entrySet()) {
                                if (packages.containsKey(pg.getKey())) {
                                    Project dup = packages.get(pg.getKey());
                                    System.out.println("Dup: " + p.name + " and " + dup.name + " both define " + pg.getKey());
                                    System.out.println(p);
                                    System.out.println(dup);
                                } else {
                                    System.out.println("New: "+pg.getKey()+" in "+p.name);
                                    packages.put(pg.getKey(), p);
                                }
                            }
                            stats(p);
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(CodeManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
        }
        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);
    }
    private static final String MVN_SRC_LOC = "src/main/java";
    public static AtomicInteger totalLines = new AtomicInteger(0);
    public static AtomicInteger totalChars = new AtomicInteger(0);
    public static AtomicInteger totalWords = new AtomicInteger(0);
    private static final Pattern pattern = Pattern.compile("\\b\\w+\\b");

    private static void stats(Project p) throws Exception {
        for (List<String> l : p.packages.values()) {
            for (String s : l) {
                try ( BufferedReader bfr = new BufferedReader(new FileReader(s))) {
                    for (String line = bfr.readLine(); null != line; line = bfr.readLine()) {
                        totalLines.incrementAndGet();
                        totalChars.addAndGet(line.length() + 1);
                        Matcher matcher = pattern.matcher(line);
                        int count = 0;
                        while (matcher.find()) {
                            count++;
                        }
                        totalWords.addAndGet(count);
                    }
                }
            }
        }
    }

    private static void loadConfig() throws Exception {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try ( FileReader fr = new FileReader(PROJ_CFG)) {
            config = gson.fromJson(fr, Config.class);
        }
    }

    private final static class Config {

        String[] projectDirectories;
        String sourceDir;

        List<File> getProjDirs() {
            File srcDir = new File(sourceDir);
            LinkedList<File> ret = new LinkedList<>();
            for (String s : projectDirectories) {
                File[] fs = new File(s).listFiles();
                if (null == fs) {
                    System.err.println("No projects found in " + s);
                } else {
                    for (File f : fs) {
                        if (f.isDirectory()) {
                            if (new File(f, "pom.xml").exists()) {
                                File f2 = new File(f, MVN_SRC_LOC);
                                if (!f2.equals(srcDir) && f2.exists() && f2.isDirectory()) {
                                    ret.add(f);
                                }
                            }
                        }
                    }
                }
            }
            return ret;
        }
    }
}
