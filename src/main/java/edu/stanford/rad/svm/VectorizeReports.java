package edu.stanford.rad.svm;

import java.io.File;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.Writer;
import org.apache.hadoop.io.Text;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
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

public class VectorizeReports {
	
	
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
		String tag = "test3";
		String trainortest = "testSets";
		String dictionaryPath = "/Users/saeedhp/Dropbox/Stanford/Code/CorpusExtraction/files/stride/abdomenCT/fullCorpus/vectors/TFIDFsparseSeqdir/dictionary.file-0";
		String documentFrequencyPath = "/Users/saeedhp/Dropbox/Stanford/Code/CorpusExtraction/files/stride/abdomenCT/fullCorpus/vectors/TFIDFsparseSeqdir/df-count/part-r-00000";
		//String inputPath = "/Users/saeedhp/Dropbox/Stanford/Code/CorpusExtraction/files/stride/abdomenCT/test1/"+tag+"/positive";
		//String outputFileName = "/Users/saeedhp/Dropbox/Stanford/Code/CorpusExtraction/files/stride/abdomenCT/test1/"+tag+"/vector";
		
		//Final Training
		String inputPath = "/Users/saeedhp/Dropbox/Stanford/Code/CorpusExtraction/files/stride/abdomenCT/corpus/"+trainortest +"/"+tag;
		String outputFileName = "/Users/saeedhp/Dropbox/Stanford/Code/CorpusExtraction/files/stride/abdomenCT/corpus/vectors/vector-"+tag;
		
		//Full evaluation
		//String inputPath = "/Users/saeedhp/Dropbox/Stanford/Code/CorpusExtraction/files/stride/abdomenCT/test1";
		//String outputFileName = "/Users/saeedhp/Dropbox/Stanford/Code/CorpusExtraction/files/stride/abdomenCT/vector";

		
		Configuration configuration = new Configuration();
		FileSystem fs = FileSystem.get(configuration);

		Map<String, Integer> dictionary = readDictionnary(configuration, new Path(dictionaryPath));
		Map<Integer, Long> documentFrequency = readDocumentFrequency(configuration, new Path(documentFrequencyPath));
		int documentCount = documentFrequency.get(-1).intValue();

		Writer writer = new SequenceFile.Writer(fs, configuration, new Path(outputFileName), Text.class, VectorWritable.class);
		Text key = new Text();
		VectorWritable value = new VectorWritable();


		int counter = 0;
		final File folder = new File(inputPath);
		List<File> files = (List<File>) FileUtils.listFiles(folder, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
		for (File fileEntry : files) {
			if (!fileEntry.isDirectory() && !fileEntry.getName().startsWith(".")  && !fileEntry.getName().equals("vector")) {
				++counter;
				String fileName = fileEntry.getName();
				String filepath = fileEntry.getPath();
				System.out.println("Working on " + fileName + "...");
				Scanner scanner = new Scanner(new File(filepath), "UTF-8");
				String text = scanner.useDelimiter("\\Z").next();
				scanner.close();
				
				String sig = filepath.substring(filepath.lastIndexOf("/", filepath.lastIndexOf("/")-1));
				key.set(sig);
				Multiset<String> words = ConcurrentHashMultiset.create();

				
				
				// extract words from input
				//Reader reader = new StringReader("This IS saeedhp@gmail.com 01/01/2012 a test string");
				//TokenStream tokenizer = new StandardTokenizer(Version.LUCENE_46, reader);
				TokenStream tokenizer = new StandardTokenizer(Version.LUCENE_46, new StringReader(text));
				tokenizer = new LowerCaseFilter(Version.LUCENE_46, tokenizer);
				//tokenizer = new PorterStemFilter(tokenizer);
				//tokenizer = new StandardFilter(Version.LUCENE_46, tokenizer);
				tokenizer = new ShingleFilter(tokenizer, 2, 3);
				CharTermAttribute charTermAttribute = tokenizer.addAttribute(CharTermAttribute.class);
				tokenizer.reset();
				int wordCount = 0;
				
				while (tokenizer.incrementToken()) {
					if (charTermAttribute.length() > 0) {
						String word = charTermAttribute.toString();
						Integer wordId = dictionary.get(word);
						// if the word is not in the dictionary, skip it
						if (wordId != null) {
							words.add(word);
							wordCount++;
						}
					}
				}
				tokenizer.end();
				tokenizer.close();
				
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
		writer.close();
		System.out.println("Files: " + counter);
	}
	
}
