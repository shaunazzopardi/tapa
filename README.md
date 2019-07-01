# tapa

Tapa is a work-in-progress pointer analysis (short for TArgeted Pointer Analysis) aimed for static program analyses that do not require a pointer analysis of all pointers (or variables) used by the program, but of a subset of these.

Soot (https://sable.github.io/soot/) is used to analyse Java source code.

The main idea is to perform pointer aliasing analysis only for pointers of a certain type, and those pointers required to perform the analysis for such pointers. A motivating analysis for this is static typestate analysis (or protocol analysis), that involves using static analysis to analyse a program with respect to some property (e.g. a finite-state machine) parametrized over some object (e.g. the property specifies that for each iterator object, the next method is never called before hasNext is called). In this case we only need to consider the aliasing relation between objects of this type.

Consider the following method:

```java
 1:    public void method(Iterator<String> it1, Iterator<String> it2){
 2:        Iterator<String> it3;
 3:        if(!it1.hasNext()){
 4:            it3 = it1;
 5:            System.out.println("Not using second iterator");
 6:        }
 7:        else{
 8:            it3 = it2;
 9:            System.out.println("First iterator is empty");
10:        }
11:        while(it3.hasNext()){
12:            System.out.println(it3.next());
13:        }
14:    }
```

For aliasing analysis we are interested in relating pairs of lines (Lines) and variables (Vars), creating two relations: a must-alias relation (must : (Line x Vars) <-> (Line x Vars)), and a may-alias relation (may : (Line x Vars) <-> (Line x Vars)). For example, it3 at line 4, and it1 at line 4 must-alias, since their values are always the same at that point (note the previous line). While it3 at line 11 may-alias with it1 at line 11, since at line 11 it1 is equal to it1 or it2, depending on the if-condition at line 3. Thus the must-alias relation relates variable-line pairs if the variables at those lines will necessarily point to the same memory location. Similarly, the may-alias relation relates variable-line pairs if there is a possible path, from the start of the program through those lines, such that the variables at those lines point to the same memory location.

We can compute these relations by annotating source code by the effects statements have on which memory locations a pointer points to. We enumeration of memory locations using the natural numbers, and then we just have to consider statements that an effect on which memory locations a pointer points to, namely assign statements. Below find an example of the previous example annotated in this manner. Note that we branch the analysis given if-else branching, and merge there results after exiting (see line 11).

```java
 1:    public void method(Iterator<String> it1, Iterator<String> it2){
 2:        Iterator<String> it3;//memloc(2) = {it1 -> {1}, it2 -> {2}}
 3:        if(!it1.hasNext()){//memloc(3) = {it1 -> {1}, it2 -> {2}, it3 -> {}}
 4:            it3 = it1;//memloc(4) = {it1 -> {1}, it2 -> {2}, it3 -> {}}
 5:            System.out.println("Not using second iterator");//memloc(5) = {it1 -> {1}, it2 -> {2}, it3 -> {1}}
 6:        }//memloc(6) = {it1 -> {1}, it2 -> {2}, it3 -> {1}}
 7:        else{//memloc(7) = {it1 -> {1}, it2 -> {2}, it3 -> {}}
 8:            it3 = it2;//memloc(8) = {it1 -> {1}, it2 -> {2}, it3 -> {}}
 9:            System.out.println("First iterator is empty");//memloc(9) = {it1 -> {1}, it2 -> {2}, it3 -> {2}}
10:        }//memloc(10) = {it1 -> {1}, it2 -> {2}, it3 -> {2}}
11:        while(it3.hasNext()){//memloc(11) = {it1 -> {1}, it2 -> {2}, it3 -> {1,2}}
12:            System.out.println(it3.next());//memloc(12) = {it1 -> {1}, it2 -> {2}, it3 -> {1,2}}
13:        }//memloc(13) = {it1 -> {1}, it2 -> {2}, it3 -> {1,2}}
14:    }//memloc(14) = {it1 -> {1}, it2 -> {2}, it3 -> {1,2} }
```

Note that here we are only giving an example of a local aliasing analysis (in contrast to a global one), which means that only the statements in a method are analysed, while we assume that the input parameters point to different memory locations. We can then compute whether a variable at a certain line aliases with a variable at another line by considering the memory locations they point to at that line:

__Definition.__ _Variable x at line n must-alias with variable y at line m if memloc(n)(x) = memloc(m)(y)._

__Definition.__ _Variable x at line n may-alias with variable y at line m if memloc(n)(x) ∩ memloc(m)(y) != {}._

Note that here, to compute enough information for aliasing we only needed to consider iterator pointers. Usually, however, we cannot only consider objects of one type, given that the objects of interest may be passed on to some other method, and to determine this method (assuming it is not static) we may need to determine the specific subtype of its target object, given that subclasses may override method definitions.

# Usage

Construct a pointerAnalysis.TargetedPointerAnalysis object for an intraprocedural pointer analysis of a method given some set of locals. pointerAnalysis.TapaTransformer has some utility functions to identify a set of locals of a method of a given type.