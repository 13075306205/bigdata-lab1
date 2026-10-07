# 实验一：大数据系统基本实验

这是人工智能学院大数据系统课程的实验一代码，主要完成了 Linux 基本操作、HDFS Shell 操作、HDFS Java API，以及三个 MapReduce 程序。

## 文件说明

- `src/HdfsLab.java`：HDFS Java API 综合实验，包含上传、下载、查看、创建、删除、追加、移动等功能。
- `src/MergeDedup.java`：合并多个输入文件并去重。
- `src/SortNums.java`：对数字进行排序，并输出序号。
- `src/ParentGrand.java`：根据 child-parent 表推断 grandchild-grandparent 关系。
- `data/`：实验使用的简单测试数据。

## 编译

先启动 Hadoop，然后在项目目录执行：

```bash
mkdir -p classes
javac -cp "$(hadoop classpath)" -d classes src/*.java
jar cf lab1.jar -C classes .
```

## 运行

### 1. HDFS Java API

```bash
java -cp "$(hadoop classpath):classes" HdfsLab
```

运行后按照菜单输入编号和路径即可。

### 2. 合并去重

```bash
hadoop fs -put data/merge1.txt /lab1/input
hadoop fs -put data/merge2.txt /lab1/input
hadoop jar lab1.jar MergeDedup /lab1/input/merge1.txt /lab1/input/merge2.txt /lab1/output_merge
hadoop fs -cat /lab1/output_merge/part-r-00000
```

### 3. 数字排序

```bash
hadoop fs -put data/nums.txt /lab1/input
hadoop jar lab1.jar SortNums /lab1/input/nums.txt /lab1/output_sort
hadoop fs -cat /lab1/output_sort/part-r-00000
```

### 4. 单表联系

```bash
hadoop fs -put data/family.txt /lab1/input
hadoop jar lab1.jar ParentGrand /lab1/input/family.txt /lab1/output_family
hadoop fs -cat /lab1/output_family/part-r-00000
```

注意：每次重新运行同一个 MapReduce 程序前，输出目录不能已经存在，可以先删除旧的输出目录。
