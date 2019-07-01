package pointerAnalysis;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

import pointerAnalysis.objects.AbstractObject;
import pointerAnalysis.objects.InstanceInvokeExprResult;
import pointerAnalysis.objects.StaticInvokeExprResult;
import pointerAnalysis.objects.NewObject;
import pointerAnalysis.objects.UnresolvedPointer;
import pointerAnalysis.pointers.ArrayRefPointer;
import pointerAnalysis.pointers.FieldPointer;
import pointerAnalysis.pointers.LocalPointer;
import pointerAnalysis.pointers.ParameterPointer;
import pointerAnalysis.pointers.Pointer;
import pointerAnalysis.pointers.StaticFieldPointer;
import soot.Local;
import soot.SootMethod;
import soot.Timers;
import soot.Trap;
import soot.Type;
import soot.Unit;
import soot.UnitBox;
import soot.Value;
import soot.jimple.*;
import soot.jimple.internal.JInstanceFieldRef;
import soot.options.Options;
import soot.toolkits.graph.PseudoTopologicalOrderer;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.graph.interaction.FlowInfo;
import soot.toolkits.graph.interaction.InteractionHandler;
import soot.toolkits.scalar.ForwardBranchedFlowAnalysis;
import soot.util.Chain;

public class SimplePointerAnalysis extends PointerAnalysis<Assignments>{

	List<Pointer> relevantPointers;
	Type objectType;
	Function<Object,Boolean> condition;
	boolean initialised = false;
	boolean resolveRelevantPointers = false;;
	//if invokexpr.getmethod is contained in relevantmethodParameters.keySet
	//save it to Map<InvokeExpr, SootMethod> , Map<SootMethod,Set<InvokeExpr>>
	//
	public SimplePointerAnalysis(UnitGraph graph, SootMethod method, Type type) {
		super(graph);
		
		this.objectType = type;

		this.relevantPointers = new ArrayList<Pointer>();
		condition = new Function<Object,Boolean>(){
			public Boolean apply(Object obj) {
				return equalToType(obj);
			}
		};
		
		this.doAnalysis();
		
		if(relevantPointers.size() != 0){
			List<Pointer> prevPointers;
			do{
				SimplePointerAnalysis spa = this;
				
				prevPointers = new ArrayList<Pointer>(spa.relevantPointers);
				
				condition = new Function<Object,Boolean>(){
					List<Pointer> pointers = new ArrayList<Pointer>(spa.relevantPointers);
					public Boolean apply(Object obj) {
						return pointers.contains(obj);
					}
				};
				
				relevantPointers.clear();
				this.doAnalysis();
//				graph.forEach(new Consumer<Unit>(){
//					@Override
//					public void accept(Unit u) {
//						List<Unit> preds = graph.getPredsOf(u);
//						
//						
//						Assignments before = spa.unitToBeforeFlow.get(u);
//						List<Assignments> fallFlowAfter = spa.unitToAfterFallFlow.get(u);
//						List<Assignments> branchFlowAfter = spa.unitToAfterBranchFlow.get(u);
//						flowThrough(before, u, fallFlowAfter, branchFlowAfter);
//						
//						
//					}});
						
			}while(!prevPointers.equals(relevantPointers));
		}
	}
	
	public boolean equalToType(Object obj){
		return (obj instanceof AbstractObject
					&& ((AbstractObject) obj).getType().equals(this.objectType))
				|| (obj instanceof Pointer
						&& ((Pointer) obj).getType().equals(this.objectType));
	}
		
	public boolean isUnresolved(Object obj){
		return obj instanceof UnresolvedPointer
				&& this.relevantPointers.contains(((UnresolvedPointer) obj).pointer);
	}
	
	@Override
	protected void flowThrough(Assignments arg0, Unit arg1, List<Assignments> arg2, List<Assignments> arg3) {
		// TODO Auto-generated method stub
		for(Assignments ass : arg2){
			this.flowThroughSingle(arg0, arg1, ass);
		}
		
		for(Assignments ass : arg3){
			this.flowThroughSingle(arg0, arg1, ass);
		}
	}

