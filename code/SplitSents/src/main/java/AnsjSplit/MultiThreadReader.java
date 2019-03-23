package AnsjSplit;
import java.nio.channels.*;
import java.nio.ByteBuffer;
import java.io.*;

public class MultiThreadReader implements Runnable{
    private FileChannel channel;
    private long startIndex;
    private long rSize;
    private FileLineDataHandler dataProcessHandler;//数据处理接口
    private int bufSize = 1024;//缓冲区大小,默认为1024
    private int threadNo ;//记录下线程号

    public MultiThreadReader(FileChannel channel,long startIndex,long rSize,DataProcessHandler dph,int tNo,String save_file,String userdict_file){
        this.channel = channel;
        this.startIndex = startIndex > 0?startIndex - 1:startIndex;
        this.rSize = rSize;
        try {
            File temp_file = new File("SplitTempFile");
            if(!temp_file.exists()){
                temp_file.mkdirs();
            }
            this.dataProcessHandler = new FileLineDataHandler(userdict_file, "SplitTempFile/"+tNo+".csv");
        }catch (Exception e) {
            System.out.println("0000");
            e.printStackTrace();
        }
        this.threadNo=tNo;
    }

    public void run(){
        readByLine();
    }

    /**
     * 按行读取文件实现逻辑
     * @return
     */
    public void readByLine(){
        try {
            ByteBuffer rbuf = ByteBuffer.allocate(bufSize);
            channel.position(startIndex);//设置读取文件的起始位置
            long endIndex = startIndex + rSize;//读取文件数据的结束位置
            byte[] temp = new byte[0];//用来缓存上次读取剩下的部分
            int LF = "\n".getBytes()[0];//换行符
            boolean isEnd = false;//用于判断数据是否读取完
            boolean isWholeLine = false;//用于判断第一行读取到的是否是完整的一行
            long lineCount = 0;//行数统计
            long endLineIndex = startIndex;//当前处理字节所在位置
            while(channel.read(rbuf) != -1 && !isEnd){
                int position = rbuf.position();
                byte[] rbyte = new byte[position];
                rbuf.flip();
                rbuf.get(rbyte);
                int startnum = 0;//每行的起始位置下标，相对于当前所读取到的byte数组
                //判断是否有换行符
                //如果读取到最后一行不是完整的一行时，则继续往后读取直至读取到完整的一行才结束
                for(int i = 0; i < rbyte.length; i++){
                    endLineIndex++;
                    if(rbyte[i] == LF){//若存在换行符
                        if(channel.position() == startIndex){//若改数据片段第一个字节为换行符，说明第一行读取到的是完整的一行
                            isWholeLine = true;
                            startnum = i + 1;
                        }else{
                            byte[] line = new byte[temp.length + i - startnum + 1];
                            System.arraycopy(temp, 0, line, 0, temp.length);
                            System.arraycopy(rbyte, startnum, line, temp.length, i - startnum + 1);
                            startnum = i + 1;
                            lineCount++;
                            temp = new byte[0];
                            //处理数据
                            if(startIndex != 0){//如果不是第一个数据段
                                if(lineCount == 1){
                                    if(isWholeLine){//当且仅当第一行为完整行时才处理
                                        dataProcessHandler.process(line);
                                    }
                                }else{
                                    dataProcessHandler.process(line);
                                }
                            }else{
                                dataProcessHandler.process(line);
                            }
                            //结束读取的判断
                            if(endLineIndex >= endIndex){
                                isEnd = true;
                                break;
                            }
                        }
                    }
                }
                if(!isEnd && startnum < rbyte.length){//说明rbyte最后还剩不完整的一行
                    byte[] temp2 = new byte[temp.length + rbyte.length - startnum];
                    System.arraycopy(temp, 0, temp2, 0, temp.length);
                    System.arraycopy(rbyte, startnum, temp2, temp.length, rbyte.length - startnum);
                    temp = temp2;
                }
                rbuf.clear();
            }
            //兼容最后一行没有换行的情况
            if(temp.length > 0){
                if(dataProcessHandler != null){
                    dataProcessHandler.process(temp);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            try {
                channel.close();
                dataProcessHandler.bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
