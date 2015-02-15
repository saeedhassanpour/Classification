package edu.stanford.rad.naivebayes;

import java.io.File;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.Writer;
import org.apache.hadoop.io.Text;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;
import org.apache.mahout.common.Pair;
import org.apache.mahout.common.iterator.sequencefile.SequenceFileIterable;
import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.VectorWritable;
import org.apache.mahout.vectorizer.TFIDF;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.Multiset;

public class VectorizeTestFiles {
	public static Map<String, Integer> readDictionnary(Configuration conf,
			Path dictionnaryPath) {
		Map<String, Integer> dictionnary = new HashMap<String, Integer>();
		for (Pair<Text, IntWritable> pair : new SequenceFileIterable<Text, IntWritable>(
				dictionnaryPath, true, conf)) {
			dictionnary.put(pair.getFirst().toString(), pair.getSecond().get());
		}
		return dictionnary;
	}

	public static Map<Integer, Long> readDocumentFrequency(Configuration conf,
			Path documentFrequencyPath) {
		Map<Integer, Long> documentFrequency = new HashMap<Integer, Long>();
		for (Pair<IntWritable, LongWritable> pair : new SequenceFileIterable<IntWritable, LongWritable>(
				documentFrequencyPath, true, conf)) {
			documentFrequency
					.put(pair.getFirst().get(), pair.getSecond().get());
		}
		return documentFrequency;
	}

	public static void main(String[] args) throws Exception {

		String dictionaryPath = "/Users/saeedhp/Dropbox/Stanford/Code/CorpusExtraction/files/stride/abdomenCT/vectors/TFIDFsparseSeqdir/dictionary.file-0";
		String documentFrequencyPath = "/Users/saeedhp/Dropbox/Stanford/Code/CorpusExtraction/files/stride/abdomenCT/vectors/TFIDFsparseSeqdir/df-count/part-r-00000";
		String inputPath = "/Users/saeedhp/Dropbox/Stanford/Code/CorpusExtraction/files/stride/abdomenCT/corpus/test/positive";
		String outputFileName = "/Users/saeedhp/Desktop/tmp/a.txt";

		Configuration configuration = new Configuration();
		FileSystem fs = FileSystem.get(configuration);

		Map<String, Integer> dictionary = readDictionnary(configuration, new Path(dictionaryPath));
		Map<Integer, Long> documentFrequency = readDocumentFrequency(configuration, new Path(documentFrequencyPath));
		int documentCount = documentFrequency.get(-1).intValue();

		Writer writer = new SequenceFile.Writer(fs, configuration, new Path(outputFileName), Text.class, VectorWritable.class);
		Text key = new Text();
		VectorWritable value = new VectorWritable();

		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_46);

		final File folder = new File(inputPath);
		for (final File fileEntry : folder.listFiles()) {
			if (!fileEntry.isDirectory() && !fileEntry.getName().startsWith(".")) {
				String fileName = fileEntry.getName();
				String filepath = fileEntry.getPath();
				System.out.println("Working on " + fileName + "...");
				Scanner scanner = new Scanner(new File(filepath), "UTF-8");
				String text = scanner.useDelimiter("\\Z").next();
				scanner.close();

				key.set(fileName);
				Multiset<String> words = ConcurrentHashMultiset.create();

				// extract words from input
				TokenStream ts = analyzer.tokenStream("text", new StringReader(text));
				CharTermAttribute termAtt = ts.addAttribute(CharTermAttribute.class);
				ts.reset();
				int wordCount = 0;
				while (ts.incrementToken()) {
					if (termAtt.length() > 0) {
						String word = ts.getAttribute(CharTermAttribute.class).toString();
						Integer wordId = dictionary.get(word);
						// if the word is not in the dictionary, skip it
						if (wordId != null) {
							words.add(word);
							wordCount++;
						}
					}
				}
				ts.end();
				ts.close();
				
				// create vector wordId => weight using tfidftmp
				Vector vector = new RandomAccessSparseVector(10000);
				TFIDF tfidf = new TFIDF();
				for (Multiset.Entry<String> entry : words.entrySet()) {
					String word = entry.getElement();
					int count = entry.getCount();
					Integer wordId = dictionary.get(word);
					// if the word is not in the dictionary, skip it
					Long freq = documentFrequency.get(wordId);
					double tfIdfValue = tfidf.calculate(count, freq.intValue(), wordCount, documentCount);
					vector.setQuick(wordId, tfIdfValue);
				}
				value.set(vector);

				writer.append(key, value);
			}
		}
		analyzer.close();
		writer.close();
	}
}
