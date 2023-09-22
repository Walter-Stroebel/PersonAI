/*
 */
package nl.infcomtec.codemanager;

import java.io.File;
import java.util.List;
import java.util.TreeMap;

/**
 *
 * @author walter
 */
public class Project {
    public final String name;
    public final String path;

    public Project(File dir) {
        name=dir.getName();
        path=dir.getAbsolutePath();
    }
    public TreeMap<String,List<String>> packages=new TreeMap<>();

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Project");
        sb.append("\n  name=").append(name);
        sb.append("\n  path=").append(path);
        sb.append("\n  packages=").append(packages.keySet());
        return sb.toString();
    }

}
