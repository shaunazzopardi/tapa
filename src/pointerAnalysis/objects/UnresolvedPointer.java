package pointerAnalysis.objects;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import pointerAnalysis.objects.AbstractObject;
import pointerAnalysis.pointers.Pointer;
import pointerAnalysis.objects.UnresolvedPointer;
import soot.Type;

public class UnresolvedPointer extends AbstractObject{

	public Pointer pointer;
	
	public UnresolvedPointer(Pointer pointer){
		this.pointer = pointer;
	}
	
	@Override
	public int hashCode() {
		 HashCodeBuilder hcb = new HashCodeBuilder(17,19);
		 hcb.append(pointer);
		 return hcb.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof UnresolvedPointer){
			return this.pointer.equals(((UnresolvedPointer) obj).pointer);
		}
		else{
			return false;
		}
	}

	@Override
	public String toString() {
		return this.pointer.toString();
	}

	public Type getType(){
		return this.pointer.getType();
	}
	
}