package org.jmlspecs.eclipse.jdt.core.tests.rac;

import org.jmlspecs.jml4.rac.runtime.JMLAssertError;
import org.jmlspecs.jml4.rac.runtime.JMLInternalPreconditionError;
import org.jmlspecs.jml4.rac.runtime.JMLLoopInvariantError;
import org.jmlspecs.jml4.rac.runtime.JMLLoopVariantError;

public class LoopTest extends RacTestCompiler {

    public LoopTest(String name) {
        super(name);
    }

    public void test_0001_while_true() {
        compileAndExecGivenStatement("X.java", "public class X {\n" + "   public void m(boolean b) { \n" + "   	//@ loop_invariant true;\n" + "   	while (b) {} \n" + "	}\n" + "}\n", "new X().m(false)");
    }

    public void test_0002_while_expr() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {\n" + "   public void m(int i, String s) { \n" + "   	//@ loop_invariant s.length() * 2 < i;\n" + "   	while (true) {} \n" + "	}\n" + "}\n", "new X().m(0, \"hello\")", null, JMLLoopInvariantError.class);
    }

    public void test_0004_while_RAC_valid() {
        compileAndExecGivenStatement("X.java", "public class X {\n" + "   public void m() { \n" + "   	int i = 0;\n" + "   	int[] vals = new int[] {2,3,5,7,9};\n" + "   	int sum = 0;\n" + "   	//@ loop_invariant sum >= 0;\n" + "   	while (i < vals.length) { sum += vals[i++]; } \n" + "	}\n" + "}\n", "new X().m()");
    }

    public void test_0005_while_RAC_invalid() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {\n" + "   public void m() { \n" + "   	int i = 0;\n" + "   	int[] vals = new int[] {2,3,5,7,9};\n" + "   	int sum = 0;\n" + "   	//@ loop_invariant sum == 111;\n" + "   	while (i < vals.length) { sum += vals[i++]; } \n" + "	}\n" + "}\n", "new X().m()", "", JMLLoopInvariantError.class);
    }

    public void test_0006_while_RAC_constTrue() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {\n" + "   public void m() { \n" + "   	int i = 0;\n" + "   	int[] vals = new int[] {2,3,5,7,9};\n" + "   	   int sum = 0;\n" + "   	   //@ loop_invariant sum == 111;\n" + "   	   while (true) { sum += vals[i++]; } \n" + "	}\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_0007_while_RAC_constFalse() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {\n" + "   static final boolean DBG = false;" + "   void m(X x) {\n" + "   	int i = 0;\n" + "   	int[] vals = new int[] {2,3,5,7,9};\n" + "   	   int sum = 0;\n" + "   	   //@ loop_invariant sum == 111;\n" + "   	   while (x.DBG) { /*empty*/ } \n" + "	}\n" + "}\n", "new X().m(new X())", null, JMLLoopInvariantError.class);
    }

    public void test_0008_while_RAC_noBody() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {\n" + "   public void m() { \n" + "   	int i = 0;\n" + "   	int[] vals = new int[] {2,3,5,7,9};\n" + "   	   int sum = 0;\n" + "   	   //@ loop_invariant sum == 111;\n" + "   	   while (i == i) { /* empty */ } \n" + "	}\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_0009_while_RAC_throwBody() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {\n" + "   boolean bool() { return true; }\n" + "   void m() {" + "   	int i = 0;\n" + "   	int[] vals = new int[] {2,3,5,7,9};\n" + "   	   int sum = 0;\n" + "   	   //@ loop_invariant sum == 111;\n" + "   	   while (bool()) { throw new RuntimeException(); } \n" + "	}\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_0010_while_RAC_break_valid() {
        compileAndExecGivenStatement("X.java", "public class X {\n" + "   public void m() { \n" + "   	int i = 0;\n" + "   	int[] vals = new int[] {2,3,5,7,9};\n" + "   	int sum = 0;\n" + "   	//@ loop_invariant sum >= 0;\n" + "   	while (i < vals.length) { break; } \n" + "	}\n" + "}\n", "new X().m()");
    }

    public void test_0011_while_RAC_break_invalid() {
        if (testIsDisabled("FIXME")) return;
        compileAndExecGivenStatement("X.java", "public class X {\n" + "   public void m() { \n" + "   	int i = 0;\n" + "   	int[] vals = new int[] {2,3,5,7,9};\n" + "       try {\n" + "   	   int sum = 0;\n" + "   	   //@ loop_invariant sum >= 0;\n" + "   	   while (i < vals.length) { sum = -1; break; } \n" + "	    } catch (Error e) {\n" + "          System.out.println(e.toString());\n" + "	    }\n" + "	}\n" + "}\n", "new X().m()");
    }

    public void test_0012_while_true() {
        compileAndExecGivenStatement("X.java", "public class X {\n" + "   public void m(boolean b) { \n" + "   	//@ maintaining true;\n" + "   	while (b) {} \n" + "	}\n" + "}\n", "new X().m(false)");
    }

    public void test_0013_while_RAC_break_valid() {
        if (testIsDisabled("FIXME - see comment above")) return;
        compileAndExecGivenStatement("X.java", "public class X {\n" + "   public void m() { \n" + "   	int i = 0;\n" + "   	int j = 100;\n" + "   	int[] vals = new int[] {2,3,5,7,9};\n" + "   	int sum = 0;\n" + "   	//@ loop_invariant sum >= 0;\n" + "   	while (i < vals.length) {\n" + "   	      //@ loop_invariant i%2 < 2;\n" + "   	      while (i % 2 > 2) { j--;}\n" + "   	      i--;\n" + "        }\n" + "	}\n" + "}\n", "new X().m()");
    }

    public void test_0014_while_RAC_seq() {
        compileAndExecGivenStatement("X.java", "public class X {\n" + "   public void m() { \n" + "   	int i = 0;\n" + "   	int[] vals = new int[] {2,3,5,7,9};\n" + "   	int sum = 0;\n" + "   	//@ loop_invariant sum >= 0;\n" + "   	//@ loop_invariant 0 <= 0;\n" + "   	//@ loop_invariant sum >= 0;\n" + "   	//@ loop_invariant 0 <= 0;\n" + "   	while (i < vals.length) { sum += vals[i++]; } \n" + "	}\n" + "}\n", "new X().m()");
    }

    public void test_0015_while_leq_expr_RAC() {
        compileAndExecGivenStatement("X.java", "public class X {\n" + "   public void m() { \n" + "   	int i = 0;\n" + "   	int[] vals = new int[] {2,3,5,7,9};\n" + "   	int sum = 0;\n" + "   	//@ loop_invariant 0 <= sum;\n" + "   	while (i < vals.length) { sum += vals[i++]; } \n" + "	}\n" + "}\n", "new X().m()");
    }

    public void test_0101_while_variant_int() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {\n" + "   public void m(int i) { \n" + "   	//@ decreases i;\n" + "   	while (i > 0) {} \n" + "	}\n" + "}\n", "new X().m(1)", null, JMLLoopVariantError.class);
    }

    public void test_0110_while_RAC_valid_variant() {
        compileAndExecGivenStatement("X.java", "public class X {\n" + "   public void m() { \n" + "   	int i = 0;\n" + "   	int[] vals = new int[] {2,3,5,7,9};\n" + "   	int sum = 0;\n" + "   	//@ decreases vals.length - i;\n" + "   	while (i < vals.length) { sum += vals[i++]; } \n" + "	}\n" + "}\n", "new X().m()");
    }

    public void test_0111_while_RAC_invalid_variant() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {\n" + "   public void m() { \n" + "   	int i = 3;\n" + "   	int[] vals = new int[] {2,3,5,7,9};\n" + "   	int sum = 0;\n" + "   	//@ decreases i;\n" + "   	while (i < vals.length) { sum += vals[i++]; } \n" + "	}\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_0112_while_RAC_invalid_variant() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {\n" + "   public static void f(int[] vals) { \n" + "   	int i = 20;\n" + "   	int sum = 0;\n" + "   	//@ decreases i;\n" + "   	while (i <= 20) { i--; } \n" + "   }\n" + "   public void m() { \n" + "   	int[] vals = new int[] {2,3,5,7,9};\n" + "       f(vals);\n" + "	}\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_0113_while_RAC_valid_variant() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {\n" + "   public void m() { \n" + "   	int i = 0;\n" + "   	int[] vals = new int[] {2,3,5,7,9};\n" + "   	int sum = 0;\n" + "       //@ decreases vals.length - i;\n" + "       while (i < vals.length) {\n" + "             //@ decreases vals.length - i;\n" + "             while (i%2==0) \n" + "                   sum += vals[i--]; } \n" + "   }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_0114_while_RAC_invalid_variant() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {\n" + "   public void m() { \n" + "   	int i = 0;\n" + "   	int[] vals = new int[] {2,3,5,7,9};\n" + "   	int sum = 0;\n" + "       //@ decreases vals.length - i;\n" + "       while (i < vals.length) {\n" + "             //@ decreases i;\n" + "             while (i%2==0) \n" + "                   sum += vals[i++]; } \n" + "   }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_0115_while_RAC_invalid_variant() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {\n" + "   public void m() { \n" + "   	int i = 0;\n" + "   	int[] vals = new int[] {2,3,5,7,9};\n" + "   	int sum = 0;\n" + "       //@ decreases  i;\n" + "       while (i < vals.length) {\n" + "             //@ decreases vals.length - i;\n" + "             while (i%2==0) \n" + "                   sum += vals[i++]; } \n" + "}\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_0116_while_RAC_seq() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {\n" + "   public void m() { \n" + "   	int i = 0;\n" + "   	int[] vals = new int[] {2,3,5,7,9};\n" + "   	int sum = 0;\n" + "   	//@ decreases (vals.length - i+10);\n" + "   	//@ decreases (vals.length - i+5);\n" + "   	//@ decreases (vals.length - i+1);\n" + "   	while (i < vals.length) { sum += vals[i++]; } \n" + "	}\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_0117_while_RAC_seq() {
        compileAndExecGivenStatement("X.java", "public class X {\n" + "   public void m() { \n" + "   	int i = 0;\n" + "   	int[] vals = new int[] {2,3,5,7,9};\n" + "   	int sum = 0;\n" + "   	//@ loop_invariant sum >= 0;\n" + "   	//@ loop_invariant i >= 0;\n" + "   	//@ loop_invariant  vals.length >= i;\n" + "   	while (i < vals.length) { sum += vals[i++]; } \n" + "	}\n" + "}\n", "new X().m()");
    }

    public void test_0200_while_synonyms() {
        compileAndExecGivenStatement("X.java", "public class X {\n" + "   public void m() { \n" + "       boolean pred = false;\n" + "   	//@ loop_invariant true;\n" + "   	while (pred) {/**/} \n" + "	}\n" + "}\n", "new X().m()");
    }

    public void test_0201_while_synonyms() {
        compileAndExecGivenStatement("X.java", "public class X {\n" + "   public void m() { \n" + "       boolean pred = false;\n" + "   	//@ loop_invariant_redundantly true;\n" + "   	while (pred) {/**/} \n" + "	}\n" + "}\n", "new X().m()");
    }

    public void test_0202_while_synonyms() {
        compileAndExecGivenStatement("X.java", "public class X {\n" + "   public void m() { \n" + "       boolean pred = false;\n" + "   	//@ maintaining true;\n" + "   	while (pred) {/**/} \n" + "	}\n" + "}\n", "new X().m()");
    }

    public void test_0203_while_synonyms() {
        compileAndExecGivenStatement("X.java", "public class X {\n" + "   public void m() { \n" + "       boolean pred = false;\n" + "   	//@ maintaining_redundantly true;\n" + "   	while (pred) {/**/} \n" + "	}\n" + "}\n", "new X().m()");
    }

    public void test_0204_while_synonyms() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {\n" + "   public void m() { \n" + "   	//@ decreases 0;\n" + "   	while (true) {/**/} \n" + "	}\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_0205_while_synonyms() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {\n" + "   public void m() { \n" + "   	//@ decreases 0;\n" + "   	while (true) {/**/} \n" + "	}\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_0300_while_label() {
        compileAndExecGivenStatement("X.java", "public class X {\n" + "   public void m(boolean b) { \n" + "   	//@ loop_invariant true;\n" + "   	here: while (b) { break here;} \n" + "	}\n" + "}\n", "new X().m(false)");
    }

    public void test_1000_while_sideEffectsInCondition() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {\n" + "   public void m() { \n" + "	   noSideEffects(10);" + "	   withSideEffects(10);" + "	   withSideEffects_varAndInvar(10);" + "	}\n" + "	public static void noSideEffects(int b) {\n" + "		//@ assume b >= 0;\n" + "		int i = b;\n" + "		//@ maintaining i+1 >= 0;\n" + "		//@ decreasing i+1;\n" + "		while (i >= 0) {\n" + "		      i--;\n" + "		}\n" + "		//@ assert i == -1;\n" + "	}\n" + "	public static void withSideEffects_varAndInvar(final int b) {\n" + "		//@ assume b >= 0;\n" + "		int i = b;\n" + "		//@ maintaining i+1 >= 0;\n" + "		//@ decreasing i+1;\n" + "		while (i-- >= 0) {\n" + "		}\n" + "		//@ assert i == -2;\n" + "	}\n" + "	public static void withSideEffects(final int b) {\n" + "		//@ assume b >= 0;\n" + "		int i = b;\n" + "		//@ maintaining i+1 >= 0;\n" + "		while (i-- >= 0) {\n" + "		}\n" + "		//@ assert i == -2;\n" + "	}\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    /** Is loop_invariant in do-while supported. */
    public void test_0500_loop_invariant() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ loop_invariant constant == 1;\n" + "	 do{\n" + "	 	sum += a[i] + constant;\n" + "	    i++;\n" + "       constant++;\n" + "	 }while(i < a.length);\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    /** Is loop_invariant in for supported. */
    public void test_0501_loop_invariant() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ loop_invariant constant == 1;\n" + "	 for (;i < a.length; i++){\n" + "	 	sum += a[i] + constant;\n" + "       constant++;\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    /** multiple invariants */
    public void test_0502_multiple_invariant() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ loop_invariant constant == 1;\n" + "    //@ loop_invariant i == 1;\n" + "    //@ loop_invariant sum > 0;\n" + "	 while(i != 4) {\n" + "	 	sum += a[i++] + constant;\n" + "       constant++;\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_0503_multiple_invariant() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ loop_invariant constant == 1;\n" + "    //@ loop_invariant i == 1;\n" + "    //@ loop_invariant sum > 0;\n" + "	 while(i != 4) {\n" + "	 	sum += a[i++] + constant;\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_0504_multiple_invariant() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ loop_invariant constant == 1;\n" + "    //@ maintaining i > -1;\n" + "    //@ loop_invariant sum == 0;\n" + "	 while(i != 4) {\n" + "	 	sum += a[i++] + constant;\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    /** multiple variants */
    public void test_0525_multiple_variant() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ decreases constant;\n" + "    //@ decreases i;\n" + "    //@ decreases sum;\n" + "	 while(i != 4) {\n" + "	 	sum += a[i++] + constant;\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_0526_multiple_variant() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ decreases constant;\n" + "    //@ decreases i;\n" + "    //@ decreases sum;\n" + "	 while(i != 4) {\n" + "	 	sum += a[i++] + constant++;\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_0527_multiple_variant() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ decreases constant;\n" + "    //@ decreases i;\n" + "    //@ decreases sum;\n" + "	 while(i != 4) {\n" + "	 	sum += a[i++] + constant++;\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    /** mixture of variants and invariants */
    public void test_0550_multiple_variant() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ maintaining sum >= 0;\n" + "    //@ decreases constant;\n" + "    //@ decreases i;\n" + "	 while(i != 4) {\n" + "	 	sum += a[i++] + constant++;\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_0551_multiple_variant() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ maintaining sum >= 0;\n" + "    //@ decreases constant;\n" + "    //@ decreases i;\n" + "	 while(i != 4) {\n" + "	 	sum += a[i++] + constant++;\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_0552_multiple_variant() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ maintaining sum > 0;\n" + "    //@ decreases constant;\n" + "    //@ decreases i;\n" + "	 while(i != 4) {\n" + "	 	sum += a[i++] + constant++;\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    /** variants and invariants inside block */
    public void test_0602_block() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    if (true) {\n" + "    //@ loop_invariant constant == 1;\n" + "    //@ loop_invariant i == 1;\n" + "    //@ loop_invariant sum > 0;\n" + "	 while(i != 4) {\n" + "	 	sum += a[i++] + constant;\n" + "       constant++;\n" + "	 }\n" + "    }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_0603_block() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    if (true) {\n" + "    //@ loop_invariant constant == 1;\n" + "    //@ loop_invariant i == 1;\n" + "    //@ loop_invariant sum > 0;\n" + "	 while(i != 4) {\n" + "	 	sum += a[i++] + constant;\n" + "	 }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_0604_block() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    if (true) {\n" + "    //@ loop_invariant constant == 1;\n" + "    //@ maintaining i > -1;\n" + "    //@ loop_invariant sum == 0;\n" + "	 while(i != 4) {\n" + "	 	sum += a[i++] + constant;\n" + "	 }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_0605_block() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    if (true) {\n" + "    //@ decreases constant;\n" + "    //@ decreases i;\n" + "    //@ decreases sum;\n" + "	 while(i != 4) {\n" + "	 	sum += a[i++] + constant;\n" + "	 }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_0606_block() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    if (true) {\n" + "    //@ decreases constant;\n" + "    //@ decreases i;\n" + "    //@ decreases sum;\n" + "	 while(i != 4) {\n" + "	 	sum += a[i++] + constant++;\n" + "	 }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_0607_block() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    if (true) {\n" + "    //@ decreases constant;\n" + "    //@ decreases i;\n" + "    //@ decreases sum;\n" + "	 while(i != 4) {\n" + "	 	sum += a[i++] + constant++;\n" + "	 }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_0608_block() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    if (true) {\n" + "    //@ maintaining sum >= 0;\n" + "    //@ decreases constant;\n" + "    //@ decreases i;\n" + "	 while(i != 4) {\n" + "	 	sum += a[i++] + constant++;\n" + "	 }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_0609_block() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    if (true) {\n" + "    //@ maintaining sum < 0;\n" + "    //@ decreases constant;\n" + "    //@ decreases i;\n" + "	 while(i < a.length ) {\n" + "	 	sum += a[i++] + constant++;\n" + "	 }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_0610_block() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    if (true) {\n" + "    //@ maintaining sum > 0;\n" + "    //@ decreases constant;\n" + "    //@ decreases i;\n" + "	 while(i != 4) {\n" + "	 	sum += a[i++] + constant++;\n" + "	 }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    /** variants and invariants inside twolevel_block */
    public void test_0652_twolevel_block() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    if (true) {\n" + "    if (true) {\n" + "    //@ loop_invariant constant == 1;\n" + "    //@ loop_invariant i == 1;\n" + "    //@ loop_invariant sum > 0;\n" + "	 while(i != 4) {\n" + "	 	sum += a[i++] + constant;\n" + "       constant++;\n" + "	 }\n" + "	 }\n" + "    }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_0653_twolevel_block() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    if (true) {\n" + "    if (true) {\n" + "    //@ loop_invariant constant == 1;\n" + "    //@ loop_invariant i == 1;\n" + "    //@ loop_invariant sum > 0;\n" + "	 while(i != 4) {\n" + "	 	sum += a[i++] + constant;\n" + "	 }\n" + "	 }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_0654_twolevel_block() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    if (true) {\n" + "    if (true) {\n" + "    //@ loop_invariant constant == 1;\n" + "    //@ maintaining i > -1;\n" + "    //@ loop_invariant sum == 0;\n" + "	 while(i != 4) {\n" + "	 	sum += a[i++] + constant;\n" + "	 }\n" + "	 }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_0655_twolevel_block() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    if (true) {\n" + "    if (true) {\n" + "    //@ decreases constant;\n" + "    //@ decreases i;\n" + "    //@ decreases sum;\n" + "	 while(i != 4) {\n" + "	 	sum += a[i++] + constant;\n" + "	 }\n" + "	 }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_0656_twolevel_block() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    if (true) {\n" + "    if (true) {\n" + "    //@ decreases constant;\n" + "    //@ decreases i;\n" + "    //@ decreases sum;\n" + "	 while(i != 4) {\n" + "	 	sum += a[i++] + constant++;\n" + "	 }\n" + "	 }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_0657_twolevel_block() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    if (true) {\n" + "    if (true) {\n" + "    //@ decreases constant;\n" + "    //@ decreases i;\n" + "    //@ decreases sum;\n" + "	 while(i != 4) {\n" + "	 	sum += a[i++] + constant++;\n" + "	 }\n" + "	 }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_0658_twolevel_block() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    if (true) {\n" + "    if (true) {\n" + "    //@ maintaining sum >= 0;\n" + "    //@ decreases constant;\n" + "    //@ decreases i;\n" + "	 while(i != 4) {\n" + "	 	sum += a[i++] + constant++;\n" + "	 }\n" + "	 }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_0659_twolevel_block() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    if (true) {\n" + "    if (true) {\n" + "    //@ maintaining sum < 0;\n" + "    //@ decreases constant;\n" + "    //@ decreases i;\n" + "	 while(i < a.length ) {\n" + "	 	sum += a[i++] + constant++;\n" + "	 }\n" + "	 }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_0610_twolevel_block() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    if (true) {\n" + "    if (true) {\n" + "    //@ maintaining sum > 0;\n" + "    //@ decreases constant;\n" + "    //@ decreases i;\n" + "	 while(i != 4) {\n" + "	 	sum += a[i++] + constant++;\n" + "	 }\n" + "	 }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    /** inner jml annotations */
    public void test_0700_inner_with_some_statements() {
        compileAndExecGivenStatement("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ maintaining true;\n" + "	 while(i < 4) {\n" + "	 	i++;\n" + "       //@ maintaining true;\n" + "	    while(i < 4) {\n" + "	 	  i++;\n" + "         //@ maintaining true;\n" + "	      while(i < 4) {\n" + "	 	   i++;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()");
    }

    public void test_0701_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ maintaining true;\n" + "	 while(i < 4) {\n" + "	 	i++;\n" + "       //@ maintaining true;\n" + "	    while(i < 4) {\n" + "	 	  i++;\n" + "         //@ maintaining false;\n" + "	      while(i < 4) {\n" + "	 	   i++;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_0702_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ maintaining true;\n" + "	 while(i < 4) {\n" + "	 	i++;\n" + "       //@ maintaining false;\n" + "	    while(i < 4) {\n" + "	 	  i++;\n" + "         //@ maintaining true;\n" + "	      while(i < 4) {\n" + "	 	   i++;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_0703_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ maintaining false;\n" + "	 while(i < 4) {\n" + "	 	i++;\n" + "       //@ maintaining true;\n" + "	    while(i < 4) {\n" + "	 	  i++;\n" + "         //@ maintaining true;\n" + "	      while(i < 4) {\n" + "	 	   i++;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_0704_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ maintaining true;\n" + "	 while(i < 4) {\n" + "	 	i++;\n" + "       //@ maintaining false;\n" + "	    while(i < 4) {\n" + "	 	  i++;\n" + "         //@ maintaining false;\n" + "	      while(i < 4) {\n" + "	 	   i++;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_0705_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ maintaining true;\n" + "	 while(i < 4) {\n" + "	 	i++;\n" + "	    while(i < 4) {\n" + "	 	  i++;\n" + "         //@ maintaining false;\n" + "	      while(i < 4) {\n" + "	 	   i++;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_0706_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ maintaining true;\n" + "	 while(i < 4) {\n" + "	 	i++;\n" + "       //@ maintaining false;\n" + "	    while(i < 4) {\n" + "	 	  i++;\n" + "	      while(i < 4) {\n" + "	 	   i++;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_0707_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "	 while(i < 4) {\n" + "	 	i++;\n" + "       //@ maintaining false;\n" + "	    while(i < 4) {\n" + "	 	  i++;\n" + "         //@ maintaining true;\n" + "	      while(i < 4) {\n" + "	 	   i++;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_0708_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ decreases sum;\n" + "	 while(i < 4) {\n" + "	 	sum--;\n" + "       //@ decreases constant;\n" + "	    while(i < 4) {\n" + "	 	  constant--;\n" + "         //@ decreases i;\n" + "	      while(i < 4) {\n" + "	 	   i++;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_0709_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ decreases sum;\n" + "	 while(i < 4) {\n" + "	 	sum--;\n" + "		i+=2;\n" + "       //@ decreases constant;\n" + "	    while(i < 4) {\n" + "	 	  constant++;\n" + "         //@ decreases i;\n" + "	      while(i < 4) {\n" + "	 	   i--;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_0710_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ decreases sum;\n" + "	 while(i < 4) {\n" + "	 	sum++;\n" + "       //@ decreases constant;\n" + "	    while(i < 4) {\n" + "	 	  constant--;\n" + "         //@ decreases i;\n" + "	      while(i < 4) {\n" + "	 	   i--;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_0711_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ decreases sum;\n" + "	 while(i < 4) {\n" + "	 	sum++;\n" + "       //@ decreases constant;\n" + "	    while(i < 4) {\n" + "	 	  constant--;\n" + "         //@ decreases i;\n" + "	      while(i < 4) {\n" + "	 	   i++;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_0712_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ decreases sum;\n" + "	 while(i < 4) {\n" + "	 	sum--;\n" + "       //@ decreases constant;\n" + "	    while(i < 4) {\n" + "	 	  constant++;\n" + "         //@ decreases i;\n" + "	      while(i < 4) {\n" + "	 	   i++;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_0713_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ decreases i;\n" + "	 while(i < 4) {\n" + "	 	i--;\n" + "	    while(i < 4) {\n" + "	 	  i+=3;\n" + "         //@ decreases i;\n" + "	      while(i < 4) {\n" + "	 	   i--;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_0714_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ decreases i;\n" + "	 while(i < 4) {\n" + "	 	i++;\n" + "       //@ decreases i;\n" + "	    while(i < 4) {\n" + "	 	  i++;\n" + "	      while(i < 4) {\n" + "	 	   i+=3;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_0715_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "	 while(i < 4) {\n" + "	 	i++;\n" + "       //@ decreases i;\n" + "	    while(i < 4) {\n" + "	 	  i++;\n" + "         //@ decreases i;\n" + "	      while(i < 4) {\n" + "	 	   i++;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_0716_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ decreases i;\n" + "	 while(i < 4) {\n" + "	 	i++;\n" + "       //@ decreases i;\n" + "	    while(i < 4) {\n" + "	 	  i++;\n" + "	      while(i++ < 4) {\n" + "	 	   //@ assert true;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_0717_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "	 while(i < 4) {\n" + "	 	i++;\n" + "       //@ decreases i;\n" + "	    while(i < 4) {\n" + "	 	  i++;\n" + "		  //@ assert false;\n" + "         //@ decreases i;\n" + "	      while(i < 4) {\n" + "	 	   i++;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLAssertError.class);
    }

    public void test_0718_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ maintaining true;\n" + "	 while(i < 3) {\n" + "	 	i++;\n" + "		//@ assert false;\n" + "       //@ decreases i;\n" + "	    while(i < 4) {\n" + "	 	  i+=3;\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLAssertError.class);
    }

    public void test_0719_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "	 while(i < 4) {\n" + "	 	i++;\n" + "       //@ decreases i;\n" + "	    while(i < 4) {\n" + "	 	  i--;\n" + "         //@ assert false;\n" + "	      while(i < 4) {\n" + "	 	   i++;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLAssertError.class);
    }

    public void test_0720_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "	 while(i < 4) {\n" + "	 	i++;\n" + "       //@ decreases i;\n" + "	    while(i < 4) {\n" + "	 	  i--;\n" + "	      while(i < 4) {\n" + "         //@ assert false;\n" + "	 	   i++;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLAssertError.class);
    }

    public void test_0721_with_empty_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ decreases i;\n" + "	 while(i++ < 2)\n" + "       //@ decreases i - 1;\n" + "	    while(i++ < 4);\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_0722_with_empty_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ maintaining true;\n" + "	 while(i < 4) \n" + "       //@ maintaining false;\n" + "	    while(i < 4)\n" + "		  i--; \n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_0723_with_empty_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ maintaining false;\n" + "	 while(i++ < 4)\n" + "       //@ maintaining true;\n" + "	    while(++i < 4);\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_0724_function_call() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {\n" + "  //@ requires false;\n" + "  void func() { }\n" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "	 //@ maintaining true;\n" + "	 while(++i < 4) {\n" + "		func();\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLInternalPreconditionError.class);
    }

    public void test_0725_assert_and_jmlannotation() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "	 //@ maintaining true;" + "	 while(++i < 4) {\n" + "		//@ assert false;\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLAssertError.class);
    }

    public void test_0726_assert_and_jmlannotation() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "	 //@ maintaining false;" + "	 while(++i < 4) {\n" + "		//@ assert true;\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_0727_while_and_jmlannotation() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "	 //@ maintaining false;" + "	 while(++i < 4) {\n" + "		while (++i < 5) {\n" + "			i += 3;\n" + "		}\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_0728_while_and_jmlannotation() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "	 //@ maintaining false;\n" + "	 while(++i < 4) {\n" + "	    //@ maintaining true;\n" + "		while (++i < 5) {\n" + "			i += 3;\n" + "			//@ assert false;\n" + "		}\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_0728a_while_and_jmlannotation() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "	 //@ maintaining true;" + "	 while(++i < 4) {\n" + "		i += 2;\n" + "		while (++i < 5) {\n" + "			i += 3;\n" + "			//@ assert false;\n" + "		}\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLAssertError.class);
    }

    public void test_0729_while_and_jmlannotation() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "	 //@ maintaining false;" + "	 while(i < 4) {\n" + "       i++;\n" + "		//@ maintaining true;\n" + "		while (++i < 5) {\n" + "			i += 3;\n" + "		}\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_0730_while_and_jmlannotation() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "	 //@ maintaining true;" + "	 while(i < 4) {\n" + "       i++;\n" + "       while (i++ < 4) { }\n" + "		//@ maintaining true;\n" + "		while (++i < 10) {\n" + "			i += 3;\n" + "			//@ assert false;\n" + "		}\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLAssertError.class);
    }

    public void test_0731_while_and_jmlannotation() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "	 while(i < 4) {\n" + "       i++;\n" + "       while (i++ < 4) { }\n" + "		//@ maintaining true;\n" + "		while (++i < 10) {\n" + "			i += 3;\n" + "			//@ assert false;\n" + "		}\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLAssertError.class);
    }

    public void test_0732_while_and_jmlannotation() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "	 while(i < 4) {\n" + "       i++;\n" + "       while (i++ < 4) { }\n" + "		while (++i < 10) {\n" + "			i += 3;\n" + "			//@ assert false;\n" + "		}\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLAssertError.class);
    }

    public void test_0733_while_and_jmlannotation() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "	 while(i < 4) {\n" + "       i++;\n" + "       while (i++ < 4) { }\n" + "		if (true)\n" + "		 while (++i < 10) {\n" + "			i += 3;\n" + "			//@ assert false;\n" + "		 }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLAssertError.class);
    }

    public void test_0734_while_and_jmlannotation() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "	 while(i < 4) {\n" + "       i++;\n" + "		//@ assert true;\n" + "       while (i++ < 4) { }\n" + "		//@ maintaining false;\n" + "		while (++i < 10) {\n" + "			i += 3;\n" + "			//@ assert false;\n" + "		}\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_0735_while_and_jmlannotation() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "	 while(i < 4) {\n" + "       i++;\n" + "		//@ assert false;\n" + "       while (i++ < 4) { }\n" + "		//@ maintaining false;\n" + "		while (++i < 10) {\n" + "			i += 3;\n" + "			//@ assert false;\n" + "		}\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLAssertError.class);
    }

    public void test_0736_while_and_jmlannotation() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "	 while(i < 4) {\n" + "       i++;\n" + "		//@ assert true;\n" + "       while (i++ < 4) { }\n" + "		//@ maintaining true;\n" + "		while (++i < 10) {\n" + "			i += 3;\n" + "			//@ assert false;\n" + "		}\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLAssertError.class);
    }

    public void test_0001_for_true() {
        compileAndExecGivenStatement("X.java", "public class X {\n" + "   public void m(boolean b) { \n" + "   	//@ loop_invariant true;\n" + "   	for (;b;) {} \n" + "	}\n" + "}\n", "new X().m(false)");
    }

    public void test_0002_for_expr() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {\n" + "   public void m(int i, String s) { \n" + "   	//@ loop_invariant s.length() * 2 < i;\n" + "   	for (;true;) {} \n" + "	}\n" + "}\n", "new X().m(0, \"hello\")", null, JMLLoopInvariantError.class);
    }

    public void test_0004_for_RAC_valid() {
        compileAndExecGivenStatement("X.java", "public class X {\n" + "   public void m() { \n" + "   	int i = 0;\n" + "   	int[] vals = new int[] {2,3,5,7,9};\n" + "   	int sum = 0;\n" + "   	//@ loop_invariant sum >= 0;\n" + "   	for (;i < vals.length;) { sum += vals[i++]; } \n" + "	}\n" + "}\n", "new X().m()");
    }

    public void test_0005_for_RAC_invalid() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {\n" + "   public void m() { \n" + "   	int i = 0;\n" + "   	int[] vals = new int[] {2,3,5,7,9};\n" + "   	   int sum = 0;\n" + "   	   //@ loop_invariant sum == 111;\n" + "   	   for (;i < vals.length;) { sum += vals[i++]; } \n" + "	}\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_0006_for_RAC_constTrue() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {\n" + "   public void m() { \n" + "   	int i = 0;\n" + "   	int[] vals = new int[] {2,3,5,7,9};\n" + "   	   int sum = 0;\n" + "   	   //@ loop_invariant sum == 111;\n" + "   	   for (;true;) { sum += vals[i++]; } \n" + "	}\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_0007_for_RAC_constFalse() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {\n" + "   static final boolean DBG = false;" + "   void m(X x) {\n" + "   	int i = 0;\n" + "   	int[] vals = new int[] {2,3,5,7,9};\n" + "   	   int sum = 0;\n" + "   	   //@ loop_invariant sum == 111;\n" + "   	   for (;x.DBG;) { /*empty*/ } \n" + "	}\n" + "}\n", "new X().m(new X())", null, JMLLoopInvariantError.class);
    }

    public void test_0008_for_RAC_noBody() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {\n" + "   public void m() { \n" + "   	int i = 0;\n" + "   	int[] vals = new int[] {2,3,5,7,9};\n" + "   	   int sum = 0;\n" + "   	   //@ loop_invariant sum == 111;\n" + "   	   for (;i == i;) { /* empty */ } \n" + "	}\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_0009_for_RAC_throwBody() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {\n" + "   boolean bool() { return true; }\n" + "   void m() {" + "   	int i = 0;\n" + "   	int[] vals = new int[] {2,3,5,7,9};\n" + "   	   int sum = 0;\n" + "   	   //@ loop_invariant sum == 111;\n" + "   	   for (;bool();) { throw new RuntimeException(); } \n" + "	}\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_0010_for_RAC_break_valid() {
        compileAndExecGivenStatement("X.java", "public class X {\n" + "   public void m() { \n" + "   	int i = 0;\n" + "   	int[] vals = new int[] {2,3,5,7,9};\n" + "   	int sum = 0;\n" + "   	//@ loop_invariant sum >= 0;\n" + "   	for (;i < vals.length;) { break; } \n" + "	}\n" + "}\n", "new X().m()");
    }

    public void test_0011_for_RAC_break_invalid() {
        if (testIsDisabled("FIXME")) return;
        compileAndExecGivenStatement("X.java", "public class X {\n" + "   public void m() { \n" + "   	int i = 0;\n" + "   	int[] vals = new int[] {2,3,5,7,9};\n" + "       try {\n" + "   	   int sum = 0;\n" + "   	   //@ loop_invariant sum >= 0;\n" + "   	   for (;i < vals.length;) { sum = -1; break; } \n" + "	    } catch (Error e) {\n" + "          System.out.println(e.toString());\n" + "	    }\n" + "	}\n" + "}\n", "new X().m()");
    }

    public void test_0012_for_true() {
        compileAndExecGivenStatement("X.java", "public class X {\n" + "   public void m(boolean b) { \n" + "   	//@ maintaining true;\n" + "   	for (;b;) {} \n" + "	}\n" + "}\n", "new X().m(false)");
    }

    public void test_0013_for_RAC_break_valid() {
        if (testIsDisabled("FIXME")) return;
        compileAndExecGivenStatement("X.java", "public class X {\n" + "   public void m() { \n" + "   	int i = 0;\n" + "   	int j = 100;\n" + "   	int[] vals = new int[] {2,3,5,7,9};\n" + "   	int sum = 0;\n" + "   	//@ loop_invariant sum >= 0;\n" + "   	for (;i < vals.length;) {\n" + "   	      //@ loop_invariant i%2 < 2;\n" + "   	      for (;i % 2 > 2;) { j--;}\n" + "   	      i--;\n" + "        }\n" + "	}\n" + "}\n", "new X().m()");
    }

    public void test_0014_for_RAC_seq() {
        compileAndExecGivenStatement("X.java", "public class X {\n" + "   public void m() { \n" + "   	int i = 0;\n" + "   	int[] vals = new int[] {2,3,5,7,9};\n" + "   	int sum = 0;\n" + "   	//@ loop_invariant sum >= 0;\n" + "   	//@ loop_invariant 0 <= 0;\n" + "   	//@ loop_invariant sum >= 0;\n" + "   	//@ loop_invariant 0 <= 0;\n" + "   	for (;i < vals.length;) { sum += vals[i++]; } \n" + "	}\n" + "}\n", "new X().m()");
    }

    public void test_0015_for_leq_expr_RAC() {
        compileAndExecGivenStatement("X.java", "public class X {\n" + "   public void m() { \n" + "   	int i = 0;\n" + "   	int[] vals = new int[] {2,3,5,7,9};\n" + "   	int sum = 0;\n" + "   	//@ loop_invariant 0 <= sum;\n" + "   	for (;i < vals.length;) { sum += vals[i++]; } \n" + "	}\n" + "}\n", "new X().m()");
    }

    public void test_0101_for_variant_int() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {\n" + "   public void m(int i) { \n" + "   	//@ decreases i;\n" + "   	for (;i > 0;) {} \n" + "	}\n" + "}\n", "new X().m(1)", null, JMLLoopVariantError.class);
    }

    public void test_0110_for_RAC_valid_variant() {
        compileAndExecGivenStatement("X.java", "public class X {\n" + "   public void m() { \n" + "   	int i = 0;\n" + "   	int[] vals = new int[] {2,3,5,7,9};\n" + "   	int sum = 0;\n" + "   	//@ decreases vals.length - i;\n" + "   	for (;i < vals.length;) { sum += vals[i++]; } \n" + "	}\n" + "}\n", "new X().m()");
    }

    public void test_0111_for_RAC_invalid_variant() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {\n" + "   public void m() { \n" + "   	int i = 3;\n" + "   	int[] vals = new int[] {2,3,5,7,9};\n" + "   	int sum = 0;\n" + "   	//@ decreases i;\n" + "   	for (;i < vals.length;) { sum += vals[i++]; } \n" + "	}\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_0112_for_RAC_invalid_variant() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {\n" + "   public static void f(int[] vals) { \n" + "   	int i = 20;\n" + "   	int sum = 0;\n" + "   	//@ decreases i;\n" + "   	for (;i <= 20;) { i--; } \n" + "   }\n" + "   public void m() { \n" + "   	int[] vals = new int[] {2,3,5,7,9};\n" + "       f(vals);\n" + "	}\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_0113_for_RAC_valid_variant() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {\n" + "   public void m() { \n" + "   	int i = 0;\n" + "   	int[] vals = new int[] {2,3,5,7,9};\n" + "   	int sum = 0;\n" + "       //@ decreases vals.length - i;\n" + "       for (;i < vals.length;) {\n" + "             //@ decreases vals.length - i;\n" + "             for (;i%2==0;) \n" + "                   sum += vals[i--]; } \n" + "	}\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_0114_for_RAC_invalid_variant() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {\n" + "   public void m() { \n" + "   	int i = 0;\n" + "   	int[] vals = new int[] {2,3,5,7,9};\n" + "   	int sum = 0;\n" + "       //@ decreases vals.length - i;\n" + "       for (;i < vals.length;) {\n" + "             //@ decreases i;\n" + "             for (;i%2==0;) \n" + "                   sum += vals[i++]; } \n" + "	}\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_0115_for_RAC_invalid_variant() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {\n" + "   public void m() { \n" + "   	int i = 0;\n" + "   	int[] vals = new int[] {2,3,5,7,9};\n" + "   	int sum = 0;\n" + "       //@ decreases  i;\n" + "       for (;i < vals.length;) {\n" + "             //@ decreases vals.length - i;\n" + "             for (;i%2==0;) \n" + "                   sum += vals[i++]; } \n" + "	}\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_0116_for_RAC_seq() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {\n" + "   public void m() { \n" + "   	int i = 0;\n" + "   	int[] vals = new int[] {2,3,5,7,9};\n" + "   	int sum = 0;\n" + "   	//@ decreases (vals.length - i+10);\n" + "   	//@ decreases (vals.length - i+5);\n" + "   	//@ decreases (vals.length - i+1);\n" + "   	for (;i < vals.length;) { sum += vals[i++]; } \n" + "	}\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_0117_for_RAC_seq() {
        compileAndExecGivenStatement("X.java", "public class X {\n" + "   public void m() { \n" + "   	int i = 0;\n" + "   	int[] vals = new int[] {2,3,5,7,9};\n" + "   	int sum = 0;\n" + "   	//@ loop_invariant sum >= 0;\n" + "   	//@ loop_invariant i >= 0;\n" + "   	//@ loop_invariant  vals.length >= i;\n" + "   	for (;i < vals.length;) { sum += vals[i++]; } \n" + "	}\n" + "}\n", "new X().m()");
    }

    public void test_0200_for_synonyms() {
        compileAndExecGivenStatement("X.java", "public class X {\n" + "   public void m() { \n" + "       boolean pred = false;\n" + "   	//@ loop_invariant true;\n" + "   	for (;pred;) {/**/} \n" + "	}\n" + "}\n", "new X().m()");
    }

    public void test_0201_for_synonyms() {
        compileAndExecGivenStatement("X.java", "public class X {\n" + "   public void m() { \n" + "       boolean pred = false;\n" + "   	//@ loop_invariant_redundantly true;\n" + "   	for (;pred;) {/**/} \n" + "	}\n" + "}\n", "new X().m()");
    }

    public void test_0202_for_synonyms() {
        compileAndExecGivenStatement("X.java", "public class X {\n" + "   public void m() { \n" + "       boolean pred = false;\n" + "   	//@ maintaining true;\n" + "   	for (;pred;) {/**/} \n" + "	}\n" + "}\n", "new X().m()");
    }

    public void test_0203_for_synonyms() {
        compileAndExecGivenStatement("X.java", "public class X {\n" + "   public void m() { \n" + "       boolean pred = false;\n" + "   	//@ maintaining_redundantly true;\n" + "   	for (;pred;) {/**/} \n" + "	}\n" + "}\n", "new X().m()");
    }

    public void test_0204_for_synonyms() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {\n" + "   public void m() { \n" + "   	//@ decreases 0;\n" + "   	for (;true;) {/**/} \n" + "	}\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_0205_for_synonyms() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {\n" + "   public void m() { \n" + "   	//@ decreases 0;\n" + "   	for (;true;) {/**/} \n" + "	}\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_0300_for_label() {
        compileAndExecGivenStatement("X.java", "public class X {\n" + "   public void m(boolean b) { \n" + "   	//@ loop_invariant true;\n" + "   	here: for (;b;) { break here;} \n" + "	}\n" + "}\n", "new X().m(false)");
    }

    public void test_0401_for_invariant() {
        compileAndExecGivenStatement("X.java", "public class X {\n" + "   public void m(boolean b) { \n" + "   	//@ loop_invariant true;\n" + "   	for (int i=0; i<10; i++) {} \n" + "	}\n" + "}\n", "new X().m(false)");
    }

    public void test_1000_for_sideEffectsInCondition() {
    }

    /** multiple invariants */
    public void test_1502_multiple_invariant() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ loop_invariant constant == 1;\n" + "    //@ loop_invariant i == 1;\n" + "    //@ loop_invariant sum > 0;\n" + "	 for(i = 0, sum = 1;i != 4;i++, sum+=2) {\n" + "	 	sum += a[i++] + constant;\n" + "       constant++;\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_1503_multiple_invariant() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ loop_invariant constant == 1;\n" + "    //@ loop_invariant i == 1;\n" + "    //@ loop_invariant sum > 0;\n" + "	 for(;i != 4;) {\n" + "	 	sum += a[i++] + constant;\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_1504_multiple_invariant() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ loop_invariant constant == 1;\n" + "    //@ maintaining i > -1;\n" + "    //@ loop_invariant sum == 0;\n" + "	 for(;i != 4;) {\n" + "	 	sum += a[i++] + constant;\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    /** multiple variants */
    public void test_1525_multiple_variant() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ decreases constant;\n" + "    //@ decreases i;\n" + "    //@ decreases sum;\n" + "	 for(;i != 4;) {\n" + "	 	sum += a[i++] + constant;\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_1526_multiple_variant() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ decreases constant;\n" + "    //@ decreases i;\n" + "    //@ decreases sum;\n" + "	 for(;i != 4;) {\n" + "	 	sum += a[i++] + constant++;\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_1527_multiple_variant() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ decreases constant;\n" + "    //@ decreases i;\n" + "    //@ decreases sum;\n" + "	 for(;i != 4;) {\n" + "	 	sum += a[i++] + constant++;\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    /** mixture of variants and invariants */
    public void test_1550_multiple_variant() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ maintaining sum >= 0;\n" + "    //@ decreases constant;\n" + "    //@ decreases i;\n" + "	 for(;i != 4;) {\n" + "	 	sum += a[i++] + constant++;\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_1551_multiple_variant() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ maintaining sum >= 0;\n" + "    //@ decreases constant;\n" + "    //@ decreases i;\n" + "	 for(;i != 4;) {\n" + "	 	sum += a[i++] + constant++;\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_1552_multiple_variant() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ maintaining sum > 0;\n" + "    //@ decreases constant;\n" + "    //@ decreases i;\n" + "	 for(;i != 4;) {\n" + "	 	sum += a[i++] + constant++;\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    /** variants and invariants inside block */
    public void test_1602_block() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    if (true) {\n" + "    //@ loop_invariant constant == 1;\n" + "    //@ loop_invariant i == 1;\n" + "    //@ loop_invariant sum > 0;\n" + "	 for(;i != 4;) {\n" + "	 	sum += a[i++] + constant;\n" + "       constant++;\n" + "	 }\n" + "    }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_1603_block() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    if (true) {\n" + "    //@ loop_invariant constant == 1;\n" + "    //@ loop_invariant i == 1;\n" + "    //@ loop_invariant sum > 0;\n" + "	 for(;i != 4;) {\n" + "	 	sum += a[i++] + constant;\n" + "	 }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_1604_block() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    if (true) {\n" + "    //@ loop_invariant constant == 1;\n" + "    //@ maintaining i > -1;\n" + "    //@ loop_invariant sum == 0;\n" + "	 for(;i != 4;) {\n" + "	 	sum += a[i++] + constant;\n" + "	 }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_1605_block() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    if (true) {\n" + "    //@ decreases constant;\n" + "    //@ decreases i;\n" + "    //@ decreases sum;\n" + "	 for(;i != 4;) {\n" + "	 	sum += a[i++] + constant;\n" + "	 }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_1606_block() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    if (true) {\n" + "    //@ decreases constant;\n" + "    //@ decreases i;\n" + "    //@ decreases sum;\n" + "	 for(;i != 4;) {\n" + "	 	sum += a[i++] + constant++;\n" + "	 }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_1607_block() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    if (true) {\n" + "    //@ decreases constant;\n" + "    //@ decreases i;\n" + "    //@ decreases sum;\n" + "	 for(;i != 4;) {\n" + "	 	sum += a[i++] + constant++;\n" + "	 }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_1608_block() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    if (true) {\n" + "    //@ maintaining sum >= 0;\n" + "    //@ decreases constant;\n" + "    //@ decreases i;\n" + "	 for(;i != 4;) {\n" + "	 	sum += a[i++] + constant++;\n" + "	 }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_1609_block() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    if (true) {\n" + "    //@ maintaining sum < 0;\n" + "    //@ decreases constant;\n" + "    //@ decreases i;\n" + "	 for(;i < a.length ;) {\n" + "	 	sum += a[i++] + constant++;\n" + "	 }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_1610_block() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    if (true) {\n" + "    //@ maintaining sum > 0;\n" + "    //@ decreases constant;\n" + "    //@ decreases i;\n" + "	 for(;i != 4;) {\n" + "	 	sum += a[i++] + constant++;\n" + "	 }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    /** variants and invariants inside twolevel_block */
    public void test_1652_twolevel_block() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    if (true) {\n" + "    if (true) {\n" + "    //@ loop_invariant constant == 1;\n" + "    //@ loop_invariant i == 1;\n" + "    //@ loop_invariant sum > 0;\n" + "	 for(;i != 4;) {\n" + "	 	sum += a[i++] + constant;\n" + "       constant++;\n" + "	 }\n" + "	 }\n" + "    }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_1653_twolevel_block() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    if (true) {\n" + "    if (true) {\n" + "    //@ loop_invariant constant == 1;\n" + "    //@ loop_invariant i == 1;\n" + "    //@ loop_invariant sum > 0;\n" + "	 for(;i != 4;) {\n" + "	 	sum += a[i++] + constant;\n" + "	 }\n" + "	 }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_1654_twolevel_block() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    if (true) {\n" + "    if (true) {\n" + "    //@ loop_invariant constant == 1;\n" + "    //@ maintaining i > -1;\n" + "    //@ loop_invariant sum == 0;\n" + "	 for(;i != 4;) {\n" + "	 	sum += a[i++] + constant;\n" + "	 }\n" + "	 }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_1655_twolevel_block() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    if (true) {\n" + "    if (true) {\n" + "    //@ decreases constant;\n" + "    //@ decreases i;\n" + "    //@ decreases sum;\n" + "	 for(;i != 4;) {\n" + "	 	sum += a[i++] + constant;\n" + "	 }\n" + "	 }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_1656_twolevel_block() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    if (true) {\n" + "    if (true) {\n" + "    //@ decreases constant;\n" + "    //@ decreases i;\n" + "    //@ decreases sum;\n" + "	 for(;i != 4;) {\n" + "	 	sum += a[i++] + constant++;\n" + "	 }\n" + "	 }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_1657_twolevel_block() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    if (true) {\n" + "    if (true) {\n" + "    //@ decreases constant;\n" + "    //@ decreases i;\n" + "    //@ decreases sum;\n" + "	 for(;i != 4;) {\n" + "	 	sum += a[i++] + constant++;\n" + "	 }\n" + "	 }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_1658_twolevel_block() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    if (true) {\n" + "    if (true) {\n" + "    //@ maintaining sum >= 0;\n" + "    //@ decreases constant;\n" + "    //@ decreases i;\n" + "	 for(;i != 4;) {\n" + "	 	sum += a[i++] + constant++;\n" + "	 }\n" + "	 }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_1659_twolevel_block() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    if (true) {\n" + "    if (true) {\n" + "    //@ maintaining sum < 0;\n" + "    //@ decreases constant;\n" + "    //@ decreases i;\n" + "	 for(;i < a.length ;) {\n" + "	 	sum += a[i++] + constant++;\n" + "	 }\n" + "	 }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_1610_twolevel_block() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    if (true) {\n" + "    if (true) {\n" + "    //@ maintaining sum > 0;\n" + "    //@ decreases constant;\n" + "    //@ decreases i;\n" + "	 for(;i != 4;) {\n" + "	 	sum += a[i++] + constant++;\n" + "	 }\n" + "	 }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    /** inner jml annotations */
    public void test_1700_inner_with_some_statements() {
        compileAndExecGivenStatement("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ maintaining true;\n" + "	 for(;i < 4;) {\n" + "	 	i++;\n" + "       //@ maintaining true;\n" + "	    for(;i < 4;) {\n" + "	 	  i++;\n" + "         //@ maintaining true;\n" + "	      for(;i < 4;) {\n" + "	 	   i++;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()");
    }

    public void test_1701_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ maintaining true;\n" + "	 for(;i < 4;) {\n" + "	 	i++;\n" + "       //@ maintaining true;\n" + "	    for(;i < 4;) {\n" + "	 	  i++;\n" + "         //@ maintaining false;\n" + "	      for(;i < 4;) {\n" + "	 	   i++;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_1702_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ maintaining true;\n" + "	 for(;i < 4;) {\n" + "	 	i++;\n" + "       //@ maintaining false;\n" + "	    for(;i < 4;) {\n" + "	 	  i++;\n" + "         //@ maintaining true;\n" + "	      for(;i < 4;) {\n" + "	 	   i++;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_1703_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ maintaining false;\n" + "	 for(;i < 4;) {\n" + "	 	i++;\n" + "       //@ maintaining true;\n" + "	    for(;i < 4;) {\n" + "	 	  i++;\n" + "         //@ maintaining true;\n" + "	      for(;i < 4;) {\n" + "	 	   i++;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_1704_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ maintaining true;\n" + "	 for(;i < 4;) {\n" + "	 	i++;\n" + "       //@ maintaining false;\n" + "	    for(;i < 4;) {\n" + "	 	  i++;\n" + "         //@ maintaining false;\n" + "	      for(;i < 4;) {\n" + "	 	   i++;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_1705_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ maintaining true;\n" + "	 for(;i < 4;) {\n" + "	 	i++;\n" + "	    for(;i < 4;) {\n" + "	 	  i++;\n" + "         //@ maintaining false;\n" + "	      for(;i < 4;) {\n" + "	 	   i++;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_1706_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ maintaining true;\n" + "	 for(;i < 4;) {\n" + "	 	i++;\n" + "       //@ maintaining false;\n" + "	    for(;i < 4;) {\n" + "	 	  i++;\n" + "	      for(;i < 4;) {\n" + "	 	   i++;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_1707_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "	 for(;i < 4;) {\n" + "	 	i++;\n" + "       //@ maintaining false;\n" + "	    for(;i < 4;) {\n" + "	 	  i++;\n" + "         //@ maintaining true;\n" + "	      for(;i < 4;) {\n" + "	 	   i++;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_1708_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ decreases sum;\n" + "	 for(;i < 4;) {\n" + "	 	sum--;\n" + "       //@ decreases constant;\n" + "	    for(;i < 4;) {\n" + "	 	  constant--;\n" + "         //@ decreases i;\n" + "	      for(;i < 4;) {\n" + "	 	   i++;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_1709_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ decreases sum;\n" + "	 for(;i < 4;) {\n" + "	 	sum--;\n" + "		i+=2;\n" + "       //@ decreases constant;\n" + "	    for(;i < 4;) {\n" + "	 	  constant++;\n" + "         //@ decreases i;\n" + "	      for(;i < 4;) {\n" + "	 	   i--;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_1710_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ decreases sum;\n" + "	 for(;i < 4;) {\n" + "	 	sum++;\n" + "       //@ decreases constant;\n" + "	    for(;i < 4;) {\n" + "	 	  constant--;\n" + "         //@ decreases i;\n" + "	      for(;i < 4;) {\n" + "	 	   i--;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_1711_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ decreases sum;\n" + "	 for(;i < 4;) {\n" + "	 	sum++;\n" + "       //@ decreases constant;\n" + "	    for(;i < 4;) {\n" + "	 	  constant--;\n" + "         //@ decreases i;\n" + "	      for(;i < 4;) {\n" + "	 	   i++;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_1712_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ decreases sum;\n" + "	 for(;i < 4;) {\n" + "	 	sum--;\n" + "       //@ decreases constant;\n" + "	    for(;i < 4;) {\n" + "	 	  constant++;\n" + "         //@ decreases i;\n" + "	      for(;i < 4;) {\n" + "	 	   i++;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_1713_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ decreases i;\n" + "	 for(;i < 4;) {\n" + "	 	i--;\n" + "	    for(;i < 4;) {\n" + "	 	  i+=3;\n" + "         //@ decreases i;\n" + "	      for(;i < 4;) {\n" + "	 	   i--;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_1714_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ decreases i;\n" + "	 for(;i < 4;) {\n" + "	 	i++;\n" + "       //@ decreases i;\n" + "	    for(;i < 4;) {\n" + "	 	  i++;\n" + "	      for(;i < 4;) {\n" + "	 	   i+=3;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_1715_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "	 for(;i < 4;) {\n" + "	 	i++;\n" + "       //@ decreases i;\n" + "	    for(;i < 4;) {\n" + "	 	  i++;\n" + "         //@ decreases i;\n" + "	      for(;i < 4;) {\n" + "	 	   i++;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_1716_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ decreases i;\n" + "	 for(;i < 4;) {\n" + "	 	i++;\n" + "       //@ decreases i;\n" + "	    for(;i < 4;) {\n" + "	 	  i++;\n" + "	      for(;i++ < 4;) {\n" + "	 	   //@ assert true;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_1717_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "	 for(;i < 4;) {\n" + "	 	i++;\n" + "       //@ decreases i;\n" + "	    for(;i < 4;) {\n" + "	 	  i++;\n" + "		  //@ assert false;\n" + "         //@ decreases i;\n" + "	      for(;i < 4;) {\n" + "	 	   i++;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLAssertError.class);
    }

    public void test_1718_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ maintaining true;\n" + "	 for(;i < 3;) {\n" + "	 	i++;\n" + "		//@ assert false;\n" + "       //@ decreases i;\n" + "	    for(;i < 4;) {\n" + "	 	  i+=3;\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLAssertError.class);
    }

    public void test_1719_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "	 for(;i < 4;) {\n" + "	 	i++;\n" + "       //@ decreases i;\n" + "	    for(;i < 4;) {\n" + "	 	  i--;\n" + "         //@ assert false;\n" + "	      for(;i < 4;) {\n" + "	 	   i++;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLAssertError.class);
    }

    public void test_1720_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "	 for(;i < 4;) {\n" + "	 	i++;\n" + "       //@ decreases i;\n" + "	    for(;i < 4;) {\n" + "	 	  i--;\n" + "	      for(;i < 4;) {\n" + "         //@ assert false;\n" + "	 	   i++;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLAssertError.class);
    }

    public void test_1721_with_empty_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ decreases i;\n" + "	 for(;i++ < 2;)\n" + "       //@ decreases i - 1;\n" + "	    for(;i++ < 4;);\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_1722_with_empty_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ maintaining true;\n" + "	 for(;i < 4;) \n" + "       //@ maintaining false;\n" + "	    for(;i < 4;)\n" + "		  i--; \n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_1723_with_empty_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ maintaining false;\n" + "	 for(;i++ < 4;)\n" + "       //@ maintaining true;\n" + "	    for(;++i < 4;);\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_1724_function_call() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {\n" + "  //@ requires false;\n" + "  void func() { }\n" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "	 //@ maintaining true;\n" + "	 for(;++i < 4;) {\n" + "		func();\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLInternalPreconditionError.class);
    }

    public void test_1725_assert_and_jmlannotation() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "	 //@ maintaining true;" + "	 for(;++i < 4;) {\n" + "		//@ assert false;\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLAssertError.class);
    }

    public void test_1726_assert_and_jmlannotation() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "	 //@ maintaining false;" + "	 for(;++i < 4;) {\n" + "		//@ assert true;\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_1727_for_and_jmlannotation() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "	 //@ maintaining false;" + "	 for(;++i < 4;) {\n" + "		for (;++i < 5;) {\n" + "			i += 3;\n" + "		}\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_1728_for_and_jmlannotation() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "	 //@ maintaining false;\n" + "	 for(;++i < 4;) {\n" + "	    //@ maintaining true;\n" + "		for (;++i < 5;) {\n" + "			i += 3;\n" + "			//@ assert false;\n" + "		}\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_1728a_for_and_jmlannotation() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "	 //@ maintaining true;" + "	 for(;++i < 4;) {\n" + "		i += 2;\n" + "		for (;++i < 5;) {\n" + "			i += 3;\n" + "			//@ assert false;\n" + "		}\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLAssertError.class);
    }

    public void test_1729_for_and_jmlannotation() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "	 //@ maintaining false;" + "	 for(;i < 4;) {\n" + "       i++;\n" + "		//@ maintaining true;\n" + "		for (;++i < 5;) {\n" + "			i += 3;\n" + "		}\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_1730_for_and_jmlannotation() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "	 //@ maintaining true;" + "	 for(;i < 4;) {\n" + "       i++;\n" + "       for (;i++ < 4;) { }\n" + "		//@ maintaining true;\n" + "		for (;++i < 10;) {\n" + "			i += 3;\n" + "			//@ assert false;\n" + "		}\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLAssertError.class);
    }

    public void test_1731_for_and_jmlannotation() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "	 for(;i < 4;) {\n" + "       i++;\n" + "       for (;i++ < 4;) { }\n" + "		//@ maintaining true;\n" + "		for (;++i < 10;) {\n" + "			i += 3;\n" + "			//@ assert false;\n" + "		}\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLAssertError.class);
    }

    public void test_1732_for_and_jmlannotation() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "	 for(;i < 4;) {\n" + "       i++;\n" + "       for (;i++ < 4;) { }\n" + "		for (;++i < 10;) {\n" + "			i += 3;\n" + "			//@ assert false;\n" + "		}\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLAssertError.class);
    }

    public void test_1733_for_and_jmlannotation() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "	 for(;i < 4;) {\n" + "       i++;\n" + "       for (;i++ < 4;) { }\n" + "		if (true)\n" + "		 for (;++i < 10;) {\n" + "			i += 3;\n" + "			//@ assert false;\n" + "		 }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLAssertError.class);
    }

    public void test_1734_for_and_jmlannotation() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "	 for(;i < 4;) {\n" + "       i++;\n" + "		//@ assert true;\n" + "       for (;i++ < 4;) { }\n" + "		//@ maintaining false;\n" + "		for (;++i < 10;) {\n" + "			i += 3;\n" + "			//@ assert false;\n" + "		}\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_1735_for_and_jmlannotation() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "	 for(;i < 4;) {\n" + "       i++;\n" + "		//@ assert false;\n" + "       for (;i++ < 4;) { }\n" + "		//@ maintaining false;\n" + "		for (;++i < 10;) {\n" + "			i += 3;\n" + "			//@ assert false;\n" + "		}\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLAssertError.class);
    }

    public void test_1736_for_and_jmlannotation() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "	 for(;i < 4;) {\n" + "       i++;\n" + "		//@ assert true;\n" + "       for (;i++ < 4;) { }\n" + "		//@ maintaining true;\n" + "		for (;++i < 10;) {\n" + "			i += 3;\n" + "			//@ assert false;\n" + "		}\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLAssertError.class);
    }

    /** jml while and jml for */
    public void test_2700_inner_with_some_statements() {
        compileAndExecGivenStatement("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "    //@ maintaining true;\n" + "	 while(i < 4) {\n" + "	 	i++;\n" + "       //@ maintaining true;\n" + "	    for(;i < 4;) {\n" + "	 	  i++;\n" + "         //@ maintaining true;\n" + "	      for(;i < 4;) {\n" + "	 	   i++;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()");
    }

    public void test_2701_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "    //@ maintaining true;\n" + "	 for(;i < 4;) {\n" + "	 	i++;\n" + "       //@ maintaining true;\n" + "	    for(;i < 4;) {\n" + "	 	  i++;\n" + "         //@ maintaining false;\n" + "	 while(i < 4) {\n" + "	 	   i++;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_2702_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "    //@ maintaining true;\n" + "	 for(;i < 4;) {\n" + "	 	i++;\n" + "       //@ maintaining false;\n" + "	 while(i < 4) {\n" + "	 	  i++;\n" + "         //@ maintaining true;\n" + "	      for(;i < 4;) {\n" + "	 	   i++;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_2703_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "    //@ maintaining false;\n" + "	 for(;i < 4;) {\n" + "	 	i++;\n" + "       //@ maintaining true;\n" + "	 while(i < 4) {\n" + "	 	  i++;\n" + "         //@ maintaining true;\n" + "	      for(;i < 4;) {\n" + "	 	   i++;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_2704_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "    //@ maintaining true;\n" + "	 for(;i < 4;) {\n" + "	 	i++;\n" + "       //@ maintaining false;\n" + "	 while(i < 4) {\n" + "	 	  i++;\n" + "         //@ maintaining false;\n" + "	 while(i < 4) {\n" + "	 	   i++;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_2705_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "    //@ maintaining true;\n" + "	 while(i < 4) {\n" + "	 	i++;\n" + "	    for(;i < 4;) {\n" + "	 	  i++;\n" + "         //@ maintaining false;\n" + "	      for(;i < 4;) {\n" + "	 	   i++;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_2706_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "    //@ maintaining true;\n" + "	 while(i < 4) {\n" + "	 	i++;\n" + "       //@ maintaining false;\n" + "	    for(;i < 4;) {\n" + "	 	  i++;\n" + "	      for(;i < 4;) {\n" + "	 	   i++;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_2707_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "	 for(;i < 4;) {\n" + "	 	i++;\n" + "       //@ maintaining false;\n" + "	    for(;i < 4;) {\n" + "	 	  i++;\n" + "         //@ maintaining true;\n" + "	 while(i < 4) {\n" + "	 	   i++;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_2708_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "    //@ decreases sum;\n" + "	 for(;i < 4;) {\n" + "	 	sum--;\n" + "       //@ decreases constant;\n" + "	 while(i < 4) {\n" + "	 	  constant--;\n" + "         //@ decreases i;\n" + "	      for(;i < 4;) {\n" + "	 	   i++;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_2709_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "    //@ decreases sum;\n" + "	 while(i < 4) {\n" + "	 	sum--;\n" + "		i+=2;\n" + "       //@ decreases constant;\n" + "	    for(;i < 4;) {\n" + "	 	  constant++;\n" + "         //@ decreases i;\n" + "	      for(;i < 4;) {\n" + "	 	   i--;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_2710_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "    //@ decreases sum;\n" + "	 for(;i < 4;) {\n" + "	 	sum++;\n" + "       //@ decreases constant;\n" + "	    for(;i < 4;) {\n" + "	 	  constant--;\n" + "         //@ decreases i;\n" + "	 while(i < 4) {\n" + "	 	   i--;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_2711_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "    //@ decreases sum;\n" + "	 while(i < 4) {\n" + "	 	sum++;\n" + "       //@ decreases constant;\n" + "	    for(;i < 4;) {\n" + "	 	  constant--;\n" + "         //@ decreases i;\n" + "	      for(;i < 4;) {\n" + "	 	   i++;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_2722_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "    //@ decreases sum;\n" + "	 while(i < 4) {\n" + "	 	sum--;\n" + "       //@ decreases constant;\n" + "	    for(;i < 4;) {\n" + "	 	  constant++;\n" + "         //@ decreases i;\n" + "	      for(;i < 4;) {\n" + "	 	   i++;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_2723_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "    //@ decreases i;\n" + "	 while(i < 4) {\n" + "	 	i--;\n" + "	    for(;i < 4;) {\n" + "	 	  i+=3;\n" + "         //@ decreases i;\n" + "	      for(;i < 4;) {\n" + "	 	   i--;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_2724_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "    //@ decreases i;\n" + "	 for(;i < 4;) {\n" + "	 	i++;\n" + "       //@ decreases i;\n" + "	 while(i < 4) {\n" + "	 	  i++;\n" + "	      for(;i < 4;) {\n" + "	 	   i+=3;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_2725_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "	 for(;i < 4;) {\n" + "	 	i++;\n" + "       //@ decreases i;\n" + "	    for(;i < 4;) {\n" + "	 	  i++;\n" + "	 while(i < 4) \n" + "	      for(;i < 4;) {\n" + "	 	   i++;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_2726_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "    //@ decreases i;\n" + "	 while(i < 4) {\n" + "	 	i++;\n" + "       //@ decreases i;\n" + "	    for(;i < 4;) {\n" + "	 	  i++;\n" + "	      for(;i++ < 4;) {\n" + "	 	   //@ assert true;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_2727_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "	 for(;i < 4;) {\n" + "	 	i++;\n" + "       //@ decreases i;\n" + "	 while(i < 4) {\n" + "	 	  i++;\n" + "		  //@ assert false;\n" + "         //@ decreases i;\n" + "	      for(;i < 4;) {\n" + "	 	   i++;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLAssertError.class);
    }

    public void test_2728_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "    //@ maintaining true;\n" + "	 for(;i < 3;) {\n" + "	 	i++;\n" + "		//@ assert false;\n" + "       //@ decreases i;\n" + "	 while(i < 4) {\n" + "	 	  i+=3;\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLAssertError.class);
    }

    public void test_2729_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "	 for(;i < 4;) {\n" + "	 	i++;\n" + "       //@ decreases i;\n" + "	 while(i < 4) {\n" + "	 	  i--;\n" + "         //@ assert false;\n" + "	      for(;i < 4;) {\n" + "	 	   i++;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLAssertError.class);
    }

