package com.github.maiqingqiang.goormhelper.ui;

import com.github.maiqingqiang.goormhelper.GoORMHelperBundle;
import com.github.maiqingqiang.goormhelper.services.GoORMHelperProjectSettings;
import com.github.maiqingqiang.goormhelper.ui.GoORMHelperSettingForm;
import com.intellij.openapi.options.ConfigurableBase;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class SettingConfigurable extends ConfigurableBase<GoORMHelperSettingForm, GoORMHelperProjectSettings> {

    private static final String ID = "go.orm.helper";
    private final Project project;

    public SettingConfigurable(Project project) {
        super(ID, GoORMHelperBundle.message("name"), null);
        this.project = project;
    }

    @Override
    protected @NotNull GoORMHelperProjectSettings getSettings() {
        return GoORMHelperProjectSettings.getInstance(project);
    }

    @Override
    protected GoORMHelperSettingForm createUi() {
        return new GoORMHelperSettingForm(project);
    }
}
