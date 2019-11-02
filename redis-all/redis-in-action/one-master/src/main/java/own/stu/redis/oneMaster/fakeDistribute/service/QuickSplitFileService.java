package own.stu.redis.oneMaster.fakeDistribute.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import own.stu.redis.oneMaster.fakeDistribute.service.bean.MergeRunnable;
import own.stu.redis.oneMaster.fakeDistribute.service.bean.SplitRunnable;
import own.stu.redis.oneMaster.fakeDistribute.service.inner.W2wzServerImpl;
import own.stu.redis.oneMaster.fakeDistribute.util.FileUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import static own.stu.redis.oneMaster.fakeDistribute.util.FileUtil.leftPad;

/**
 * 快速切割文件，并上传到指定服务器，根据返回的url，合并为新得种子文件。
 * <p>
 * // TODO fake image
 */
@Slf4j
@Service
public class QuickSplitFileService {

    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    public QuickSplitFileService(ThreadPoolExecutor threadPoolExecutor) {
        this.threadPoolExecutor = threadPoolExecutor;
    }

    /**
     * 拆分文件
     *
     * @param fileName 待拆分的完整文件名
     * @param byteSize 按多少字节大小拆分
     * @return 拆分后的文件名列表
     */
    public List<String> splitBySize(String fileName, int byteSize) {

        List<String> parts = new ArrayList<>();

        File file = new File(fileName);
        int count = (int) ((file.length() + byteSize - 1) / byteSize);
        int countLen = (count + "").length();

        for (int i = 0; i < count; i++) {
            String partFileName = file.getName() + "."
                    + leftPad((i + 1) + "", countLen, '0') + ".jpg";
            threadPoolExecutor.execute(new SplitRunnable(byteSize, i * byteSize,
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
        ArrayList<File> partFiles = FileUtil.getDirFiles(dirPath, partFileSuffix);
        Collections.sort(partFiles, new FileUtil.FileComparator());

        RandomAccessFile randomAccessFile = new RandomAccessFile(mergeFileName, "rw");
        randomAccessFile.setLength(partFileSize * (partFiles.size() - 1)
                + partFiles.get(partFiles.size() - 1).length());
        randomAccessFile.close();

        for (int i = 0; i < partFiles.size(); i++) {
            threadPoolExecutor.execute(
                    new MergeRunnable(i * partFileSize, mergeFileName, partFiles.get(i)));
        }

    }

    public void mergePartFiles(List<String> fileNameList,
                               int partFileSize, String mergeFileName) throws IOException {

    }


    public void saveToRemote() {
//        restTemplate.p
    }
}