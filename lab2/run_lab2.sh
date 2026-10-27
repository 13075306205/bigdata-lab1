#!/bin/zsh
set -e

source "$HOME/Documents/ChatGPT/大数据管理分析实验/hadoop_lab/env.sh"

LAB_DIR="$HOME/Documents/ChatGPT/大数据管理分析实验/lab2"
HDFS_INPUT=/lab2/input
HDFS_OUTPUT=/lab2/output
LOCAL_OUTPUT="$LAB_DIR/output_local"

mkdir -p "$LOCAL_OUTPUT"
"$LAB_DIR/prepare_documents.py"

hdfs dfs -mkdir -p "$HDFS_INPUT"
hdfs dfs -put -f "$LAB_DIR/stopwords.txt" "$HDFS_INPUT/stopwords.txt"
hdfs dfs -put -f "$LAB_DIR"/data/document*.txt "$HDFS_INPUT"
hdfs dfs -rm -r -f "$HDFS_OUTPUT"

rm -rf "$LAB_DIR/classes"
mkdir -p "$LAB_DIR/classes"
javac -encoding UTF-8 -cp "$(hadoop classpath)" -d "$LAB_DIR/classes" "$LAB_DIR/InvertedIndex.java"
jar cf "$LAB_DIR/inverted-index.jar" -C "$LAB_DIR/classes" .

"$HADOOP_HOME/bin/hadoop" jar "$LAB_DIR/inverted-index.jar" \
  InvertedIndex "$HDFS_INPUT" "$HDFS_OUTPUT" 2>&1 | \
  tee "$LOCAL_OUTPUT/inverted_index_run.log"

hdfs dfs -cat "$HDFS_OUTPUT/part-r-00000" | tee "$LOCAL_OUTPUT/inverted_index_result.txt"
printf '\n===== HDFS output list =====\n'
hdfs dfs -ls "$HDFS_OUTPUT" | tee "$LOCAL_OUTPUT/hdfs_output_list.txt"
