package org.drools.compiler.drlx;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import org.drools.compiler.lang.api.DescrFactory;
import org.drools.compiler.lang.api.ImportDescrBuilder;
import org.drools.compiler.lang.api.PackageDescrBuilder;
import org.drools.compiler.lang.api.RuleDescrBuilder;
import org.drools.compiler.lang.descr.BaseDescr;
import org.drools.compiler.lang.descr.ImportDescr;
import org.drools.compiler.lang.descr.PackageDescr;
import org.drools.compiler.lang.descr.RuleDescr;
import org.drools.mvel.parser.ast.expr.RuleDeclaration;
import org.drools.mvel.parser.ast.visitor.DrlGenericVisitor;

public class DrlxCompiler implements DrlGenericVisitor<BaseDescr, Void> {
    PackageDescrBuilder builder = DescrFactory.newPackage();
    public PackageDescr visit(CompilationUnit u, Void arg) {
        u.getImports().forEach(i -> this.visit(i, null));
        u.getTypes().stream().map(RuleDeclaration.class::cast).forEach(rd -> this.visit(rd, null));
        return builder.getDescr();
    }

    @Override
    public ImportDescr visit(ImportDeclaration decl, Void v) {
        ImportDescrBuilder importDescrBuilder = builder.newImport();
        importDescrBuilder.target(decl.getNameAsString());
        return importDescrBuilder.getDescr();
    }

    public RuleDescr visit(RuleDeclaration decl, Void v) {
        RuleDescrBuilder ruleDescrBuilder = builder.newRule();
        ruleDescrBuilder.name(decl.getNameAsString());
        return ruleDescrBuilder.getDescr();
    }

}
