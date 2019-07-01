package pointerAnalysis.pointers;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import pointerAnalysis.pointers.Pointer;
import pointerAnalysis.pointers.UnknownPointer;
import soot.Local;
import soot.Type;

public class UnknownPointer extends Pointer{
	
	Local local;
	
	public UnknownPointer(Local local){
		this.local = local;
	}

	@Override
	public String toString(){
		return local.getName();
	}

	@Override
	public int hashCode() {
		HashCodeBuilder hcb = new HashCodeBuilder(15,17);
		hcb.append(local);
		return hcb.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof UnknownPointer){
			return this.local.equals(((UnknownPointer)obj).local);
		}
		else{
			return false;
		}
	}
	
	public Type getType(){
		return this.local.getType();
	}
}
