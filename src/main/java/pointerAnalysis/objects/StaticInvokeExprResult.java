package pointerAnalysis.objects;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import pointerAnalysis.objects.AbstractObject;
import pointerAnalysis.objects.StaticInvokeExprResult;
import soot.Type;
import soot.jimple.InvokeExpr;
import soot.jimple.StaticInvokeExpr;

public class StaticInvokeExprResult extends AbstractObject{

	StaticInvokeExpr expr;
	
	public StaticInvokeExprResult(StaticInvokeExpr expr){
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
		if(obj instanceof StaticInvokeExprResult){
			return this.expr.equals(((StaticInvokeExprResult) obj).expr);
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
