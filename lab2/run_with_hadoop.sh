#!/bin/zsh
set -e

ROOT="$HOME/Documents/ChatGPT/大数据管理分析实验"
source "$ROOT/hadoop_lab/env.sh"

"$ROOT/hadoop_lab/start_hadoop.sh"
"$HADOOP_HOME/bin/mapred" --daemon start historyserver

hdfs_ready=false
yarn_ready=false
for i in {1..30}; do
  if [[ "$hdfs_ready" != true ]] && hdfs dfsadmin -report 2>/dev/null | grep -q 'Live datanodes (1)'; then
    hdfs_ready=true
  fi
  if [[ "$yarn_ready" != true ]] && yarn node -list -all 2>/dev/null | grep -q 'RUNNING'; then
    yarn_ready=true
  fi
  [[ "$hdfs_ready" == true && "$yarn_ready" == true ]] && break
  sleep 1
done

if [[ "$hdfs_ready" != true || "$yarn_ready" != true ]]; then
  echo "HDFS or YARN was not ready." >&2
  exit 1
fi

"$ROOT/lab2/run_lab2.sh"
mkdir -p "$ROOT/lab2/figures"
NODE_PATH="$HOME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules" \
  "$HOME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/bin/node" \
  "$ROOT/lab2/capture_ui.js"

APP_ID=$(rg -o 'application_[0-9]+_[0-9]+' "$ROOT/lab2/output_local/inverted_index_run.log" | tail -1)
yarn application -status "$APP_ID" | tee "$ROOT/lab2/output_local/yarn_app_status.txt"

"$HADOOP_HOME/bin/mapred" --daemon stop historyserver
"$ROOT/hadoop_lab/stop_hadoop.sh"
