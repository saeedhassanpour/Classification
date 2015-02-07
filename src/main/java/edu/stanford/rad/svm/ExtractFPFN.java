package edu.stanford.rad.svm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class ExtractFPFN {

	public static void main(String[] args) throws IOException {
		String dataFileFolder = "/Users/saeedhp/Dropbox/Stanford/Tools/libsvm-3.20/Data/spontaneusAbortion/2to1/";
		File dataFile = new File(dataFileFolder + "evaluation-threshold.tsv");

		PrintWriter fppw = new PrintWriter(dataFileFolder + "fp.txt", "UTF-8");
		PrintWriter fnpw = new PrintWriter(dataFileFolder + "fn.txt", "UTF-8");

		BufferedReader bReader = new BufferedReader(new FileReader(dataFile));
		String line;

		while ((line = bReader.readLine()) != null) {
			if (line.isEmpty() || line.startsWith("file"))
				continue;
			String[] tokens = line.split("\t");
			String fileName = tokens[0];
			int trueLabel = Integer.parseInt(tokens[1]);
			int prediction = Integer.parseInt(tokens[2]);
			if (trueLabel != prediction) {
				if (prediction == +1) {
					fppw.printf(" %s", fileName);
				} else if (prediction == -1) {
					fnpw.printf(" %s", fileName);
				}
			}
		}

		bReader.close();
		fppw.close();
		fnpw.close();
	}

}