    public void test_2720_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "	 while(i < 4) {\n" + "	 	i++;\n" + "       //@ decreases i;\n" + "	    for(;i < 4;) {\n" + "	 	  i--;\n" + "	      for(;i < 4;) {\n" + "         //@ assert false;\n" + "	 	   i++;\n" + "	      }\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLAssertError.class);
    }

    public void test_2721_with_empty_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "    //@ decreases i;\n" + "	 while(i < 4) \n" + "       //@ decreases i - 2;\n" + "	    for(;i++ < 4;);\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_2722_with_empty_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "    //@ maintaining true;\n" + "	 for(;i < 4;) \n" + "       //@ maintaining false;\n" + "	 while(i < 4) \n" + "		  i--; \n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_2723_with_empty_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "    //@ maintaining false;\n" + "	 while(i < 4) \n" + "       //@ maintaining true;\n" + "	    for(;++i < 4;);\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_2727_for_and_jmlannotation() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "	 //@ maintaining false;" + "	 for(;++i < 4;) {\n" + "	 while(i < 4) {\n" + "			i += 3;\n" + "		}\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_2728_for_and_jmlannotation() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "	 //@ maintaining false;\n" + "	 while(i < 4) {\n" + "	    //@ maintaining true;\n" + "		for (;++i < 5;) {\n" + "			i += 3;\n" + "			//@ assert false;\n" + "		}\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_2728a_for_and_jmlannotation() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "	 //@ maintaining true;" + "	 while(i < 4) {\n" + "		i += 2;\n" + "		for (;++i < 5;) {\n" + "			i += 3;\n" + "			//@ assert false;\n" + "		}\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLAssertError.class);
    }

    public void test_2729_for_and_jmlannotation() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "	 //@ maintaining false;" + "	 for(;i < 4;) {\n" + "       i++;\n" + "		//@ maintaining true;\n" + "	 while(i < 4) {\n" + "			i += 3;\n" + "		}\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_2730_for_and_jmlannotation() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "	 //@ maintaining true;" + "	 for(;i < 4;) {\n" + "       i++;\n" + "       while(i++ < 4) { }\n" + "		//@ maintaining true;\n" + "		for (;++i < 20;) {\n" + "			i += 3;\n" + "			//@ assert false;\n" + "		}\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLAssertError.class);
    }

    public void test_2731_for_and_jmlannotation() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "	 for(;i < 4;) {\n" + "       i++;\n" + "       while(i++ < 4) { }\n" + "		//@ maintaining true;\n" + "		for (;++i < 20;) {\n" + "			i += 3;\n" + "			//@ assert false;\n" + "		}\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLAssertError.class);
    }

    public void test_2732_for_and_jmlannotation() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "	 for(;i < 4;) {\n" + "       i++;\n" + "       while(i++ < 4) { }\n" + "		for (;++i < 20;) {\n" + "			i += 3;\n" + "			//@ assert false;\n" + "		}\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLAssertError.class);
    }

    public void test_2733_for_and_jmlannotation() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "	 for(;i < 4;) {\n" + "       i++;\n" + "       while(i++ < 4) { }\n" + "		if (true)\n" + "		 for (;++i < 20;) {\n" + "			i += 3;\n" + "			//@ assert false;\n" + "		 }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLAssertError.class);
    }

    public void test_2734_for_and_jmlannotation() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "	 for(;i < 4;) {\n" + "       i++;\n" + "		//@ assert true;\n" + "       while(i++ < 4) { }\n" + "		//@ maintaining false;\n" + "		for (;++i < 20;) {\n" + "			i += 3;\n" + "			//@ assert false;\n" + "		}\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_2735_for_and_jmlannotation() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "	 for(;i < 4;) {\n" + "       i++;\n" + "		//@ assert false;\n" + "       while(i++ < 4) { }\n" + "		//@ maintaining false;\n" + "		for (;++i < 20;) {\n" + "			i += 3;\n" + "			//@ assert false;\n" + "		}\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLAssertError.class);
    }

    public void test_2736_for_and_jmlannotation() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "	 for(;i < 4;) {\n" + "       i++;\n" + "		//@ assert true;\n" + "       while(i++ < 4) { }\n" + "		//@ maintaining true;\n" + "		for (;++i < 20;) {\n" + "			i += 3;\n" + "			//@ assert false;\n" + "		}\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLAssertError.class);
    }

    public void test_3001_while_true() {
        compileAndExecGivenStatement("X.java", "public class X {\n" + "   public void m(boolean b) { \n" + "   	//@ loop_invariant true;\n" + "   	do {} while (b);\n" + "	}\n" + "}\n", "new X().m(false)");
    }

    public void test_3002_while_expr() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {\n" + "   public void m(int i, String s) { \n" + "   	//@ loop_invariant s.length() * 2 < i;\n" + "   	do {} while (true);\n" + "	}\n" + "}\n", "new X().m(0, \"hello\")", null, JMLLoopInvariantError.class);
    }

    public void test_3004_while_RAC_valid() {
        compileAndExecGivenStatement("X.java", "public class X {\n" + "   public void m() { \n" + "   	int i = 0;\n" + "   	int[] vals = new int[] {2,3,5,7,9};\n" + "   	int sum = 0;\n" + "   	//@ loop_invariant sum >= 0;\n" + "   	do { sum += vals[i++]; } while (i < vals.length) ;\n" + "	}\n" + "}\n", "new X().m()");
    }

    public void test_3005_while_RAC_invalid() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {\n" + "   public void m() { \n" + "   	int i = 0;\n" + "   	int[] vals = new int[] {2,3,5,7,9};\n" + "   	   int sum = 0;\n" + "   	   //@ loop_invariant sum == 111;\n" + "   	   do { sum += vals[i++]; } while (i < vals.length);\n" + "	}\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_3006_while_RAC_constTrue() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {\n" + "   public void m() { \n" + "   	int i = 0;\n" + "   	int[] vals = new int[] {2,3,5,7,9};\n" + "   	   int sum = 0;\n" + "   	   //@ loop_invariant sum == 111;\n" + "   	   do { sum += vals[i++]; } while (true);\n" + "	}\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_3007_while_RAC_constFalse() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {\n" + "   static final boolean DBG = false;" + "   void m(X x) {\n" + "   	int i = 0;\n" + "   	int[] vals = new int[] {2,3,5,7,9};\n" + "   	   int sum = 0;\n" + "   	   //@ loop_invariant sum == 111;\n" + "   	   do { /*empty*/ } while (x.DBG) ;\n" + "	}\n" + "}\n", "new X().m(new X())", null, JMLLoopInvariantError.class);
    }

    public void test_3008_while_RAC_noBody() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {\n" + "   public void m() { \n" + "   	int i = 0;\n" + "   	int[] vals = new int[] {2,3,5,7,9};\n" + "   	   int sum = 0;\n" + "   	   //@ loop_invariant sum == 111;\n" + "   	   do { /* empty */ } while (i == i); \n" + "	}\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_3009_while_RAC_throwBody() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {\n" + "   boolean bool() { return true; }\n" + "   void m() {" + "   	int i = 0;\n" + "   	int[] vals = new int[] {2,3,5,7,9};\n" + "   	   int sum = 0;\n" + "   	   //@ loop_invariant sum == 111;\n" + "   	   do { throw new RuntimeException(); } while (bool());\n" + "	}\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_3010_while_RAC_break_valid() {
        compileAndExecGivenStatement("X.java", "public class X {\n" + "   public void m() { \n" + "   	int i = 0;\n" + "   	int[] vals = new int[] {2,3,5,7,9};\n" + "   	int sum = 0;\n" + "   	//@ loop_invariant sum >= 0;\n" + "   	do { break; } while (i < vals.length); \n" + "	}\n" + "}\n", "new X().m()");
    }

    public void test_3011_while_RAC_break_invalid() {
        if (testIsDisabled("FIXME")) return;
        compileAndExecGivenStatement("X.java", "public class X {\n" + "   public void m() { \n" + "   	int i = 0;\n" + "   	int[] vals = new int[] {2,3,5,7,9};\n" + "       try {\n" + "   	   int sum = 0;\n" + "   	   //@ loop_invariant sum >= 0;\n" + "   	   do { sum = -1; break; } while (i < vals.length);\n" + "	    } catch (Error e) {\n" + "          System.out.println(e.toString());\n" + "	    }\n" + "	}\n" + "}\n", "new X().m()");
    }

    public void test_3012_while_true() {
        compileAndExecGivenStatement("X.java", "public class X {\n" + "   public void m(boolean b) { \n" + "   	//@ maintaining true;\n" + "   	do {} while (b);\n" + "	}\n" + "}\n", "new X().m(false)");
    }

    public void test_3013_while_RAC_break_valid() {
        if (testIsDisabled("FIXME")) return;
        compileAndRunExpectRuntimeError("X.java", "public class X {\n" + "   public void m() { \n" + "   	int i = 0;\n" + "   	int j = 100;\n" + "   	int[] vals = new int[] {2,3,5,7,9};\n" + "   	int sum = 0;\n" + "   	//@ loop_invariant sum >= 0;\n" + "   	do {\n" + "   	      //@ loop_invariant i%2 < 2;\n" + "   	      do { j--;} while (i % 2 > 2);\n" + "   	      i--;\n" + "        } while (i < vals.length);\n" + "	}\n" + "}\n", "new X().m()");
    }

    public void test_3014_while_RAC_seq() {
        compileAndExecGivenStatement("X.java", "public class X {\n" + "   public void m() { \n" + "   	int i = 0;\n" + "   	int[] vals = new int[] {2,3,5,7,9};\n" + "   	int sum = 0;\n" + "   	//@ loop_invariant sum >= 0;\n" + "   	//@ loop_invariant 0 <= 0;\n" + "   	//@ loop_invariant sum >= 0;\n" + "   	//@ loop_invariant 0 <= 0;\n" + "   	do { sum += vals[i++]; } while (i < vals.length);\n" + "	}\n" + "}\n", "new X().m()");
    }

    public void test_3015_while_leq_expr_RAC() {
        compileAndExecGivenStatement("X.java", "public class X {\n" + "   public void m() { \n" + "   	int i = 0;\n" + "   	int[] vals = new int[] {2,3,5,7,9};\n" + "   	int sum = 0;\n" + "   	//@ loop_invariant 0 <= sum;\n" + "   	do { sum += vals[i++]; } while (i < vals.length);\n" + "	}\n" + "}\n", "new X().m()");
    }

    public void test_3101_while_variant_int() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {\n" + "   public void m(int i) { \n" + "   	//@ decreases i;\n" + "   	do {} while (i > 0) ;\n" + "	}\n" + "}\n", "new X().m(1)", null, JMLLoopVariantError.class);
    }

    public void test_3110_while_RAC_valid_variant() {
        compileAndExecGivenStatement("X.java", "public class X {\n" + "   public void m() { \n" + "   	int i = 0;\n" + "   	int[] vals = new int[] {2,3,5,7,9};\n" + "   	int sum = 0;\n" + "   	//@ decreases vals.length - i;\n" + "   	do { sum += vals[i++]; } while (i < vals.length);\n" + "	}\n" + "}\n", "new X().m()");
    }

    public void test_3111_while_RAC_invalid_variant() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {\n" + "   public void m() { \n" + "   	int i = 3;\n" + "   	int[] vals = new int[] {2,3,5,7,9};\n" + "   	int sum = 0;\n" + "   	//@ decreases i;\n" + "   	do { sum += vals[i++]; } while (i < vals.length) ;\n" + "	}\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_3112_while_RAC_invalid_variant() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {\n" + "   public static void f(int[] vals) { \n" + "   	int i = 20;\n" + "   	int sum = 0;\n" + "   	//@ decreases i;\n" + "   	do { i--; } while (i <= 20);\n" + "   }\n" + "   public void m() { \n" + "   	int[] vals = new int[] {2,3,5,7,9};\n" + "       f(vals);\n" + "	}\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_3113_while_RAC_valid_variant() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {\n" + "   public void m() { \n" + "   	int i = 0;\n" + "   	int[] vals = new int[] {2,3,5,7,9};\n" + "   	int sum = 0;\n" + "       //@ decreases vals.length - i;\n" + "       do {\n" + "             //@ decreases vals.length - i;\n" + "             do { \n" + "              sum += vals[i--];} while (i%2==0);\n" + "		} while (i < vals.length); \n" + "	}\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_3114_while_RAC_invalid_variant() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {\n" + "   public void m() { \n" + "   	int i = 0;\n" + "   	int[] vals = new int[] {2,3,5,7,9};\n" + "   	int sum = 0;\n" + "       //@ decreases vals.length - i;\n" + "       do {\n" + "             //@ decreases i;\n" + "             do {\n" + "                   sum += vals[i++]; }\n while (i%2==0);\n " + "		} while (i < vals.length);\n" + "	}\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_3115_while_RAC_invalid_variant() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {\n" + "   public void m() { \n" + "   	int i = 0;\n" + "   	int[] vals = new int[] {2,3,5,7,9};\n" + "   	int sum = 0;\n" + "       //@ decreases  i;\n" + "       do {\n" + "             //@ decreases vals.length - i;\n" + "             do { \n" + "                   sum += vals[i++]; } while (i%2==0);\n" + "		} while (i < vals.length);\n" + "	}\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_3116_while_RAC_seq() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {\n" + "   public void m() { \n" + "   	int i = 0;\n" + "   	int[] vals = new int[] {2,3,5,7,9};\n" + "   	int sum = 0;\n" + "   	//@ decreases (vals.length - i+10);\n" + "   	//@ decreases (vals.length - i+5);\n" + "   	//@ decreases (vals.length - i+1);\n" + "   	do { sum += vals[i++]; } while (i < vals.length) ;\n" + "	}\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_3117_while_RAC_seq() {
        compileAndExecGivenStatement("X.java", "public class X {\n" + "   public void m() { \n" + "   	int i = 0;\n" + "   	int[] vals = new int[] {2,3,5,7,9};\n" + "   	int sum = 0;\n" + "   	//@ loop_invariant sum >= 0;\n" + "   	//@ loop_invariant i >= 0;\n" + "   	//@ loop_invariant  vals.length >= i;\n" + "   	do { sum += vals[i++]; } while (i < vals.length);\n" + "	}\n" + "}\n", "new X().m()");
    }

    public void test_3200_while_synonyms() {
        compileAndExecGivenStatement("X.java", "public class X {\n" + "   public void m() { \n" + "       boolean pred = false;\n" + "   	//@ loop_invariant true;\n" + "   	do {/**/} while (pred);\n" + "	}\n" + "}\n", "new X().m()");
    }

    public void test_3201_while_synonyms() {
        compileAndExecGivenStatement("X.java", "public class X {\n" + "   public void m() { \n" + "       boolean pred = false;\n" + "   	//@ loop_invariant_redundantly true;\n" + "   	do {/**/} while (pred);\n" + "	}\n" + "}\n", "new X().m()");
    }

    public void test_3202_while_synonyms() {
        compileAndExecGivenStatement("X.java", "public class X {\n" + "   public void m() { \n" + "       boolean pred = false;\n" + "   	//@ maintaining true;\n" + "   	do {/**/} while (pred);\n" + "	}\n" + "}\n", "new X().m()");
    }

    public void test_3203_while_synonyms() {
        compileAndExecGivenStatement("X.java", "public class X {\n" + "   public void m() { \n" + "       boolean pred = false;\n" + "   	//@ maintaining_redundantly true;\n" + "   	do {/**/} while (pred);\n" + "	}\n" + "}\n", "new X().m()");
    }

    public void test_3204_while_synonyms() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {\n" + "   public void m() { \n" + "   	//@ decreases 0;\n" + "   	do {/**/} while (true);\n" + "	}\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_3205_while_synonyms() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {\n" + "   public void m() { \n" + "   	//@ decreases 0;\n" + "   	do {/**/} while (true);\n" + "	}\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_3300_while_label() {
        compileAndExecGivenStatement("X.java", "public class X {\n" + "   public void m(boolean b) { \n" + "   	//@ loop_invariant true;\n" + "   	here: do { break here;} while (b);\n" + "	}\n" + "}\n", "new X().m(false)");
    }

    public void test_3000_while_sideEffectsInCondition() {
    }

    /** Is loop_invariant in do-while supported. */
    public void test_3500_loop_invariant() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ loop_invariant constant == 1;\n" + "	 do{\n" + "	 	sum += a[i] + constant;\n" + "	    i++;\n" + "       constant++;\n" + "	 }while(i < a.length);\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    /** multiple invariants */
    public void test_3502_multiple_invariant() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ loop_invariant constant == 1;\n" + "    //@ loop_invariant i == 1;\n" + "    //@ loop_invariant sum > 0;\n" + "	 do {\n" + "	 	sum += a[i++] + constant;\n" + "       constant++;\n" + "	 }while(i != 4);\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_3503_multiple_invariant() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ loop_invariant constant == 1;\n" + "    //@ loop_invariant i == 1;\n" + "    //@ loop_invariant sum > 0;\n" + "	 do {\n" + "	 	sum += a[i++] + constant;\n" + "	 } while(i != 4);\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_3504_multiple_invariant() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ loop_invariant constant == 1;\n" + "    //@ maintaining i > -1;\n" + "    //@ loop_invariant sum == 0;\n" + "	 do {\n" + "	 	sum += a[i++] + constant;\n" + "	 } while(i != 4);\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    /** multiple variants */
    public void test_3525_multiple_variant() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ decreases constant;\n" + "    //@ decreases i;\n" + "    //@ decreases sum;\n" + "	 do {\n" + "	 	sum += a[i++] + constant;\n" + "	 } while(i != 4);\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_3526_multiple_variant() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ decreases constant;\n" + "    //@ decreases i;\n" + "    //@ decreases sum;\n" + "	 do {\n" + "	 	sum += a[i++] + constant;\n" + "	 } while(i != 4);\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_3527_multiple_variant() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ decreases constant;\n" + "    //@ decreases i;\n" + "    //@ decreases sum;\n" + "	 do {\n" + "	 	sum += a[i++] + constant;\n" + "	 } while(i != 4);\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    /** mixture of variants and invariants */
    public void test_3550_multiple_variant() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ maintaining sum >= 0;\n" + "    //@ decreases constant;\n" + "    //@ decreases i;\n" + "	 do {\n" + "	 	sum += a[i++] + constant;\n" + "	 } while(i != 4);\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_3551_multiple_variant() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ maintaining sum >= 0;\n" + "    //@ decreases constant;\n" + "    //@ decreases i;\n" + "	 do {\n" + "	 	sum += a[i++] + constant;\n" + "	 } while(i != 4);\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_3552_multiple_variant() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ maintaining sum > 0;\n" + "    //@ decreases constant;\n" + "    //@ decreases i;\n" + "	 do {\n" + "	 	sum += a[i++] + constant;\n" + "	 } while(i != 4);\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    /** variants and invariants inside block */
    public void test_3602_block() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    if (true) {\n" + "    //@ loop_invariant constant == 1;\n" + "    //@ loop_invariant i == 1;\n" + "    //@ loop_invariant sum > 0;\n" + "	 do {\n" + "	 	sum += a[i++] + constant++;\n" + "	 } while(i != 4);\n" + "    }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_3603_block() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    if (true) {\n" + "    //@ loop_invariant constant == 1;\n" + "    //@ loop_invariant i == 1;\n" + "    //@ loop_invariant sum > 0;\n" + "	 do {\n" + "	 	sum += a[i++] + constant;\n" + "	 } while(i != 4);\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_3604_block() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    if (true) {\n" + "    //@ loop_invariant constant == 1;\n" + "    //@ maintaining i > -1;\n" + "    //@ loop_invariant sum == 0;\n" + "	 do {\n" + "	 	sum += a[i++] + constant;\n" + "	 } while(i != 4);\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_3605_block() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    if (true) {\n" + "    //@ decreases constant;\n" + "    //@ decreases i;\n" + "    //@ decreases sum;\n" + "	 do {\n" + "	 	sum += a[i++] + constant;\n" + "	 } while(i != 4);\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_3606_block() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    if (true) {\n" + "    //@ decreases constant;\n" + "    //@ decreases i;\n" + "    //@ decreases sum;\n" + "	 do {\n" + "	 	sum += a[i++] + constant;\n" + "	 } while(i != 4);\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_3607_block() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    if (true) {\n" + "    //@ decreases constant;\n" + "    //@ decreases i;\n" + "    //@ decreases sum;\n" + "	 do {\n" + "	 	sum += a[i++] + constant;\n" + "	 } while(i != 4);\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_3608_block() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    if (true) {\n" + "    //@ maintaining sum >= 0;\n" + "    //@ decreases constant;\n" + "    //@ decreases i;\n" + "	 do {\n" + "	 	sum += a[i++] + constant;\n" + "	 } while(i != 4);\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_3609_block() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    if (true) {\n" + "    //@ maintaining sum < 0;\n" + "    //@ decreases constant;\n" + "    //@ decreases i;\n" + "	 do {\n" + "	 	sum += a[i++] + constant;\n" + "	 } while(i != 4);\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_3610_block() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    if (true) {\n" + "    //@ maintaining sum > 0;\n" + "    //@ decreases constant;\n" + "    //@ decreases i;\n" + "	 do {\n" + "	 	sum += a[i++] + constant;\n" + "	 } while(i != 4);\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    /** variants and invariants inside twolevel_block */
    public void test_3652_twolevel_block() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    if (true) {\n" + "    if (true) {\n" + "    //@ loop_invariant constant == 1;\n" + "    //@ loop_invariant i == 1;\n" + "    //@ loop_invariant sum > 0;\n" + "	 do {\n" + "	 	sum += a[i++] + constant++;\n" + "	 } while(i != 4);\n" + "	 }\n" + "    }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_3653_twolevel_block() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    if (true) {\n" + "    if (true) {\n" + "    //@ loop_invariant constant == 1;\n" + "    //@ loop_invariant i == 1;\n" + "    //@ loop_invariant sum > 0;\n" + "	 do {\n" + "	 	sum += a[i++] + constant;\n" + "	 } while(i != 4);\n" + "	 }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_3654_twolevel_block() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    if (true) {\n" + "    if (true) {\n" + "    //@ loop_invariant constant == 1;\n" + "    //@ maintaining i > -1;\n" + "    //@ loop_invariant sum == 0;\n" + "	 do {\n" + "	 	sum += a[i++] + constant;\n" + "	 } while(i != 4);\n" + "	 }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_3655_twolevel_block() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    if (true) {\n" + "    if (true) {\n" + "    //@ decreases constant;\n" + "    //@ decreases i;\n" + "    //@ decreases sum;\n" + "	 do {\n" + "	 	sum += a[i++] + constant;\n" + "	 } while(i != 4);\n" + "	 }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_3656_twolevel_block() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    if (true) {\n" + "    if (true) {\n" + "    //@ decreases constant;\n" + "    //@ decreases i;\n" + "    //@ decreases sum;\n" + "	 do {\n" + "	 	sum += a[i++] + constant;\n" + "	 } while(i != 4);\n" + "	 }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_3657_twolevel_block() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    if (true) {\n" + "    if (true) {\n" + "    //@ decreases constant;\n" + "    //@ decreases i;\n" + "    //@ decreases sum;\n" + "	 do {\n" + "	 	sum += a[i++] + constant;\n" + "	 } while(i != 4);\n" + "	 }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_3658_twolevel_block() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    if (true) {\n" + "    if (true) {\n" + "    //@ maintaining sum >= 0;\n" + "    //@ decreases constant;\n" + "    //@ decreases i;\n" + "	 do {\n" + "	 	sum += a[i++] + constant++;\n" + "	 } while(i != 4);\n" + "	 }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_3659_twolevel_block() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    if (true) {\n" + "    if (true) {\n" + "    //@ maintaining sum < 0;\n" + "    //@ decreases constant;\n" + "    //@ decreases i;\n" + "	 do {\n" + "	 	sum += a[i++] + constant++;\n" + "	 } while(i < a.length);\n" + "	 }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_3610_twolevel_block() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    if (true) {\n" + "    if (true) {\n" + "    //@ maintaining sum > 0;\n" + "    //@ decreases constant;\n" + "    //@ decreases i;\n" + "	 do {\n" + "	 	sum += a[i++] + constant++;\n" + "	 } while(i != 4);\n" + "	 }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    /** inner jml annotations */
    public void test_3700_inner_with_some_statements() {
        compileAndExecGivenStatement("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ maintaining true;\n" + "	 do {\n" + "	 	i++;\n" + "       //@ maintaining true;\n" + "	    do {\n" + "	 	  i++;\n" + "         //@ maintaining true;\n" + "	 do {\n" + "	 	i++;\n" + "	 } while(i < 4);\n" + "	    }while(i < 4);\n" + "	 }while(i < 4);\n" + "  }\n" + "}\n", "new X().m()");
    }

    public void test_3701_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ maintaining true;\n" + "	 do {\n" + "	 	i++;\n" + "       //@ maintaining true;\n" + "	    do {\n" + "	 	  i++;\n" + "         //@ maintaining false;\n" + "	      do {\n" + "	 	   i++;\n" + "	      }while(i < 4);\n" + "	      }while(i < 4);\n" + "	      }while(i < 4);\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_3702_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ maintaining true;\n" + "	 do {\n" + "	 	i++;\n" + "       //@ maintaining false;\n" + "	    do {\n" + "	 	  i++;\n" + "         //@ maintaining true;\n" + "	      do {\n" + "	 	   i++;\n" + "	      }while(i < 4);\n" + "	      }while(i < 4);\n" + "	      }while(i < 4);\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_3703_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ maintaining false;\n" + "	 do {\n" + "	 	i++;\n" + "       //@ maintaining true;\n" + "	    do {\n" + "	 	  i++;\n" + "         //@ maintaining true;\n" + "	      do {\n" + "	 	   i++;\n" + "	      }while(i < 4);\n" + "	      }while(i < 4);\n" + "	      }while(i < 4);\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_3704_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ maintaining true;\n" + "	 do {\n" + "	 	i++;\n" + "       //@ maintaining false;\n" + "	    do {\n" + "	 	  i++;\n" + "         //@ maintaining false;\n" + "	      do {\n" + "	 	   i++;\n" + "	      }while(i < 4);\n" + "	      }while(i < 4);\n" + "	      }while(i < 4);\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_3705_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ maintaining true;\n" + "	 do{\n" + "	 	i++;\n" + "	 do{\n" + "	 	  i++;\n" + "         //@ maintaining false;\n" + "	 do{\n" + "	 	   i++;\n" + "	      }while(i < 4);\n" + "	      }while(i < 4);\n" + "	      }while(i < 4);\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_3706_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ maintaining true;\n" + "	 do{\n" + "	 	i++;\n" + "       //@ maintaining false;\n" + "	 do{\n" + "	 	  i++;\n" + "	 do{\n" + "	 	   i++;\n" + "	      }while(i < 4);\n" + "	      }while(i < 4);\n" + "	      }while(i < 4);\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_3707_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "	 do{\n" + "	 	i++;\n" + "       //@ maintaining false;\n" + "	 do{\n" + "	 	  i++;\n" + "         //@ maintaining true;\n" + "	 do{\n" + "	 	   i++;\n" + "	      }while(i < 4);\n" + "	      }while(i < 4);\n" + "	      }while(i < 4);\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_3708_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ decreases sum;\n" + "	 do{\n" + "	 	sum--;\n" + "       //@ decreases constant;\n" + "	 do{\n" + "	 	  constant--;\n" + "         //@ decreases i;\n" + "	 do{\n" + "	 	   i++;\n" + "	      }while(i < 4);\n" + "	      }while(i < 4);\n" + "	      }while(i < 4);\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_3709_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ decreases sum;\n" + "	 do{\n" + "	 	sum--;\n" + "		i+=2;\n" + "       //@ decreases constant;\n" + "	 do{\n" + "	 	  constant++;\n" + "         //@ decreases i;\n" + "	 do{\n" + "	 	   i--;\n" + "	      }while(i < 4);\n" + "	      }while(i < 4);\n" + "	      }while(i < 4);\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_3710_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ decreases sum;\n" + "	 do{\n" + "	 	sum++;\n" + "       //@ decreases constant;\n" + "	 do{\n" + "	 	  constant--;\n" + "         //@ decreases i;\n" + "	 do{\n" + "	 	   i--;\n" + "	      }while(i < 4);\n" + "	      }while(i < 4);\n" + "	      }while(i < 4);\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_3711_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ decreases sum;\n" + "	 do{\n" + "	 	sum++;\n" + "       //@ decreases constant;\n" + "	 do{\n" + "	 	  constant--;\n" + "         //@ decreases i;\n" + "	 do{\n" + "	 	   i++;\n" + "	      }while(i < 4);\n" + "	      }while(i < 4);\n" + "	      }while(i < 4);\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_3712_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ decreases sum;\n" + "	 do{\n" + "	 	sum--;\n" + "       //@ decreases constant;\n" + "	 do{\n" + "	 	  constant++;\n" + "         //@ decreases i;\n" + "	 do{\n" + "	 	   i++;\n" + "	      }while(i < 4);\n" + "	      }while(i < 4);\n" + "	      }while(i < 4);\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_3713_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ decreases i;\n" + "	 do{\n" + "	 	i--;\n" + "	 do{\n" + "	 	  i+=3;\n" + "         //@ decreases i;\n" + "	 do{\n" + "	 	   i--;\n" + "	      }while(i < 4);\n" + "	      }while(i < 4);\n" + "	      }while(i < 4);\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_3714_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ decreases i;\n" + "	 do{\n" + "	 	i++;\n" + "       //@ decreases i;\n" + "	 do{\n" + "	 	  i++;\n" + "	 do{\n" + "	 	   i+=3;\n" + "	      }while(i < 4);\n" + "	      }while(i < 8);\n" + "	      }while(i < 10);\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_3715_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "	 do{\n" + "	 	i++;\n" + "       //@ decreases i;\n" + "	 do{\n" + "	 	  i++;\n" + "         //@ decreases i;\n" + "	 do{\n" + "	 	   i++;\n" + "	      }while(i < 4);\n" + "	      }while(i < 4);\n" + "	      }while(i < 4);\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_3716_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ decreases i;\n" + "	 do{\n" + "	 	i++;\n" + "       //@ decreases i;\n" + "	 do{\n" + "	 	  i++;\n" + "	 do{\n" + "	 	   //@ assert true;\n" + "	      }while(i++ < 4);\n" + "	      }while(i < 8);\n" + "	      }while(i < 10);\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_3717_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "	 do{\n" + "	 	i++;\n" + "       //@ decreases i;\n" + "	 do{\n" + "	 	  i++;\n" + "		  //@ assert false;\n" + "         //@ decreases i;\n" + "	 do{\n" + "	 	   i++;\n" + "	      }while(i < 4);\n" + "	      }while(i < 4);\n" + "	      }while(i < 4);\n" + "  }\n" + "}\n", "new X().m()", null, JMLAssertError.class);
    }

    public void test_3718_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ maintaining true;\n" + "	 do {\n" + "	 	i++;\n" + "		//@ assert false;\n" + "       //@ decreases i;\n" + "	 do{\n" + "	 	  i+=3;\n" + "	      }while(i < 4);\n" + "	      }while(i < 3);\n" + "  }\n" + "}\n", "new X().m()", null, JMLAssertError.class);
    }

    public void test_3719_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "	 do{\n" + "	 	i++;\n" + "       //@ decreases i;\n" + "	 do{\n" + "	 	  i--;\n" + "         //@ assert false;\n" + "	 do{\n" + "	 	   i++;\n" + "	      }while(i < 4);\n" + "	      }while(i < 4);\n" + "	      }while(i < 4);\n" + "  }\n" + "}\n", "new X().m()", null, JMLAssertError.class);
    }

    public void test_3720_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "	 do{\n" + "	 	i++;\n" + "       //@ decreases i;\n" + "	 do{\n" + "	 	  i--;\n" + "	 do{\n" + "         //@ assert false;\n" + "	 	   i++;\n" + "	      }while(i < 4);\n" + "	      }while(i < 4);\n" + "	      }while(i < 4);\n" + "  }\n" + "}\n", "new X().m()", null, JMLAssertError.class);
    }

    public void test_3721_with_empty_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ decreases i;\n" + "	 do\n" + "       //@ decreases i - 1;\n" + "	    do;\n" + "		while(i++ < 2);\n" + "		while(i++ < 2);\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_3722_with_empty_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ maintaining true;\n" + "	 do \n" + "       //@ maintaining false;\n" + "	    do\n" + "		  i--; \n" + "		while(i < 4);\n" + "		while(i < 4);\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_3723_with_empty_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "    //@ maintaining false;\n" + "	 do\n" + "       //@ maintaining true;\n" + "	    do;\n" + "		while(++i < 4);\n" + "		while(i++ < 4);\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_3724_function_call() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {\n" + "  //@ requires false;\n" + "  public void func() { }\n" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "	 //@ maintaining true;\n" + "	 do {\n" + "		func();\n" + "	 }while(++i < 4);\n" + "  }\n" + "}\n", "new X().m()", null, JMLInternalPreconditionError.class);
    }

    public void test_3725_assert_and_jmlannotation() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "	 //@ maintaining true;" + "	 do {\n" + "		//@ assert false;\n" + "	 }while(++i < 4);\n" + "  }\n" + "}\n", "new X().m()", null, JMLAssertError.class);
    }

    public void test_3726_assert_and_jmlannotation() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "	 //@ maintaining false;" + "	 do {\n" + "		//@ assert true;\n" + "	 }while(++i < 4);\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_3727_while_and_jmlannotation() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "	 //@ maintaining false;" + "	 do {\n" + "		do{\n" + "			i += 3;\n" + "		}while (++i < 5) ;\n" + "	 }while(++i < 4) ;\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_3728_while_and_jmlannotation() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "	 //@ maintaining false;\n" + "	 do {\n" + "	    //@ maintaining true;\n" + "		do {\n" + "			i += 3;\n" + "			//@ assert false;\n" + "		}while (++i < 5);\n" + "	 }while(++i < 4);\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_3728a_while_and_jmlannotation() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "	 //@ maintaining true;" + "	 do {\n" + "		i += 2;\n" + "		do {\n" + "			i += 3;\n" + "			//@ assert false;\n" + "		}while (++i < 5);\n" + "	 }while(++i < 4);\n" + "  }\n" + "}\n", "new X().m()", null, JMLAssertError.class);
    }

    public void test_3729_while_and_jmlannotation() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "	 //@ maintaining false;" + "	 do {\n" + "       i++;\n" + "		//@ maintaining true;\n" + "		do {\n" + "			i += 3;\n" + "		}while (++i < 5);\n" + "	 }while(i < 4);\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_3730_while_and_jmlannotation() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "	 //@ maintaining true;" + "	 do {\n" + "       i++;\n" + "       do { }while (i++ < 4);\n" + "		//@ maintaining true;\n" + "		do {\n" + "			i += 3;\n" + "			//@ assert false;\n" + "		}while (++i < 10);\n" + "	 }while(i < 4);\n" + "  }\n" + "}\n", "new X().m()", null, JMLAssertError.class);
    }

    public void test_3731_while_and_jmlannotation() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "	 while(i < 4) {\n" + "       i++;\n" + "       do { }while (i++ < 4);\n" + "		//@ maintaining true;\n" + "		do {\n" + "			i += 3;\n" + "			//@ assert false;\n" + "		}while (++i < 10);\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLAssertError.class);
    }

    public void test_3732_while_and_jmlannotation() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "	 do {\n" + "       i++;\n" + "       do { }while (i++ < 4);\n" + "		do {\n" + "			i += 3;\n" + "			//@ assert false;\n" + "		}while (++i < 10);\n" + "	 }while(i < 4);\n" + "  }\n" + "}\n", "new X().m()", null, JMLAssertError.class);
    }

    public void test_3733_while_and_jmlannotation() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "	 do {\n" + "       i++;\n" + "       do { }while (i++ < 4);\n" + "		if (true)\n" + "		 do {\n" + "			i += 3;\n" + "			//@ assert false;\n" + "		 }while (++i < 10);\n" + "	 }while(i < 4);\n" + "  }\n" + "}\n", "new X().m()", null, JMLAssertError.class);
    }

    public void test_3734_while_and_jmlannotation() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "	 do {\n" + "       i++;\n" + "		//@ assert true;\n" + "       do { }while (i++ < 4);\n" + "		//@ maintaining false;\n" + "		do {\n" + "			i += 3;\n" + "			//@ assert false;\n" + "		}while (++i < 10);\n" + "	 }while(i < 4);\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_3735_while_and_jmlannotation() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "	 do {\n" + "       i++;\n" + "		//@ assert false;\n" + "       do { }while (i++ < 4);\n" + "		//@ maintaining false;\n" + "		do {\n" + "			i += 3;\n" + "			//@ assert false;\n" + "		}while (++i < 10);\n" + "	 }while(i < 4);\n" + "  }\n" + "}\n", "new X().m()", null, JMLAssertError.class);
    }

    public void test_3736_while_and_jmlannotation() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,1,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 1;\n" + "	 do {\n" + "       i++;\n" + "		//@ assert true;\n" + "       do { }while (i++ < 4);\n" + "		//@ maintaining true;\n" + "		do {\n" + "			i += 3;\n" + "			//@ assert false;\n" + "		}while (++i < 10);\n" + "	 }while(i < 4);\n" + "  }\n" + "}\n", "new X().m()", null, JMLAssertError.class);
    }

    /** jml while, jml for, jml do-while */
    public void test_4700_inner_with_some_statements() {
        compileAndExecGivenStatement("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "    //@ maintaining true;\n" + "	 while(i < 4) {\n" + "	 	i++;\n" + "       //@ maintaining true;\n" + "	    for(;i < 4;) {\n" + "	 	  i++;\n" + "         //@ maintaining true;\n" + "	      do {\n" + "	 	   i++;\n" + "	      }while (i< 4);\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()");
    }

    public void test_4701_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "    //@ maintaining true;\n" + "	 for(;i < 4;) {\n" + "	 	i++;\n" + "       //@ maintaining true;\n" + "	     do {\n" + "	 	  i++;\n" + "         //@ maintaining false;\n" + "	 while(i < 4) {\n" + "	 	   i++;\n" + "	      }\n" + "	    }while (i< 4);\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_4702_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "    //@ maintaining true;\n" + "	 do {\n" + "	 	i++;\n" + "       //@ maintaining false;\n" + "	 while(i < 4) {\n" + "	 	  i++;\n" + "         //@ maintaining true;\n" + "	      for(;i < 4;) {\n" + "	 	   i++;\n" + "	      }\n" + "	    }\n" + "	 }while (i< 4);\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_4703_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "    //@ maintaining false;\n" + "	 for(;i < 4;) {\n" + "	 	i++;\n" + "       //@ maintaining true;\n" + "	 while(i < 4) {\n" + "	 	  i++;\n" + "         //@ maintaining true;\n" + "	      do {\n" + "	 	   i++;\n" + "	      }while (i< 4);\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_4704_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "    //@ maintaining true;\n" + "	 do {\n" + "	 	i++;\n" + "       //@ maintaining false;\n" + "	 while(i < 4) {\n" + "	 	  i++;\n" + "         //@ maintaining false;\n" + "	 while(i < 4) {\n" + "	 	   i++;\n" + "	      }\n" + "	    }\n" + "	 }while (i< 4);\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_4705_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "    //@ maintaining true;\n" + "	 while(i < 4) {\n" + "	 	i++;\n" + "	    do {\n" + "	 	  i++;\n" + "         //@ maintaining false;\n" + "	      for(;i < 4;) {\n" + "	 	   i++;\n" + "	      }\n" + "	    }while (i< 4);\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_4706_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "    //@ maintaining true;\n" + "	 while(i < 4) {\n" + "	 	i++;\n" + "       //@ maintaining false;\n" + "	    for(;i < 4;) {\n" + "	 	  i++;\n" + "	      do {\n" + "	 	   i++;\n" + "	      }while (i< 4);\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_4707_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "	 for(;i < 4;) {\n" + "	 	i++;\n" + "       //@ maintaining false;\n" + "	    do {\n" + "	 	  i++;\n" + "         //@ maintaining true;\n" + "	 while(i < 4) {\n" + "	 	   i++;\n" + "	      }\n" + "	    }while (i< 4);\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_4708_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "    //@ decreases sum;\n" + "	 do {\n" + "	 	sum--;\n" + "       //@ decreases constant;\n" + "	 while(i < 4) {\n" + "	 	  constant--;\n" + "         //@ decreases i;\n" + "	      for(;i < 4;) {\n" + "	 	   i++;\n" + "	      }\n" + "	    }\n" + "	 }while (i< 4);\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_4709_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "    //@ decreases sum;\n" + "	 do {\n" + "	 	sum--;\n" + "		i+=2;\n" + "       //@ decreases constant;\n" + "	    for(;i < 4;) {\n" + "	 	  constant++;\n" + "         //@ decreases i;\n" + "	      for(;i < 4;) {\n" + "	 	   i--;\n" + "	      }\n" + "	    }\n" + "	 }while (i< 4);\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_4710_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "    //@ decreases sum;\n" + "	 for(;i < 4;) {\n" + "	 	sum++;\n" + "       //@ decreases constant;\n" + "	    do {\n" + "	 	  constant--;\n" + "         //@ decreases i;\n" + "	 while(i < 4) {\n" + "	 	   i--;\n" + "	      }\n" + "	    }while (i< 4);\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_4711_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "    //@ decreases sum;\n" + "	 while(i < 4) {\n" + "	 	sum++;\n" + "       //@ decreases constant;\n" + "	    for(;i < 4;) {\n" + "	 	  constant--;\n" + "         //@ decreases i;\n" + "	      do {\n" + "	 	   i++;\n" + "	      }while (i< 4);\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_4722_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "    //@ decreases sum;\n" + "	 while(i < 4) {\n" + "	 	sum--;\n" + "       //@ decreases constant;\n" + "	    do {\n" + "	 	  constant++;\n" + "         //@ decreases i;\n" + "	      for(;i < 4;) {\n" + "	 	   i++;\n" + "	      }\n" + "	    }while (i< 4);\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_4723_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "    //@ decreases i;\n" + "	 do {\n" + "	 	i--;\n" + "	    for(;i < 4;) {\n" + "	 	  i+=3;\n" + "         //@ decreases i;\n" + "	      for(;i < 4;) {\n" + "	 	   i--;\n" + "	      }\n" + "	    }\n" + "	 }while (i< 4);\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_4724_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "    //@ decreases i;\n" + "	 do {\n" + "	 	i++;\n" + "       //@ decreases i;\n" + "	 while(i < 4) {\n" + "	 	  i++;\n" + "	      for(;i < 4;) {\n" + "	 	   i+=3;\n" + "	      }\n" + "	    }\n" + "	 }while (i< 4);\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_4725_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "	 for(;i < 4;) {\n" + "	 	i++;\n" + "       //@ decreases i;\n" + "	    for(;i < 4;) {\n" + "	 	  i++;\n" + "	 while(i < 4) \n" + "	      do {\n" + "	 	   i++;\n" + "	      }while (i< 4);\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_4726_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "    //@ decreases i;\n" + "	 while(i < 4) {\n" + "	 	i++;\n" + "       //@ decreases i;\n" + "	    for(;i < 4;) {\n" + "	 	  i++;\n" + "	      do {\n" + "	 	   //@ assert true;\n" + "	      }while (++i< 4);\n" + "	    }\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_4727_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "	 for(;i < 4;) {\n" + "	 	i++;\n" + "       //@ decreases i;\n" + "	 do {\n" + "	 	  i++;\n" + "		  //@ assert false;\n" + "         //@ decreases i;\n" + "	      for(;i < 4;) {\n" + "	 	   i++;\n" + "	      }\n" + "	    }while (i< 4);\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLAssertError.class);
    }

    public void test_4728_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "    //@ maintaining true;\n" + "	 do {\n" + "	 	i++;\n" + "		//@ assert false;\n" + "       //@ decreases i;\n" + "	 while(i < 4) {\n" + "	 	  i+=3;\n" + "	    }\n" + "	 }while (i< 4);\n" + "  }\n" + "}\n", "new X().m()", null, JMLAssertError.class);
    }

    public void test_4729_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "	 do {\n" + "	 	i++;\n" + "       //@ decreases i;\n" + "	 while(i < 4) {\n" + "	 	  i--;\n" + "         //@ assert false;\n" + "	      for(;i < 4;) {\n" + "	 	   i++;\n" + "	      }\n" + "	    }\n" + "	 }while (i< 4);\n" + "  }\n" + "}\n", "new X().m()", null, JMLAssertError.class);
    }

    public void test_4720_inner_with_some_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "	 while(i < 4) {\n" + "	 	i++;\n" + "       //@ decreases i;\n" + "	    do {\n" + "	 	  i--;\n" + "	      for(;i < 4;) {\n" + "         //@ assert false;\n" + "	 	   i++;\n" + "	      }\n" + "	    }while (i< 4);\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLAssertError.class);
    }

    public void test_4721_with_empty_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "    //@ decreases i;\n" + "	 while(i < 4) \n" + "       //@ decreases i - 2;\n" + "	    do {} while (i< 4);\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopVariantError.class);
    }

    public void test_4722_with_empty_statements() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "    //@ maintaining true;\n" + "	 for(;i < 4;) \n" + "       //@ maintaining false;\n" + "	 do { \n" + "		  i--;}while (i< 4); \n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_4727_for_and_jmlannotation() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "	 //@ maintaining false;" + "	 for(;++i < 4;) {\n" + "	 do {\n" + "			i += 3;\n" + "		}while (i< 4);\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_4728_for_and_jmlannotation() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "	 //@ maintaining false;\n" + "	 do {\n" + "	    //@ maintaining true;\n" + "		for (;++i < 5;) {\n" + "			i += 3;\n" + "			//@ assert false;\n" + "		}\n" + "	 }while (i< 4);\n" + "  }\n" + "}\n", "new X().m()", null, JMLLoopInvariantError.class);
    }

    public void test_4728a_for_and_jmlannotation() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "	 //@ maintaining true;" + "	 while(i < 4) {\n" + "		i += 2;\n" + "		do {\n" + "			i += 3;\n" + "			//@ assert false;\n" + "		}while (++i< 5);\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLAssertError.class);
    }

    public void test_4730_for_and_jmlannotation() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "	 //@ maintaining true;" + "	 for(;i < 4;) {\n" + "       i++;\n" + "       do { }while (++i< 4);\n" + "		//@ maintaining true;\n" + "		for (;++i < 20;) {\n" + "			i += 3;\n" + "			//@ assert false;\n" + "		}\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLAssertError.class);
    }

    public void test_4731_for_and_jmlannotation() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "	 for(;i < 4;) {\n" + "       i++;\n" + "       while(i++ < 4) { }\n" + "		//@ maintaining true;\n" + "		do {\n" + "			i += 3;\n" + "			//@ assert false;\n" + "		}while (++i< 20);\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLAssertError.class);
    }

    public void test_4732_for_and_jmlannotation() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "	 do {\n" + "       i++;\n" + "       while(i++ < 4) { }\n" + "		for (;++i < 20;) {\n" + "			i += 3;\n" + "			//@ assert false;\n" + "		}\n" + "	 }while (i< 4);\n" + "  }\n" + "}\n", "new X().m()", null, JMLAssertError.class);
    }

    public void test_4733_for_and_jmlannotation() {
        compileAndExecGivenStatementExpectRuntimeError("X.java", "public class X {" + "  void m() {\n" + "    int a[] = {0,2,2,-3,4};\n" + "	 int i = 0, sum = 0, constant = 2;\n" + "	 for(;i < 4;) {\n" + "       i++;\n" + "       while(i++ < 4) { }\n" + "		if (true)\n" + "		 do {\n" + "			i += 3;\n" + "			//@ assert false;\n" + "		 }while (i< 4);\n" + "	 }\n" + "  }\n" + "}\n", "new X().m()", null, JMLAssertError.class);
    }

    public void test_5000_for_label() {
        compileAndExecGivenStatement("X.java", "public class X {" + "  Object x = new Object();\n" + "  void m() {\n" + "    b3: {int a[] = {0,2,2,-3,4};\n" + "	 b2:b1:\n" + "	 //@ maintaining true;\n" + "	 c1: for(int i = 0; i < 10; i++) {\n" + "	  if (i == 1) {\n" + "		break;\n" + "	  }\n" + "	if (i == 2) { break b1; }\n" + "	if (i == 3) { break b2; }\n" + "	if (i == 4) { break b3; }\n" + "	if (i == 5) { continue c1;	}\n" + "	if (i == 6) { continue;}\n" + "	if (i == 8) { continue;}\n" + "}\n" + "}\n" + "}\n" + "}\n", "new X().m()");
    }

    public void test_5001_for_nested_label() {
        compileAndExecGivenStatement("X.java", "public class X {" + "  void m() {\n" + "    b3: {int a[] = {0,2,2,-3,4};\n" + "	 v1: \n" + "	 //@ maintaining true;\n" + "	 for(int i = 0; i < 10; i++) { continue v1; }\n" + "	 b2:b1:c1:\n" + "	 //@ maintaining true;\n" + "	 for(int i = 0; i < 10; i++) {\n" + "	  if (i == 1) {\n" + "		break;\n" + "	  }\n" + "	if (i == 2) { break b1; }\n" + "	if (i == 3) { break b2; }\n" + "	if (i == 4) { break b3; }\n" + "	if (i == 5) { continue c1;	}\n" + "	if (i == 6) { continue;}\n" + "	if (i == 8) { continue;}\n" + "}\n" + "}\n" + "}\n" + "}\n", "new X().m()");
    }

    public void test_5002_for_label() {
        compileAndExecGivenStatement("X.java", "public class X {" + "  void m() {\n" + "    b3: {int a[] = {0,2,2,-3,4};\n" + "	 b2:b1:c1:\n" + "	 //@ maintaining true;\n" + "	 for(int i = 0; i < 10; i++) {\n" + "	  if (i == 1) {\n" + "		break;\n" + "	  }\n" + "	if (i == 2) { break b1; }\n" + "	if (i == 3) { break b2; }\n" + "	if (i == 4) { break b3; }\n" + "	if (i == 5) { continue c1;	}\n" + "	if (i == 6) { continue;}\n" + "	if (i == 8) { continue;}\n" + "	 b4:c10:\n" + "	 //@ maintaining true;\n" + "	 for(int j = 0; j < 10; j++) {\n" + "		 if (j == 0)\n" + "			break b1;\n" + "		else if (j == 1)\n" + "			continue c10;\n" + "		else\n" + "			break b4;\n" + "	}\n" + "}\n" + "}\n" + "}\n" + "}\n", "new X().m()");
    }
}
