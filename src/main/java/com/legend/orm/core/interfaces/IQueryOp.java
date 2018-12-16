package com.legend.orm.core.interfaces;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface IQueryOp {
    ResultSet query(PreparedStatement stmt) throws SQLException;
}