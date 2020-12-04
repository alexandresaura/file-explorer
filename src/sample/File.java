package sample;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class File {
    private static long incrementId = 0;

    private long id;
    private String name;
    private String type;
    private long lastModified;
    private long startPosition;
    private long size;
    private long directoryId;
    private byte[] fileContent;

    public File(long id, String name, String type, long lastModified, long startPosition, long size, long directoryId) {
        this.id = id;
        this.name = name;
        if (type == null) this.type = "";
        else this.type = type;
        this.lastModified = lastModified;
        this.size = size;
        this.startPosition = startPosition;
        this.directoryId = directoryId;
    }

    public File(String name, String type, long lastModified, long startPosition, long size, long directoryId) {
        this(incrementId++, name, type, lastModified, startPosition, size, directoryId);
    }

    public static void updateIncrementId(long incrementId) {
        File.incrementId = incrementId;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public long getLastModified() {
        return lastModified;
    }

    public String getLastModifiedFormatted() {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(lastModified), ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public long getSize() {
        return size;
    }

    public String getSizeFormatted() {
        if (size < 1000) return size + " B";
        else if (size < 1000000) return Math.round(size / 1000.0) + " KB";
        else if (size < 1000000000) return Math.round(size / 1000000.0) + " MB";
        else return Math.round(size / 1000000000.0) + " GB";
    }

    public byte[] getFileContent() throws IOException {
        if (fileContent == null) {
            java.io.File contentFile = new java.io.File("content_file.bin");

            try (RandomAccessFile raf = new RandomAccessFile(contentFile, "r")) {
                fileContent = new byte[(int) size];
                raf.seek(startPosition);
                raf.read(fileContent);
            }
        }

        return fileContent;
    }

    public void insert(byte[] fileContent) throws IOException {
        this.fileContent = fileContent;

        java.io.File contentFile = new java.io.File("content_file.bin");
        if (!contentFile.exists() && !contentFile.createNewFile()) return;

        try (RandomAccessFile raf = new RandomAccessFile(contentFile, "rw")) {
            raf.seek(startPosition);
            raf.write(this.fileContent);
        }

        java.io.File headerFile = new java.io.File("header.txt");
        if (!headerFile.exists() && !headerFile.createNewFile()) return;

        try (FileOutputStream fos = new FileOutputStream(headerFile, true)) {
            String line = directoryId + "D|" + id + "F|" + name + "|" + type + "|" + lastModified + "|" + startPosition + "|" + size + "\n";
            fos.write(line.getBytes());
        }
    }

    public void delete() throws IOException {
        java.io.File headerFile = new java.io.File("header.txt");

        StringBuilder stringBuilderFile = new StringBuilder();

        String lineToRemove = directoryId + "D|" + id + "F|" + name + "|" + type + "|" + lastModified + "|" + startPosition + "|" + size;

        Files.readAllLines(Paths.get(headerFile.getPath())).forEach(line -> {
            if (!line.equals(lineToRemove)) {
                stringBuilderFile.append(line + "\n");
            }
        });

        try (FileOutputStream fos = new FileOutputStream(headerFile)) {
            fos.write(stringBuilderFile.toString().getBytes());
        }
    }
}
