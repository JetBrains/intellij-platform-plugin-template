package com.github.maiqingqiang.goormhelper.ui;

import com.goide.GoFileType;
import com.goide.GoIcons;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.ui.JBColor;
import com.intellij.util.PlatformIcons;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.table.IconTableCellRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;

public class LocationColumn extends ColumnInfo<String, String> {

    public LocationColumn(@NlsContexts.ColumnName String name) {
        super(name);
    }

    @Override
    public @Nullable String valueOf(String s) {
        return s;
    }

    @Override
    public @Nullable TableCellRenderer getRenderer(String s) {
        return new IconTableCellRenderer<String>() {
            @Override
            protected @Nullable Icon getIcon(@NotNull String value, JTable table, int row) {
                VirtualFile file = VirtualFileManager.getInstance().findFileByUrl(value);
                if (file != null) {
                    if (file.isDirectory()) {
                        return PlatformIcons.FOLDER_ICON;
                    } else if (file.getPath().endsWith('.' + GoFileType.DEFAULT_EXTENSION)) {
                        return GoIcons.ICON;
                    }
                }
                return AllIcons.General.Error;
            }

            @Override
            protected void setValue(Object value) {
                String url = (String) value;
                VirtualFile file = VirtualFileManager.getInstance().findFileByUrl(url);
                if ((file == null)) {
                    setForeground(JBColor.RED);
                }
                setText(file != null ? file.getPresentableUrl() : url);
            }
        };
    }
}
