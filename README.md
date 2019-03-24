# Multi_Thread_Word_Segmentation

## java 实现对超大文本文件多线程自定义词典优先快速分词

## jar包运行：
参数说明
args:<br>
  arg1：待分词数据文件（内容一句话为一行）<br>
  arg2：用户自定义字典（要求字典词汇尽可能多和全,一行为一个词）<br>
  arg3：线程数（整数，多线程并行数）<br>
  arg4：输出文件（字符串）<br>
  
## 运行示例：<br>
java -jar SplitSents-1.0-SNAPSHOT.jar org.txt word_list.txt 10 splited.txt


## 分词效果：<br>
算法根据用户字典为首要条件，尽可能分出词汇表中包含的词，减少OOV词汇

## 引用：<br>
项目核心分词逻辑借用AnsjSplit实现

## 效率：<br>
处理1个G的数据文件大约需要2分钟左右
