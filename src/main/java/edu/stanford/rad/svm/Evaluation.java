package edu.stanford.rad.svm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class Evaluation {

	public static void main(String[] args) throws IOException {
		String tag = "test2";
		String dataFileFolder = "/Users/saeedhp/Dropbox/Stanford/Tools/libsvm-3.20/Data/abdomenCT/t5/"+ tag +"/";
		List<Integer> trueLable = new ArrayList<Integer>();
		List<Integer> prediction = new ArrayList<Integer>();
		
		// Read true labels
		File dataFile = new File(dataFileFolder + "label-"+tag+".txt");
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
		
		if (prediction.size() != trueLable.size()) {
			System.out.println("not equal sizes: p:"+ prediction.size() + " t:" + trueLable.size());
		} else {
			System.out.println("Evaluation size: " + prediction.size());
		}
		
		PrintWriter pw = new PrintWriter(dataFileFolder + "evaluation.tsv", "UTF-8");
		pw.printf("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n", "threshold","size", "accuracy", "precision", "recall", "f1", "sensitivity", "specificity", "tpr", "fpr");
		computeMetrics("", 0, trueLable, prediction, pw);
		pw.close();
	}

	static void computeMetrics(String fname, double threshold, List<Integer> trueLable, List<Integer> prediction, PrintWriter pw) {
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
				
		pw.printf("%.4f\t%d\t%.4f\t%.4f\t%.4f\t%.4f\t%.4f\t%.4f\t%.4f\t%.4f\n", threshold, size, accuracy, precision, recall, f1, sensitivity,specificity,tpr,fpr);
	}

}