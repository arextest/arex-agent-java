package io.arex.agent.bootstrap.util;

import java.io.FileNotFoundException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;

import io.arex.agent.bootstrap.CreateFileCommon;

import static org.junit.jupiter.api.Assertions.*;

class FileUtilsTest {
    @AfterAll
    static void afterAll() {
        Mockito.clearAllCaches();
    }
    // 1. directory not exit
    // 2. directory is not directory
    // 3. directory is a directory, but cannot list files
    // 4. directory is a directory, files can be listed, but file deletion fails
    // 5. directory is a directory, you can list files, delete files successfully
    @Test
    void cleanDirectory() throws IOException {
        final File file = Mockito.mock(File.class);
        // 1. directory not exit
        Mockito.when(file.exists()).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> {
            FileUtils.cleanDirectory(file);
        });
        // 2. directory is not directory
        Mockito.when(file.exists()).thenReturn(true);
        Mockito.when(file.isDirectory()).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> {
            FileUtils.cleanDirectory(file);
        });
        // 3. directory is a directory, but cannot list files
        Mockito.when(file.exists()).thenReturn(true);
        Mockito.when(file.isDirectory()).thenReturn(true);
        Mockito.when(file.listFiles()).thenReturn(null);
        assertThrows(IOException.class, () -> {
            FileUtils.cleanDirectory(file);
        });
        // 4. directory is a directory, files can be listed, but file deletion fails
        Mockito.when(file.exists()).thenReturn(true);
        Mockito.when(file.isDirectory()).thenReturn(true);
        final File file1 = Mockito.mock(File.class);
        File[] files = new File[1];
        files[0] = file1;
        Mockito.when(file1.exists()).thenReturn(true);
        Mockito.when(file1.delete()).thenReturn(false);
        Mockito.when(file.listFiles()).thenReturn(files);
        assertThrows(IOException.class, () -> FileUtils.cleanDirectory(file));
        // 5. directory is a directory, you can list files, delete files successfully
        final File file2 = CreateFileCommon.createFile("/testclean/test2.txt");
        final File directory = file2.getParentFile();
        FileUtils.cleanDirectory(directory);
        assertFalse(file2.exists());
    }

    @Test
    void forceDelete() throws IOException {
        final File fileMock = Mockito.mock(File.class);
        // 1. file not exit
        Mockito.when(fileMock.exists()).thenReturn(false);
        Mockito.when(fileMock.isDirectory()).thenReturn(false);
        assertThrows(FileNotFoundException.class, () -> {
            FileUtils.forceDelete(fileMock);
        });
        // 2. file exit，delete fail
        Mockito.when(fileMock.exists()).thenReturn(true);
        Mockito.when(fileMock.delete()).thenReturn(false);
        assertThrows(IOException.class, () -> {
            FileUtils.forceDelete(fileMock);
        });
        // 3. file exit，delete success
        Mockito.when(fileMock.exists()).thenReturn(true);
        Mockito.when(fileMock.delete()).thenReturn(true);
        FileUtils.forceDelete(fileMock);
        // 4. file is directory
        Mockito.when(fileMock.isDirectory()).thenReturn(true);
        assertThrows(IOException.class, () -> FileUtils.forceDelete(fileMock));
    }

    // deleteDirectory
    @Test
    void deleteDirectory() throws IOException {
        final File fileMock = Mockito.mock(File.class);
        Mockito.when(fileMock.exists()).thenReturn(true);
        Mockito.when(fileMock.isDirectory()).thenReturn(true);
        Mockito.when(fileMock.listFiles()).thenReturn(new File[0]);
        Mockito.when(fileMock.delete()).thenReturn(false);
        assertThrows(IOException.class, () ->FileUtils.deleteDirectory(fileMock));
        Mockito.when(fileMock.delete()).thenReturn(true);
        assertDoesNotThrow(() -> FileUtils.deleteDirectory(fileMock));
   }



}