package edu.stanford.rad.svm;

import java.io.File;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

public class CreateTrainingTestSets {
	
	
	public static void main(String[] args) throws Exception {
		String inputPath = "/Users/saeedhp/Dropbox/Stanford/Code/CorpusExtraction/files/stride/abdomenCT/corpus/train";
		String cvPath = "/Users/saeedhp/Dropbox/Stanford/Code/CorpusExtraction/files/stride/abdomenCT/corpus/test";
		Random rand = new Random(123);
		
		int counter = 0, changed =0;
		final File folder = new File(inputPath);
		List<File> files = (List<File>) FileUtils.listFiles(folder, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
		for (File fileEntry : files) {
			if (!fileEntry.isDirectory() && !fileEntry.getName().startsWith(".")  && !fileEntry.getName().equals("vector")) {
				++counter;
				//String fileName = fileEntry.getName();
				String filepath = fileEntry.getPath();
				//System.out.println("Working on " + fileName + "...");
				String ending = filepath.substring(filepath.indexOf("/", filepath.indexOf("train")+1));
				//System.out.println(ending);
				
				if(rand.nextDouble()<0.2)
				{
					String newPath = cvPath +ending;
					if(!fileEntry.renameTo(new File(newPath)))
					{
						System.out.println("Error for " + newPath);
					}else
					{
						++changed;
					}
				}
			}
		}
		System.out.println("Files: " + counter);
		System.out.println("Moved: " + changed);
		System.out.println("% :" + changed*100.0/counter);
	}
	
}
