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
 * @version : TransferPlan.java, v 0.1 2020年09月18日 14:33 calvin.gxy Exp $
 */
@Data
public class TransferPlan {

    private String startStation;

    private TransferConstraint transferConstraint;
}