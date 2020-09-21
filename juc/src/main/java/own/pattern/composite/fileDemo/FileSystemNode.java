package own.pattern.composite.fileDemo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileSystemNode {
    private String path;

    private boolean isFile;

    private List<FileSystemNode> subNodes = new ArrayList<>();

    public FileSystemNode(String path, boolean isFile) {
        this.path = path;
        this.isFile = isFile;
    }

    public long countSizeOfFiles() {
        if (isFile) {
            File file = new File(path);
            if (!file.exists()) return 0;
            return file.length();
        }
        long sizeofFiles = 0;
        for (FileSystemNode fileOrDir : subNodes) {
            sizeofFiles += fileOrDir.countSizeOfFiles();
        }
        return sizeofFiles;
    }

    public String getPath() {
        return path;
    }

    public void addSubNode(FileSystemNode fileOrDir) {
        subNodes.add(fileOrDir);
    }

    public void removeSubNode(FileSystemNode fileOrDir) {

        int size = subNodes.size();
        int i = 0;

        for (; i < size; i++) {
            if (subNodes.get(i).path.equalsIgnoreCase(fileOrDir.getPath())) {
                break;
            }
        }

        if (i < size)
            subNodes.remove(i);
    }

    public int countNumOfFiles() {
        if (isFile)
            return 1;

        int numOfFiles = 0;
        for (FileSystemNode node : subNodes) {
            numOfFiles += node.countNumOfFiles();
        }

        return numOfFiles;
    }

}
