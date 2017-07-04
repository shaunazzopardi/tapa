package pointerAnalysis.pointers;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import pointerAnalysis.pointers.Pointer;
import soot.Type;
import soot.jimple.ArrayRef;

public class ArrayRefPointer extends Pointer{
	
	ArrayRef arrayRef;
	
	public ArrayRefPointer(ArrayRef arrayRef){
		this.arrayRef = arrayRef;
	}

	@Override
	public String toString(){
		return arrayRef.getBase() + "[" + arrayRef.getIndex() + "]";
	}

	@Override
	public int hashCode() {
		HashCodeBuilder hcb = new HashCodeBuilder(15,17);
		hcb.append(arrayRef);
		return hcb.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof ArrayRefPointer){
			return this.arrayRef.equals(((ArrayRefPointer)obj).arrayRef);
		}
		else{
			return false;
		}
	}
	
	public Type getType(){
		return this.arrayRef.getType();
	}
}
