package com.github.maiqingqiang.goormhelper.sql2struct.impl;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLColumnDefinition;
import com.alibaba.druid.sql.ast.statement.SQLCreateTableStatement;
import com.alibaba.druid.sql.ast.statement.SQLTableElement;
import com.github.maiqingqiang.goormhelper.sql2struct.ISQL2Struct;
import com.github.maiqingqiang.goormhelper.utils.Strings;
import com.google.common.base.CaseFormat;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class SQL2Struct implements ISQL2Struct {
    private final String sql;
    private final DbType dbType;

    private static final Map<String, String> dataType = Map.ofEntries(
            Map.entry("numeric", "int32"),
            Map.entry("integer", "int32"),
            Map.entry("int", "int32"),
            Map.entry("smallint", "int32"),
            Map.entry("mediumint", "int32"),
            Map.entry("tinyint", "int32"),
            Map.entry("bigint", "int64"),
            Map.entry("float", "float32"),
            Map.entry("real", "float64"),
            Map.entry("double", "float64"),
            Map.entry("decimal", "float64"),
            Map.entry("char", "string"),
            Map.entry("varchar", "string"),
            Map.entry("tinytext", "string"),
            Map.entry("mediumtext", "string"),
            Map.entry("longtext", "string"),
            Map.entry("binary", "[]byte"),
            Map.entry("varbinary", "[]byte"),
            Map.entry("tinyblob", "[]byte"),
            Map.entry("blob", "[]byte"),
            Map.entry("mediumblob", "[]byte"),
            Map.entry("longblob", "[]byte"),
            Map.entry("text", "string"),
            Map.entry("json", "string"),
            Map.entry("enum", "string"),
            Map.entry("time", "time.Time"),
            Map.entry("date", "time.Time"),
            Map.entry("datetime", "time.Time"),
            Map.entry("timestamp", "time.Time"),
            Map.entry("year", "int32"),
            Map.entry("bit", "[]uint8"),
            Map.entry("boolean", "bool")
    );

    private static final String defaultDataType = "string";


    public SQL2Struct(String sql, DbType dbType) {
        this.sql = sql;
        this.dbType = dbType;
    }

    public String convert() {
        StringBuilder stringBuilder = new StringBuilder();

        List<SQLStatement> statementList = SQLUtils.parseStatements(sql, dbType);

        for (SQLStatement statement : statementList) {
            singleConvert(stringBuilder, statement);
        }


        return stringBuilder.toString();
    }

    private void singleConvert(@NotNull StringBuilder stringBuilder, SQLStatement statement) {
        SQLCreateTableStatement createTableStatement = (SQLCreateTableStatement) statement;

        String tableName = Strings.clearQuote(createTableStatement.getTableName());
        tableName = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, tableName);

        stringBuilder.append("type ").append(tableName).append(" struct {\n");


        List<SQLTableElement> tableElementList = createTableStatement.getTableElementList();

        for (SQLTableElement sqlTableElement : tableElementList) {
            if (sqlTableElement instanceof SQLColumnDefinition sqlColumnDefinition) {
                generateStructField(stringBuilder, sqlColumnDefinition);
            }
        }

        stringBuilder.append("}\n\n");
    }

    private String getGoType(String type) {
        return dataType.getOrDefault(type, defaultDataType);
    }

    protected String getGoType(@NotNull SQLColumnDefinition definition) {
        return getGoType(definition.getDataType().getName());
    }

    protected void generateStructField(@NotNull StringBuilder stringBuilder, @NotNull SQLColumnDefinition definition) {
        stringBuilder.append("\t")
                .append(getField(definition))
                .append(" ")
                .append(getGoType(definition))
                .append(" ");

        generateStructTags(stringBuilder, definition);

        stringBuilder.append("// ")
                .append(getComment(definition))
                .append("\n");
    }

    protected void generateStructTags(@NotNull StringBuilder stringBuilder, @NotNull SQLColumnDefinition definition) {
        stringBuilder.append("`");
        generateORMTag(stringBuilder, definition);
        generateJsonTag(stringBuilder, definition);
        stringBuilder.append("`");
    }

    protected void generateORMTag(@NotNull StringBuilder stringBuilder, @NotNull SQLColumnDefinition definition) {

    }

    protected void generateJsonTag(@NotNull StringBuilder stringBuilder, @NotNull SQLColumnDefinition definition) {
        stringBuilder.append("json:\"").append(getColumn(definition)).append("\"");
    }

    @NotNull
    protected String getField(@NotNull SQLColumnDefinition definition) {
        return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, getColumn(definition));
    }

    protected String getDBType(@NotNull SQLColumnDefinition definition) {
        return definition.getDataType().toString();
    }

    @NotNull
    protected String getColumn(@NotNull SQLColumnDefinition definition) {
        return Strings.clearQuote(definition.getName().getSimpleName());
    }

    @NotNull
    protected String getComment(@NotNull SQLColumnDefinition definition) {
        return Strings.clearSingleQuotn(definition.getComment().toString());
    }

}
