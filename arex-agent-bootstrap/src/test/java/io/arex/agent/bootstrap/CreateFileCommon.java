package io.arex.agent.bootstrap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class CreateFileCommon {
    private static final String path;

    static {
        try {
            path = Files.createTempDirectory("arex").toAbsolutePath().toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private  static File file1 = null, file2 = null, file3 = null, zipFile = null, zipExtensionFile = null;

    static {
        try {
            file1 = createFile(path, "/test1.class");
            file2 = createFile(path, "/test2.txt");
            file3 = createFile(path , "/io/arex/inst/ArexTest.class");
            List<File> files = Arrays.asList(file1, file2, file3);
            zipFile = createFile(path, "/arex-agent-test.jar");
            zipExtensionFile = createFile(path + "/extensions/", "/arex-extension-test.jar");
            createZip(zipFile, files);
            createZip(zipExtensionFile, files);
            file1.deleteOnExit();
            file2.deleteOnExit();
            file3.deleteOnExit();
        } catch (IOException e) {
            assert file1 != null;
            file1.deleteOnExit();
            assert file2 != null;
            file2.deleteOnExit();
            assert file3 != null;
            file3.deleteOnExit();
            assert zipFile != null;
            zipFile.deleteOnExit();
            assert zipExtensionFile != null;
            zipExtensionFile.deleteOnExit();
        }
    }

    public static File getZipFile() {
        return zipFile;
    }

    public static File getZipExtensionFile() {
        return zipExtensionFile;
    }

    private static File createFile(String path, String name) {
        try {
            File file = new File(path + name);
            if (!file.getParentFile().exists()) {
                boolean mkdirs = file.getParentFile().mkdirs();
            }
            boolean newFile = file.createNewFile();
            return file;
        } catch (Throwable ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private static void createZip(File zipFile, List<File> files) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFile.toPath()))) {

            for (File file : files) {
                if (!file.exists()) {
                    throw new FileNotFoundException("File Not Found For To Do Zip File! Please Provide A File..");
                }
                FileInputStream fis = new FileInputStream(file);
                ZipEntry zipEntry;
                String absolutePath = file.getAbsolutePath();
                if (absolutePath.contains("arex") || absolutePath.contains("extensions")) {
                    int index = absolutePath.indexOf("io");
                    if (index >= 0) {
                        String substring = absolutePath.substring(index);
                        String replace = substring.replace("\\", "/");
                        zipEntry = new ZipEntry(replace);
                    } else {
                        zipEntry = new ZipEntry(file.getName());
                    }
                } else {
                    zipEntry = new ZipEntry(file.getName());
                }
                zos.putNextEntry(zipEntry);

                byte[] buffer = new byte[1024];
                int len;
                while ((len = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }
                zos.closeEntry();
                fis.close();
            }
        }
    }


}