	protected void flowThroughSingle(Assignments arg0, Unit arg1, Assignments arg2) {
		arg2.merge(arg0.localAssignedTo);
		
		Value var;
		Value value;
		
		if(arg1 instanceof IdentityStmt){
			IdentityStmt identityStmt = (IdentityStmt) arg1;
			
			var = identityStmt.getLeftOp();
			value = identityStmt.getRightOp();
		}
		else if(arg1 instanceof AssignStmt){
			
			AssignStmt assignment = (AssignStmt) arg1;
			
			var = assignment.getLeftOp();
			value = assignment.getRightOp();
		}
		else{
			return;
		}
		
			Pointer pointer = this.pointer(arg0, var);
			//we need to take care of super types here
			if(value instanceof NewExpr){
				NewObject obj = new NewObject((NewExpr) value);
				if(condition.apply(obj)
						|| condition.apply(pointer)){
					arg2.replace(pointer, obj);
				}
			}
			else if(value instanceof StaticInvokeExpr){
				StaticInvokeExprResult obj = new StaticInvokeExprResult((StaticInvokeExpr) value);
				if(condition.apply(obj)
						|| condition.apply(pointer)){
					arg2.replace(pointer, obj);
				}
			}
			else if(value instanceof InstanceInvokeExpr){
				InstanceInvokeExprResult obj = new InstanceInvokeExprResult((InstanceInvokeExpr) value);
				if(condition.apply(obj)
						|| condition.apply(pointer)){
					arg2.replace(pointer, obj);
				}
			}
			else {
				//consider representing pointers as strings
				Pointer valuePointer = this.pointer(arg0, value);
				
				if(valuePointer == null) return;
				
				if(arg0.localAssignedTo.containsKey(valuePointer)){
					arg2.replace(pointer, arg0.localAssignedTo.get(valuePointer));
				}
				else{
					if(condition.apply(valuePointer)
							|| condition.apply(pointer)){
						arg2.replace(pointer, new UnresolvedPointer(valuePointer));
						this.relevantPointers.add(valuePointer);
					}
				}
			}
	}

	@Override
	protected void copy(Assignments arg0, Assignments arg1) {
		arg1.copy(arg0.localAssignedTo);
	}

	@Override
	protected void merge(Assignments arg0, Assignments arg1, Assignments arg2) {
		for(Pointer p : arg0.localAssignedTo.keySet()){
			arg2.localAssignedTo.put(p, new HashSet<AbstractObject>(arg0.localAssignedTo.get(p)));
		}
		
		for(Pointer p : arg1.localAssignedTo.keySet()){
			arg2.merge(p, arg1.localAssignedTo.get(p));
		}
		
	}

