package com.github.maiqingqiang.goormhelper.services;

import com.github.maiqingqiang.goormhelper.Types;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SimpleModificationTracker;
import com.intellij.serviceContainer.NonInjectable;
import com.intellij.util.SmartList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Service(Service.Level.PROJECT)
@State(name = "GoORMHelperProjectSettings", storages = @Storage("goORMHelperSettings.xml"))
public final class GoORMHelperProjectSettings implements PersistentStateComponent<GoORMHelperProjectSettings.State> {

    private State state;
    private final Project project;

    public GoORMHelperProjectSettings(@NotNull Project project) {
        this(project, new State());
    }

    @NonInjectable
    private GoORMHelperProjectSettings(@NotNull Project project, State state) {
        this.state = state;
        this.project = project;
    }

    public static GoORMHelperProjectSettings getInstance(Project project) {
        return project.getService(GoORMHelperProjectSettings.class);
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull GoORMHelperProjectSettings.State state) {
        this.state = state;
    }

    public void setDefaultORM(Types.ORM orm) {
        state.defaultORM = orm;
    }

    public void setDefaultDatabase(Types.Database database) {
        state.defaultDatabase = database;
    }

    public void setEnableGlobalScan(boolean enable) {
        state.enableGlobalScan = enable;
    }

    public void setScanPathList(List<String> scanPathList) {
        state.scanPathList = scanPathList;
    }

    public void setSQLPath(String customTableCompletion) {
        state.sqlPath = customTableCompletion;
    }

    public static class State extends SimpleModificationTracker {
        public Types.ORM defaultORM = Types.ORM.AskEveryTime;
        public Types.Database defaultDatabase = Types.Database.AskEveryTime;
        public boolean enableGlobalScan = true;
        public List<String> scanPathList = new SmartList<>();

        public String sqlPath = "";
    }
}
