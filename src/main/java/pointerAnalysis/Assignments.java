package pointerAnalysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import pointerAnalysis.objects.AbstractObject;
import pointerAnalysis.pointers.Pointer;

public class Assignments{
//	private static List<NewExpr> objects = new ArrayList<NewExpr>();
	
	public Map<Pointer,Set<AbstractObject>> localAssignedTo;

	//public Map<Pair<Unit,NewExpr>,Pair<Unit,Value>> objectsUsedBy; 
	public Assignments(){
		localAssignedTo = new HashMap<Pointer,Set<AbstractObject>>();
	}
	
	public void replace(Pointer pointer, AbstractObject value) {
		if(this.localAssignedTo.keySet().contains(pointer)){
			this.localAssignedTo.get(pointer).clear();
			this.localAssignedTo.get(pointer).add(value);
		}
		else{
			Set<AbstractObject> pointerObjs = new HashSet<AbstractObject>();
			pointerObjs.add(value);
			this.localAssignedTo.put(pointer, pointerObjs);
		}
				
//		if(!objects.contains(value)){
//			objects.add(value);
//		}
	}

	public void replace(Pointer pointer, Set<AbstractObject> objs) {
		this.localAssignedTo.put(pointer, objs);
		
//		for(NewExpr value : objs){
//			if(!objects.contains(value)){
//				objects.add(value);
//			}
//		}
	}

	public void copy(Map<Pointer,Set<AbstractObject>> localAssignedTo){
		this.localAssignedTo = new HashMap<Pointer,Set<AbstractObject>>(localAssignedTo);
	}
	
	public void set(Assignments ass){
		this.localAssignedTo = new HashMap<Pointer,Set<AbstractObject>>(ass.localAssignedTo);
	}

	public void merge(Assignments ass){
		merge(ass.localAssignedTo);
	}
	
	public void merge(Map<Pointer, Set<AbstractObject>> otherMap){
		for(Pointer p : otherMap.keySet()){
			this.merge(p, otherMap.get(p));
		}
	}
	
	public void merge(Pointer p, Set<AbstractObject> objs){
		this.localAssignedTo.merge(p, objs, Assignments::addToSet);
	}
	
	public void merge(Pointer p, AbstractObject obj){
		if(this.localAssignedTo.containsKey(p)){
			this.localAssignedTo.get(p).add(obj);
		}
		else{
			HashSet<AbstractObject> objSet = new HashSet<AbstractObject>();
			objSet.add(obj);
			this.localAssignedTo.put(p, objSet);
		}
		
//		if(!objects.contains(obj)){
//			objects.add(obj);
//		}
	}
	
	public static Set<AbstractObject> addToSet(Set<AbstractObject> objs, Set<AbstractObject> otherObjs){
		Set<AbstractObject> toReturn;
		if(otherObjs == null) return objs;
		if(objs == null) toReturn = otherObjs;
		else{
			toReturn = objs;
			toReturn.addAll(otherObjs);
		}

//		for(NewExpr value : otherObjs){
//			if(!objects.contains(value)){
//				objects.add(value);
//			}
//		}
		
		return objs;
	}
	
	public String toString(){
		String representation = "[";
		for(Pointer p : this.localAssignedTo.keySet()){
			representation += p.toString() + " = {";
			for(AbstractObject ne : this.localAssignedTo.get(p)){
				representation += ne.toString() + ","; //objects.indexOf(ne) + ",";
			}
			representation = representation.substring(0, representation.length() - 1) + "}\n";
		}
		
		if(representation.length() == 1) return "[]";
		else{
			representation = representation.substring(0, representation.length()-1) + "]";
	
			
			return representation;
		}
	}
	
	public boolean equals(Object o){
		return this.toString().equals(o.toString());
	}
	
	public int hashCode(){
		HashCodeBuilder hcb = new HashCodeBuilder(17,19);
		hcb.append(this.localAssignedTo);
		return hcb.toHashCode();
	}
}