	@Override
	protected Assignments newInitialFlow() {
		return new Assignments();
	}
	
	
	public Pointer pointer(Assignments prevAssignments, Value value){
		if(value instanceof ParameterRef){
			return new ParameterPointer((ParameterRef) value);
		}
		else if(value instanceof JInstanceFieldRef){
			Set<AbstractObject> targetObjects = new HashSet<AbstractObject>();
			Pointer pointer = pointer(prevAssignments, (((JInstanceFieldRef) value).getBase()));
			
			if(prevAssignments.localAssignedTo.get(pointer) == null){
				UnresolvedPointer unresolvedPointer = new UnresolvedPointer(pointer);
				targetObjects.add(unresolvedPointer);
				this.relevantPointers.add(pointer);
			}
			else{
				targetObjects.addAll(prevAssignments.localAssignedTo.get(pointer));
			}
			
			return new FieldPointer(targetObjects, (FieldRef) value);
		}
		else if(value instanceof StaticFieldRef){
			return new StaticFieldPointer((FieldRef) value);
		}
		else if(value instanceof Local){
			return new LocalPointer((Local) value);
		}
		else if(value instanceof ArrayRef){
			return new ArrayRefPointer((ArrayRef) value);
		}
		else return null;
	}
	
	
	@Override
	protected void doAnalysis() {
		final Map<Unit, Integer> numbers = new HashMap<Unit, Integer>();
		List<Unit> orderedUnits = new PseudoTopologicalOrderer<Unit>().newList(graph, false);
		{
			int i = 1;
			for (Unit u : orderedUnits) {
				numbers.put(u, new Integer(i));
				i++;
			}
		}

		TreeSet<Unit> changedUnits = new TreeSet<Unit>(new Comparator<Unit>() {
			public int compare(Unit o1, Unit o2) {
				Integer i1 = numbers.get(o1);
				Integer i2 = numbers.get(o2);
				return (i1.intValue() - i2.intValue());
			}
		});

		Map<Unit, ArrayList<Assignments>> unitToIncomingFlowSets = new HashMap<Unit, ArrayList<Assignments>>(graph.size() * 2 + 1, 0.7f);
		List<Unit> heads = graph.getHeads();
		int numNodes = graph.size();
		int numComputations = 0;
		int maxBranchSize = 0;

		// initialize unitToIncomingFlowSets
		for (Unit s : graph) {
			unitToIncomingFlowSets.put(s, new ArrayList<Assignments>());
		}

		// Set initial values and nodes to visit.
		// WARNING: DO NOT HANDLE THE CASE OF THE TRAPS
		{
			Chain<Unit> sl = ((UnitGraph) graph).getBody().getUnits();
			for (Unit s : graph) {
				changedUnits.add(s);

				unitToBeforeFlow.put(s, newInitialFlow());

				if (s.fallsThrough()) {
					List<Assignments> fl = new ArrayList<Assignments>();

					fl.add((newInitialFlow()));
					unitToAfterFallFlow.put(s, fl);

					Unit succ = sl.getSuccOf(s);
					// it's possible for someone to insert some (dead)
					// fall through code at the very end of a method body
					if (succ != null) {
						List<Assignments> l = (unitToIncomingFlowSets.get(sl.getSuccOf(s)));
						l.addAll(fl);
					}
				} else
					unitToAfterFallFlow.put(s, new ArrayList<Assignments>());

				List<Assignments> l = new ArrayList<Assignments>();
				if (s.branches()) {
					List<Assignments> incList;
					for (UnitBox ub : s.getUnitBoxes()) {
						Assignments f = (newInitialFlow());

						l.add(f);
						Unit ss = ub.getUnit();
						incList = (unitToIncomingFlowSets.get(ss));

						incList.add(f);
					}
					
				}
				unitToAfterBranchFlow.put(s, l);

				if (s.getUnitBoxes().size() > maxBranchSize)
					maxBranchSize = s.getUnitBoxes().size();
			}
		}

		// Feng Qian: March 07, 2002
		// init entry points
		{
			for (Unit s : heads) {
				// this is a forward flow analysis
				unitToBeforeFlow.put(s, entryInitialFlow());
			}
		}

		if (treatTrapHandlersAsEntries()) {
			for (Trap trap : ((UnitGraph) graph).getBody().getTraps()) {
				Unit handler = trap.getHandlerUnit();
				unitToBeforeFlow.put(handler, entryInitialFlow());
			}
		}

		// Perform fixed point flow analysis
		{
			List<Object> previousAfterFlows = new ArrayList<Object>();
			List<Object> afterFlows = new ArrayList<Object>();
			Assignments[] flowRepositories = new Assignments[maxBranchSize + 1];//(Assignments[]) new Object[maxBranchSize + 1];
			for (int i = 0; i < maxBranchSize + 1; i++)
				flowRepositories[i] = newInitialFlow();
			Assignments[] previousFlowRepositories = new Assignments[maxBranchSize + 1];//(Assignments[]) new Object[maxBranchSize + 1];
			for (int i = 0; i < maxBranchSize + 1; i++)
				previousFlowRepositories[i] = newInitialFlow();

			while (!changedUnits.isEmpty()) {
				Assignments beforeFlow;

				Unit s = changedUnits.first();
				changedUnits.remove(s);
				boolean isHead = heads.contains(s);

				accumulateAfterFlowSets(s, previousFlowRepositories, previousAfterFlows);

				// Compute and store beforeFlow
				{
					List<Assignments> preds = unitToIncomingFlowSets.get(s);

					beforeFlow = getFlowBefore(s);

					if (preds.size() == 1)
						copy(preds.get(0), beforeFlow);
					else if (preds.size() != 0) {
						Iterator<Assignments> predIt = preds.iterator();

						copy(predIt.next(), beforeFlow);

						while (predIt.hasNext()) {
							Assignments otherBranchFlow = predIt.next();
							Assignments newBeforeFlow = newInitialFlow();
							merge(s, beforeFlow, otherBranchFlow, newBeforeFlow);
							copy(newBeforeFlow, beforeFlow);
						}
					}

					if (isHead && preds.size() != 0)
						mergeInto(s, beforeFlow, entryInitialFlow());
				}

				// Compute afterFlow and store it.
				{
					List<Assignments> afterFallFlow = unitToAfterFallFlow.get(s);
					List<Assignments> afterBranchFlow = getBranchFlowAfter(s);
					if (Options.v().interactive_mode()) {
						InteractionHandler ih = InteractionHandler.v();
						Assignments savedFlow = newInitialFlow();
						copy(beforeFlow, savedFlow);
						FlowInfo<Assignments, Unit> fi = new FlowInfo<Assignments, Unit>(savedFlow, s, true);
						if (ih.getStopUnitList() != null
								&& ih.getStopUnitList().contains(s)) {
							ih.handleStopAtNodeEvent(s);
						}
						ih.handleBeforeAnalysisEvent(fi);
					}
					flowThrough(beforeFlow, s, afterFallFlow, afterBranchFlow);
					if (Options.v().interactive_mode()) {
						List<Assignments> l = new ArrayList<Assignments>();
						if (!afterFallFlow.isEmpty()) {
							l.addAll(afterFallFlow);
						}
						if (!afterBranchFlow.isEmpty()) {
							l.addAll(afterBranchFlow);
						}

						/*
						 * if (s instanceof soot.jimple.IfStmt){
						 * l.addAll((List)afterFallFlow);
						 * l.addAll((List)afterBranchFlow); } else {
						 * l.addAll((List)afterFallFlow); }
						 */
						FlowInfo<List<Assignments>, Unit> fi = new FlowInfo<List<Assignments>, Unit>(l, s, false);
						InteractionHandler.v().handleAfterAnalysisEvent(fi);
					}
					numComputations++;
				}

				accumulateAfterFlowSets(s, flowRepositories, afterFlows);

				// Update queue appropriately
				if (!afterFlows.equals(previousAfterFlows)) {
					for (Unit succ : graph.getSuccsOf(s)) {
						changedUnits.add(succ);
					}
				}
			}
		}

		// G.v().out.println(graph.getBody().getMethod().getSignature() +
		// " numNodes: " + numNodes +
		// " numComputations: " + numComputations + " avg: " +
		// Main.truncatedOf((double) numComputations / numNodes, 2));

		Timers.v().totalFlowNodes += numNodes;
		Timers.v().totalFlowComputations += numComputations;

	} // end doAnalysis
	
