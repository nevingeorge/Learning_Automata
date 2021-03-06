# Learning Automata
To run the programs, in Terminal/Command Prompt change your current directory to the Java Algorithms folder and input the command "java -jar" followed by the name of the desired jar file. The program will then ask for the input file name and optional flags. Make sure the desired input file is in the same folder as the jar file. Each input file must be a text document following the format required of its intended program (specific details described below). Each file must have no line separation, entries must be space separated, and lines beginning with // are ignored. Example input files for all of the programs can be found in the repository.

Optional flags:\
-v - display more verbose information regarding the procedures and outputs of the algorithms\
-m - display the progress of the minimization algorithm\
-d - only display the dimension of the minimized M2MA\
-a - display the number of states of a minimal DFA equivalent to the minimized M2MA

## Learning mod-2-multiplicity automata
M2MA.java takes in as input a M2MA and prints to stdout the M2MA obtained after learning the input function through a series of membership and equivalence queries.

### Input File Format
Contains the specifications of a M2MA.

Line 1: alphabet

Line 2: size

Line 3: final vector

Lines 4-end: transition matrices for each character in the alphabet

By default, the initial vector is (1,0,0,...,0).

## Learning strongly unambiguous Büchi automata (SUBA)
SUBA.java takes in as input a SUBA of n states and converts it into an equivalent UFA of 2n<sup>2</sup>+n states. The UFA is then converted into an equivalent M2MA of the same size and learned using M2MA.java.

### Input File Format
Contains the specifications of a SUBA of the form (Q, Σ, ∆, F).

Line 1: number of states

Line 2: alphabet

Line 3: final states

Line 4: number of transitions

Lines 5-end: transitions - each line has the form q_i a q_j, where q_i,q_j∈Q and a∈Σ.

By default, the only initial state of the SUBA (and therefore also the UFA) is q_1.

## Learning non-deterministic Büchi automata (NBA)
NBA.java takes in as input a NBA and prints to stdout the M2MA obtained after learning the NBA through a series of membership and statistical equivalence queries.

### Input File Format
Contains the specifications of a NBA of the form (Q, Σ, ∆, F) and the desired level of approximation for the statistical equivalence queries.

Line 1: maximum length of a test in the statistical equivalence query

Line 2: number of tests the statistical equivalence query will check

Line 3: limit on the number of equivalence queries to run

Line 4: number of states

Line 5: alphabet

Line 6: final states

Line 7: number of transitions

Lines 8-end: transitions - each line has the form q_i a q_j, where q_i,q_j∈Q and a∈Σ.

By default the only initial state of the NBA is q_1.

## Learning arbitrary automata
arbitrary.java displays to stdout the M2MA learned using a membership query method specified in MQ.java and statistical equivalence queries. The program can be used to approximately learn any type of automata, provided that MQ.java contains the desired automata's membership query function.

### Input File Format
Contains the name of the desired membership query function in MQ.java and level of approximation for the statistical equivalence queries.

Line 1: name of the desired membership query function in MQ.java

Line 2: maximum length of a test in the statistical equivalence query

Line 3: number of tests the statistical equivalence query will check

Line 4: limit on the number of equivalence queries to run

Line 5: alphabet

## Minimizing automata
minimize.java takes in as input a M2MA or SUBA and prints to stdout the M2MA obtained after minimizing the input function (in the SUBA case, it first converts the function into an equivalent UFA then M2MA). The format for the M2MA/SUBA inputs were described earlier.

## Converting SUBA, NBA, and DBA to M2MA and DFA
convert.jar takes in as input a series of SUBA, NBA, or DBA, and for each input omega automaton, the program adds to an output file the size of a minimal M2MA or DFA that accepts the same language. The program displays to stdout the average converted M2MA/DFA size for each input omega automaton size. Also, statistics.jar can be used to obtain more detailed statistics on the results of multiple output files representing the same conversion (e.g. SUBA->DFA or NBA->M2MA). Various experiments have been run using convert.jar, and the experiment input files and results can be found in the GitHub.

### SUBA Input File Format
Line 1: number of SUBA in the input file

Lines 2-end: input SUBA - follows the same format as SUBA.jar

### NBA/DBA Input File Format
Line 1: maximum length of a test in the statistical equivalence query

Line 2: number of tests the statistical equivalence query will check

Line 3: limit on the number of equivalence queries to run

Line 4: alphabet

Line 5: number of lines to follow

Lines 6-end: lines of the form (number of NBA/DBA to generate, max number of states, max number of transitions to remove, number of final states)

## Author: Nevin George

## Advisor: Dana Angluin

## References
Amos Beimel, Francesco Bergadano, Nader H. Bshouty, Eyal Kushilevitz, Stefano Varric- chio. Learning functions represented    as multiplicity automata. *J. ACM*, 47(3):506–530, May 2000.

Dana Angluin. Learning regular sets from queries and counterexamples. *Inf. Comput.*, 75(2):87–106, 1987.

Dana Angluin, Timos Antonopoulos, Dana Fisman. Strongly Unambiguous Büchi Automata Are Polynomially Predictable with Membership Queries. *28th International Conference on Computer Science Logic*, 8:1–8:17, 2020.

Michael Thon and Herbert Jaeger. Links Between Multiplicity Automata, Observable Operator Models and Predictive State Representations — a Unified Learning Framework. *Journal of Machine Learning Research*, 16(4):103−147, 2015.

N. Bousquet and C. Löding. Equivalence and inclusion problem for strongly unambiguous büchi automata. In *Language and Automata Theory and Applications, 4th International Conference, LATA 2010, Trier, Germany, May 24-28, 2010. Proceedings,* pages 118–129, 2010. URL: https: //doi.org/10.1007/978-3-642-13089-2_10, doi:10.1007/978-3-642-13089-2\_10.
