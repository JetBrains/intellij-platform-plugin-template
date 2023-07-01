package com.github.maiqingqiang.goormhelper.listeners;

import com.github.maiqingqiang.goormhelper.GoORMHelperBundle;
import com.github.maiqingqiang.goormhelper.actions.EditorPasteListener;
import com.github.maiqingqiang.goormhelper.services.GoORMHelperManager;
import com.github.maiqingqiang.goormhelper.services.GoORMHelperProjectSettings;
import com.goide.GoFileType;
import com.goide.psi.GoFile;
import com.goide.psi.GoStructType;
import com.goide.psi.GoTypeSpec;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.actionSystem.EditorActionManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.BackgroundTaskQueue;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiManager;
import com.intellij.workspaceModel.ide.VirtualFileUrlManagerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MyProjectManagerListener implements ProjectManagerListener {
    private static final Logger LOG = Logger.getInstance(MyProjectManagerListener.class);

    @Override
    public void projectOpened(@NotNull Project project) {
        intializing(project);
    }

    private void intializing(@NotNull Project project) {

        BackgroundTaskQueue taskQueue = new BackgroundTaskQueue(project, GoORMHelperBundle.message("name"));
        taskQueue.run(new Task.Backgroundable(project, GoORMHelperBundle.message("initializing.title")) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                GoORMHelperManager.getInstance(project).scan();
            }
        });
    }
}
