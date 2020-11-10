package org.drools.mvelcompiler;

import com.github.javaparser.ast.CompilationUnit;
import org.drools.mvel.parser.ast.visitor.DrlGenericVisitor;
import org.kie.api.definition.process.Node;

public class DrlxCompiler implements DrlGenericVisitor<Node, Void> {
    public PackageDescr visit(CompilationUnit n, A arg) {
        return defaultMethod(n, arg);
    }
}
