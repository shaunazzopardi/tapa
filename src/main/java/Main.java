import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.google.common.io.Files;
import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.options.Options;
import pointerAnalysis.TapaTransformer;

import javax.swing.text.html.Option;

public class Main {

	public static void main(String[] args){
		if(args.length < 3){
			System.out.println("Wrong input. Enter at least three parameters: <canonical-name-of-main-class> <location-of-compiled-code> <target-object-class>. Append with \"dump-info\" to dump computed aliasing data.");
		} else{
			Main.initializeSoot(args[0], args[1], args[2], args.length >= 4);
		}
	}

	@SuppressWarnings("static-access")
	private static void initializeSoot(String mainClass, String sootCp, String targetObjectClass, boolean dumpInfo) {
		G.v().reset();

		TapaTransformer.dumpInfo = dumpInfo;
		TapaTransformer.targetObjectClass = targetObjectClass;

		List<String> args = new ArrayList<>();

		args.add("-w");

		args.add("-p");
		args.add("jb");
		args.add("enabled:true,use-original-names:true");

		args.add("-p");
		args.add("cg.cha");
		args.add("enabled:true");

		args.add("-exclude");
		args.add("java.*");
		args.add("-exclude");
		args.add("jdk.*");
		args.add("-exclude");
		args.add("sun.*");

		args.add("-cp");
		args.add(sootCp);

		args.add("-pp");

		args.add("-allow-phantom-refs");

		args.add("-keep-line-number");

		args.add("-main-class");
		args.add(mainClass);

//        args.add("-permissive-resolving");


		args.add("-f");
		args.add("c");

		args.add("-process-dir");
		args.add(sootCp);

		// Add a transformer
		// A transformer is a Soot concept that transforms or computes something about a Java program
		PackManager.v().getPack("wjtp")
				.add(new Transform("wjtp.pointerAnalysis.TapaTransformer", new TapaTransformer()));

//		// Applies call graph phase, that computes the call graph of the program
//		PackManager.v().getPack("cg").apply();
//
//		// Applies wjtp (the whole-jimple transformation pack) phase, that computes our targeted pointer analysis
//		PackManager.v().getPack("wjtp").apply();


		soot.Main.main(args.toArray(new String[]{}));




	}
}
