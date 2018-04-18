package com.tiho.multidex;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.zip.ZipFile;

import dalvik.system.DexFile;

/**
 * 加载额外的dex
 * Created by Jerry on 2016/8/25.
 */
public class MultiDex {
    public static final String TAG = "Shell";
    private static final String CODE_CACHE_NAME = "dex_cache";


    //MultiDex.install(this, "dlShootCore","base2","dlShootCore.dat");
    public static void install(Context context, String assetsName,String sourceName,String saveName) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        try {
            // data/data/包名/dex_cache
            File dexOutputFile = getDexDir(context, applicationInfo);
            // data/data/包名/files/shootDl/base2
            File sourceFile = MultiDexHelper.getSourceFile(context,assetsName,sourceName,saveName);
            //将shootDl/base2文件中的classes.dex解压缩到dex_cache文件夹的base2.dex
            List<File> files = MultiDexExtractor.load(sourceFile, dexOutputFile);
            installSecondaryDexes(context.getClassLoader(), dexOutputFile, files);
            deleteFile(sourceFile,files);
        } catch (IOException | InvocationTargetException | NoSuchMethodException | NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    private static void deleteFile(File sourceFile,List<File> files){
        for(File file:files){
            if(!file.delete()){
                Log.e(TAG, "Failed to delete to "+file.getPath());
            }
        }
        if(!sourceFile.delete()){
            Log.e(TAG, "Failed to delete to "+sourceFile.getPath());
        }
    }




    private static File getDexDir(Context context, ApplicationInfo applicationInfo)
            throws IOException {
        File dexDir = new File(applicationInfo.dataDir, CODE_CACHE_NAME);
        try {
            mkdirChecked(dexDir);
        } catch (IOException e) {
            /* If we can't emulate code_cache, then store to filesDir. This means abandoning useless
             * files on disk if the device ever updates to android 5+. But since this seems to
             * happen only on some devices running android 2, this should cause no pollution.
             */
            dexDir = new File(context.getFilesDir(), CODE_CACHE_NAME);
            mkdirChecked(dexDir);
        }
        return dexDir;
    }

    private static void mkdirChecked(File dir) throws IOException {
        dir.mkdir();
        if (!dir.isDirectory()) {
            File parent = dir.getParentFile();
            if (parent == null) {
                Log.e(TAG, "Failed to create dir " + dir.getPath() + ". Parent file is null.");
            } else {
                Log.e(TAG, "Failed to create dir " + dir.getPath() +
                        ". parent file is a dir " + parent.isDirectory() +
                        ", a file " + parent.isFile() +
                        ", exists " + parent.exists() +
                        ", readable " + parent.canRead() +
                        ", writable " + parent.canWrite());
            }
            throw new IOException("Failed to create directory " + dir.getPath());
        }
    }

    public static void installSecondaryDexes(ClassLoader loader, File dexDir, List<File> files)
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException,
            InvocationTargetException, NoSuchMethodException, IOException {
        if (!files.isEmpty()) {
            if (Build.VERSION.SDK_INT >= 23) {
                V23.install(loader, files, dexDir);
            } else if (Build.VERSION.SDK_INT >= 19) {
                V19.install(loader, files, dexDir);
            } else if (Build.VERSION.SDK_INT >= 14) {
                V14.install(loader, files, dexDir);
            } else {
                V4.install(loader, files);
            }
        }
    }

    /**
     * Locates a given field anywhere in the class inheritance hierarchy.
     *
     * @param instance an object to search the field into.
     * @param name     field name
     * @return a field object
     * @throws NoSuchFieldException if the field cannot be located
     */
    private static Field findField(Object instance, String name) throws NoSuchFieldException {
        for (Class<?> clazz = instance.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            try {
                Field field = clazz.getDeclaredField(name);
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }

                return field;
            } catch (NoSuchFieldException e) {
                // ignore and search next
            }
        }

        throw new NoSuchFieldException("Field " + name + " not found in " + instance.getClass());
    }

    /**
     * Locates a given method anywhere in the class inheritance hierarchy.
     *
     * @param instance       an object to search the method into.
     * @param name           method name
     * @param parameterTypes method parameter types
     * @return a method object
     * @throws NoSuchMethodException if the method cannot be located
     */
    private static Method findMethod(Object instance, String name, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        for (Class<?> clazz = instance.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            try {
                Method method = clazz.getDeclaredMethod(name, parameterTypes);


                if (!method.isAccessible()) {
                    method.setAccessible(true);
                }

                return method;
            } catch (NoSuchMethodException e) {
                // ignore and search next
            }
        }

        throw new NoSuchMethodException("Method " + name + " with parameters " +
                Arrays.asList(parameterTypes) + " not found in " + instance.getClass());
    }

