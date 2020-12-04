package sample;

import javafx.scene.control.TreeItem;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Directory {
    private static long incrementId = 1;
    private static final List<Directory> directories;

    private long id;
    private long parentId;
    private String name;
    private List<File> files;

    private TreeItem<Directory> treeItem;

    static {
        directories = new ArrayList<>();
    }

    public Directory(long id, long parentId, String name) {
        this.id = id;
        this.parentId = parentId;
        this.name = name;
        this.files = new LinkedList<>();
        directories.add(this);
    }

    public Directory(long parentId, String name) throws IOException {
        this(incrementId++, parentId, name);
        insert();
    }

    public static void updateIncrementId(long incrementId) {
        Directory.incrementId = incrementId;
    }

    public static List<Directory> getDirectories() {
        return Collections.unmodifiableList(directories);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<File> getFiles() {
        return Collections.unmodifiableList(files);
    }

    public void insertFile(File file) {
        files.add(file);
    }

    public void setTreeItem(TreeItem<Directory> treeItem) {
        this.treeItem = treeItem;
    }

    public TreeItem<Directory> getTreeItem() {
        return treeItem;
    }

    public void insert() throws IOException {
        java.io.File headerFile = new java.io.File("header.txt");
        if (!headerFile.exists() && !headerFile.createNewFile()) return;

        try (FileOutputStream fos = new FileOutputStream(headerFile, true)) {
            String line = parentId + "D|" + id + "D|" + name + "\n";
            fos.write(line.getBytes());
        }
    }

    public void remove(File file) {
        files.remove(file);
    }

    @Override
    public String toString() {
        return getName();
    }
}
