/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package FileTool;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author Administrator
 */
public class Zip {

    public boolean zipFile(String fileNameInZip, String zipPath, String detail) {
        File zipFile = new File(zipPath);
        if (zipFile.exists() && !zipFile.delete()) {
            return false;
        }
        try ( ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipPath))) {
            try ( BufferedOutputStream bos = new BufferedOutputStream(out)) {
                out.putNextEntry(new ZipEntry(fileNameInZip));
                byte[] buf = detail.getBytes();
                bos.write(buf);
                bos.flush();
            }
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean zipFile(String zipPath, File file) {
        return zipFile(zipPath, new File[]{file});
    }

    public void zipFolder(File sourceFile, File zipFile) throws IOException {
        zipFolder(sourceFile.getPath(), zipFile.getPath());
    }

    public void zipFolder(String sourceDirPath, String zipFilePath) throws IOException {
        Path p = Files.createFile(Paths.get(zipFilePath));
        try ( ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(p))) {
            Path pp = Paths.get(sourceDirPath);
            Files.walk(pp)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        ZipEntry zipEntry = new ZipEntry(pp.relativize(path).toString());
                        try {
                            zs.putNextEntry(zipEntry);
                            Files.copy(path, zs);
                            zs.closeEntry();
                        } catch (IOException e) {
                            System.err.println(e);
                        }
                    });
        }
    }

    public boolean zipFile(String zipPath, File[] files) {
        File zipFile = new File(zipPath);
        if (zipFile.exists() && !zipFile.delete()) {
            return false;
        }
        try ( ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipPath))) {
            try ( BufferedOutputStream bos = new BufferedOutputStream(out)) {
                for (File file : files) {
                    if (!file.exists()) {
                        return false;
                    }
                    out.putNextEntry(new ZipEntry(file.getName()));
                    try ( FileInputStream reader = new FileInputStream(file)) {
                        transferFile(reader, bos);
                    }
                    out.closeEntry();
                }
            }
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean unzip(String zipFilePath, String destDirectory) {
        return unzip(new File(zipFilePath), new File(destDirectory));
    }

    public boolean unzip(File zipFile, File destDir) {
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        try ( ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                File filePath = new File(String.format("%s%s%s",
                        destDir, File.separator, entry.getName()));
                if (!entry.isDirectory()) {
                    if (!filePath.getParentFile().exists()) {
                        filePath.getParentFile().mkdirs();
                    }
                    try ( BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(filePath))) {
                        transferFile(zipIn, output);
                    }
                } else {
                    filePath.mkdirs();
                }
                zipIn.closeEntry();
            }
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public void transferFile(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] bytesIn = new byte[4096];
        int read;
        while ((read = inputStream.read(bytesIn)) != -1) {
            outputStream.write(bytesIn, 0, read);
            outputStream.flush();
        }
    }
}
