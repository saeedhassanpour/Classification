package edu.stanford.rad.svm;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class PrepareData {

	public static void main(String[] args) throws NumberFormatException, IOException {
		
		final File Datafolder = new File("files/rawData");
		
		for (final File inputFile : Datafolder.listFiles()) {
			if (!inputFile.isDirectory() && !inputFile.getName().startsWith(".")) {
				String inputFileName = inputFile.getName();
				String inputFilePath = inputFile.getPath();
				File dataFile = new File(inputFilePath);
				
				System.out.println("Working on " + inputFileName + "...");

				String line;
				PrintWriter pw = new PrintWriter("files/svmData/" + inputFileName, "UTF-8");
				PrintWriter lpw = new PrintWriter("files/svmData/label-" + inputFileName, "UTF-8");
				PrintWriter fpw = new PrintWriter("files/svmData/fileNames-" + inputFileName, "UTF-8");

				
				BufferedReader bReader = new BufferedReader(new FileReader(dataFile));
				
				while ((line = bReader.readLine()) != null) {
					if (line.isEmpty())
						continue;

					if (!line.startsWith("Key:")) {
						continue;
					}
					
					StringBuilder svmLine = new StringBuilder();
					String label = line.substring(line.indexOf("/")+1, line.indexOf("/", line.indexOf("/")+1));
					String[] tmp = line.substring(line.indexOf(":")+1, line.indexOf(":", line.indexOf(":")+1)).split("/");
					String fileName = tmp[tmp.length-1];
					fpw.println(fileName);

					String features = line.substring(line.indexOf("{")+1, line.length()-1);
					
					if (label.equals("positive")) {
						svmLine.append("+1");
						lpw.println("+1");
					} else if (label.equals("negative")) {
						svmLine.append("-1");
						lpw.println("-1");
					}

					String[] tokens = features.split(",");
					for(String token : tokens)
					{
						String[] tokenParts = token.split(":");
						int index = Integer.parseInt(tokenParts[0]) + 1; //indices start from 1
						double value = Double.parseDouble(tokenParts[1]);
						svmLine.append(" " + index + ":" +value);
					}
					//System.out.println(svmLine.toString());
					pw.printf("%s\n", svmLine.toString());	
				}
				bReader.close();
				pw.close();
				lpw.close();
				fpw.close();
			}
		}
	}

}