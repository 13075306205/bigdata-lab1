import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class InvertedIndex {
    private static final String WORD_PATTERN = "[^A-Za-z0-9']+";

    public static class IndexMapper extends Mapper<Object, Text, Text, Text> {
        private final Map<String, Boolean> stopwords = new HashMap<>();
        private final Text word = new Text();
        private final Text posting = new Text();

        @Override
        protected void setup(Context context) throws IOException {
            URI[] cacheFiles = context.getCacheFiles();
            if (cacheFiles == null || cacheFiles.length == 0) {
                throw new IOException("The stop-word file was not distributed to this task.");
            }

            URI uri = cacheFiles[0];
            String localName = uri.getFragment() == null
                    ? new Path(uri.getPath()).getName()
                    : uri.getFragment();
            for (String line : Files.readAllLines(Paths.get(localName))) {
                String term = line.trim().toLowerCase();
                if (!term.isEmpty()) {
                    stopwords.put(term, true);
                }
            }
        }

        @Override
        public void map(Object key, Text value, Context context)
                throws IOException, InterruptedException {
            FileSplit split = (FileSplit) context.getInputSplit();
            String document = split.getPath().getName();
            String lowerLine = value.toString().toLowerCase();

            for (String rawTerm : lowerLine.split(WORD_PATTERN)) {
                if (rawTerm.isEmpty() || stopwords.containsKey(rawTerm)) {
                    continue;
                }
                word.set(rawTerm);
                posting.set(document + ",1");
                context.write(word, posting);
            }
        }
    }

    public static class IndexReducer extends Reducer<Text, Text, Text, Text> {
        private final Text result = new Text();

        @Override
        public void reduce(Text key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {
            Map<String, Integer> counts = new TreeMap<>();
            for (Text value : values) {
                String[] parts = value.toString().split(",", 2);
                if (parts.length != 2) {
                    continue;
                }
                counts.merge(parts[0], Integer.parseInt(parts[1]), Integer::sum);
            }

            if (counts.isEmpty()) {
                return;
            }

            int total = 0;
            StringBuilder text = new StringBuilder();
            for (Map.Entry<String, Integer> entry : counts.entrySet()) {
                total += entry.getValue();
                if (text.length() > 0) {
                    text.append(";");
                }
                text.append("<").append(entry.getKey()).append(",").append(entry.getValue()).append(">");
            }
            text.append(";<total,").append(total).append(">.");
            result.set(text.toString());
            context.write(key, result);
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: InvertedIndex <input dir> <output dir>");
            System.exit(2);
        }

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "inverted index");
        job.setJarByClass(InvertedIndex.class);
        job.setMapperClass(IndexMapper.class);
        job.setReducerClass(IndexReducer.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.addCacheFile(new URI("/lab2/input/stopwords.txt#stopwords.txt"));
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
