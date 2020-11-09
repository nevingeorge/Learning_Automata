/*
 * Author: Nevin George
 * Advisor: Dana Angluin
 * Program Description: The program takes in as input a SUBA of n states and converts it into an equivalent UFA of 
 * 2n^2+n states. The UFA is then converted into an equivalent mod-2-MA of the same size and learned using
 * Mod2_MA.java.
 * 
 * References:
 * 1 Amos Beimel, Francesco Bergadano, Nader H. Bshouty, Eyal Kushilevitz, Stefano Varric- chio. Learning 
 *   functions represented as multiplicity automata. J. ACM, 47(3):506–530, May 2000.
 * 2 Dana Angluin. Learning regular sets from queries and counterexamples. Inf. Comput., 75(2):87–106, 1987.
 * 3 Dana Angluin, Timos Antonopoulos, Dana Fisman. Strongly Unambiguous Büchi Automata Are Polynomially 
 *   Predictable with Membership Queries. 28th International Conference on Computer Science Logic, 8:1–8:17, 2020.
 * 4 Michael Thon and Herbert Jaeger. Links Between Multiplicity Automata, Observable Operator Models and Predictive 
 *   State Representations — a Unified Learning Framework. Journal of Machine Learning Research, 16(4):103−147, 2015.
 * 5 N. Bousquet and C. Löding. Equivalence and inclusion problem for strongly unambiguous büchi automata. In 
 *   Language and Automata Theory and Applications, 4th International Conference, LATA 2010, Trier, Germany, May 
 *   24-28, 2010. Proceedings, pages 118–129, 2010. 
 *   URL: https: //doi.org/10.1007/978-3-642-13089-2_10, doi:10.1007/978-3-642-13089-2\_10.
 */

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

public class SUBA {
	
	// SUBA
	public static int SUBAStates;
	public static ArrayList<Integer>[][] SUBATransitions;
	public static boolean[] SUBAFinalStates;
	
	// UFA
	public static int UFAStates;
	public static boolean[][][] UFATransitions;
	public static boolean[] UFAFinalStates;

	public static void main(String[] args) throws Exception {		
		SUBAtoUFA();
		
		UFAtoMod2MA();

		Mod2_MA.minimize();
		
		Mod2_MA.learn();
		
		if (Mod2_MA.minSize != Mod2_MA.learnedSize) {
			Mod2_MA.throwException(null, "Algorithm failed: the learned mod-2-MA has a different dimension "
					+ "(" + Mod2_MA.learnedSize + ") than the minimized mod-2-MA (" + Mod2_MA.minSize + ").");
		}
		
		if (finalCheck(25,1000)) {
			Mod2_MA.displayResults();
		} else {
			Mod2_MA.throwException(null, "Failed final check.");
		}
		
		Mod2_MA.displayRuntime();
		
		Mod2_MA.operationsOnLearnedMA();
	}
	
