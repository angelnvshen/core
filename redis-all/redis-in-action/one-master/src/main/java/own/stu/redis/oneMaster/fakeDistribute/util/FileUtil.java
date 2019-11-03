package own.stu.redis.oneMaster.fakeDistribute.util;

import org.springframework.util.Assert;
import own.stu.redis.oneMaster.fakeDistribute.service.DistributeService;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * 文件处理辅助类
 */
public class FileUtil {

    /**
     * 当前目录路径
     */
    public static String currentWorkDir = System.getProperty("user.dir") + "/";

    /**
     * 左填充
     *
     * @param str
     * @param length
     * @param ch
     * @return
     */
    public static String leftPad(String str, int length, char ch) {
        if (str.length() >= length) {
            return str;
        }
        char[] chs = new char[length];
        Arrays.fill(chs, ch);
        char[] src = str.toCharArray();
        System.arraycopy(src, 0, chs, length - src.length, src.length);
        return new String(chs);

    }

    /**
     * 删除文件
     *
     * @param fileName 待删除的完整文件名
     * @return
     */
    public static boolean delete(String fileName) {
        boolean result = false;
        File f = new File(fileName);
        if (f.exists()) {
            result = f.delete();

        } else {
            result = true;
        }
        return result;
    }

    /***
     * 递归获取指定目录下的所有的文件（不包括文件夹）
     *
     * @param dirPath
     * @return
     */
    public static ArrayList<File> getAllFiles(String dirPath) {
        File dir = new File(dirPath);

        ArrayList<File> files = new ArrayList<File>();

        if (dir.isDirectory()) {
            File[] fileArr = dir.listFiles();
            for (int i = 0; i < fileArr.length; i++) {
                File f = fileArr[i];
                if (f.isFile()) {
                    files.add(f);
                } else {
                    files.addAll(getAllFiles(f.getPath()));
                }
            }
        }
        return files;
    }

    /**
     * 获取指定目录下的所有文件(不包括子文件夹)
     *
     * @param dirPath
     * @return
     */
    public static ArrayList<File> getDirFiles(String dirPath) {
        File path = new File(dirPath);
        File[] fileArr = path.listFiles();
        ArrayList<File> files = new ArrayList<File>();

        for (File f : fileArr) {
            if (f.isFile()) {
                files.add(f);
            }
        }
        return files;
    }

