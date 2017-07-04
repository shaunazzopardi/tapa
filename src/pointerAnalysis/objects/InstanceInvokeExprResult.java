package pointerAnalysis.objects;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import pointerAnalysis.objects.AbstractObject;
import soot.Type;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;

public class InstanceInvokeExprResult extends AbstractObject{

	InstanceInvokeExpr expr;
	
	public InstanceInvokeExprResult(InstanceInvokeExpr expr){
		this.expr = expr;
	}
	
	@Override
	public int hashCode() {
		HashCodeBuilder hcb = new HashCodeBuilder(17,31);
		hcb.append(expr);
		return hcb.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof InstanceInvokeExprResult){
			return this.expr.equals(((InstanceInvokeExprResult) obj).expr);
		}
		else{
			return false;
		}
	}

	@Override
	public String toString() {
		return this.expr.toString();
	}

	@Override
	public Type getType() {
		return this.expr.getType();
	}
	
}