	// Accumulate the previous afterFlow sets.
		private void accumulateAfterFlowSets(Unit s, Assignments[] flowRepositories, List<Object> previousAfterFlows) {
			int repCount = 0;

			previousAfterFlows.clear();
			if (s.fallsThrough()) {
				copy(unitToAfterFallFlow.get(s).get(0), flowRepositories[repCount]);
				previousAfterFlows.add(flowRepositories[repCount++]);
			}

			if (s.branches()) {
				List<Assignments> l = (getBranchFlowAfter(s));
				Iterator<Assignments> it = l.iterator();

				while (it.hasNext()) {
					Assignments fs = (it.next());
					copy(fs, flowRepositories[repCount]);
					previousAfterFlows.add(flowRepositories[repCount++]);
				}
			}
		} // end accumulateAfterFlowSets

	public boolean mayAlias(Pointer p, Stmt s,
							Pointer q, Stmt r){
		Collection<AbstractObject> objectsOfPAts = this.getFlowBefore(s).localAssignedTo.get(p);
		Collection<AbstractObject> objectsOfQAtr = this.getFlowBefore(r).localAssignedTo.get(q);

		if(objectsOfPAts == null
				|| objectsOfQAtr == null){
			return false;
		}

		return !Collections.disjoint(objectsOfPAts, objectsOfQAtr);
	}

	public boolean mustAlias(Pointer p, Stmt s,
							 Pointer q, Stmt r){
		//need to disregard method invocations here
		//also need to treat fieldrefs appropriately
		//i.e. currently:
		Collection<AbstractObject> objectsOfPAts = this.getFlowBefore(s).localAssignedTo.get(p);
		Collection<AbstractObject> objectsOfQAtr = this.getFlowBefore(r).localAssignedTo.get(q);

		if(objectsOfPAts == null
				|| objectsOfQAtr == null){
			return false;
		}

		return objectsOfPAts.size() == 1 && objectsOfPAts.equals(objectsOfQAtr);
	}
}


