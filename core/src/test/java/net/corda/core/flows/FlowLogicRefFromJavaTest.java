package net.corda.core.flows;

import org.junit.Test;

import static java.util.Collections.singleton;

public class FlowLogicRefFromJavaTest {

    @SuppressWarnings("unused")
    private static class ParamType1 {
        final int value;

        ParamType1(int v) {
            value = v;
        }
    }

    @SuppressWarnings("unused")
    private static class ParamType2 {
        final String value;

        ParamType2(String v) {
            value = v;
        }
    }

    @SuppressWarnings("unused")
    private static class JavaFlowLogic extends FlowLogic<Void> {

        public JavaFlowLogic(ParamType1 A, ParamType2 b) {
        }

        @Override
        public Void call() {
            return null;
        }
    }

    @SuppressWarnings("unused")
    private static class JavaNoArgFlowLogic extends FlowLogic<Void> {

        public JavaNoArgFlowLogic() {
        }

        @Override
        public Void call() {
            return null;
        }
    }

    @Test
    public void test() {
        FlowLogicRefFactory factory = new FlowLogicRefFactory(singleton(JavaFlowLogic.class.getName()));
        factory.create(JavaFlowLogic.class, new ParamType1(1), new ParamType2("Hello Jack"));
    }

    @Test
    public void testNoArg() {
        FlowLogicRefFactory factory = new FlowLogicRefFactory(singleton(JavaNoArgFlowLogic.class.getName()));
        factory.create(JavaNoArgFlowLogic.class);
    }
}
