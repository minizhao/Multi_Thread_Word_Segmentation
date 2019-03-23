package AnsjSplit;

import org.ansj.domain.Result;
import org.ansj.domain.Term;
import java.util.*;
import java.io.*;

import org.ansj.splitWord.analysis.ToAnalysis;

public class SplitSent {
    private static String data_file;
    private static String userdict_file;
    private static String save_file;
    private static String orguserdict_file;
    private static String threadNum;


//    分词算法测试函数
    public static void ansj_seg()  throws Exception {
        long startTime = System.currentTimeMillis();

        Forest forest = Library.makeForest(userdict_file);
        BufferedReader reader_dict = new BufferedReader(new FileReader(userdict_file));
        Set<String> userWordsSet = new HashSet<String>();
        try {
            String tempString = null;
            while ((tempString = reader_dict.readLine()) != null) {
                String[] tempList=tempString.split("\t");
                userWordsSet.add(tempList[0]);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader_dict != null) {
                try {
                    reader_dict.close();
                } catch (IOException e1) {
                }
            }
        }

        try (FileReader reader = new FileReader("/Users/zhao/Desktop/sub_news.txt");
             BufferedReader br = new BufferedReader(reader)) {
            String line;
            int count=0;
//            定义文本中词的集合
            Set<String> wordsSet = new HashSet<String>();
            while ((line = br.readLine()) != null) {
//                line="jerryschwartz132520汽车城搭架子广而告之";

                Result result =DicAnalysis.parse(line,forest);

//                Result result = ToAnalysis.parse(line,forest);
                List<Term> terms = result.getTerms(); //拿到terms

                System.out.println(terms);
//                System.exit(0);
//
//                result = ToAnalysis.parse(line);
//                terms = result.getTerms(); //拿到terms
//                System.out.println(terms);
//
//                System.exit(0);
                String unKown="";
                for(int i=0;i<terms.size();i++){
                    String word = terms.get(i).getName(); //拿到词
                    if (!userWordsSet.contains(word)){
                        unKown+=word+"--";
                    }
                    wordsSet.add(word);
                }
//                System.out.println(line);
//                System.out.println(unKown);
            }

            System.out.println(userWordsSet.size());
            System.out.println(wordsSet.size());
            wordsSet.removeAll(userWordsSet);
            System.out.println(wordsSet.size());

//            for (String str : wordsSet) {
//                System.out.println(str);
//            }




        }catch(IOException e){
                e.printStackTrace();
            }
        long endTime = System.currentTimeMillis();
        System.out.println("程序运行时间：" + (endTime - startTime) + "ms");


    }

//  生成输出用户字典
    public static void gen_user_dict() throws Exception{

//      读入原始词典文件，每行是一个词
        File file = new File(orguserdict_file);
        BufferedReader reader = null;

//      写入生成词典文件
        FileOutputStream fos=new FileOutputStream(new File(userdict_file));
        OutputStreamWriter osw=new OutputStreamWriter(fos, "UTF-8");
        BufferedWriter  bw=new BufferedWriter(osw);

        try {
//            System.out.println("以行为单位读取文件内容，一次读一整行：");
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                // 显示行号
                Result result = ToAnalysis.parse(tempString);

                List<Term> terms = result.getTerms(); //拿到terms
                String word = terms.get(0).getName(); //拿到词
                String natureStr = terms.get(0).getNatureStr(); //拿到词性
                bw.write(tempString+"\t"+natureStr+"\t"+100000000+"\n");
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
            bw.close();
            osw.close();
            fos.close();
        }
    }

//    多进程处理
    public static void ansj_seg_multi() throws Exception{
        FastFileReader fileReader = new FastFileReader(data_file,3000,Integer.valueOf(threadNum),save_file,userdict_file);
        System.out.println(userdict_file);
//        fileReader.registerHanlder(new FileLineDataHandler(userdict_file,save_file));
        fileReader.startRead();
    }


    public static void main(String[] args)  throws Exception {
//        data_file="/Users/zhao/Desktop/sub_news_2w.txt";
        userdict_file="userLibrary.dic";
//        save_file="splited_news.txt";
//        ansj_seg();
        try {
            data_file=args[0]; //第一个参数，带分词文件
            orguserdict_file=args[1];//第二个参数，用于自定义字典
            threadNum=args[2];//第三个参数，输出文件
            save_file=args[3];//第四个参数，线程数
        }
        catch (Exception e){
            System.out.println("参数错误（arg1:带分词文件，arg2:用于自定义字典，arg3:线程数，arg4:，输出文件）");
            System.exit(0);
        }

        gen_user_dict();
//
        ansj_seg_multi();

//       删除生成的词典为你文件
        MergeDelFile.deleteFile(userdict_file);

    }


}