	@SuppressWarnings("unchecked")
	public static void SUBAtoUFA() throws Exception {	
		if (Mod2_MA.inMinimize) {
			System.out.println("Input file name and optional flag -m or -d (e.g. SUBA_input1.txt, SUBA_input1.txt -d)");
		} else {
			System.out.println("Input file name and optional flag -vm (e.g. SUBA_input1.txt -v, SUBA_input1.txt -m, SUBA_input1.txt -vm)");
		}

		BufferedReader f = Mod2_MA.getFile(true, true, true);

		// UFAStates = SUBAStates U (SUBAStates x SUBAStates x {0,1})
		SUBAStates = Integer.parseInt(Mod2_MA.readFile(f));
		UFAStates = SUBAStates + SUBAStates * SUBAStates * 2;
		
		// alphabet ΣU{$}
		Mod2_MA.readAlphabet(f, true);
		
		StringTokenizer st = new StringTokenizer(Mod2_MA.readFile(f));
		SUBAFinalStates = new boolean[SUBAStates+1];
		while (st.hasMoreTokens()) {
			int state = Integer.parseInt(st.nextToken());
			if (1 <= state && state <= SUBAStates && !SUBAFinalStates[state]) {
				SUBAFinalStates[state] = true;
			} else {
				Mod2_MA.throwException(f,"Invalid input: invalid or duplicate final state.");
			}
		}
		
		/* 
		 * Following the paper by Bousquet and Löding, UFATransitions contains (where q,p,p'∈SUBAStates)
		 * - SUBATransitions
		 * - all transitions of the form (q,$,(q,q,0))
		 * - all transitions of the form ((q,p,i),a,(q,p',i')), where (p,a,p')∈SUBATransitions, and
		 * 	 i' = 1 if p'∈SUBAFinalStates and i if p'∉SUBAFinalStates
		 * 
		 * Transitions will be stored in a (UFAStates x alphabetSize x UFAStates) adjacency matrix.
		 * The first states of UFATransitions will be SUBAStates.
		 * The remaining states will be of the form (q_j,q_k,i), where q_j,q_k∈SUBAStates and i∈{0,1}.
		 * State (q_j,q_k,i) will be found at index (2*SUBAStates*j)+(2*k)-(SUBAStates)+(i-1) of UFATransitions.
		*/
		int numTransitions = Integer.parseInt(Mod2_MA.readFile(f));
		if (numTransitions > ((Mod2_MA.alphabet.length - 1) * SUBAStates * SUBAStates)) {
			Mod2_MA.throwException(f, "Invalid input: invalid number of transitions.");
		}
		
		/*
		 *  For each index (q,a) where q∈SUBAStates and a∈alphabet, transition_SUBA[q][a] is an ArrayList containing 
		 *  all of the reachable states from (q,a).
		 *  The alphabet for the SUBA does not include $.
		 */
		SUBATransitions = new ArrayList[SUBAStates + 1][Mod2_MA.alphabet.length - 1];
		for (int i=1; i<=SUBAStates; i++) {
			for (int j=0; j<Mod2_MA.alphabet.length-1; j++) {
				SUBATransitions[i][j] = new ArrayList<Integer>();
			}
		}
		
		// (start state, letter, end state)
		UFATransitions = new boolean[UFAStates + 1][Mod2_MA.alphabet.length][UFAStates + 1];
		
		// lines of the form q_j a q_k, where q_j,q_k∈SUBAStates and a∈alphabet
		for (int i=0; i<numTransitions; i++) {
			st = new StringTokenizer(Mod2_MA.readFile(f));
			int p_start = Integer.parseInt(st.nextToken());
			
			String letter = st.nextToken();
			
			int a = Mod2_MA.letterToIndex.get(letter);
			int p_end = Integer.parseInt(st.nextToken());
			if (p_start < 1 || p_start > SUBAStates || p_end < 1 || p_end > SUBAStates) {
				Mod2_MA.throwException(f, "Invalid input: invalid transition.");
			}
			
			// SUBATransitions ⊆ UFATransitions 
			SUBATransitions[p_start][a].add(p_end);
			UFATransitions[p_start][a][p_end] = true;
			
			// transitions of the form ((q,p,i),a,(q,p',i'))
			// p'∈SUBAFinalStates so i'=1
			if (SUBAFinalStates[p_end]) {
				for (int q=1; q<=SUBAStates; q++) {
					// ((q,p,0),a,(q,p',1))
					UFATransitions[getIndex(q, p_start, 0)][a][getIndex(q, p_end, 1)] = true;
					// ((q,p,1),a,(q,p',1))
					UFATransitions[getIndex(q, p_start, 1)][a][getIndex(q, p_end, 1)] = true;
				}
			}
			// p'∉SUBAFinalStates so i'=i
			else {
				for (int q=1; q<=SUBAStates; q++) {
					// ((q,p,0),a,(q,p',0))
					UFATransitions[getIndex(q, p_start, 0)][a][getIndex(q, p_end, 0)] = true;
					// ((q,p,1),a,(q,p',1))
					UFATransitions[getIndex(q, p_start, 1)][a][getIndex(q, p_end, 1)] = true;
				}
			}
		}
		
		// transitions for the UFA of the form (q,$,(q,q,0)), where q∈SUBAStates
		// final states for the UFA of the form (q,q,1), where q∈SUBAStates
		UFAFinalStates = new boolean[UFAStates+1];
		for (int q=1; q<=SUBAStates; q++) {
			UFATransitions[q][Mod2_MA.letterToIndex.get("$")][getIndex(q, q, 0)] = true;
			UFAFinalStates[getIndex(q, q, 1)] = true;
		}
		
		if (Mod2_MA.readFile(f) != null) {
			Mod2_MA.throwException(f,"Invalid input: more transitions inputted than specified.");
		}

		f.close();
	}
	
	public static int getIndex(int j, int k, int i) {
		return (2 * SUBAStates * j) + (2 * k) - SUBAStates + i - 1;
	}

