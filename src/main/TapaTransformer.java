package main;

import java.util.Iterator;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;

import pointerAnalysis.Pair;
import pointerAnalysis.TargetedPointerAnalysis;
import pointerAnalysis.objects.AbstractObject;
import pointerAnalysis.objects.NewObject;
import pointerAnalysis.pointers.Pointer;
import soot.RefLikeType;
import soot.RefType;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.toolkits.graph.TrapUnitGraph;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.internal.JInstanceFieldRef;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.callgraph.ReachableMethods;

public class TapaTransformer extends SceneTransformer{

	Map<SootMethod, TargetedPointerAnalysis> methodToTapa;
	
	//get main method
	//do pointer analysis for relevant objects and for objects related to virtual methods
	//add static methods to methods to consider, and all possible virtual method call invocations
	//is it cheaper to do an analysis on classes before? and check if a virtual method is only defined one?
	//perhaps we should also limit packages to consider
	
	@Override
	protected void internalTransform(String arg0, Map<String, String> arg1) {
	//	SootMethod method = Scene.v().getMethod("<example.Example: void stuff(java.lang.Object)>");

	//	Iterator<SootMethod> iterator = Scene.v().getMethodNumberer().iterator();
		
		Map<SootMethod, TargetedPointerAnalysis> methodToPA = new HashMap<SootMethod, TargetedPointerAnalysis>();
		
	//	CallGraph relevantCG = new CallGraph();
		
		Set<SootMethod> methodsLeft = new HashSet<SootMethod>();
		methodsLeft.add(Scene.v().getMainMethod());
		
		while(methodsLeft.size() != 0){
			Set<SootMethod> methodDone = new HashSet<SootMethod>();
			
			for(SootMethod method : methodsLeft){
				TargetedPointerAnalysis tpa = 
						this.pointerAnalysis(method, Scene.v().getType("java.lang.Object"));
				methodToPA.put(method, tpa);
				
			}	
			
			methodsLeft.removeAll(methodDone);
		}
	}
	
	protected CallGraph constructICSCG(TargetedPointerAnalysis mainTPA, CallGraph cg){
		Map<SootMethod,TargetedPointerAnalysis> methodToTPA = new HashMap<SootMethod,TargetedPointerAnalysis>();
		
		//for each invocation in the main method
		for(InvokeExpr e : mainTPA.invokeExprs){
			//if the invocation is not static
			if(e instanceof InstanceInvokeExpr){
				InstanceInvokeExpr iie = (InstanceInvokeExpr) e;
				//get the pointer to the target of the invocation
				Pointer p = mainTPA.valuePointer.get(iie.getBase());
				//get the invocation unit
				Unit u = mainTPA.unitToInvokeExpr.get(e);
				//for each possible object that the target pointer can point to
				for(AbstractObject o : mainTPA.getFlowBefore(u).localAssignedTo.get(p)){
					//if o is a resolved object (i.e. not an object from another method invocation)
					if(o instanceof NewObject){
						NewObject newObj = (NewObject) o;
						//get the type of the object
						////a possible optimisation would be to only keep one object of one type for method variables
						Type type = newObj.getType();
						//if the type is a type from the user program
						if(type instanceof RefLikeType){
							if(type instanceof RefType){
								RefType refType = (RefType) type;
								//get the class corresponding to the type
								SootClass sootClass = refType.getSootClass();
								//get the method possibly invoked
								SootMethod method = sootClass.getMethod(iie.getMethod().getSignature());
								//add an edge from the main method to this method
								cg.addEdge(new Edge(mainTPA.method, (Stmt) u, method));
							}
							else{
								
							}
						}
					}
				}
			}
		}
		
		return cg;
	}
	
