/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package FileTool;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.zip.ZipEntry;
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
        File file = new File(name);
        file.mkdirs();
        if (file.exists()) {
            file.delete();
        }
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(data);
            writer.flush();
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public String readFile(File file) {
        StringBuilder str = new StringBuilder();
        if (!file.exists()) {
            return str.toString();
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                str.append(line).append("\r\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return str.toString();
    }

    public boolean zipFile(String file, String zipPath, String detail) {
        File zipFile = new File(zipPath);
        if (zipFile.exists() && !zipFile.delete()) {
            return false;
        }
        ZipOutputStream out = null;
        try {
            out = new ZipOutputStream(new FileOutputStream(zipPath));
            try ( BufferedOutputStream bos = new BufferedOutputStream(out)) {
                out.putNextEntry(new ZipEntry(file));
                byte[] buf = detail.getBytes();
                bos.write(buf);
                bos.flush();
            }
            out.close();
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        } finally {
            try {
                if (out == null) {
                    out.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public boolean zipFile(String zipPath, File fileDetail) {
        if (!fileDetail.exists()) {
            return false;
        }
        File zipFile = new File(zipPath);
        if (zipFile.exists() && !zipFile.delete()) {
            return false;
        }
        ZipOutputStream out = null;
        try {
            out = new ZipOutputStream(new FileOutputStream(zipPath));
            try ( BufferedOutputStream bos = new BufferedOutputStream(out)) {
                out.putNextEntry(new ZipEntry(fileDetail.getName()));
                BufferedReader reader = new BufferedReader(new FileReader(fileDetail));
                String line;
                while ((line = reader.readLine()) != null) {
                    byte[] buf = line.getBytes();
                    bos.write(buf);
                    bos.flush();
                }
            }
            out.close();
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        } finally {
            try {
                if (out == null) {
                    out.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
