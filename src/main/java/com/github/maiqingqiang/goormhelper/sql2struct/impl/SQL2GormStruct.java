package com.github.maiqingqiang.goormhelper.sql2struct.impl;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.ast.statement.SQLColumnDefinition;
import com.github.maiqingqiang.goormhelper.utils.Strings;
import org.jetbrains.annotations.NotNull;

public class SQL2GormStruct extends SQL2Struct {
    public SQL2GormStruct(String sql, DbType dbType) {
        super(sql, dbType);
    }

    @Override
    protected void generateORMTag(@NotNull StringBuilder stringBuilder, @NotNull SQLColumnDefinition definition) {
        stringBuilder.append("gorm:\"")
                .append("column:").append(getColumn(definition)).append(";")
                .append("type:").append(getDBType(definition)).append(";")
                .append("comment:").append(getComment(definition)).append(";");

        if (definition.isPrimaryKey()) {
            stringBuilder.append("primaryKey;");
        }

        if (definition.containsNotNullConstaint()) {
            stringBuilder.append("not null;");
        }

        if (definition.getDefaultExpr() != null) {
            String def = Strings.clearSingleQuotn(definition.getDefaultExpr().toString());

            if (!def.isEmpty()) {
                stringBuilder.append("default:").append(def).append(";");
            }
        }

        stringBuilder.append("\" ");
    }
}
