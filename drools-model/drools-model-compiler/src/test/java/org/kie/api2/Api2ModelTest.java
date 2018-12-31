package org.kie.api2;

import org.drools.core.RuleBaseConfiguration;
import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.model.Index;
import org.drools.model.Model;
import org.drools.model.Rule;
import org.drools.model.Variable;
import org.drools.model.impl.ModelImpl;
import org.drools.modelcompiler.KiePackagesBuilder;
import org.drools.modelcompiler.builder.KieBaseBuilder;
import org.drools.modelcompiler.domain.Person;
import org.drools.modelcompiler.domain.Result;
import org.junit.Test;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.api2.api.DataSource;
import org.kie.api2.api.Kie;
import org.kie.api2.api.RuleUnitInstance;
import org.kie.api2.impl.DataSourceImpl;

import static org.drools.model.DSL.declarationOf;
import static org.drools.model.DSL.on;
import static org.drools.model.PatternDSL.alphaIndexedBy;
import static org.drools.model.PatternDSL.betaIndexedBy;
import static org.drools.model.PatternDSL.pattern;
import static org.drools.model.PatternDSL.reactOn;
import static org.drools.model.PatternDSL.rule;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class Api2ModelTest {

    @Test
    public void testApi2() {
        Result result = new Result();
        Variable<Person> markV = declarationOf(Person.class);
        Variable<Person> olderV = declarationOf(Person.class);

        Rule rule = rule("beta")
                .unit(MyUnit.class)
                .build(
                        pattern(markV)
                                .expr("exprA", p -> p.getName().equals("Mark"),
                                      alphaIndexedBy(String.class, Index.ConstraintType.EQUAL, 1, p -> p.getName(), "Mark"),
                                      reactOn("name", "age")),
                        pattern(olderV)
                                .expr("exprB", p -> !p.getName().equals("Mark"),
                                      alphaIndexedBy(String.class, Index.ConstraintType.NOT_EQUAL, 1, p -> p.getName(), "Mark"),
                                      reactOn("name"))
                                .expr("exprC", markV, (p1, p2) -> p1.getAge() > p2.getAge(),
                                      betaIndexedBy(int.class, Index.ConstraintType.GREATER_THAN, 0, p -> p.getAge(), p -> p.getAge()),
                                      reactOn("age")),
                        on(olderV, markV).execute((p1, p2) -> result.setValue(p1.getName() + " is older than " + p2.getName()))
                );

        Model model = new ModelImpl().addRule(rule);
        RuleBaseConfiguration kieBaseConf = new RuleBaseConfiguration();
        KiePackagesBuilder builder = new KiePackagesBuilder(kieBaseConf);
        builder.addModel(model);
        InternalKnowledgeBase kieBase = new KieBaseBuilder(kieBaseConf).createKieBase(builder.build());

        DataSource<Person> ps = new DataSourceImpl<>();

        RuleUnitInstance<MyUnit> rui = Kie.runtime().of(new MyUnit(ps));

        Person mark = new Person("Mark", 37);
        Person edson = new Person("Edson", 35);
        Person mario = new Person("Mario", 40);

        FactHandle markFH = ps.add(mark);
        FactHandle edsonFH = ps.add(edson);
        FactHandle marioFH = ps.add(mario);

        rui.run();
        assertEquals("Mario is older than Mark", result.getValue());

        result.setValue(null);
        ps.remove(marioFH);
        rui.run();
        assertNotNull(result.getValue());

        mark.setAge(34);
        ps.update(markFH, mark);

        rui.run();
        assertEquals("Edson is older than Mark", result.getValue());

        System.out.println(result.getValue());

        System.out.println("all done");
    }
}

class MyUnit implements org.kie.api2.api.RuleUnit {

    private final DataSource<Person> persons;

    public MyUnit(DataSource<Person> persons) {
        this.persons = persons;
    }
}