package com.github.maiqingqiang.goormhelper.sql2struct.impl;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.ast.statement.SQLColumnDefinition;
import com.github.maiqingqiang.goormhelper.utils.Strings;
import org.jetbrains.annotations.NotNull;

public class SQL2XormStruct extends SQL2Struct {
    public SQL2XormStruct(String sql, DbType dbType) {
        super(sql, dbType);
    }

    @Override
    protected void generateORMTag(@NotNull StringBuilder stringBuilder, @NotNull SQLColumnDefinition definition) {
        stringBuilder.append("xorm:\"");

        stringBuilder.append(getDBType(definition)).append(" ");
        stringBuilder.append("'").append(getColumn(definition)).append("' ");
        stringBuilder.append("comment('").append(getComment(definition)).append("') ");

        if (definition.isPrimaryKey()) {
            stringBuilder.append("pk ");
        }

        if (definition.isAutoIncrement()){
            stringBuilder.append("autoincr ");
        }

        if (definition.containsNotNullConstaint()) {
            stringBuilder.append("notnull ");
        }

        if (definition.getDefaultExpr() != null) {
            String def = Strings.clearSingleQuotn(definition.getDefaultExpr().toString());

            if (!def.isEmpty()) {
                stringBuilder.append("default ").append(def).append(" ");
            }
        }

        stringBuilder.append("\" ");
    }
}
