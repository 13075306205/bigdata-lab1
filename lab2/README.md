# 实验二：文档倒排索引算法实现

本目录使用 Hadoop MapReduce 对 10 个英文技术文档建立倒排索引。Map 阶段读取每个单词所属的文件名，输出 `<word, document,count>`；Reduce 阶段汇总同一个单词在不同文档中的次数，并追加 `<total,total-count>`。

## 文件说明

- `InvertedIndex.java`：倒排索引 MapReduce 程序。
- `prepare_documents.py`：从本机 Apache Hadoop 3.5.0 英文官方文档生成 10 个英文输入文档。
- `data/`：`prepare_documents.py` 生成的 `document01.txt` 到 `document10.txt`。
- `stopwords.txt`：常用英文停词表。
- `run_lab2.sh`：编译、上传 HDFS、提交作业并导出结果。

## 输入数据

输入来自 Apache Hadoop 3.5.0 发行包中的英文官方文档，主题包括 MapReduce Tutorial、HDFS Design、FileSystem Shell、Cluster Setup、Single Cluster、Commands Manual、HDFS User Guide、DistCp、Hadoop Streaming 和 Capacity Scheduler。执行 `prepare_documents.py` 后，文档会保存为 `data/document01.txt` 到 `data/document10.txt`。

## 输出格式

每一行的键是单词，值是该单词的倒排链，例如：

```text
hadoop <document01.txt,10>;<document03.txt,5>;<total,15>.
```

倒排链按文档编号排序，末尾的 `<total,n>` 表示该单词在全部 10 个文档中的总出现次数。

## 运行

先启动 Hadoop，再在当前目录执行：

```bash
zsh run_lab2.sh
```

脚本会生成英文文档、编译 Java 程序、上传 `data` 和停词表到 `/lab2/input`，提交 MapReduce 作业到 YARN，最后把结果保存到 `output_local/inverted_index_result.txt`。
