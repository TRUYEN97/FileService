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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author Administrator
 */
public class FileService {

    public boolean saveFile(String name, String data) {
        if (name == null || data == null || data.isEmpty()) {
            return false;
        }
        File file = initFile(name);
        try ( FileWriter writer = new FileWriter(file)) {
            writer.write(data);
            writer.flush();
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
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
