package org.drools.compiler.drlx;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.modules.ModuleDeclaration;
import org.drools.compiler.lang.ParseException;
import org.drools.compiler.lang.api.CEDescrBuilder;
import org.drools.compiler.lang.api.DescrFactory;
import org.drools.compiler.lang.api.ImportDescrBuilder;
import org.drools.compiler.lang.api.PackageDescrBuilder;
import org.drools.compiler.lang.api.PatternDescrBuilder;
import org.drools.compiler.lang.api.RuleDescrBuilder;
import org.drools.compiler.lang.descr.AndDescr;
import org.drools.compiler.lang.descr.BaseDescr;
import org.drools.compiler.lang.descr.ImportDescr;
import org.drools.compiler.lang.descr.PackageDescr;
import org.drools.compiler.lang.descr.RuleDescr;
import org.drools.mvel.parser.ast.expr.RuleConsequence;
import org.drools.mvel.parser.ast.expr.RuleDeclaration;
import org.drools.mvel.parser.ast.expr.RuleItem;
import org.drools.mvel.parser.ast.expr.RulePattern;
import org.drools.mvel.parser.ast.visitor.DrlGenericVisitor;

public class DrlxCompiler implements DrlGenericVisitor<BaseDescr, Void> {

    PackageDescrBuilder builder = DescrFactory.newPackage();

    public PackageDescr visit(CompilationUnit u, Void arg) {
        PackageDeclaration packageDeclaration = u.getPackageDeclaration().orElseThrow(() -> {
            throw new ParseException("Expected package declaration.", -1);
        });
        String pkgName = packageDeclaration.getNameAsString();
        builder.name(pkgName);

        for (ImportDeclaration i : u.getImports()) {
            this.visit(i, null);
        }
        ModuleDeclaration moduleDeclaration = u.getModule().orElseThrow(() -> {
            throw new ParseException("Expected unit declaration.", -1);
        });
        builder.newUnit().target(String.format("%s.%s", pkgName, moduleDeclaration.getNameAsString()));

        for (TypeDeclaration<?> typeDeclaration : u.getTypes()) {
            RuleDeclaration rd = (RuleDeclaration) typeDeclaration;
            this.visit(rd, null);
        }
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
        PatternDescrBuilder<CEDescrBuilder<RuleDescrBuilder, AndDescr>> pat = ruleDescrBuilder.lhs().pattern();
        for (RuleItem item : decl.getRuleBody().getItems()) {
            if (item instanceof RulePattern) {
                RulePattern p = (RulePattern) item;
                if (p.getBind() == null) {
                    pat.constraint(p.getExpr().toString());
                } else {
                    pat.id(p.getBind().toString(), false).constraint(p.getExpr().toString());
                }
            } else if (item instanceof RuleConsequence) {
                RuleConsequence c = (RuleConsequence) item;
                ruleDescrBuilder.rhs(c.getBlock().toString());
            } else {
                throw new IllegalArgumentException(item.getClass().getCanonicalName());
            }
        }
        return ruleDescrBuilder.getDescr();
    }
}
