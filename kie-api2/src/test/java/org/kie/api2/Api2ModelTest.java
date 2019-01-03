package org.kie.api2;

import org.drools.core.RuleBaseConfiguration;
import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.model.Model;
import org.drools.model.Rule;
import org.drools.model.Variable;
import org.drools.model.impl.ModelImpl;
import org.drools.modelcompiler.KiePackagesBuilder;
import org.drools.modelcompiler.builder.KieBaseBuilder;
import org.junit.Test;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.api2.api.DataSource;
import org.kie.api2.api.Kie;
import org.kie.api2.api.ProcessUnit;
import org.kie.api2.api.RuleUnitInstance;
import org.kie.api2.impl.DataSourceImpl;
import org.kie.api2.model.Person;
import org.kie.api2.model.Result;

import static org.drools.model.DSL.declarationOf;
import static org.drools.model.DSL.on;
import static org.drools.model.PatternDSL.pattern;
import static org.drools.model.PatternDSL.rule;
import static org.junit.Assert.assertNotNull;

public class Api2ModelTest {

    @Test
    public void testApi2() {
        Result result = new Result();
        Variable<Person> markV = declarationOf(Person.class);
//        Variable<Person> olderV = declarationOf(Person.class);

        Rule rule = rule("beta")
                .unit(PersonUnit.class)
                .build(
                        pattern(markV)
                                .expr("exprA", p -> p.getName().equals("Mark")),

                        on(markV).execute(result::setValue)
                );

        Model model = new ModelImpl().addRule(rule);
        RuleBaseConfiguration kieBaseConf = new RuleBaseConfiguration();
        KiePackagesBuilder builder = new KiePackagesBuilder(kieBaseConf);
        builder.addModel(model);
        InternalKnowledgeBase kieBase = new KieBaseBuilder(kieBaseConf).createKieBase(builder.build());

        DataSource<Person> ps = new DataSourceImpl<>();

        RuleUnitInstance<PersonUnit> rui = Kie.runtime(kieBase).of(new PersonUnit(ps));

        Person mark = new Person("Mark", 37);
        Person edson = new Person("Edson", 35);
        Person mario = new Person("Mario", 40);

        FactHandle markFH = ps.add(mark);
        FactHandle edsonFH = ps.add(edson);
        FactHandle marioFH = ps.add(mario);

        rui.run();
        assertNotNull(result.getValue());

        System.out.println(result.getValue());

        System.out.println("all done");
    }
}

class PersonUnit implements org.kie.api2.api.RuleUnit {

    private final DataSource<Person> persons;

    public PersonUnit(DataSource<Person> persons) {
        this.persons = persons;
    }
}