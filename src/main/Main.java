package main;

import java.util.LinkedList;
import java.util.List;

import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.options.Options;
import tapa.TapaTransformer;

public class Main {

	public static void main(String[] args){
		Main.initializeSoot("example.Example", "bin");
	}

	@SuppressWarnings("static-access")
	private static void initializeSoot(String mainClass, String sootCp) {

		//Setting several soot options
		G.v().reset();
		Options.v().set_whole_program(true);
		Options.v().setPhaseOption("jb", "use-original-names:true");
		
		Options.v().set_soot_classpath(sootCp);

		Options.v().set_prepend_classpath(true);
		Options.v().set_no_bodies_for_excluded(true);
		Options.v().set_allow_phantom_refs(true);
		Options.v().keep_line_number();
		Options.v().set_main_class(mainClass);

		Scene.v().addBasicClass(mainClass, SootClass.BODIES);
		Scene.v().loadNecessaryClasses();
		SootClass c = Scene.v().forceResolve(mainClass, SootClass.BODIES);
		if (c != null) {
			c.setApplicationClass();
		}

		//Set entry points to file
		//TODO Get these from input args once functional
		SootMethod methodByName = c.getMethodByName("main");
		List<SootMethod> entryPoints = new LinkedList<>();
		entryPoints.add(methodByName);
		Scene.v().setEntryPoints(entryPoints);

		// Add a transformer
		// A transformer is a Soot concept that transforms or computes something about a Java program
		PackManager.v().getPack("wjtp")
		.add(new Transform("wjtp.TapaTransformer", new TapaTransformer()));

		// Applies call graph phase, that computes the call graph of the program
		PackManager.v().getPack("cg").apply();

		// Applies wjtp (the whole-jimple transformation pack) phase, that computes our targeted pointer analysis
		PackManager.v().getPack("wjtp").apply();

	}
}
