package edu.stanford.rad.svm;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.mahout.common.Pair;
import org.apache.mahout.common.iterator.sequencefile.SequenceFileIterable;

public class ExtractTopFeatures {
	
	public static Map<String, Integer> readDictionnary(Configuration conf, Path dictionnaryPath) {
		Map<String, Integer> dictionnary = new HashMap<String, Integer>();
		for (Pair<Text, IntWritable> pair : new SequenceFileIterable<Text, IntWritable>(dictionnaryPath, true, conf)) {
			dictionnary.put(pair.getFirst().toString(), pair.getSecond().get());
		}
		return dictionnary;
	}
	
	public static void main(String[] args) throws Exception {
		int topk=50, counter=0;
		String dictionaryPath = "/Users/saeedhp/Dropbox/Stanford/Code/CorpusExtraction/files/stride/abdomenCT/fullCorpus/vectors/TFIDFsparseSeqdir/dictionary.file-0";
		String inputFile = "/Users/saeedhp/Dropbox/Stanford/Tools/libsvm-3.20/tools/trainr3-3-10.txt.fscore";
		String outputFileName = inputFile + "-top" + topk + "terms.txt";
		
		Configuration configuration = new Configuration();
		Map<String, Integer> dictionary = readDictionnary(configuration, new Path(dictionaryPath));
		
		BufferedReader reader = new BufferedReader(new FileReader(inputFile));
		PrintWriter pw = new PrintWriter(outputFileName, "UTF-8");
		
		
		while (true) {
			++counter;
			String line = reader.readLine();
			if (line == null || topk <counter ) {
				break;
			}

			String[] tokens = line.split(":\\s+");
			int featureId = Integer.parseInt(tokens[0]);
			double weight = Double.parseDouble(tokens[1]);

			String term = "-";
			for (Entry<String, Integer> entry : dictionary.entrySet()) {
				if (entry.getValue() == featureId) {
					term = entry.getKey();
					break;
				}
			}
			
			pw.printf("%d\t%s\t%f\n", featureId, term, weight);
		}
		pw.close();
		reader.close();
	}
	
}
