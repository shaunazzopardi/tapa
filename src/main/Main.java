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

public class Main {

	public static void main(String[] args){
		Main.initializeSoot("example.Example", "bin");
	}

	@SuppressWarnings("static-access")
	private static void initializeSoot(String mainClass, String sootCp) {
		G.v().reset();
		Options.v().set_whole_program(true);
		Options.v().setPhaseOption("jb", "use-original-names:true");
		
		//we will try to use boomerang for pointer analysis
//		Options.v().setPhaseOption("cg.spark", "on");
		//Options.v().setPhaseOption("cg.spark", "simplify-offile:false");
		//geom pts introduces context-sensitivity
		//however we will attempt to deal with that ourselves
//		Options.v().setPhaseOption("cg.spark", "geom-pta:true");
//		Options.v().setPhaseOption("cg.spark", "geom-runs:2");

		//    String userdir = System.getProperty("user.dir");
		//    String sootCp = userdir + "/targets";
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
		SootMethod methodByName = c.getMethodByName("main");
		List<SootMethod> ePoints = new LinkedList<>();
		ePoints.add(methodByName);
		Scene.v().setEntryPoints(ePoints);
		// Add a transformer
		PackManager.v().getPack("wjtp")
		.add(new Transform("wjtp.TapaTransformer", new TapaTransformer()));
		PackManager.v().getPack("cg").apply();
		PackManager.v().getPack("wjtp").apply();

	}
}
