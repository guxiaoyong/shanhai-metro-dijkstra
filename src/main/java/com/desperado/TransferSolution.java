/*
 * Alibaba.com Inc.
 * Copyright (c) 2004-2020 All Rights Reserved.
 */
package com.desperado;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO:doc
 *
 * @author calvin.gxy
 * @version : TransferSolution.java, v 0.1 2020年09月18日 16:32 calvin.gxy Exp $
 */
@Data
public class TransferSolution {

    private String station;

    private List<RouterManager> routerManagers;

    private double score;

    private double[] weight = new double[] {0.6, 0.35, 0.15};

    public void score() {
        List<Double> scores = new ArrayList<>();
        for (RouterManager routerManager : routerManagers) {
            double pscore = 15;
            List<Router> stations = routerManager.getStations();
            int value = routerManager.getValue();
            pscore = pscore - value - (stations.size() - 1) * 3;
            scores.add(pscore);
        }
        for (int i = 0; i < scores.size(); i++) {
            score += scores.get(i) * weight[i];
        }
    }
}