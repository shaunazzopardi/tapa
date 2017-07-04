package pointerAnalysis;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Pair<S,R> {

	public S first;
	public R second;
	
	public Pair(S first, R second){
		this.first = first;
		this.second = second;
	}
	
	public String toString(){
		return "(" + this.first.toString() + ", " + this.second.toString() + ")";
	}
	
	public int hashCode(){
		HashCodeBuilder hcb = new HashCodeBuilder(17,37);
		hcb.append(first);
		hcb.append(second);
		
		return hcb.toHashCode();
	}
	
	public boolean equals(Object obj){
		if(obj.getClass() == this.getClass()){
			Pair otherPair = (Pair) obj;
			if(otherPair.first.equals(this.first)){
				if(otherPair.second.equals(this.second)){
					return true;
				}
			}
		}
		
		return false;
	}
}
