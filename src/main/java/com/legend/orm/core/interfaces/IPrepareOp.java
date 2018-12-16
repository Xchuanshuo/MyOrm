package com.legend.orm.core.interfaces;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface IPrepareOp {
    void prepare(PreparedStatement stmt) throws SQLException;
}