	protected Pair<Set<Value>,Set<InvokeExpr>> getValuesWithType(SootMethod method, Type objectType){
		Set<Value> values = new HashSet<Value>();
		Set<InvokeExpr> invokeExprs = new HashSet<InvokeExpr>();
		
		if(method.hasActiveBody()){
			start:
			for(Unit u : method.getActiveBody().getUnits()){
				Value var;
				Value value;
				
				if(u instanceof IdentityStmt){
					IdentityStmt identityStmt = (IdentityStmt) u;
					
					var = identityStmt.getLeftOp();
					value = identityStmt.getRightOp();
				}
				else if(u instanceof AssignStmt){
					
					AssignStmt assignment = (AssignStmt) u;
					
					var = assignment.getLeftOp();
					value = assignment.getRightOp();
				}
				else if(u instanceof InvokeExpr){
					invokeExprs.add((InvokeExpr) u);
					values.addAll(this.getRelevantValues((InvokeExpr) u));

					continue start;
				}
				else{
					continue start;
				}
				
				if(value instanceof InvokeExpr){
					values.add(var);
					invokeExprs.add((InvokeExpr) value);
					values.addAll(this.getRelevantValues((InvokeExpr) value));
				}
				
				if(var.getType().equals(objectType)
						|| value.getType().equals(objectType)
						|| values.contains(var)
						|| values.contains(value)){
					values.add(value);
					values.add(var);
					values.addAll(this.getRelevantValues(var));
					values.addAll(this.getRelevantValues(value));
				}
			}
		}
		
		return new Pair<Set<Value>,Set<InvokeExpr>>(values, invokeExprs);
	}
	
	public Pair<Set<Value>,Set<InvokeExpr>> relevantPointers(SootMethod method, Pair<Set<Value>,Set<InvokeExpr>> pointerInvocations){
		Set<Value> values = pointerInvocations.first;
		Set<InvokeExpr> invocations = pointerInvocations.second;

		if(method.hasActiveBody()){

			start:
			for(Unit u : method.getActiveBody().getUnits()){
				Value var;
				Value value;
				
				if(u instanceof IdentityStmt){
					IdentityStmt identityStmt = (IdentityStmt) u;
					
					var = identityStmt.getLeftOp();
					value = identityStmt.getRightOp();
				}
				else if(u instanceof AssignStmt){
					
					AssignStmt assignment = (AssignStmt) u;
					
					var = assignment.getLeftOp();
					value = assignment.getRightOp();
				}
				else if(u instanceof InstanceInvokeExpr){
					values.add(((InstanceInvokeExpr) u).getBase());
					//we have to consider the args for methods that overloaded
					//need to perhaps do a pre-analysis to check which are overloaded?
					values.addAll(((InstanceInvokeExpr) u).getArgs());
					continue start;
				}
				else{
					continue start;
				}
				
				if(values.contains(var)
						|| values.contains(value)){
					values.add(value);
					values.add(var);
					values.addAll(this.getRelevantValues(var));
					values.addAll(this.getRelevantValues(value));
				}
			}
		}
		
		return new Pair<Set<Value>,Set<InvokeExpr>>(values, invocations);
	}
	
	public Set<Value> getRelevantValues(Value v){
		Set<Value> bases = new HashSet<Value>();
		if(v instanceof JInstanceFieldRef){
			Value base = ((JInstanceFieldRef) v).getBase();
			bases.add(base);
			bases.addAll(getRelevantValues(base));
		}
		else if(v instanceof InstanceInvokeExpr){
			InstanceInvokeExpr iie = (InstanceInvokeExpr) v;
			bases.add(iie.getBase());
			bases.addAll(iie.getArgs());
		}
		else if(v instanceof StaticInvokeExpr){
			StaticInvokeExpr iie = (StaticInvokeExpr) v;
			bases.addAll(iie.getArgs());
		}
		
		return bases;
	}
	
