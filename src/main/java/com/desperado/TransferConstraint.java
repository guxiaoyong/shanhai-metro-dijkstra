/*
 * Alibaba.com Inc.
 * Copyright (c) 2004-2020 All Rights Reserved.
 */
package com.desperado;

import lombok.Data;

/**
 * TODO:doc
 *
 * @author calvin.gxy
 * @version : TransferConstraint.java, v 0.1 2020年09月18日 12:06 calvin.gxy Exp $
 */
@Data
public class TransferConstraint {

    /**
     * 总站数
     */
    private int totalStations;
    /**
     * 换乘次数
     */
    private int transferTimes;
}