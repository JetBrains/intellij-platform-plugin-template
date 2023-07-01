package com.github.maiqingqiang.goormhelper.ui;

import com.github.maiqingqiang.goormhelper.GoORMHelperBundle;
import com.github.maiqingqiang.goormhelper.Types;
import com.github.maiqingqiang.goormhelper.services.GoORMHelperProjectSettings;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.LabeledComponent;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;

public class ConvertSettingDialogWrapper extends DialogWrapper {
    private JPanel contentPane;

    @lombok.Getter
    private LabeledComponent<JComboBox<Types.Database>> databaseComponent;

    @lombok.Getter
    private LabeledComponent<JComboBox<Types.ORM>> ormComponent;

    public ConvertSettingDialogWrapper(Project project) {
        super(true);

        GoORMHelperProjectSettings.State state = Objects.requireNonNull(GoORMHelperProjectSettings.getInstance(project).getState());

        ComboBox<Types.ORM> ormComboBox = new ComboBox<>(Types.ORM.values());
        ormComboBox.removeItem(Types.ORM.AskEveryTime);
        ormComboBox.setSelectedItem(state.defaultORM);

        ormComponent.setComponent(ormComboBox);

        ComboBox<Types.Database> databaseComboBox = new ComboBox<>(Types.Database.values());
        databaseComboBox.removeItem(Types.Database.AskEveryTime);
        databaseComboBox.setSelectedItem(state.defaultDatabase);
        databaseComponent.setComponent(databaseComboBox);

        setTitle(GoORMHelperBundle.message("sql.convert.struct"));

        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return contentPane;
    }
}
