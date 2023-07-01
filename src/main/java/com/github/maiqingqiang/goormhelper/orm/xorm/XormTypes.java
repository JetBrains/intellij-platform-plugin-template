package com.github.maiqingqiang.goormhelper.orm.xorm;

import com.goide.inspections.core.GoCallableDescriptor;
import com.goide.inspections.core.GoCallableDescriptorSet;
import com.goide.inspections.core.GoMethodDescriptor;

import java.util.List;
import java.util.Map;

public interface XormTypes {
    Map<GoCallableDescriptor, Integer> XORM_CALLABLES = Map.ofEntries(
            Map.entry(GoMethodDescriptor.of("(*xorm.io/xorm.Engine).Where"), 0),
            Map.entry(GoMethodDescriptor.of("(*xorm.io/xorm.Session).Where"), 0),
            Map.entry(GoMethodDescriptor.of("(*xorm.io/xorm.Session).And"), 0),
            Map.entry(GoMethodDescriptor.of("(*xorm.io/xorm.Session).Asc"), 0),
            Map.entry(GoMethodDescriptor.of("(*xorm.io/xorm.Session).Desc"), 0),
            Map.entry(GoMethodDescriptor.of("(*xorm.io/xorm.Engine).Select"), 0),
            Map.entry(GoMethodDescriptor.of("(*xorm.io/xorm.Session).Select"), 0),
            Map.entry(GoMethodDescriptor.of("(*xorm.io/xorm.Engine).In"), 0),
            Map.entry(GoMethodDescriptor.of("(*xorm.io/xorm.Session).In"), 0),
            Map.entry(GoMethodDescriptor.of("(*xorm.io/xorm.Session).Or"), 0),
            Map.entry(GoMethodDescriptor.of("(*xorm.io/xorm.Engine).Cols"), -1),
            Map.entry(GoMethodDescriptor.of("(*xorm.io/xorm.Session).Cols"), -1),
            Map.entry(GoMethodDescriptor.of("(*xorm.io/xorm.Engine).Omit"), -1),
            Map.entry(GoMethodDescriptor.of("(*xorm.io/xorm.Session).Omit"), -1),
            Map.entry(GoMethodDescriptor.of("(*xorm.io/xorm.Engine).Distinct"), -1),
            Map.entry(GoMethodDescriptor.of("(*xorm.io/xorm.Session).Distinct"), -1),
            Map.entry(GoMethodDescriptor.of("(*xorm.io/xorm.Engine).GroupBy"), 0),
            Map.entry(GoMethodDescriptor.of("(*xorm.io/xorm.Session).GroupBy"), 0),
            Map.entry(GoMethodDescriptor.of("(*xorm.io/xorm.Engine).Having"), 0),
            Map.entry(GoMethodDescriptor.of("(*xorm.io/xorm.Session).Having"), 0)
    );

    Map<GoCallableDescriptor, Integer> XORM_MODEL_CALLABLES = Map.ofEntries(
            Map.entry(GoMethodDescriptor.of("(*xorm.io/xorm.Session).Get"), 0),
            Map.entry(GoMethodDescriptor.of("(*xorm.io/xorm.Session).Find"), 0),
            Map.entry(GoMethodDescriptor.of("(*xorm.io/xorm.Session).Count"), 0),
            Map.entry(GoMethodDescriptor.of("(*xorm.io/xorm.Session).Iterate"), 0),
            Map.entry(GoMethodDescriptor.of("(*xorm.io/xorm.Session).Rows"), 0),
            Map.entry(GoMethodDescriptor.of("(*xorm.io/xorm.Session).Sum"), 0),
            Map.entry(GoMethodDescriptor.of("(*xorm.io/xorm.Session).Update"), 0),
            Map.entry(GoMethodDescriptor.of("(*xorm.io/xorm.Session).Delete"), 0)
    );

    List<String> OPERATOR_EXPR = List.of(
            "%s = ?", "%s <> ?", "%s IN ?", "%s LIKE ?", "%s > ?", "%s BETWEEN ? AND ?");

    Map<GoCallableDescriptor, List<String>> XORM_WHERE_EXPR = Map.ofEntries(
            Map.entry(GoMethodDescriptor.of("(*xorm.io/xorm.Engine).Where"), OPERATOR_EXPR),
            Map.entry(GoMethodDescriptor.of("(*xorm.io/xorm.Session).Where"), OPERATOR_EXPR),
            Map.entry(GoMethodDescriptor.of("(*xorm.io/xorm.Session).And"), OPERATOR_EXPR),
            Map.entry(GoMethodDescriptor.of("(*xorm.io/xorm.Session).Or"), OPERATOR_EXPR)
    );

    GoCallableDescriptorSet XORM_MODEL_CALLABLES_SET = new GoCallableDescriptorSet(XORM_MODEL_CALLABLES.keySet());

    GoCallableDescriptorSet XORM_CALLABLES_SET = new GoCallableDescriptorSet(XORM_CALLABLES.keySet());
}
