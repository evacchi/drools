M2=/home/evacchi/.m2/repository/
#native-image  -Dfile.encoding=UTF-8 -classpath /home/evacchi/Devel/fun/drools-playground/target/classes:$M2/org/drools/drools-core/7.16.0-SNAPSHOT/drools-core-7.16.0-SNAPSHOT.jar:$M2/org/mvel/mvel2/2.4.3.Final/mvel2-2.4.3.Final.jar:$M2/org/kie/kie-api/7.16.0-SNAPSHOT/kie-api-7.16.0-SNAPSHOT.jar:$M2/org/kie/kie-internal/7.16.0-SNAPSHOT/kie-internal-7.16.0-SNAPSHOT.jar:$M2/org/kie/soup/kie-soup-commons/7.16.0-SNAPSHOT/kie-soup-commons-7.16.0-SNAPSHOT.jar:$M2/org/kie/soup/kie-soup-project-datamodel-commons/7.16.0-SNAPSHOT/kie-soup-project-datamodel-commons-7.16.0-SNAPSHOT.jar:$M2/org/kie/soup/kie-soup-project-datamodel-api/7.16.0-SNAPSHOT/kie-soup-project-datamodel-api-7.16.0-SNAPSHOT.jar:$M2/commons-codec/commons-codec/1.10/commons-codec-1.10.jar:$M2/org/drools/drools-compiler/7.16.0-SNAPSHOT/drools-compiler-7.16.0-SNAPSHOT.jar:$M2/org/kie/soup/kie-soup-maven-support/7.16.0-SNAPSHOT/kie-soup-maven-support-7.16.0-SNAPSHOT.jar:$M2/org/antlr/antlr-runtime/3.5.2/antlr-runtime-3.5.2.jar:$M2/org/eclipse/jdt/core/compiler/ecj/4.6.1/ecj-4.6.1.jar:$M2/com/thoughtworks/xstream/xstream/1.4.10/xstream-1.4.10.jar:$M2/xmlpull/xmlpull/1.1.3.1/xmlpull-1.1.3.1.jar:$M2/xpp3/xpp3_min/1.1.4c/xpp3_min-1.1.4c.jar:$M2/com/google/protobuf/protobuf-java/3.6.1/protobuf-java-3.6.1.jar:$M2/org/drools/drools-model-compiler/7.16.0-SNAPSHOT/drools-model-compiler-7.16.0-SNAPSHOT.jar:$M2/org/drools/drlx-parser/7.16.0-SNAPSHOT/drlx-parser-7.16.0-SNAPSHOT.jar:$M2/org/drools/drools-canonical-model/7.16.0-SNAPSHOT/drools-canonical-model-7.16.0-SNAPSHOT.jar:$M2/org/slf4j/slf4j-api/1.7.25/slf4j-api-1.7.25.jar:$M2/ch/qos/logback/logback-classic/1.1.3/logback-classic-1.1.3.jar:$M2/ch/qos/logback/logback-core/1.1.3/logback-core-1.1.3.jar io.github.evacchi.A

$HOME/Apps/jdk/graalvm-ce-1.0.0-rc9/bin/native-image \
                    -classpath /home/evacchi/Devel/redhat/drools/drools-core/target/classes:/home/evacchi/.m2/repository/com/google/protobuf/protobuf-java/3.6.1/protobuf-java-3.6.1.jar:/home/evacchi/.m2/repository/com/thoughtworks/xstream/xstream/1.4.10/xstream-1.4.10.jar:/home/evacchi/.m2/repository/xmlpull/xmlpull/1.1.3.1/xmlpull-1.1.3.1.jar:/home/evacchi/.m2/repository/xpp3/xpp3_min/1.1.4c/xpp3_min-1.1.4c.jar:/home/evacchi/.m2/repository/org/mvel/mvel2/2.4.3.Final/mvel2-2.4.3.Final.jar:/home/evacchi/Devel/redhat/droolsjbpm-knowledge/kie-api/target/classes:/home/evacchi/.m2/repository/org/kie/soup/kie-soup-maven-support/7.16.0-SNAPSHOT/kie-soup-maven-support-7.16.0-20181129.204509-2.jar:/home/evacchi/Devel/redhat/droolsjbpm-knowledge/kie-internal/target/classes:/home/evacchi/Devel/redhat/drools/drools-core-reflective/target/classes:/home/evacchi/Devel/redhat/drools/drools-core-dynamic/target/classes:/home/evacchi/.m2/repository/org/kie/soup/kie-soup-commons/7.16.0-SNAPSHOT/kie-soup-commons-7.16.0-20181129.204405-2.jar:/home/evacchi/.m2/repository/org/kie/soup/kie-soup-project-datamodel-commons/7.16.0-SNAPSHOT/kie-soup-project-datamodel-commons-7.16.0-20181129.204444-2.jar:/home/evacchi/.m2/repository/org/kie/soup/kie-soup-project-datamodel-api/7.16.0-SNAPSHOT/kie-soup-project-datamodel-api-7.16.0-20181129.204431-2.jar:/home/evacchi/.m2/repository/org/slf4j/slf4j-api/1.7.25/slf4j-api-1.7.25.jar:/home/evacchi/.m2/repository/commons-codec/commons-codec/1.10/commons-codec-1.10.jar \
                    org.drools.A
