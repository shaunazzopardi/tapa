# tapa

Tapa is a work-in-progress pointer analysis (short for TArgeted Pointer Analysis) aimed for static program analyses that do not require a pointer analysis of all pointers (or variables) used by the program, but of a subset of these.

Soot (https://sable.github.io/soot/) is used to analyse Java source code.

The main idea is to perform pointer analysis only for pointers of a certain type, and those pointers required to perform the analysis for such pointer.
