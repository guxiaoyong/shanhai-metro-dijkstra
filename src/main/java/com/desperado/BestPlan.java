package com.desperado;

import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class BestPlan {

    static Router[][] matrix = null;

    static LineManger lineManger = null;

    static List<Station> stations = new ArrayList<>();

    static Map<String, Integer> stationIndex = new HashMap<>();

    static {
        List list = GetStationInfo.getStation();
        StationManager stationManager = (StationManager) list.get(0);
        lineManger = (LineManger) list.get(1);
        int length = stationManager.getStations_number();
        matrix = new Router[length][length];

        int index = 0;
        for (Station station : stationManager.getStations()) {
            stationIndex.put(station.getStation(), index);
            index++;
            stations.add(station);
        }

        //构造矩阵
        for (int i = 0; i < stations.size(); i++) {
            for (int j = 0; j < stations.size(); j++) {
                List sameLines = stationManager.getSameLines(stations.get(i), stations.get(j));
                matrix[i][j] = lineManger.getBestRouter(stations.get(i).getStation(), stations.get(j).getStation(), sameLines);
            }
        }
    }

    public static void main(String[] args) {
        TransferConstraint transferConstraint = new TransferConstraint();
        transferConstraint.setTotalStations(8);
        transferConstraint.setTransferTimes(1);

        TransferPlan transferPlan1 = new TransferPlan();
        transferPlan1.setStartStation("北新泾");
        transferPlan1.setTransferConstraint(transferConstraint);

        TransferPlan transferPlan2 = new TransferPlan();
        transferPlan2.setStartStation("云锦路");
        transferPlan2.setTransferConstraint(transferConstraint);

        TransferPlan transferPlan3 = new TransferPlan();
        transferPlan3.setStartStation("上海马戏城");
        transferPlan3.setTransferConstraint(transferConstraint);

        List<TransferSolution> recommendPlan = bestPlan(Arrays.asList(transferPlan1, transferPlan2, transferPlan3),
                PlanJointType.AND);
        System.out.println(String.format("总站数不超过%d,且换乘次数不超过%d次的方案一共有%d种.", transferConstraint.getTotalStations(),
                transferConstraint.getTransferTimes(), recommendPlan.size()));

        System.out.println("========================");
        for (int i = 1; i <= recommendPlan.size(); i++) {
            TransferSolution transferSolution = recommendPlan.get(i - 1);
            String station = transferSolution.getStation();
            double score = transferSolution.getScore();
            List<RouterManager> routerManagers = transferSolution.getRouterManagers();
            System.out.println(String.format("%s.%s 得分:%.2f", i, station, score));
            for (RouterManager routerManager : routerManagers) {
                System.out.println("----------------------");
                for (Router router : routerManager.getStations()) {
                    System.out.print(router.getLine() + "号线:");
                    lineManger.printStops(Integer.parseInt(router.getLine()), router.getFromStation(), router.getToStation());
                }
            }
            System.out.println("========================");
        }
    }

    public static List<TransferSolution> bestPlan(List<TransferPlan> transferPlans, PlanJointType planJointType) {
        List<String> possibleStations = stations.stream().map(Station::getStation).collect(Collectors.toList());
        Map<String, List<RouterManager>> solutionMap = new HashMap<>();
        Map<TransferPlan, Map<String, RouterManager>> possibleRouters = new HashMap<>();
        for (TransferPlan transferPlan : transferPlans) {
            List<RouterManager> routerManagers = generateRouterManager(transferPlan, stationIndex, matrix, stations);
            Map<String, RouterManager> stationMap = new HashMap<>();
            routerManagers.stream().forEach(
                    routerManager -> {
                        Router router = routerManager.getStations().get(routerManager.getStations().size() - 1);
                        stationMap.put(router.getToStation(), routerManager);
                    });
            possibleRouters.put(transferPlan, stationMap);
            if (PlanJointType.AND == planJointType) {
                possibleStations = (List<String>) CollectionUtils.intersection(possibleStations, stationMap.keySet());
            }
        }

        for (String possibleStation : possibleStations) {
            List<RouterManager> currentStationRouters = new ArrayList<>();
            for (TransferPlan transferPlan : transferPlans) {
                RouterManager routerManager = possibleRouters.get(transferPlan).get(possibleStation);
                currentStationRouters.add(routerManager);
            }
            solutionMap.put(possibleStation, currentStationRouters);
        }
        List<TransferSolution> solutions = new ArrayList<>(solutionMap.size());
        for (Entry<String, List<RouterManager>> solutionEntry : solutionMap.entrySet()) {
            TransferSolution transferSolution = new TransferSolution();
            transferSolution.setStation(solutionEntry.getKey());
            transferSolution.setRouterManagers(solutionEntry.getValue());
            transferSolution.score();
            solutions.add(transferSolution);
        }
        Collections.sort(solutions, Comparator.comparing(TransferSolution::getScore).reversed());
        return solutions;
    }

    private static List<RouterManager> generateRouterManager(TransferPlan transferPlan, Map<String, Integer> stationIndex, Router[][]
            matrix, List<Station> stations) {

        RouterManager[] managers = new RouterManager[matrix.length - 1];
        int[] book = new int[matrix.length - 1];
        Integer startIndex = stationIndex.get(transferPlan.getStartStation());

        //初始化数据
        for (int i = 0; i < matrix.length - 1; i++) {
            List<Router> routers1 = new ArrayList<>();
            routers1.add(matrix[startIndex][i]);
            RouterManager manager = new RouterManager();
            manager.setValue(matrix[startIndex][i].getStations());
            manager.setStations(routers1);
            managers[i] = manager;
            book[i] = 0;
        }

        book[0] = 1;
        int u = 0;
        int n = stations.size();

        //dijkstra 算法实现
        for (int i = 0; i < n - 1; i++) {
            RouterManager minRouterM = new RouterManager();
            minRouterM.setValue(9999);
            List<Router> routers = new ArrayList<>();
            routers.add(new Router());
            minRouterM.setStations(routers);

            for (int j = 0; j < n - 1; j++) {
                if (book[j] == 0 && managers[j].getValue() < minRouterM.getValue()) {
                    minRouterM = managers[j];
                    u = j;
                }
            }
            book[u] = 1;

            for (int v = 0; v < n - 1; v++) {
                if (matrix[u][v].getStations() <= 9999) {
                    if (book[v] == 0 && managers[v].getValue() > (managers[u].getValue() + matrix[u][v].getStations() + 30)) {
                        List<Router> list1 = new ArrayList<>();
                        for (Router router : managers[u].getStations()) {
                            list1.add(router);
                        }
                        list1.add(matrix[u][v]);
                        RouterManager routerManager = new RouterManager();
                        routerManager.setValue(managers[u].getValue() + matrix[u][v].getStations());
                        routerManager.setStations(list1);
                        managers[v] = routerManager;
                    }
                }
            }
        }

        List<RouterManager> results = new ArrayList<>();
        for (RouterManager manager : managers) {
            boolean checkConstraint = checkConstraint(manager, transferPlan.getTransferConstraint());
            if (checkConstraint) {
                results.add(manager);
            }
        }
        return results;
    }

    public static boolean checkConstraint(RouterManager routerManager, TransferConstraint transferConstraint) {
        int transferTimes = routerManager.getStations().size() - 1;
        int totalStations = routerManager.getStations().stream().mapToInt(Router::getStations).sum();
        if (totalStations <= transferConstraint.getTotalStations() && transferTimes <= transferConstraint.getTransferTimes()) {
            return true;
        }
        return false;
    }

}