    /**
     * 获取指定目录下特定文件后缀名的文件列表(不包括子文件夹)
     *
     * @param dirPath 目录路径
     * @param suffix  文件后缀
     * @return
     */
    public static ArrayList<File> getDirFiles(String dirPath,
                                              final String suffix) {
        File path = new File(dirPath);
        File[] fileArr = path.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                String lowerName = name.toLowerCase();
                String lowerSuffix = suffix.toLowerCase();
                if (lowerName.endsWith(lowerSuffix)) {
                    return true;
                }
                return false;
            }

        });
        ArrayList<File> files = new ArrayList<File>();

        for (File f : fileArr) {
            if (f.isFile()) {
                files.add(f);
            }
        }
        return files;
    }

    /**
     * 读取文件内容
     *
     * @param fileName 待读取的完整文件名
     * @return 文件内容
     * @throws IOException
     */
    public static String read(String fileName) throws IOException {
        File f = new File(fileName);
        FileInputStream fs = new FileInputStream(f);
        String result = null;
        byte[] b = new byte[fs.available()];
        fs.read(b);
        fs.close();
        result = new String(b);
        return result;
    }

    public static String getSimpleFileName(String fileName) {
        Assert.notNull(fileName, "fileName is null");
        int index = fileName.lastIndexOf(File.separatorChar);
        return fileName.substring(index + 1);
    }

    public static String writeTempFile(String fileName, byte[] fileContent) {
        Assert.notNull(fileName, "fileName is null");
        int dotIndex = fileName.lastIndexOf(".");
        String prefix = fileName.substring(0, dotIndex);
        String suffix = fileName.substring(dotIndex);

        File file = null;
        FileOutputStream fs = null;
        try {
            file = File.createTempFile(prefix, suffix);
            fs = new FileOutputStream(file);
            fs.write(fileContent);
            fs.flush();
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (fs != null) {
                try {
                    fs.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 追加内容到指定文件
     *
     * @param fileName
     * @param fileContent
     * @return
     * @throws IOException
     */
    public static boolean append(String fileName, String fileContent)
            throws IOException {
        boolean result = false;
        File f = new File(fileName);
        if (f.exists()) {
            RandomAccessFile rFile = new RandomAccessFile(f, "rw");
            byte[] b = fileContent.getBytes();
            long originLen = f.length();
            rFile.setLength(originLen + b.length);
            rFile.seek(originLen);
            rFile.write(b);
            rFile.close();
        }
        result = true;
        return result;
    }

    /**
     * 拆分文件
     *
     * @param fileName 待拆分的完整文件名
     * @param byteSize 按多少字节大小拆分
     * @return 拆分后的文件名列表
     * @throws IOException
     */
    public List<String> splitBySize(String fileName, int byteSize)
            throws IOException {
        List<String> parts = new ArrayList<String>();
        File file = new File(fileName);
        int count = (int) Math.ceil(file.length() / (double) byteSize);
        int countLen = (count + "").length();
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(count,
                count * 3, 1, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(count * 2));

        for (int i = 0; i < count; i++) {
            String partFileName = file.getName() + "."
                    + leftPad((i + 1) + "", countLen, '0') + ".part";
            threadPool.execute(new SplitRunnable(byteSize, i * byteSize,
                    partFileName, file));
            parts.add(partFileName);
        }
        return parts;
    }

    /**
     * 合并文件
     *
     * @param dirPath        拆分文件所在目录名
     * @param partFileSuffix 拆分文件后缀名
     * @param partFileSize   拆分文件的字节数大小
     * @param mergeFileName  合并后的文件名
     * @throws IOException
     */
    public void mergePartFiles(String dirPath, String partFileSuffix,
                               int partFileSize, String mergeFileName) throws IOException {
        ArrayList<File> partFiles = FileUtil.getDirFiles(dirPath,
                partFileSuffix);
        Collections.sort(partFiles, new FileComparator());

        RandomAccessFile randomAccessFile = new RandomAccessFile(mergeFileName,
                "rw");
        randomAccessFile.setLength(partFileSize * (partFiles.size() - 1)
                + partFiles.get(partFiles.size() - 1).length());
        randomAccessFile.close();

        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
                partFiles.size(), partFiles.size() * 3, 1, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(partFiles.size() * 2));

        for (int i = 0; i < partFiles.size(); i++) {
            threadPool.execute(new MergeRunnable(i * partFileSize,
                    mergeFileName, partFiles.get(i)));
        }

    }

    /**
     * 根据文件名，比较文件
     *
     * @author yjmyzz@126.com
     */
    public static class FileComparator implements Comparator<File> {
        public int compare(File o1, File o2) {
            return o1.getName().compareToIgnoreCase(o2.getName());
        }
    }

    /**
     * 分割处理Runnable
     *
     * @author yjmyzz@126.com
     */
    public static class SplitRunnable implements Runnable {
        int byteSize;
        String partFileName;
        File originFile;
        int startPos;

        public SplitRunnable(int byteSize, int startPos, String partFileName, File originFile) {
            this.startPos = startPos;
            this.byteSize = byteSize;
            this.partFileName = partFileName;
            this.originFile = originFile;
        }

        public void run() {

            try (
                    RandomAccessFile rFile = new RandomAccessFile(originFile, "r");
                    OutputStream os = new FileOutputStream(partFileName)
            ) {
                byte[] b = new byte[byteSize];
                rFile.seek(startPos);// 移动指针到每“段”开头
                int s = rFile.read(b);
                os.write(b, 0, s);
                os.flush();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 合并处理Runnable
     *
     * @author yjmyzz@126.com
     */
    public static class MergeRunnable implements Runnable {
        long startPos;
        String mergeFileName;
        File partFile;

        public MergeRunnable(long startPos, String mergeFileName, File partFile) {
            this.startPos = startPos;
            this.mergeFileName = mergeFileName;
            this.partFile = partFile;
        }

        public void run() {

            try (
                    RandomAccessFile rFile = new RandomAccessFile(mergeFileName, "rw");
                    FileInputStream fs = new FileInputStream(partFile);
            ) {

                rFile.seek(startPos);
                byte[] b = new byte[fs.available()];
                fs.read(b);
                rFile.write(b);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                deleteIfExisted(partFile.getName());
            }
        }
    }

    public static void deleteIfExisted(String fileName) {
        File file = new File(fileName);
        deleteIfExisted(file);
    }

    public static void deleteIfExisted(File file) {

        // 路径为文件且不为空则进行删除
        if (file.isFile() && file.exists())
            file.delete();// 文件删除
    }

    public static void deleteIfExisted(List<String> fileName) {
        for (String s : fileName) {
            deleteIfExisted(s);
        }
    }
}