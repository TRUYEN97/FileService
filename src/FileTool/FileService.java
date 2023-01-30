/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package FileTool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Administrator
 */
public class FileService {

    public boolean writeFile(String name, String data, boolean appand) {
        if (name == null || data == null || data.isEmpty()) {
            return false;
        }
        File file = initFile(name);
        try ( FileWriter writer = new FileWriter(file, appand)) {
            writer.write(data);
            writer.flush();
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public void moveFolder(String sourceFile, String destFile) throws IOException {
        moveFolder(new File(sourceFile), new File(destFile));
    }

    public void moveFolder(File sourceFile, File destFile) throws IOException {
        copyFilesInDirectory(sourceFile, destFile);
        deleteFolder(sourceFile);
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

    public boolean deleteFolder(File folder) {
        if (!folder.exists()) {
            return true;
        }
        if (folder.isFile()) {
            return folder.delete();
        }
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

    public void copyFilesInDirectory(File sourceFile, File targetFolder) throws IOException {
        try {
            copy(sourceFile, targetFolder, sourceFile);
        } catch (IOException e) {
            deleteFolder(targetFolder);
            throw e;
        }
    }

    public void copyFilesInDirectory(File sourceFolder, List<File> files, File targetFolder) throws IOException, Exception {
        try {
            if (targetFolder.isFile()) {
                throw new Exception("sourceFolder and targetFolder need to be the directory");
            }
            for (File file : files) {
                String filePath = file.getPath();
                File targat;
                if (sourceFolder != null && !sourceFolder.isFile()) {
                    targat = new File(filePath.replaceFirst(sourceFolder.getPath().replaceAll("\\\\", "\\\\\\\\"),
                            targetFolder.getPath().replaceAll("\\\\", "\\\\\\\\")));
                } else {
                    targat = new File(String.format("%s%s%s", targetFolder, File.separator, file.getName()));
                }
                copy(file, targat);
            }
        } catch (IOException e) {
            this.deleteFolder(targetFolder);
            throw e;
        }
    }

    public void copy(File source, File targetFile) throws IOException {
        if (source == null || !source.exists()) {
            throw new NullPointerException("File source is null or not exists!");
        }
        if (targetFile == null) {
            throw new NullPointerException("File copy is null!");
        }
        targetFile.getParentFile().mkdirs();
        if (targetFile.exists() && !targetFile.delete()) {
            throw new IOException(String.format("File copy can not override!\r\n%s", targetFile.getName()));
        }
        try ( FileInputStream inputStream = new FileInputStream(source)) {
            try ( FileOutputStream outputStream = new FileOutputStream(targetFile)) {
                transferFile(inputStream, outputStream);
            }
        }
    }

    private void copy(File sourceFile, File destFile, File parent) throws IOException {
        if (sourceFile.isDirectory()) {
            for (File file : sourceFile.listFiles()) {
                copy(file, destFile, parent);
            }
        } else if (destFile.isFile()) {
            copy(sourceFile, destFile);
        } else {
            File target;
            if (parent != null && !sourceFile.getPath().equals(parent.getPath())) {
                target = new File(sourceFile.getPath().replaceFirst(parent.getPath().replaceAll("\\\\", "\\\\\\\\"),
                        destFile.getPath().replaceAll("\\\\", "\\\\\\\\")));
            }else{
                 target = new File(String.format("%s/%s", destFile,sourceFile.getName()));
            }
            copy(sourceFile, target);
        }
    }

    public List<File> getAllFile(File folder) {
        List<File> files = new ArrayList<>();
        getAllFile(folder, files);
        return files;
    }
    
    private void getAllFile(File folder, List<File> files){
        if (folder == null) {
            return;
        }
        if (folder.isFile()) {
            files.add(folder);
            return;
        }
        for (File file : folder.listFiles()) {
           getAllFile(file, files);
        }
    }

}
