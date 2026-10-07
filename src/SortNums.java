import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class SortNums {
    public static class NumMapper extends Mapper<Object, Text, IntWritable, NullWritable> {
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            String s = value.toString().trim();
            if (!s.isEmpty()) context.write(new IntWritable(Integer.parseInt(s)), NullWritable.get());
        }
    }

    public static class NumReducer extends Reducer<IntWritable, NullWritable, Text, NullWritable> {
        private int rank = 0;
        public void reduce(IntWritable key, Iterable<NullWritable> values, Context context) throws IOException, InterruptedException {
            context.write(new Text((++rank) + " " + key.get()), NullWritable.get());
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "number sort");
        job.setJarByClass(SortNums.class);
        job.setMapperClass(NumMapper.class);
        job.setReducerClass(NumReducer.class);
        job.setMapOutputKeyClass(IntWritable.class);
        job.setMapOutputValueClass(NullWritable.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(NullWritable.class);
        job.setNumReduceTasks(1);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
