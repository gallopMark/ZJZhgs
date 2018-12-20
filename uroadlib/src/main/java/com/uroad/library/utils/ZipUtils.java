package com.uroad.library.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtils {
    /**
     * 解压zip到指定的路径
     *
     * @param zipFileString ZIP的名称
     * @param outPathString 要解压缩路径
     */
    public static String UnZipFolder(String zipFileString, String outPathString) {
        ZipInputStream inZip = null;
        ZipEntry zipEntry;
        String szName;
        FileOutputStream fos = null;
        try {
            inZip = new ZipInputStream(new FileInputStream(zipFileString));
            while ((zipEntry = inZip.getNextEntry()) != null) {
                szName = zipEntry.getName();
                if (zipEntry.isDirectory()) {
                    //获取部件的文件夹名
                    szName = szName.substring(0, szName.length() - 1);
                    File folder = new File(outPathString + File.separator + szName);
                    folder.mkdirs();
                } else {
                    File file = new File(outPathString + File.separator + szName);
                    if (!file.exists()) {
                        file.getParentFile().mkdirs();
                        file.createNewFile();
                    }
                    // 获取文件的输出流
                    fos = new FileOutputStream(file);
                    int len;
                    byte[] buffer = new byte[1024];
                    // 读取（字节）字节到缓冲区
                    while ((len = inZip.read(buffer)) != -1) {
                        // 从缓冲区（0）位置写入（字节）字节
                        fos.write(buffer, 0, len);
                        fos.flush();
                    }
                    fos.close();
                }
            }
            return outPathString;
        } catch (Exception e) {
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            if (inZip != null) {
                try {
                    inZip.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
        return null;
    }

    public static void UnZipFolder(String zipFileString, String outPathString, String szName) {
        ZipInputStream inZip = null;
        ZipEntry zipEntry;
        FileOutputStream fos = null;
        try {
            inZip = new ZipInputStream(new FileInputStream(zipFileString));
            while ((zipEntry = inZip.getNextEntry()) != null) {
                //szName = zipEntry.getName();
                if (zipEntry.isDirectory()) {
                    //获取部件的文件夹名
                    szName = szName.substring(0, szName.length() - 1);
                    File folder = new File(outPathString + File.separator + szName);
                    folder.mkdirs();
                } else {
                    File file = new File(outPathString + File.separator + szName);
                    if (!file.exists()) {
                        file.getParentFile().mkdirs();
                        file.createNewFile();
                    }
                    // 获取文件的输出流
                    fos = new FileOutputStream(file);
                    int len;
                    byte[] buffer = new byte[1024];
                    // 读取（字节）字节到缓冲区
                    while ((len = inZip.read(buffer)) != -1) {
                        // 从缓冲区（0）位置写入（字节）字节
                        fos.write(buffer, 0, len);
                        fos.flush();
                    }
                    fos.close();
                }
            }
            inZip.close();
        } catch (IOException e) {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (inZip != null) {
                try {
                    inZip.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

    /**
     * 压缩文件和文件夹
     *
     * @param srcFileString 要压缩的文件或文件夹
     * @param zipFileString 解压完成的Zip路径
     */
    public static void ZipFolder(String srcFileString, String zipFileString) {
        ZipOutputStream outZip = null;
        try {
            //创建ZIP
            outZip = new ZipOutputStream(new FileOutputStream(zipFileString));
            //创建文件
            File file = new File(srcFileString);
            //压缩
            ZipFiles(file.getParent() + File.separator, file.getName(), outZip);
            //完成和关闭
            outZip.finish();
            outZip.close();
        } catch (Exception e) {
            if (outZip != null) {
                try {
                    outZip.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    /**
     * 压缩文件
     */
    private static void ZipFiles(String folderString, String fileString, ZipOutputStream zipOutputSteam) {
        if (zipOutputSteam == null)
            return;
        File file = new File(folderString + fileString);
        FileInputStream inputStream = null;
        try {
            if (file.isFile()) {
                ZipEntry zipEntry = new ZipEntry(fileString);
                inputStream = new FileInputStream(file);
                zipOutputSteam.putNextEntry(zipEntry);
                int len;
                byte[] buffer = new byte[4096];
                while ((len = inputStream.read(buffer)) != -1) {
                    zipOutputSteam.write(buffer, 0, len);
                }
                zipOutputSteam.closeEntry();
            } else {
                //文件夹
                String fileList[] = file.list();
                //没有子文件和压缩
                if (fileList.length <= 0) {
                    ZipEntry zipEntry = new ZipEntry(fileString + File.separator);
                    zipOutputSteam.putNextEntry(zipEntry);
                    zipOutputSteam.closeEntry();
                }
                //子文件和递归
                for (String aFileList : fileList) {
                    ZipFiles(folderString, fileString + File.separator + aFileList, zipOutputSteam);
                }
            }
        } catch (Exception e) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    /**
     * 返回zip的文件输入流
     *
     * @param zipFileString zip的名称
     * @param fileString    ZIP的文件名
     * @return InputStream
     */
    public static InputStream UpZip(String zipFileString, String fileString) throws Exception {
        ZipFile zipFile = new ZipFile(zipFileString);
        ZipEntry zipEntry = zipFile.getEntry(fileString);
        return zipFile.getInputStream(zipEntry);
    }

    /**
     * 返回ZIP中的文件列表（文件和文件夹）
     *
     * @param zipFileString  ZIP的名称
     * @param bContainFolder 是否包含文件夹
     * @param bContainFile   是否包含文件
     */
    public static List<File> getFileList(String zipFileString, boolean bContainFolder, boolean bContainFile) throws Exception {
        List<File> fileList = new ArrayList<>();
        ZipInputStream inZip = new ZipInputStream(new FileInputStream(zipFileString));
        ZipEntry zipEntry;
        String szName;
        while ((zipEntry = inZip.getNextEntry()) != null) {
            szName = zipEntry.getName();
            if (zipEntry.isDirectory()) {
                // 获取部件的文件夹名
                szName = szName.substring(0, szName.length() - 1);
                File folder = new File(szName);
                if (bContainFolder) {
                    fileList.add(folder);
                }
            } else {
                File file = new File(szName);
                if (bContainFile) {
                    fileList.add(file);
                }
            }
        }
        inZip.close();
        return fileList;
    }
}