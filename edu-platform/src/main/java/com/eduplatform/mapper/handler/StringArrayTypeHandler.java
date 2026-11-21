package com.eduplatform.mapper.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * PostgreSQL {@code text[]} 与 {@code String[]} 的互转。
 */
@MappedTypes(String[].class)
@MappedJdbcTypes(JdbcType.ARRAY)
public class StringArrayTypeHandler extends BaseTypeHandler<String[]> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String[] parameter, JdbcType jdbcType)
            throws SQLException {
        Array array = ps.getConnection().createArrayOf("text", parameter);
        ps.setArray(i, array);
    }

    @Override
    public String[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toArray(rs.getArray(columnName));
    }

    @Override
    public String[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toArray(rs.getArray(columnIndex));
    }

    @Override
    public String[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toArray(cs.getArray(columnIndex));
    }

    private static String[] toArray(Array array) throws SQLException {
        if (array == null) {
            return null;
        }
        Object value = array.getArray();
        if (value instanceof String[] strings) {
            return strings;
        }
        Object[] objects = (Object[]) value;
        String[] strings = new String[objects.length];
        for (int i = 0; i < objects.length; i++) {
            strings[i] = objects[i] == null ? null : objects[i].toString();
        }
        return strings;
    }
}
