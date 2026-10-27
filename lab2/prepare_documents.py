#!/usr/bin/env python3
"""Extract plain English text from the English Hadoop documentation pages."""

from html.parser import HTMLParser
from pathlib import Path


HADOOP_DOCS = Path("/opt/homebrew/Cellar/hadoop/3.5.0/libexec/share/doc/hadoop")
OUTPUT = Path(__file__).resolve().parent / "data"

SOURCES = [
    "hadoop-mapreduce-client/hadoop-mapreduce-client-core/MapReduceTutorial.html",
    "hadoop-project-dist/hadoop-hdfs/HdfsDesign.html",
    "hadoop-project-dist/hadoop-common/FileSystemShell.html",
    "hadoop-project-dist/hadoop-common/ClusterSetup.html",
    "hadoop-project-dist/hadoop-common/SingleCluster.html",
    "hadoop-project-dist/hadoop-common/CommandsManual.html",
    "hadoop-project-dist/hadoop-hdfs/HdfsUserGuide.html",
    "hadoop-distcp/DistCp.html",
    "hadoop-streaming/HadoopStreaming.html",
    "hadoop-yarn/hadoop-yarn-site/CapacityScheduler.html",
]


class TextExtractor(HTMLParser):
    skip_depth = 0
    pieces = []

    def handle_starttag(self, tag, attrs):
        if tag in {"script", "style"}:
            self.skip_depth += 1
        elif tag in {"p", "br", "div", "li", "h1", "h2", "h3", "h4", "tr"}:
            self.pieces.append("\n")

    def handle_endtag(self, tag):
        if tag in {"script", "style"} and self.skip_depth:
            self.skip_depth -= 1

    def handle_data(self, data):
        if self.skip_depth == 0 and data.strip():
            self.pieces.append(data.strip() + " ")


def extract(source: Path) -> str:
    parser = TextExtractor()
    parser.pieces = []
    parser.feed(source.read_text(encoding="utf-8", errors="ignore"))
    lines = []
    for raw_line in "".join(parser.pieces).splitlines():
        words = raw_line.split()
        if words:
            lines.append(" ".join(words))
    return "\n".join(lines) + "\n"


def main():
    OUTPUT.mkdir(exist_ok=True)
    for index, relative_path in enumerate(SOURCES, 1):
        source = HADOOP_DOCS / relative_path
        target = OUTPUT / f"document{index:02d}.txt"
        target.write_text(extract(source), encoding="utf-8")
        print(f"{target.name}: {source}")


if __name__ == "__main__":
    main()