    /**
     * Replace the value of a field containing a non null array, by a new array containing the
     * elements of the original array plus the elements of extraElements.
     *
     * @param instance      the instance whose field is to be modified.
     * @param fieldName     the field to modify.
     * @param extraElements elements to append at the end of the array.
     */
    private static void expandFieldArray(Object instance, String fieldName,
                                         Object[] extraElements) throws NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException {
        Field jlrField = findField(instance, fieldName);
        Object[] original = (Object[]) jlrField.get(instance);
        Object[] combined = (Object[]) Array.newInstance(
                original.getClass().getComponentType(), original.length + extraElements.length);
        System.arraycopy(original, 0, combined, 0, original.length);
        System.arraycopy(extraElements, 0, combined, original.length, extraElements.length);
        jlrField.set(instance, combined);
    }


    /**
     * Installer for platform versions 23.
     */
    private static final class V23 {

        private static void install(ClassLoader loader, List<File> additionalClassPathEntries,
                                    File optimizedDirectory)
                throws IllegalArgumentException, IllegalAccessException,
                NoSuchFieldException, InvocationTargetException, NoSuchMethodException {
            /* The patched class loader is expected to be a descendant of
             * dalvik.system.BaseDexClassLoader. We modify its
             * dalvik.system.DexPathList pathList field to append additional DEX
             * file entries.
             */
            Field pathListField = findField(loader, "pathList");
            Object dexPathList = pathListField.get(loader);
            ArrayList<IOException> suppressedExceptions = new ArrayList<IOException>();
            expandFieldArray(dexPathList, "dexElements", makeDexElements(dexPathList,
                    new ArrayList<File>(additionalClassPathEntries), optimizedDirectory,
                    suppressedExceptions));
            if (suppressedExceptions.size() > 0) {
                for (IOException e : suppressedExceptions) {
                    Log.w(TAG, "Exception in makeDexElement", e);
                }
                Field suppressedExceptionsField =
                        findField(dexPathList, "dexElementsSuppressedExceptions");
                IOException[] dexElementsSuppressedExceptions =
                        (IOException[]) suppressedExceptionsField.get(dexPathList);

                if (dexElementsSuppressedExceptions == null) {
                    dexElementsSuppressedExceptions =
                            suppressedExceptions.toArray(
                                    new IOException[suppressedExceptions.size()]);
                } else {
                    IOException[] combined =
                            new IOException[suppressedExceptions.size() +
                                    dexElementsSuppressedExceptions.length];
                    suppressedExceptions.toArray(combined);
                    System.arraycopy(dexElementsSuppressedExceptions, 0, combined,
                            suppressedExceptions.size(), dexElementsSuppressedExceptions.length);
                    dexElementsSuppressedExceptions = combined;
                }

                suppressedExceptionsField.set(dexPathList, dexElementsSuppressedExceptions);
            }
        }

        /**
         * A wrapper around
         * {@code private static final dalvik.system.DexPathList#makeDexElements}.
         */
        private static Object[] makeDexElements(
                Object dexPathList, ArrayList<File> files, File optimizedDirectory,
                ArrayList<IOException> suppressedExceptions)
                throws IllegalAccessException, InvocationTargetException,
                NoSuchMethodException {
            Method makeDexElements =
                    findMethod(dexPathList, "makePathElements", List.class, File.class,
                            List.class);

            return (Object[]) makeDexElements.invoke(dexPathList, files, optimizedDirectory,
                    suppressedExceptions);
        }
    }

    /**
     * Installer for platform versions 19.
     */
    private static final class V19 {

        private static void install(ClassLoader loader, List<File> additionalClassPathEntries,
                                    File optimizedDirectory)
                throws IllegalArgumentException, IllegalAccessException,
                NoSuchFieldException, InvocationTargetException, NoSuchMethodException {
            /* The patched class loader is expected to be a descendant of
             * dalvik.system.BaseDexClassLoader. We modify its
             * dalvik.system.DexPathList pathList field to append additional DEX
             * file entries.
             */
            Field pathListField = findField(loader, "pathList");
            Object dexPathList = pathListField.get(loader);
            ArrayList<IOException> suppressedExceptions = new ArrayList<IOException>();
            expandFieldArray(dexPathList, "dexElements", makeDexElements(dexPathList,
                    new ArrayList<File>(additionalClassPathEntries), optimizedDirectory,
                    suppressedExceptions));
            if (suppressedExceptions.size() > 0) {
                for (IOException e : suppressedExceptions) {
                    Log.w(TAG, "Exception in makeDexElement", e);
                }
                Field suppressedExceptionsField =
                        findField(dexPathList, "dexElementsSuppressedExceptions");
                IOException[] dexElementsSuppressedExceptions =
                        (IOException[]) suppressedExceptionsField.get(dexPathList);

                if (dexElementsSuppressedExceptions == null) {
                    dexElementsSuppressedExceptions =
                            suppressedExceptions.toArray(
                                    new IOException[suppressedExceptions.size()]);
                } else {
                    IOException[] combined =
                            new IOException[suppressedExceptions.size() +
                                    dexElementsSuppressedExceptions.length];
                    suppressedExceptions.toArray(combined);
                    System.arraycopy(dexElementsSuppressedExceptions, 0, combined,
                            suppressedExceptions.size(), dexElementsSuppressedExceptions.length);
                    dexElementsSuppressedExceptions = combined;
                }

                suppressedExceptionsField.set(dexPathList, dexElementsSuppressedExceptions);
            }
        }

