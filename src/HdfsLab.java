import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;

public class HdfsLab {
    private static final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(URI.create("hdfs://localhost:9000"), conf);
        while (true) {
            menu();
            int op = Integer.parseInt(in.readLine());
            if (op == 0) break;
            try {
                run(fs, op);
            } catch (Exception e) {
                System.out.println("Operation failed: " + e.getMessage());
            }
        }
        fs.close();
    }

    private static void menu() {
        System.out.println("\n1.upload  2.download  3.cat  4.file info  5.list dir");
        System.out.println("6.create/delete file  7.create/delete dir  8.append");
        System.out.println("9.delete file  10.move file  0.exit");
        System.out.print("choice: ");
    }

    private static void run(FileSystem fs, int op) throws IOException {
        if (op == 1) upload(fs);
        else if (op == 2) download(fs);
        else if (op == 3) cat(fs);
        else if (op == 4) fileInfo(fs);
        else if (op == 5) listAll(fs);
        else if (op == 6) createOrDeleteFile(fs);
        else if (op == 7) createOrDeleteDir(fs);
        else if (op == 8) append(fs);
        else if (op == 9) deleteFile(fs);
        else if (op == 10) moveFile(fs);
    }

    private static void upload(FileSystem fs) throws IOException {
        System.out.print("local file: ");
        Path local = new Path(in.readLine());
        System.out.print("hdfs file: ");
        Path hdfs = new Path(in.readLine());
        if (fs.exists(hdfs)) {
            System.out.print("file exists, append or overwrite? (a/o): ");
            String mode = in.readLine();
            if (mode.equalsIgnoreCase("a")) appendToTail(fs, hdfs, local);
            else try (FSDataOutputStream out = fs.create(hdfs, true)) {
                out.write(Files.readAllBytes(Paths.get(local.toString())));
            }
        } else {
            fs.copyFromLocalFile(local, hdfs);
        }
        System.out.println("upload ok: " + hdfs);
    }

    private static void download(FileSystem fs) throws IOException {
        System.out.print("hdfs file: ");
        Path hdfs = new Path(in.readLine());
        System.out.print("local file: ");
        Path local = new Path(in.readLine());
        if (new java.io.File(local.toString()).exists()) {
            int n = 1;
            Path renamed = local;
            while (new java.io.File(renamed.toString()).exists()) {
                renamed = new Path(local.toString() + "_" + n++);
            }
            local = renamed;
            System.out.println("local file exists, save as " + local);
        }
        fs.copyToLocalFile(hdfs, local);
        System.out.println("download ok: " + local);
    }

    private static void cat(FileSystem fs) throws IOException {
        System.out.print("hdfs file: ");
        Path hdfs = new Path(in.readLine());
        try (FSDataInputStream input = fs.open(hdfs)) {
            IOUtils.copyBytes(input, System.out, 4096, false);
        }
    }

    private static void fileInfo(FileSystem fs) throws IOException {
        System.out.print("hdfs file: ");
        show(fs.getFileStatus(new Path(in.readLine())));
    }

    private static void listAll(FileSystem fs) throws IOException {
        System.out.print("hdfs dir: ");
        list(fs, fs.getFileStatus(new Path(in.readLine())));
    }

    private static void list(FileSystem fs, FileStatus s) throws IOException {
        show(s);
        if (s.isDirectory()) for (FileStatus x : fs.listStatus(s.getPath())) list(fs, x);
    }

    private static void show(FileStatus s) {
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(s.getModificationTime()));
        System.out.printf("%s %8d %s %s%n", s.isDirectory() ? "d" : "-", s.getLen(), time, s.getPath());
    }

    private static void createOrDeleteFile(FileSystem fs) throws IOException {
        System.out.print("create or delete? (c/d): ");
        boolean create = in.readLine().equalsIgnoreCase("c");
        System.out.print("hdfs file: ");
        Path p = new Path(in.readLine());
        if (create) {
            if (p.getParent() != null && !fs.exists(p.getParent())) fs.mkdirs(p.getParent());
            try (FSDataOutputStream out = fs.create(p, true)) { out.writeUTF("hello hdfs"); }
        } else {
            fs.delete(p, false);
        }
        System.out.println("done: " + p);
    }

    private static void createOrDeleteDir(FileSystem fs) throws IOException {
        System.out.print("create or delete? (c/d): ");
        boolean create = in.readLine().equalsIgnoreCase("c");
        System.out.print("hdfs dir: ");
        Path p = new Path(in.readLine());
        if (create) {
            fs.mkdirs(p);
        } else if (fs.listStatus(p).length == 0) {
            fs.delete(p, false);
        } else {
            System.out.println("directory is not empty, not deleted");
        }
        System.out.println("done: " + p);
    }

    private static void append(FileSystem fs) throws IOException {
        System.out.print("hdfs file: ");
        Path hdfs = new Path(in.readLine());
        System.out.print("local append file: ");
        Path local = new Path(in.readLine());
        System.out.print("add to head or tail? (h/t): ");
        boolean head = in.readLine().equalsIgnoreCase("h");
        if (head) appendToHead(fs, hdfs, local);
        else appendToTail(fs, hdfs, local);
        System.out.println("append ok");
    }

    private static void deleteFile(FileSystem fs) throws IOException {
        System.out.print("hdfs file: ");
        Path p = new Path(in.readLine());
        fs.delete(p, false);
        System.out.println("deleted: " + p);
    }

    private static void moveFile(FileSystem fs) throws IOException {
        System.out.print("source hdfs path: ");
        Path from = new Path(in.readLine());
        System.out.print("target hdfs path: ");
        Path to = new Path(in.readLine());
        fs.rename(from, to);
        System.out.println("moved to: " + to);
    }

    private static void appendToHead(FileSystem fs, Path hdfs, Path local) throws IOException {
        ByteArrayOutputStream oldData = new ByteArrayOutputStream();
        try (FSDataInputStream input = fs.open(hdfs)) {
            IOUtils.copyBytes(input, oldData, 4096, false);
        }
        Path tmp = new Path(hdfs.getParent(), hdfs.getName() + ".tmp");
        try (FSDataOutputStream out = fs.create(tmp, true)) {
            out.write(Files.readAllBytes(Paths.get(local.toString())));
            out.write(oldData.toByteArray());
        }
        fs.delete(hdfs, false);
        fs.rename(tmp, hdfs);
    }

    private static void appendToTail(FileSystem fs, Path hdfs, Path local) throws IOException {
        ByteArrayOutputStream oldData = new ByteArrayOutputStream();
        try (FSDataInputStream input = fs.open(hdfs)) {
            IOUtils.copyBytes(input, oldData, 4096, false);
        }
        Path tmp = new Path(hdfs.getParent(), hdfs.getName() + ".tmp");
        try (FSDataOutputStream out = fs.create(tmp, true)) {
            out.write(oldData.toByteArray());
            out.write(Files.readAllBytes(Paths.get(local.toString())));
        }
        fs.delete(hdfs, false);
        fs.rename(tmp, hdfs);
    }
}
