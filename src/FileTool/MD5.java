/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package FileTool;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Administrator
 */
public class MD5 {

    private final FileService fileService;

    public MD5() {
        this.fileService = new FileService();
    }

    public String MD5(File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        if (file.isDirectory()) {
            return getOnlyFilesMD5(this.fileService.getAllFile(file), md);
        }
        return getFileMD5(file, md);
    }

    public String MD5(File[] files) throws IOException, NoSuchAlgorithmException {
        return MD5(Arrays.asList(files));
    }

    public String MD5(Collection<File> files) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        List<File> allFiles = new ArrayList<>();
        for (File file : allFiles) {
            if (file.isDirectory()) {
                allFiles.addAll(this.fileService.getAllFile(file));
            } else {
                allFiles.add(file);
            }
        }
        return getOnlyFilesMD5(files, md);
    }

    private String getOnlyFilesMD5(Collection<File> files, MessageDigest md) throws IOException {
        StringBuilder listMD5 = new StringBuilder();
        for (File file : files) {
            if (!file.exists()) {
                continue;
            }
            listMD5.append(getFileMD5(file, md));
        }
        md.update(listMD5.toString().getBytes());
        return convertToHex(md.digest());
    }

    private String getFileMD5(File file, MessageDigest md) throws IOException {
        if (!file.exists()) {
            return null;
        }
        try ( InputStream is = Files.newInputStream(file.toPath());  DigestInputStream dis = new DigestInputStream(is, md)) {
            byte[] byteArray = new byte[4096];
            int bytesCount;
            while ((bytesCount = dis.read(byteArray)) != -1) {
                md.update(byteArray, 0, bytesCount);
            }
            return convertToHex(md.digest());
        }
    }

    public boolean isTrueMD5(File file, String MD5) {
        if (file == null || MD5 == null) {
            return false;
        }
        try {
            String ftpMD5 = MD5(file);
            return ftpMD5.equals(MD5);
        } catch (IOException | NoSuchAlgorithmException ex) {
            return false;
        }
    }

    public boolean isTrueMD5(File[] file, String MD5) {
        return isTrueMD5(Arrays.asList(file), MD5);
    }

    public boolean isTrueMD5(List<File> file, String MD5) {
        if (file == null || MD5 == null) {
            return false;
        }
        try {
            String ftpMD5 = MD5(file);
            return ftpMD5.equals(MD5);
        } catch (IOException | NoSuchAlgorithmException ex) {
            return false;
        }
    }

    private String convertToHex(byte[] bytes) {
        StringBuilder MD5 = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            MD5.append(Integer
                    .toString((bytes[i] & 0xff) + 0x100, 16)
                    .substring(1));
        }
        return MD5.toString();
    }
}
