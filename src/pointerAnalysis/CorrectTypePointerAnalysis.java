package pointerAnalysis;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.internal.JInstanceFieldRef;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ForwardBranchedFlowAnalysis;

public class CorrectTypePointerAnalysis extends ForwardBranchedFlowAnalysis<Set<Value>>{

	Type objectType;
	public Set<Value> values;
	//if invokexpr.getmethod is contained in relevantmethodParameters.keySet
	//save it to Map<InvokeExpr, SootMethod> , Map<SootMethod,Set<InvokeExpr>>
	//
	public CorrectTypePointerAnalysis(UnitGraph graph, SootMethod method, Type type) {
		super(graph);
		
		this.objectType = type;
		
		values = new HashSet<Value>();
		
		this.doAnalysis();
	}
				
	@Override
	protected void flowThrough(Set<Value> arg0, Unit arg1, List<Set<Value>> arg2, List<Set<Value>> arg3) {
		// TODO Auto-generated method stub
		for(Set<Value> ass : arg2){
			this.flowThroughSingle(arg0, arg1, ass);
		}
		
		for(Set<Value> ass : arg3){
			this.flowThroughSingle(arg0, arg1, ass);
		}
	}

	protected void flowThroughSingle(Set<Value> arg0, Unit arg1, Set<Value> arg2) {
		arg2.addAll(arg0);
		
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
		
		if(var.getType().equals(objectType)
				|| value.getType().equals(objectType)
				|| arg0.contains(var)
				|| arg0.contains(value)){
			arg2.add(value);
			arg2.add(var);
			arg2.addAll(this.getBases(var));
			arg2.addAll(this.getBases(value));
		}
		
		this.values.addAll(arg2);
	}
	
	public Set<Value> getBases(Value v){
		Set<Value> bases = new HashSet<Value>();
		if(v instanceof JInstanceFieldRef){
			Value base = ((JInstanceFieldRef) v).getBase();
			bases.add(base);
			bases.addAll(getBases(base));
		}
		
		return bases;
	}

	@Override
	protected void copy(Set<Value> arg0, Set<Value> arg1) {
		arg1 = new HashSet<Value>(arg0);
	}

	@Override
	protected void merge(Set<Value> arg0, Set<Value> arg1, Set<Value> arg2) {
		arg2.addAll(arg0);
		arg2.addAll(arg1);
	}

	@Override
	protected Set<Value> newInitialFlow() {
		return new HashSet<Value>();
	}
	
		
}