	@SuppressWarnings("unchecked")
	public static void UFAtoMod2MA() throws Exception {
		// the size of the target function equals the number of states in the UFA
		Mod2_MA.inputSize = UFAStates;
		
		// inputFinalVector is the characteristic vector of UFAFinalStates
		Mod2_MA.inputFinalVector = Mod2_MA.initialize(1, Mod2_MA.inputSize);
		for (int i=1; i<=UFAStates; i++) {
			if(UFAFinalStates[i]) {
				Mod2_MA.addElement(Mod2_MA.inputFinalVector, 1, i);
			}
		}
		
		// for each letter in the alphabet, [transitionMatrix_letter]i,j = 1 iff (q_i,letter,q_j)∈UFATransitions
		Mod2_MA.inputTransitionMatrices = new HashMap[Mod2_MA.alphabet.length];
		for (int i=0; i<Mod2_MA.alphabet.length; i++) {
			Mod2_MA.inputTransitionMatrices[i] = Mod2_MA.initialize(Mod2_MA.inputSize, Mod2_MA.inputSize);
			
			for (int j=1; j<=Mod2_MA.inputSize; j++) {
				for (int k=1; k<=Mod2_MA.inputSize; k++) {
					if (UFATransitions[j][i][k]) {
						Mod2_MA.addElement(Mod2_MA.inputTransitionMatrices[i], j, k);
					}
				}
			}
		}
	}
	
	public static boolean MQ_SUBA(String u, String v, int curState, boolean passedFinal, int q_u) {
		/* 
		 * From Bosquet and Löding, u(v)^ω is accepted by the SUBA iff there is a state q∈SUBAStates such that
		 * q_1 (read u) -> q (read v and pass by a final state) -> q.
		 */
		
		// read u
		if (u.length() != 0) {
			String[] uArr = u.split(" ");
			// look at the first character of u
			String c = uArr[0];
			
			// check all possible states reachable from (curState, c)
			for (int i=0; i<SUBATransitions[curState][Mod2_MA.letterToIndex.get(c)].size(); i++) {
				int newState = SUBATransitions[curState][Mod2_MA.letterToIndex.get(c)].get(i);
				
				if (uArr.length == 1) {
					// ready to read v
					if (MQ_SUBA("", v, newState, false, newState)) {
						return true;
					}
				} else if (MQ_SUBA(u.substring(Math.min(c.length()+1, u.length())), v, newState, false, q_u)) {
					// more left to read in u
					return true;
				}
			}
			
			// no successful transitions from a letter in u
			return false;
		} else {
			// read v
			
			// finished reading v
			if (v.length() == 0) {
				// for u(v)^ω to be accepted, must be at state q_u and passed a final state
				if (passedFinal && (curState == q_u)) {
					return true;
				}
				return false;
			}
			
			// reached a final state
			if (SUBAFinalStates[curState]) {
				passedFinal = true;
			}
			
			String[] vArr = v.split(" ");
			
			// look at the first character of v
			String c = vArr[0];
			
			// check all possible states reachable from (curState, c)
			for (int i=0; i<SUBATransitions[curState][Mod2_MA.letterToIndex.get(c)].size(); i++) {
				int newState = SUBATransitions[curState][Mod2_MA.letterToIndex.get(c)].get(i);
				if (MQ_SUBA("", v.substring(Math.min(c.length() + 1, v.length())), newState, passedFinal, q_u)) {
					return true;
				}
			}
			return false;
		}
	}
	
	// performs a statistical EQ between the input SUBA and learned mod-2-MA
	public static boolean finalCheck(int maxTestLen, int numTests) throws Exception {		
		for (int i=1; i<=numTests; i++) {
			// SUBA: ultimately periodic words of the form u(v)^w
			int lenU = (int) (Math.random() * (maxTestLen + 1));
			int lenV = (int) (Math.random() * (maxTestLen - lenU + 1));
			String u = Mod2_MA.genTest(lenU, true);
			String v = Mod2_MA.genTest(lenV, true);
			boolean SUBA_accepts = MQ_SUBA(u, v, 1, false, 1);
			
			// mod-2-MA: words of the form u$v
			int mod2_MA_accepts = Mod2_MA.MQArbitrary(Mod2_MA.resultFinalVector, Mod2_MA.resultTransitionMatrices, Mod2_MA.addStrings(Mod2_MA.addStrings(u, "$"), v));
			
			if ((SUBA_accepts && mod2_MA_accepts == 0) || (!SUBA_accepts && mod2_MA_accepts == 1)) {
				System.out.println("u: " + u);
				System.out.println("v: " + v);
				System.out.println("SUBA_accepts: " + SUBA_accepts);
				return false;
			}
		}
		return true;
	}
}