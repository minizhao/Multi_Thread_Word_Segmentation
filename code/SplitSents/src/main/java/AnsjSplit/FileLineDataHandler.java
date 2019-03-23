package AnsjSplit;
import java.io.FileWriter;

import org.ansj.domain.*;
import org.ansj.library.AmbiguityLibrary;
import org.ansj.splitWord.impl.GetWordsImpl;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.util.*;


public class FileLineDataHandler extends DataProcessHandler {

//    private static  DicAnalysis dicAnalysis ;
    private static Forest myforest;
    private static String userdict_file;
    private static String save_file;
    public   BufferedWriter bw;
    private  GetWordsImpl gwi ;

    protected org.nlpcn.commons.lang.tire.domain.Forest[] forests = null;

    private org.nlpcn.commons.lang.tire.domain.Forest ambiguityForest = AmbiguityLibrary.get();


    public  FileLineDataHandler(String usf,String sf) throws Exception {
        this.userdict_file=usf;
        this.save_file=sf;

        load_user_dict();
        open_writer();
    }

    public  void open_writer() throws Exception {
         bw=new BufferedWriter(new FileWriter(save_file, true));;
    }

    public static void load_user_dict() throws Exception {
        myforest = Library.makeForest(userdict_file);
    }

    public  void seg_sent (String sent) {

        try {
            Result result =DicAnalysis.parse(sent,myforest);
            String sent_splited="";
            List<Term> terms = result.getTerms();
            for(int i=0; i<terms.size(); i++) {
                String word = terms.get(i).getName(); //拿到词
                if (i==terms.size()-1){
                    sent_splited+=word;
                }
                else{
                    sent_splited+=word+" ";
                }
            }
            WriteString(sent_splited);

        }
        catch (Exception e) {
            System.out.println("---");
        }

//        System.out.print(sent_splited);
//        System.exit(0);

    }

    public  void WriteString(String save_line) throws Exception{
        try {
            bw.write(save_line);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private String encode = "utf-8";
    @Override
    public void process(byte[] data){
        try {
            seg_sent(new String(data,encode));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
