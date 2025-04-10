package net.sourceforge.pmd.lang.java.rule.unusedcode;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTConstructorDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTInitializer;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclarator;
import net.sourceforge.pmd.lang.java.ast.AccessNode;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.java.symboltable.ClassScope;
import net.sourceforge.pmd.lang.java.symboltable.MethodNameDeclaration;
import net.sourceforge.pmd.lang.java.symboltable.NameOccurrence;

public class UnusedPrivateMethodRule extends AbstractJavaRule {

    public Object visit(ASTClassOrInterfaceDeclaration node, Object data) {
        if (node.isInterface()) {
            return data;
        }
        Map<MethodNameDeclaration, List<NameOccurrence>> methods = ((ClassScope) node.getScope()).getMethodDeclarations();
        for (MethodNameDeclaration mnd : findUnique(methods)) {
            List<NameOccurrence> occs = methods.get(mnd);
            if (!privateAndNotExcluded(mnd)) {
                continue;
            }
            if (occs.isEmpty()) {
                addViolation(data, mnd.getNode(), mnd.getImage() + mnd.getParameterDisplaySignature());
            } else {
                if (calledFromOutsideItself(occs, mnd)) {
                    addViolation(data, mnd.getNode(), mnd.getImage() + mnd.getParameterDisplaySignature());
                }
            }
        }
        return data;
    }

    private Set<MethodNameDeclaration> findUnique(Map<MethodNameDeclaration, List<NameOccurrence>> methods) {
        Set<MethodNameDeclaration> unique = new HashSet<MethodNameDeclaration>();
        Set<String> sigs = new HashSet<String>();
        for (MethodNameDeclaration mnd : methods.keySet()) {
            String sig = mnd.getImage() + mnd.getParameterCount() + mnd.isVarargs();
            if (!sigs.contains(sig)) {
                unique.add(mnd);
            }
            sigs.add(sig);
        }
        return unique;
    }

    private boolean calledFromOutsideItself(List<NameOccurrence> occs, MethodNameDeclaration mnd) {
        int callsFromOutsideMethod = 0;
        for (NameOccurrence occ : occs) {
            Node occNode = occ.getLocation();
            ASTConstructorDeclaration enclosingConstructor = occNode.getFirstParentOfType(ASTConstructorDeclaration.class);
            if (enclosingConstructor != null) {
                callsFromOutsideMethod++;
                break;
            }
            ASTInitializer enclosingInitializer = occNode.getFirstParentOfType(ASTInitializer.class);
            if (enclosingInitializer != null) {
                callsFromOutsideMethod++;
                break;
            }
            ASTMethodDeclaration enclosingMethod = occNode.getFirstParentOfType(ASTMethodDeclaration.class);
            if (enclosingMethod == null || !mnd.getNode().jjtGetParent().equals(enclosingMethod)) {
                callsFromOutsideMethod++;
            }
        }
        return callsFromOutsideMethod == 0;
    }

    private boolean privateAndNotExcluded(MethodNameDeclaration mnd) {
        ASTMethodDeclarator node = (ASTMethodDeclarator) mnd.getNode();
        return ((AccessNode) node.jjtGetParent()).isPrivate() && !node.hasImageEqualTo("readObject") && !node.hasImageEqualTo("writeObject") && !node.hasImageEqualTo("readResolve") && !node.hasImageEqualTo("writeReplace");
    }
}
