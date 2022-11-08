/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package FileTool;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author Administrator
 */
public class FileService {

    public boolean writeFile(String name, String data, boolean appand) {
        if (name == null || data == null || data.isEmpty()) {
            return false;
        }
        File file = new File(name);
        try ( FileWriter writer = new FileWriter(file, appand)) {
            writer.write(data);
            writer.flush();
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean saveFile(String name, String data) {
        return writeFile(name, data, false);
    }

    public boolean saveFile(String path, byte[] data) {
        if (path == null || data == null) {
            return false;
        }
        File file = initFile(path);
        try ( FileOutputStream writer = new FileOutputStream(file)) {
            writer.write(data);
            writer.flush();
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public String MD5(File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        return MD5(file, md);
    }

    public String MD5(File file, MessageDigest md) throws IOException {

        StringBuilder MD5 = new StringBuilder();
        if (file.isDirectory()) {
            for (File listFile : file.listFiles()) {
                MD5.append(MD5(listFile, md));
            }
        } else {
            try ( InputStream is = Files.newInputStream(file.toPath());  DigestInputStream dis = new DigestInputStream(is, md)) {
                byte[] byteArray = new byte[1024];
                int bytesCount;
                while ((bytesCount = dis.read(byteArray)) != -1) {
                    md.update(byteArray, 0, bytesCount);
                }
                byte[] bytes = md.digest();
                for (int i = 0; i < bytes.length; i++) {
                    MD5.append(Integer
                            .toString((bytes[i] & 0xff) + 0x100, 16)
                            .substring(1));
                }
            }
        }
        return MD5.toString();
    }

    public boolean deleteFolder(File folder) {
        for (File child : folder.listFiles()) {
            if (child.isDirectory()) {
                deleteFolder(child);
            } else if (!child.delete()) {
                return false;
            }
        }
        return folder.delete();
    }

    public void deleteFolder(String newFolder) {
        deleteFolder(new File(newFolder));
    }

    private File initFile(String name) {
        File file = new File(name);
        file.mkdirs();
        if (file.exists()) {
            file.delete();
        }
        return file;
    }

    public String readFile(File file) {
        StringBuilder str = new StringBuilder();
        if (!file.exists()) {
            return str.toString();
        }
        try ( BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                str.append(line).append("\r\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str.toString();
    }

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
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        try ( ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                String filePath = destDirectory + File.separator + entry.getName();
                if (!entry.isDirectory()) {
                    try ( BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(new File(filePath)))) {
                        transferFile(zipIn, output);
                    }
                } else {
                    File dir = new File(filePath);
                    dir.mkdirs();
                }
                zipIn.closeEntry();
            }
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     *
     *
     * @param inputStream
     * @param outputStream
     * @throws IOException
     */
    public void transferFile(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] bytesIn = new byte[4096];
        int read;
        while ((read = inputStream.read(bytesIn)) != -1) {
            outputStream.write(bytesIn, 0, read);
            outputStream.flush();
        }
    }

    public byte[] getByte(String path) {
        try ( FileInputStream fileInputStream = new FileInputStream(new File(path))) {
            return fileInputStream.readAllBytes();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void copy(File source, File fileCopy) throws IOException {
        if (source == null || !source.exists()) {
            throw new NullPointerException("File source is null or not exists!");
        }
        if (fileCopy == null) {
            throw new NullPointerException("File copy is null!");
        }
        fileCopy.getParentFile().mkdirs();
        if (fileCopy.exists() && !fileCopy.delete()) {
            throw new IOException("File copy can not override!");
        }
        try ( FileInputStream inputStream = new FileInputStream(source)) {
            try ( FileOutputStream outputStream = new FileOutputStream(fileCopy)) {
                transferFile(inputStream, outputStream);
            }
        }
    }

}
