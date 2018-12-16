package com.legend.orm.core.interfaces;

import java.sql.Connection;

@FunctionalInterface
public interface ITransactionOp {
    void execute(Connection conn);
}