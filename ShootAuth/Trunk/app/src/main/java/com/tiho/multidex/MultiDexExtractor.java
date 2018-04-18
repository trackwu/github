package com.tiho.multidex;

import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * 解压dex文件
 * Created by Jerry on 2016/8/25.
 */
public class MultiDexExtractor {
    private static final String TAG =  MultiDex.TAG;
    private static final String EXTRACTED_SUFFIX = ".zip";
    private static final String LOCK_FILENAME = "MultiDex.lock";
    private static final int BUFFER_SIZE = 0x4000;


    static List<File> load(File sourceApk, File dexDir) throws IOException {
        //// data/data/包名/dex_cache/MultiDex.lock
        File lockFile = new File(dexDir, LOCK_FILENAME);
        RandomAccessFile lockRaf = new RandomAccessFile(lockFile, "rw");
        FileChannel lockChannel = null;
        FileLock cacheLock = null;
        List<File> files;
        IOException releaseLockException = null;
        try {
            lockChannel = lockRaf.getChannel();
            Log.i(TAG, "Blocking on lock " + lockFile.getPath());
            cacheLock = lockChannel.lock();
            Log.i(TAG, lockFile.getPath() + " locked");
            Log.i(TAG, "Detected that extraction must be performed.");
            files = performExtractions(sourceApk, dexDir);
        } finally {
            if (cacheLock != null) {
                try {
                    cacheLock.release();
                } catch (IOException e) {
                    Log.e(TAG, "Failed to release lock on " + lockFile.getPath());
                    // Exception while releasing the lock is bad, we want to report it, but not at
                    // the price of overriding any already pending exception.
                    releaseLockException = e;
                }
            }
            if (lockChannel != null) {
                closeQuietly(lockChannel);
            }
            closeQuietly(lockRaf);
        }

        if (releaseLockException != null) {
            throw releaseLockException;
        }
        Log.i(TAG, "load found " + files.size() + " secondary dex files");
        return files;
    }


    private static List<File> performExtractions(File sourceApk, File dexDir)
            throws IOException {

        final String extractedFilePrefix = sourceApk.getName();

        // Ensure that whatever deletions happen in prepareDexDir only happen if the zip that
        // contains a secondary dex file in there is not consistent with the latest apk.  Otherwise,
        // multi-process race conditions can cause a crash loop where one process deletes the zip
        // while another had created it.
        prepareDexDir(dexDir, extractedFilePrefix);

        List<File> files = new ArrayList<>();

        final ZipFile apk = new ZipFile(sourceApk);
        try {
            ZipEntry dexFile = apk.getEntry("classes.dex");
            if (dexFile != null) {
                String fileName = extractedFilePrefix+ EXTRACTED_SUFFIX;
                File extractedFile = new File(dexDir, fileName);
                files.add(extractedFile);

                Log.i(TAG, "Extraction is needed for file " + extractedFile);
                boolean isExtractionSuccessful = false;
                // Create a zip file (extractedFile) containing only the secondary dex file
                // (dexFile) from the apk.
                extract(apk, dexFile, extractedFile, extractedFilePrefix);

                // Verify that the extracted file is indeed a zip file.
                isExtractionSuccessful = verifyZipFile(extractedFile);

                // Log the sha1 of the extracted zip file
                Log.i(TAG, "Extraction " + (isExtractionSuccessful ? "success" : "failed") +
                        " - length " + extractedFile.getAbsolutePath() + ": " +
                        extractedFile.length());
                if (!isExtractionSuccessful) {
                    // Delete the extracted file
                    extractedFile.delete();
                    if (extractedFile.exists()) {
                        Log.w(TAG, "Failed to delete corrupted secondary dex '" +
                                extractedFile.getPath() + "'");
                    }
                }
                if (!isExtractionSuccessful) {
                    throw new IOException("Could not create zip file " +
                            extractedFile.getAbsolutePath());
                }
            }
        } finally {
            try {
                apk.close();
            } catch (IOException e) {
                Log.w(TAG, "Failed to close resource", e);
            }
        }
        return files;
    }

    /**
     * This removes old files.
     */
    private static void prepareDexDir(File dexDir, final String extractedFilePrefix) {
        FileFilter filter = new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                String name = pathname.getName();
                return !(name.startsWith(extractedFilePrefix)
                        || name.equals(LOCK_FILENAME));
            }
        };
        File[] files = dexDir.listFiles(filter);
        if (files == null) {
            Log.w(TAG, "Failed to list secondary dex dir content (" + dexDir.getPath() + ").");
            return;
        }
        for (File oldFile : files) {
            Log.i(TAG, "Trying to delete old file " + oldFile.getPath() + " of size " +
                    oldFile.length());
            if (!oldFile.delete()) {
                Log.w(TAG, "Failed to delete old file " + oldFile.getPath());
            } else {
                Log.i(TAG, "Deleted old file " + oldFile.getPath());
            }
        }
    }

    private static void extract(ZipFile apk, ZipEntry dexFile, File extractTo,
                                String extractedFilePrefix) throws IOException, FileNotFoundException {

        InputStream in = apk.getInputStream(dexFile);
        ZipOutputStream out = null;
        File tmp = File.createTempFile(extractedFilePrefix, EXTRACTED_SUFFIX,
                extractTo.getParentFile());
        Log.i(TAG, "Extracting " + tmp.getPath());
        try {
            out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(tmp)));
            try {
                ZipEntry classesDex = new ZipEntry("classes.dex");
                // keep zip entry time since it is the criteria used by Dalvik
                classesDex.setTime(dexFile.getTime());
                out.putNextEntry(classesDex);

                byte[] buffer = new byte[BUFFER_SIZE];
                int length = in.read(buffer);
                while (length != -1) {
                    out.write(buffer, 0, length);
                    length = in.read(buffer);
                }
                out.closeEntry();
            } finally {
                out.close();
            }
            Log.i(TAG, "Renaming to " + extractTo.getPath());
            if (!tmp.renameTo(extractTo)) {
                throw new IOException("Failed to rename \"" + tmp.getAbsolutePath() +
                        "\" to \"" + extractTo.getAbsolutePath() + "\"");
            }
        } finally {
            closeQuietly(in);
            tmp.delete(); // return status ignored
        }
    }

    private static void closeQuietly(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
            Log.w(TAG, "Failed to close resource", e);
        }
    }

    private static boolean verifyZipFile(File file) {
        try {
            ZipFile zipFile = new ZipFile(file);
            try {
                zipFile.close();
                return true;
            } catch (IOException e) {
                Log.w(TAG, "Failed to close zip file: " + file.getAbsolutePath());
            }
        } catch (ZipException ex) {
            Log.w(TAG, "File " + file.getAbsolutePath() + " is not a valid zip file.", ex);
        } catch (IOException ex) {
            Log.w(TAG, "Got an IOException trying to open zip file: " + file.getAbsolutePath(), ex);
        }
        return false;
    }

}
