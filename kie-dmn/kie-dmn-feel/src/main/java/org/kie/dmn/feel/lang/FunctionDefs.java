package org.kie.dmn.feel.lang;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.drools.javaparser.JavaParser;
import org.drools.javaparser.ast.NodeList;
import org.drools.javaparser.ast.expr.CastExpr;
import org.drools.javaparser.ast.expr.ClassExpr;
import org.drools.javaparser.ast.expr.Expression;
import org.drools.javaparser.ast.expr.MethodCallExpr;
import org.drools.javaparser.ast.expr.NameExpr;
import org.drools.javaparser.ast.expr.NullLiteralExpr;
import org.drools.javaparser.ast.expr.StringLiteralExpr;
import org.drools.javaparser.ast.type.Type;

public class FunctionDefs {

    private static final String ANONYMOUS = "<anonymous>";
    private static final Pattern METHOD_PARSER = Pattern.compile("(.+)\\((.*)\\)");
    private static final Pattern PARAMETER_PARSER = Pattern.compile("([^, ]+)");
    private static final Type TYPE_BIG_DECIMAL = JavaParser.parseType(BigDecimal.class.getCanonicalName());

    public static Expression asMethodCall(
            String className,
            String methodSignature,
            List<String> params) {
        // creating a simple algorithm to find the method in java
        // without using any external libraries in this initial implementation
        // might need to explicitly use a classloader here
        String[] mp = parseMethod(methodSignature);
        try {
            String methodName = mp[0];
            List<String> paramTypeNames = parseParams(mp[1]);
            ArrayList<Expression> paramExprs = new ArrayList<>();
            if (paramTypeNames.size() == params.size()) {
                for (int i = 0; i < params.size(); i++) {
                    String paramName = params.get(i);
                    String paramTypeName = paramTypeNames.get(i);
                    Type paramTypeCanonicalName =
                            JavaParser.parseType(
                                    getType(paramTypeName).getCanonicalName());

                    Expression param =
                        new CastExpr(paramTypeCanonicalName,
                            new MethodCallExpr(
                                null,
                                "coerceTo",
                                new NodeList<>(
                                        new ClassExpr(paramTypeCanonicalName),
                                        new MethodCallExpr(
                                            new NameExpr("feelExprCtx"),
                                            "getValue",
                                            new NodeList<>(new StringLiteralExpr(paramName))
                                        ))));

                    paramExprs.add(param);
                }

                return new MethodCallExpr(
                        new NameExpr(className),
                        methodName,
                        new NodeList<>(paramExprs));
            } else {
                return new NullLiteralExpr();
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static Class<?> getType(String typeName)
            throws ClassNotFoundException {
        // first check if it is primitive
        Class<?> type = convertPrimitiveNameToType( typeName );
        if( type == null ) {
            // if it is not, then try to load it
            type = Class.forName( typeName );

        }
        return type;
    }

    static String[] parseMethod(String signature) {
        Matcher m = METHOD_PARSER.matcher(signature);
        if (m.matches()) {
            String[] result = new String[2];
            result[0] = m.group(1);
            result[1] = m.group(2);
            return result;
        }
        return null;
    }

    static List<String> parseParams(String params) {
        List<String> ps = new ArrayList<>();
        if (params.trim().length() > 0) {
            Matcher m = PARAMETER_PARSER.matcher(params.trim());
            while (m.find()) {
                ps.add(m.group().trim());
            }
        }
        return ps;
    }

    static Class<?> convertPrimitiveNameToType(String typeName) {
        if (typeName.equals("int")) {
            return int.class;
        }
        if (typeName.equals("boolean")) {
            return boolean.class;
        }
        if (typeName.equals("char")) {
            return char.class;
        }
        if (typeName.equals("byte")) {
            return byte.class;
        }
        if (typeName.equals("short")) {
            return short.class;
        }
        if (typeName.equals("float")) {
            return float.class;
        }
        if (typeName.equals("long")) {
            return long.class;
        }
        if (typeName.equals("double")) {
            return double.class;
        }
        return null;
    }
}
