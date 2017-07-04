package pointerAnalysis.objects;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import pointerAnalysis.objects.AbstractObject;
import pointerAnalysis.objects.NewObject;
import soot.Type;
import soot.jimple.NewExpr;

public class NewObject extends AbstractObject{
	static List<NewExpr> objects = new ArrayList<NewExpr>();
	NewExpr newexpr;

	public NewObject(NewExpr newexpr){
		if(!objects.contains(newexpr)){
			objects.add(newexpr);
		}
		this.newexpr = newexpr;
	}
	
	@Override
	public int hashCode() {
		HashCodeBuilder hcb = new HashCodeBuilder(17,11);
		hcb.append(newexpr);
		return hcb.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof NewObject){
			return this.newexpr.equals(((NewObject) obj).newexpr);
		}
		else{
			return false;
		}
	}

	@Override
	public String toString() {
		return this.newexpr.getType().toString() + objects.indexOf(newexpr);
	}

	public Type getType(){
		return this.newexpr.getType();
	}
}