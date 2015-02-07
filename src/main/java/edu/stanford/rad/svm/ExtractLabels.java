package edu.stanford.rad.svm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class ExtractLabels {

	public static void main(String[] args) throws IOException {
		String dataFileFolder = "/Users/saeedhp/Dropbox/Stanford/Tools/libsvm-3.20/Data/spontaneusAbortion/2to1/";
		File dataFile = new File(dataFileFolder + "test.txt" );
		PrintWriter pw = new PrintWriter(dataFileFolder + "label-test.txt", "UTF-8");

		BufferedReader bReader = new BufferedReader(new FileReader(dataFile));
		String line;

		while ((line = bReader.readLine()) != null) {
			if (line.isEmpty())
				continue;
			String[] tokens = line.split(" ");
			pw.printf("%s\n", tokens[0]);
		}

		bReader.close();
		pw.close();

	}

}



