package com.github.maiqingqiang.goormhelper.listeners;

import com.github.maiqingqiang.goormhelper.bean.ScannedPath;
import com.github.maiqingqiang.goormhelper.services.GoORMHelperManager;
import com.goide.GoFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SchemaFileListener implements BulkFileListener {


    private final Project project;

    public SchemaFileListener(Project project) {
        this.project = project;
    }

    @Override
    public void after(@NotNull List<? extends @NotNull VFileEvent> events) {
        for (VFileEvent event : events) {
            if (event.isFromSave() && event.getFile() != null && event.getFile().isValid() && event.getPath().endsWith('.' + GoFileType.DEFAULT_EXTENSION)) {

                VirtualFile file = event.getFile();

                GoORMHelperManager goORMHelperManager = GoORMHelperManager.getInstance(this.project);

                ScannedPath scannedPath = goORMHelperManager.getScannedPath(file);
                if (scannedPath == null) continue;
                goORMHelperManager.clearScanned(file);

                goORMHelperManager.parseGoFile(file);
            }
        }
    }
}