	protected TargetedPointerAnalysis pointerAnalysis(SootMethod method, Type objectType) {
		
		TrapUnitGraph cfg = new TrapUnitGraph(method.getActiveBody());

		//The first two pointer analyses can just be done as iterations over all edges
		//CorrectTypePointerAnalysis ctpa = new CorrectTypePointerAnalysis(cfg, method, Scene.v().getType("java.lang.Object"));
		
		Pair<Set<Value>,Set<InvokeExpr>> valueInvocationsPair = this.getValuesWithType(method, objectType);
		Set<Value> values = valueInvocationsPair.first;
		Set<InvokeExpr> invocations = valueInvocationsPair.second;
		Pair<Set<Value>,Set<InvokeExpr>> newValueInvocationsPair = valueInvocationsPair;
		do{
			valueInvocationsPair = new Pair<Set<Value>,Set<InvokeExpr>>(new HashSet<Value>(newValueInvocationsPair.first), new HashSet<InvokeExpr>(newValueInvocationsPair.second));
		//	RelevantPointerAnalysis rpa = new RelevantPointerAnalysis(cfg, method, values, new HashSet<SootMethod>());
			
			newValueInvocationsPair = this.relevantPointers(method, valueInvocationsPair);
			
		}while(!valueInvocationsPair.equals(newValueInvocationsPair));
		//SimplePointerAnalysis spa = new SimplePointerAnalysis(cfg, stuff, Scene.v().getType("java.lang.Object"));
		
		TargetedPointerAnalysis tpa = new TargetedPointerAnalysis(cfg, method, valueInvocationsPair.first);
		
		dumpInfo(tpa, method);
		
		return tpa;
	}
	
	public void dumpInfo(TargetedPointerAnalysis tpa, SootMethod method){
		Iterator<Unit> unitIterator = method.getActiveBody().getUnits().iterator();
		
		while(unitIterator.hasNext()){
			Unit u = unitIterator.next();
			System.out.println("Alias relationships:");
			for(Pointer p : tpa.getFlowBefore(u).localAssignedTo.keySet()){
				for(Pointer q : tpa.getFlowBefore(u).localAssignedTo.keySet()){
					if(p.equals(q)) continue;
					if(this.mustAlias(tpa, p, (Stmt)u, q, (Stmt)u)){
						System.out.println(p.toString() + " must alias with " + q.toString());
					}
					else if(this.mayAlias(tpa, p, (Stmt)u, q, (Stmt)u)){
						System.out.println(p.toString() + " may alias with " + q.toString());
					}
					else{
						System.out.println(p.toString() + " may not alias with " + q.toString());
					}
				}
			}
			System.out.println("FlowBefore: " + tpa.getFlowBefore(u));
			System.out.println("Statement: " + u);
			System.out.println("FallFlow: " + tpa.getFallFlowAfter(u));
			System.out.println("BranchFlow: " + tpa.getBranchFlowAfter(u));
			System.out.println("--------------------");
		}
		
		System.out.println(method.getActiveBody());
	}

	public boolean mayAlias(TargetedPointerAnalysis spa,
								Pointer p, Stmt s,
								Pointer q, Stmt r){
		Collection<AbstractObject> objectsOfPAts = spa.getFlowBefore(s).localAssignedTo.get(p);
		Collection<AbstractObject> objectsOfQAtr = spa.getFlowBefore(r).localAssignedTo.get(q);
		
		if(objectsOfPAts == null
				|| objectsOfQAtr == null){
			return false;
		}
		
		return !Collections.disjoint(objectsOfPAts, objectsOfQAtr);
	}

	public boolean mustAlias(TargetedPointerAnalysis spa,
								Pointer p, Stmt s,
								Pointer q, Stmt r){
		//need to disregard method invocations here
		//also need to treat fieldrefs appropriately
		  //i.e. currently: 
		Collection<AbstractObject> objectsOfPAts = spa.getFlowBefore(s).localAssignedTo.get(p);
		Collection<AbstractObject> objectsOfQAtr = spa.getFlowBefore(r).localAssignedTo.get(q);
		
		if(objectsOfPAts == null
				|| objectsOfQAtr == null){
			return false;
		}
		
		return objectsOfPAts.size() == 1 && objectsOfPAts.equals(objectsOfQAtr);
	}
}
