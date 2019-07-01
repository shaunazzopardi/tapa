package pointerAnalysis.pointers;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import pointerAnalysis.pointers.Pointer;
import soot.Type;
import soot.jimple.ParameterRef;

public class ParameterPointer extends Pointer{
	
	ParameterRef paramref;
	
	public ParameterPointer(ParameterRef paramref){
		this.paramref = paramref;
	}
	
	public String toString(){
		return paramref.toString();
	}

	@Override
	public int hashCode() {
		HashCodeBuilder hcb = new HashCodeBuilder(7,113);
		hcb.append(paramref);
		return hcb.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof ParameterRef){
			return this.paramref.equals(((ParameterPointer) obj).paramref);
		}
		else{
			return false;
		}
	}

	public Type getType(){
		return this.paramref.getType();
	}
}
