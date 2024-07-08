package com.insa;

import org.dom4j.DocumentException;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.Import;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.parser.YangYinParser;

import static org.junit.jupiter.api.Assertions.*;


import java.io.IOException;
import java.util.*;

public class GraphTest {

    private YangSchemaContext getSchemaContext(String yangFile) {
        return getSchemaContext(yangFile, true);
    }

    private YangSchemaContext getSchemaContext(String yangFile, boolean assertTrue) {
        try {
            YangSchemaContext schemaContext = YangYinParser.parse(yangFile);
            ValidatorResult result = schemaContext.validate();
            if (assertTrue) assertTrue(result.isOk(), "schema is not correct");
            Collections.shuffle(schemaContext.getModules());
            return schemaContext;
        } catch (IOException | YangParserException | DocumentException e) {
            return null;
        }
    }

    private boolean hasLocalImport(YangSchemaContext context, Module module) {
        for (Import currentImport : module.getImports()) {
            List<Module> list = context.getModule(currentImport.getArgStr());
            if (list != null && !list.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private HashSet<Module> getGraph(Module m){
        HashSet<Module> graph = new HashSet<>();
        Deque<Module> todo = new LinkedList<>(Collections.singleton(m));
        Module current = todo.pollFirst();
        while(current != null) {
            graph.add(current);
            todo.addAll(current.getDependentBys());
            current = todo.pollFirst();
        }
        return graph;
    }

    private List<List<Module>> mergeLevel0(List<Module> lvl0, HashMap<Module, HashSet<Module>> graphs) {
        List<List<Module>> newLvl0 = new ArrayList<>();
        HashSet<Module> alreadyInGraph = new HashSet<>();
        for (Module m1 : lvl0) {
            List<Module> temp = new ArrayList<>(Collections.singleton(m1));
            if (alreadyInGraph.contains(m1)) continue;
            alreadyInGraph.add(m1);
            for (Module m2 : lvl0) {
                if (m1 == m2 || alreadyInGraph.contains(m2)) continue;
                HashSet<Module> tg1 = new HashSet<>(graphs.get(m1));
                tg1.retainAll(graphs.get(m2));
                if (!tg1.isEmpty()) {
                    alreadyInGraph.add(m2);
                    temp.add(m2);
                }
            }
            newLvl0.add(temp);
        }
        return newLvl0;
    }

    private List<List<Module>> getModulesInOrder(YangSchemaContext context, List<Module> modules) {
        List<List<Module>> correctOrder = new ArrayList<>();
        List<Module> level0Module = new ArrayList<>();
        HashMap<Module, Integer> cost = new HashMap<>();
        HashMap<Module, HashSet<Module>> graphs = new HashMap<>();
        for (Module m : modules) {
            if (!hasLocalImport(context, m)) {
                level0Module.add(m);
                graphs.put(m, getGraph(m));
            }
            cost.put(m, m.getImports().size());
        }
        level0Module.sort(Comparator.comparing(Module::getArgStr));
        List<List<Module>> mergedLevel0 = mergeLevel0(level0Module, graphs);
        for (List<Module> l0List : mergedLevel0) {
            List<Module> tempCorrectOrder = new ArrayList<>();
            Deque<Module> moduleStack = new LinkedList<>(l0List);
            Module current = moduleStack.pollFirst();
            while (current != null) {
                List<Module> dependencies = current.getDependentBys();
                dependencies.sort(Comparator.comparing(Module::getArgStr));
                for (Module dependency : dependencies) {
                    int newCost = cost.get(dependency) - 1;
                    cost.replace(dependency, newCost);
                    if (newCost == 0) {
                        moduleStack.add(dependency);
                    }
                }
                tempCorrectOrder.add(current);
                current = moduleStack.pollFirst();
            }
            correctOrder.add(tempCorrectOrder);
        }
        return correctOrder;
    }

    @Test
    public void test1() {
        String[] correctOrder = {"base", "augments-2", "augments-1", "augments-3"};
        YangSchemaContext context = getSchemaContext(GraphTest.class.getClassLoader().getResource("graph1").getFile());
        List<Module> modules = context.getModules();
        List<Module> modulesInOrder = getModulesInOrder(context, modules).get(0);
        System.out.println("expected " + Arrays.deepToString(correctOrder));
        System.out.println("result " + modulesInOrder);
        assertArrayEquals(correctOrder, modulesInOrder.stream().map(Module::getArgStr).toArray(), "order is not correct");
    }

    @Test
    public void test2() {
        String[] correctOrder = {"base", "augments-2", "augments-1", "augments-3"};
        YangSchemaContext context = getSchemaContext(GraphTest.class.getClassLoader().getResource("graph2").getFile());
        List<Module> modules = context.getModules();
        List<Module> modulesInOrder = getModulesInOrder(context, modules).get(0);
        System.out.println("expected " + Arrays.deepToString(correctOrder));
        System.out.println("result " + modulesInOrder);
        assertArrayEquals(correctOrder, modulesInOrder.stream().map(Module::getArgStr).toArray(), "order is not correct");
    }

    @Test
    public void test3() {
        String[] correctOrder = {"base", "augments-1", "augments-2", "augments-3"};
        YangSchemaContext context = getSchemaContext(GraphTest.class.getClassLoader().getResource("graph3").getFile());
        List<Module> modules = context.getModules();
        List<Module> modulesInOrder = getModulesInOrder(context, modules).get(0);
        System.out.println("expected " + Arrays.deepToString(correctOrder));
        System.out.println("result " + modulesInOrder);
        assertArrayEquals(correctOrder, modulesInOrder.stream().map(Module::getArgStr).toArray(), "order is not correct");
    }

    @Test
    public void test4() {
        String[][] correctOrder = {
                {"base1", "augments1-1", "augments1-2", "augments1-3"},
                {"base2", "augments2-1", "augments2-2"},
                {"base3", "augments3-1"},
        };
        YangSchemaContext context = getSchemaContext(GraphTest.class.getClassLoader().getResource("graph4").getFile());
        List<Module> modules = context.getModules();
        List<List<Module>> modulesInOrder = getModulesInOrder(context, modules);
        System.out.println("expected " + Arrays.deepToString(correctOrder));
        System.out.println("result " + modulesInOrder);
        List<Module> modulesInOrder0 = modulesInOrder.get(0);
        List<Module> modulesInOrder1 = modulesInOrder.get(1);
        List<Module> modulesInOrder2 = modulesInOrder.get(2);
        assertArrayEquals(correctOrder[0], modulesInOrder0.stream().map(Module::getArgStr).toArray(), "order is not correct");
        assertArrayEquals(correctOrder[1], modulesInOrder1.stream().map(Module::getArgStr).toArray(), "order is not correct");
        assertArrayEquals(correctOrder[2], modulesInOrder2.stream().map(Module::getArgStr).toArray(), "order is not correct");
    }

    @Test
    public void test5() {
        YangSchemaContext context = getSchemaContext(GraphTest.class.getClassLoader().getResource("testYang").getFile());
    }

    @Test
    public void test6() {
        String[] correctOrder = {"base", "base2", "augments-1", "augments-2", "augments-3"};
        YangSchemaContext context = getSchemaContext(GraphTest.class.getClassLoader().getResource("graph6").getFile());
        List<Module> modules = context.getModules();
        List<Module> modulesInOrder = getModulesInOrder(context, modules).get(0);
        System.out.println("expected " + Arrays.deepToString(correctOrder));
        System.out.println("result " + getModulesInOrder(context, modules));
        assertArrayEquals(correctOrder, modulesInOrder.stream().map(Module::getArgStr).toArray(), "order is not correct");
    }

    @Test
    public void test7() {
        String[] correctOrder = {"remote"};
        YangSchemaContext context = getSchemaContext(GraphTest.class.getClassLoader().getResource("graph7").getFile(), false);
        List<Module> modules = context.getModules();
        List<Module> modulesInOrder = getModulesInOrder(context, modules).get(0);
        System.out.println("expected " + Arrays.deepToString(correctOrder));
        System.out.println("result " + getModulesInOrder(context, modules));
        assertArrayEquals(correctOrder, modulesInOrder.stream().map(Module::getArgStr).toArray(), "order is not correct");
    }

}
