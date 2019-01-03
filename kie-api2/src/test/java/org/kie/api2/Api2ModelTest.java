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
import org.kie.api2.api.RuleUnitInstance;
import org.kie.api2.impl.DataSourceImpl;
import org.kie.api2.model.Person;
import org.kie.api2.model.Result;

import static org.drools.model.DSL.declarationOf;
import static org.drools.model.DSL.on;
import static org.drools.model.PatternDSL.pattern;
import static org.drools.model.PatternDSL.rule;
import static org.junit.Assert.assertSame;

public class Api2ModelTest {

    @Test
    public void testApi2() {
        Result result = new Result();

        // we create a model using the canonical model (no DRL)
        Model model = createTestModel(result);
        // we pack it into a kbase
        // notice that default behavior would be to scan classpath, so this would be done automatically
        InternalKnowledgeBase kieBase = createKieBase(model);

        // BEGIN actual code:

        DataSource<Person> ps = new DataSourceImpl<>();

        Person mark = new Person("Mark", 37);
        Person edson = new Person("Edson", 35);
        Person mario = new Person("Mario", 40);

        FactHandle markFH = ps.add(mark);
        FactHandle edsonFH = ps.add(edson);
        FactHandle marioFH = ps.add(mario);

        // get a runtime for the kieBase we created above
        Kie.Runtime.Factory runtime = Kie.runtime(kieBase);
        // create a RuleUnit instance.
        // Notice that the API is type safe, it the sub-type of PersonUnit (which implements RuleUnit)
        // in fact, it returns a RuleUnitInstance!
        RuleUnitInstance<PersonUnit> rui = runtime.of(new PersonUnit(ps));
        // start the unit
        rui.run();

        assertSame(mark, result.getValue());
    }

    private InternalKnowledgeBase createKieBase(Model model) {
        RuleBaseConfiguration kieBaseConf = new RuleBaseConfiguration();
        KiePackagesBuilder builder = new KiePackagesBuilder(kieBaseConf);
        builder.addModel(model);
        return new KieBaseBuilder(kieBaseConf).createKieBase(builder.build());
    }

    private Model createTestModel(Result result) {
        Variable<Person> markV = declarationOf(Person.class);

        Rule rule = rule("beta")
                .unit(PersonUnit.class)
                .build(
                        pattern(markV)
                                .expr("exprA", p -> p.getName().equals("Mark")),

                        on(markV).execute(result::setValue)
                );

        return new ModelImpl().addRule(rule);
    }
}

class PersonUnit implements org.kie.api2.api.RuleUnit {

    private final DataSource<Person> persons;

    public PersonUnit(DataSource<Person> persons) {
        this.persons = persons;
    }
}