package edu.stanford.rad.svm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EvaluationPerCat {

	public static void main(String[] args) throws IOException {
		String tag = "test1";
		String dataFileFolder = "/Users/saeedhp/Dropbox/Stanford/Tools/libsvm-3.20/Data/abdomenCT/t5/" + tag + "/";
		List<Integer> trueLable = new ArrayList<Integer>();
		List<Integer> prediction = new ArrayList<Integer>();
		List<String> fileName = new ArrayList<String>();

		// Read true labels
		File dataFile = new File(dataFileFolder + "label-" + tag + ".txt");
		BufferedReader bReader = new BufferedReader(new FileReader(dataFile));
		String line;
		while ((line = bReader.readLine()) != null) {
			if (line.isEmpty())
				continue;
			trueLable.add(Integer.parseInt(line));
		}
		bReader.close();

		// Read predictions
		dataFile = new File(dataFileFolder + "output.txt");
		bReader = new BufferedReader(new FileReader(dataFile));
		while ((line = bReader.readLine()) != null) {
			if (line.isEmpty())
				continue;
			prediction.add(Integer.parseInt(line));
		}
		bReader.close();

		// Read filenames
		dataFile = new File(dataFileFolder + "fileNames-" + tag + ".txt");
		bReader = new BufferedReader(new FileReader(dataFile));
		while ((line = bReader.readLine()) != null) {
			if (line.isEmpty())
				continue;
			fileName.add(line);
		}
		bReader.close();

		if (prediction.size() != trueLable.size()) {
			System.out.println("not equal sizes: p:" + prediction.size()
					+ " t:" + trueLable.size());
		} else {
			System.out.println("Evaluation size: " + prediction.size());
		}

		Map<Integer, Set<String>> catPatient = null;
		String outputFolder = "/Users/saeedhp/Dropbox/Stanford/Code/CorpusExtraction/files/stride/abdomenCT/corpus/categoryPatients.ser";

		try {
			FileInputStream fileIn = new FileInputStream(outputFolder);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			catPatient = (Map<Integer, Set<String>>) in.readObject();
			in.close();
			fileIn.close();
		} catch (IOException i) {
			i.printStackTrace();
			return;
		} catch (ClassNotFoundException c) {
			System.out.println("Map class not found");
			c.printStackTrace();
			return;
		}
		
//		for(int k :catPatient.keySet())
//		{
//			System.out.println(k + " : " + catPatient.get(k).size());
//		}
		
		//int sum = 0;
		 PrintWriter pw = new PrintWriter(dataFileFolder + "evaluationPerCat.tsv", "UTF-8");
		 pw.printf("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n", "k", "threshold","size", "accuracy", "precision", "recall", "f1", "sensitivity", "specificity", "tpr", "fpr");
		for(int k :catPatient.keySet())
		{
			System.out.println(k);
			Set<String> currSet = catPatient.get(k);
			List<Integer> currTrueLable = new ArrayList<Integer>();
			List<Integer> currPrediction = new ArrayList<Integer>();
			
			for(int i=0; i<fileName.size(); ++i)
			{
				if(currSet.contains(fileName.get(i)))
				{
					currTrueLable.add(trueLable.get(i));
					currPrediction.add(prediction.get(i));
				}
			}
			
			//sum += currPrediction.size();
			computeMetrics(k, 0, currTrueLable, currPrediction, pw);
			
		}
		pw.close();
		//System.out.println(sum);
	}

	static void computeMetrics(int k, double threshold,
			List<Integer> trueLable, List<Integer> prediction, PrintWriter pw) {
		double tp = 0, fp = 0, tn = 0, fn = 0;
		for (int i = 0; i < prediction.size(); ++i) {
			if (prediction.get(i) == 1) {
				if (trueLable.get(i) == 1) {
					tp++;
				} else {
					fp++;
				}
			} else {
				if (trueLable.get(i) == -1) {
					tn++;
				} else {
					fn++;
				}
			}
		}

		double accuracy = (tp + tn) / (tp + tn + fp + fn);
		double precision = tp / (tp + fp);
		double recall = tp / (tp + fn);
		double f1 = 2 * precision * recall / (precision + recall);
		double sensitivity = recall;
		double specificity = tn / (tn + fp);
		double tpr = recall;
		double fpr = fp / (fp + tn);
		int size = trueLable.size();

		pw.printf("%d\t%.4f\t%d\t%.4f\t%.4f\t%.4f\t%.4f\t%.4f\t%.4f\t%.4f\t%.4f\n",
				k, threshold, size, accuracy, precision, recall, f1, sensitivity,
				specificity, tpr, fpr);
	}

}