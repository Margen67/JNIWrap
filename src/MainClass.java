import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

import cs.jniwrap.GlobalAnalyzer;
import cs.jniwrap.JNIClass;
import cs.jniwrap.JVPException;
import cs.jniwrap.JVPParser;
import cs.jniwrap.utils.BitfieldVector;
import cs.jniwrap.webcrawl.OracleCrawler;
import cs.jniwrap.webcrawl.WebCrawler;
import cs.tests.VectorizationTest;
import org.jsoup.*;
public class MainClass {
	public static Vector<JNIClass> classes;
	public static Vector<String> files;
	public static final String javapInvocation = "javap -s -protected";
	public static void main(String[] args) {
		//WebCrawler crawler = new WebCrawler("https://docs.oracle.com/javase/8/docs/api/overview-summary.html");
		OracleCrawler crawler = new OracleCrawler();
		crawler.listPackages();
		
		BitfieldVector bVec = new BitfieldVector(3, 400);
		//float f = new VectorizationTest().test();
		VectorizationTest t = new VectorizationTest();
		for(int i = 0; i < 54; ++i)
			System.out.println(t.dot(t.randomFloatArray(64), t.randomFloatArray(64)));
		for(int i = 0; i < 400; ++i) {
			bVec.add(new Integer(2));
		}
		
		for(int i = 0; i < bVec.size(); ++i) {
			if(bVec.get(i) != 2)
				bVec.clear();
		}
			
		if(args.length < 1) {
			System.out.println("No class file argument provided!");
			return;
		}
		final long start = System.currentTimeMillis();
		File srcFile = new File(args[0]);
		files = new Vector<>();
		classes = new Vector<>();
		if(!srcFile.exists()) {
			System.out.println(args[0] + " does not exist.");
		}
		else
			buildFileList(srcFile);
		
		Runtime rt = Runtime.getRuntime();
		int processors = rt.availableProcessors();
		if(files.size() > processors) {
			int filesPerCore = files.size() / processors;
			int lastCoreAdditional = files.size() % processors;
			class ClassWorker extends Thread {
				String[] files;
				public ClassWorker(String[] files) {
					this.files = files;
				}
				public void run() {
					/*for(String file : files)
						parseClass(file);*/
					parseClasses(files);
				}
			};
			ClassWorker workers[] = new ClassWorker[processors];
			for(int i = 0; i < processors; ++i) {
				int fileCount = filesPerCore + (i == processors - 1 ? lastCoreAdditional : 0);
				String workerFiles[] = new String[fileCount];
				
				for(int j = 0; j < fileCount; ++j) 
					workerFiles[j] = files.get(j + (i*filesPerCore));
				workers[i] = new ClassWorker(workerFiles);
				workers[i].start();
 			}
			for(ClassWorker worker : workers) {
				try {
					worker.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		
		} else {
			for(String file : files)
				parseClass(file);
		}
		
		/*
		 * we no longer need to use a vector since the class array wont have to grow
		 * this allows us to conserve memory
		 */
		
		
		GlobalAnalyzer analyzer = new GlobalAnalyzer(classes.toArray(new JNIClass[classes.size()]));
		classes = null;
		analyzer.invariants();
		analyzer.performAnalysis();
		double end = (double)(System.currentTimeMillis() - start) / 1000.0;
		System.out.println("Processing classes took " + Double.toString(end) + " seconds.");
		
	}
	
	private static void buildFileList(File path) {
		if(path.isDirectory()) {
			File files[] = path.listFiles();
			for(File file : files)
				buildFileList(file);
		}
		else if(!path.getName().endsWith(".class")) {
			return;
		}
		else
			files.add(path.getAbsolutePath());//parseClass(path.getAbsolutePath());
		
	}

	private static void parseClass(String arg) {
		JVPParser parser = null;

		try {
			Vector<String> lineVec = executeJavap(arg);
				//cmdOutput += (s + "\n");
			
			try {
				parser = new JVPParser(lineVec);
			} catch (JVPException e) {
				System.out.println("Failed to parse javap output: " + e.toString());
				e.printStackTrace();
				return;
			}
			
		} catch (IOException e) {
			System.out.println("Failed to executed javap on class " + arg + ". Exception: " + e.toString());
			return;
		}
		/*
		 * shouldn't happen
		 */
		if(parser == null) {
			System.out.println("How is parser null???");
			return;
		}
		synchronized(classes) {
			classes.add(parser.parse(arg));
		}
	}
	private static void parseClasses(String args[]) {
		JVPParser parser = null;

		try {
			Vector<Vector<String>> lineVec = executeJavap(args);
				//cmdOutput += (s + "\n");
			int i = 0;
			for(Vector<String> classvec : lineVec) {
				try {
					parser = new JVPParser(classvec);
				} catch (JVPException e) {
					System.out.println("Failed to parse javap output: " + e.toString());
					e.printStackTrace();
					return;
				}
				JNIClass parseResult = parser.parse(args[i++]);
				synchronized(classes) {
					classes.add(parseResult);
				}
			}
		} catch (IOException e) {
			System.out.println("Failed to executed javap on classes " + args.toString() + ". Exception: " + e.toString());
			return;
		}
	}
	//executing javap one file at a time is literally 100x slower than batching them all together
	private static Vector<String> executeJavap(String arg) throws IOException {
		Runtime rt = Runtime.getRuntime();
		Process javap = rt.exec(javapInvocation + " " + arg);
		BufferedReader stdin = new BufferedReader(new InputStreamReader(javap.getInputStream()));
		//String cmdOutput = "";
		String s = null;
		Vector<String> lineVec = new Vector<>();
		while((s = stdin.readLine()) != null) {
			if(s.isEmpty())
				continue;
			lineVec.add(s);
		}
		return lineVec;
	}

	private static Vector< Vector<String> > executeJavap(String arg[]) throws IOException {
		Runtime rt = Runtime.getRuntime();
		String cmd = javapInvocation;
		for(String s: arg) 
			cmd += " " + s;
		
		Process javap = rt.exec(cmd);
		BufferedReader stdin = new BufferedReader(new InputStreamReader(javap.getInputStream()));
		//String cmdOutput = "";
		String s = null;
		Vector<String> lineVec = new Vector<>();
		Vector<Vector<String>> result = new Vector<>();
		boolean first = true;
		while((s = stdin.readLine()) != null) {
			if(s.isEmpty())
				continue;
			if(s.contains("Compiled from ")) {
				if(first)
					first = false;
				else {
					result.add(lineVec);
					lineVec = new Vector<>();
				}
			}
			lineVec.add(s);
		}
		return result;
	}
}