        /**
         * A wrapper around
         * {@code private static final dalvik.system.DexPathList#makeDexElements}.
         */
        private static Object[] makeDexElements(
                Object dexPathList, ArrayList<File> files, File optimizedDirectory,
                ArrayList<IOException> suppressedExceptions)
                throws IllegalAccessException, InvocationTargetException,
                NoSuchMethodException {
            Method makeDexElements =
                    findMethod(dexPathList, "makeDexElements", ArrayList.class, File.class,
                            ArrayList.class);

            return (Object[]) makeDexElements.invoke(dexPathList, files, optimizedDirectory,
                    suppressedExceptions);
        }
    }

    /**
     * Installer for platform versions 14, 15, 16, 17 and 18.
     */
    private static final class V14 {

        private static void install(ClassLoader loader, List<File> additionalClassPathEntries,
                                    File optimizedDirectory)
                throws IllegalArgumentException, IllegalAccessException,
                NoSuchFieldException, InvocationTargetException, NoSuchMethodException {
            /* The patched class loader is expected to be a descendant of
             * dalvik.system.BaseDexClassLoader. We modify its
             * dalvik.system.DexPathList pathList field to append additional DEX
             * file entries.
             */
            Field pathListField = findField(loader, "pathList");
            Object dexPathList = pathListField.get(loader);
            expandFieldArray(dexPathList, "dexElements", makeDexElements(dexPathList,
                    new ArrayList<File>(additionalClassPathEntries), optimizedDirectory));
        }

        /**
         * A wrapper around
         * {@code private static final dalvik.system.DexPathList#makeDexElements}.
         */
        private static Object[] makeDexElements(
                Object dexPathList, ArrayList<File> files, File optimizedDirectory)
                throws IllegalAccessException, InvocationTargetException,
                NoSuchMethodException {
            Method makeDexElements =
                    findMethod(dexPathList, "makeDexElements", ArrayList.class, File.class);

            return (Object[]) makeDexElements.invoke(dexPathList, files, optimizedDirectory);
        }
    }

    /**
     * Installer for platform versions 4 to 13.
     */
    private static final class V4 {
        private static void install(ClassLoader loader, List<File> additionalClassPathEntries)
                throws IllegalArgumentException, IllegalAccessException,
                NoSuchFieldException, IOException {
            /* The patched class loader is expected to be a descendant of
             * dalvik.system.DexClassLoader. We modify its
             * fields mPaths, mFiles, mZips and mDexs to append additional DEX
             * file entries.
             */
            int extraSize = additionalClassPathEntries.size();
            Field pathField = findField(loader, "path");
            StringBuilder path = new StringBuilder((String) pathField.get(loader));
            String[] extraPaths = new String[extraSize];
            File[] extraFiles = new File[extraSize];
            ZipFile[] extraZips = new ZipFile[extraSize];
            DexFile[] extraDexs = new DexFile[extraSize];
            for (ListIterator<File> iterator = additionalClassPathEntries.listIterator();
                 iterator.hasNext(); ) {
                File additionalEntry = iterator.next();
                String entryPath = additionalEntry.getAbsolutePath();
                path.append(':').append(entryPath);
                int index = iterator.previousIndex();
                extraPaths[index] = entryPath;
                extraFiles[index] = additionalEntry;
                extraZips[index] = new ZipFile(additionalEntry);
                extraDexs[index] = DexFile.loadDex(entryPath, entryPath + ".dex", 0);
            }
            pathField.set(loader, path.toString());
            expandFieldArray(loader, "mPaths", extraPaths);
            expandFieldArray(loader, "mFiles", extraFiles);
            expandFieldArray(loader, "mZips", extraZips);
            expandFieldArray(loader, "mDexs", extraDexs);
        }
    }
}
