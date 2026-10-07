import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class ParentGrand {
    public static class FamilyMapper extends Mapper<Object, Text, Text, Text> {
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            String[] a = value.toString().trim().split("\\s+");
            if (a.length == 2 && !a[0].equals("child")) {
                context.write(new Text(a[0]), new Text("P," + a[1]));
                context.write(new Text(a[1]), new Text("C," + a[0]));
            }
        }
    }

    public static class FamilyReducer extends Reducer<Text, Text, Text, Text> {
        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            java.util.List<String> parents = new java.util.ArrayList<>();
            java.util.List<String> children = new java.util.ArrayList<>();
            for (Text v : values) {
                String[] a = v.toString().split(",", 2);
                if (a[0].equals("P")) parents.add(a[1]);
                else children.add(a[1]);
            }
            for (String child : children) {
                for (String parent : parents) {
                    context.write(new Text(child), new Text(parent));
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "find grandchildren and grandparents");
        job.setJarByClass(ParentGrand.class);
        job.setMapperClass(FamilyMapper.class);
        job.setReducerClass(FamilyReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
