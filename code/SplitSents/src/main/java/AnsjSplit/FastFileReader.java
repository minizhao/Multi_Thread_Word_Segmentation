package AnsjSplit;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.*;
import java.nio.channels.*;

public class FastFileReader {
    private int threadNum = 3;//线程数,默认为3
    private String filePath;//文件路径
    private int bufSize = 1024;//缓冲区大小,默认为1024
    private DataProcessHandler dataProcessHandler;//数据处理接口
    private ExecutorService threadPool;
    private String save_file;
    private String userdict_file;
    private String[] files;

//    构造函数
    public FastFileReader(String filePath, int bufSize, int threadNum,String save_file,String userdict_file){
        this.threadNum = threadNum;
        this.bufSize = bufSize;
        this.filePath = filePath;
        this.threadPool = Executors.newFixedThreadPool(threadNum);
        this.save_file = save_file;
        this.userdict_file = userdict_file;
        this.files=new String[threadNum];
    }

    public void startRead() throws Exception{
        FileChannel infile = null;
        long startTime = System.currentTimeMillis();

        try {
            RandomAccessFile raf = new RandomAccessFile(filePath,"r");
            infile = raf.getChannel();
            long size = infile.size();
            long subSize = size/threadNum;
            for(int i = 0; i < threadNum; i++){
                long startIndex = i*subSize;

                if(size%threadNum > 0 && i == threadNum - 1){
                    subSize += size%threadNum;
                }
                RandomAccessFile accessFile = new RandomAccessFile(filePath,"r");
                FileChannel inch = accessFile.getChannel();
                threadPool.execute(new MultiThreadReader(inch,startIndex,subSize,this.dataProcessHandler,i,this.save_file,userdict_file));
                this.files[i]="SplitTempFile/"+i+".csv";
            }
            threadPool.shutdown();
            while (true) {
                if (threadPool.isTerminated()) {
                    long time = System.currentTimeMillis() - startTime;
                    System.out.println("分词结束总耗时：" + time + " ms(毫秒)");
                    System.out.println("开始合并文件，请等待");
                    MergeDelFile.mergeFiles(save_file,this.files);
                    System.out.println("完成合并文件,运行结束");
                    MergeDelFile.deleteDirectory("SplitTempFile");
                    break;
                }
            }

        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            try {
                if(infile != null){
                    infile.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
