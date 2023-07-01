package com.github.maiqingqiang.goormhelper.ui;

import com.github.maiqingqiang.goormhelper.GoORMHelperBundle;
import com.github.maiqingqiang.goormhelper.Types;
import com.github.maiqingqiang.goormhelper.services.GoORMHelperManager;
import com.github.maiqingqiang.goormhelper.services.GoORMHelperProjectSettings;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.ConfigurableUi;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.TableView;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.ListTableModel;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.List;
import java.util.Objects;

public class GoORMHelperSettingForm implements ConfigurableUi<GoORMHelperProjectSettings> {

    private final Project project;
    private JPanel panel;
    private ComboBox<Types.ORM> ormComboBox;
    private ComboBox<Types.Database> databaseComboBox;
    private JCheckBox enableGlobalScanCheckBox;
    private ListTableModel<String> scanPathListTableModel;
//    private TextFieldWithBrowseButton sqlPathTextField;
    private TableView<String> scanPathTableView;

    public GoORMHelperSettingForm(Project project) {
        this.project = project;
        initComponent();
    }

    private void initComponent() {
        ormComboBox = new ComboBox<>(Types.ORM.values());
        databaseComboBox = new ComboBox<>(Types.Database.values());
        enableGlobalScanCheckBox = new JCheckBox(GoORMHelperBundle.message("setting.enableGlobalScanCheckBox.title"));

//        sqlPathTextField = new TextFieldWithBrowseButton();
//        sqlPathTextField.addBrowseFolderListener(GoORMHelperBundle.message("setting.sqlPathTextField.title"), null, null, FileChooserDescriptorFactory.createSingleFolderDescriptor());

        panel = FormBuilder.createFormBuilder()
                .addLabeledComponent(GoORMHelperBundle.message("setting.ormComboBox.title"), ormComboBox)
                .addLabeledComponent(GoORMHelperBundle.message("setting.databaseComboBox.title"), databaseComboBox)
//                .addLabeledComponent(GoORMHelperBundle.message("setting.sqlPathTextField.title"), sqlPathTextField)
                .addComponent(enableGlobalScanCheckBox)
                .addComponentFillVertically(initScanPathComponent(), 0)
                .getPanel();


        enableGlobalScanCheckBox.addChangeListener(e -> {
            scanPathTableView.setEnabled(!enableGlobalScanCheckBox.isSelected());
        });
    }

    private @NotNull JPanel initScanPathComponent() {
        scanPathListTableModel = new ListTableModel<>(new LocationColumn(GoORMHelperBundle.message("setting.tableview.column.location")));
        scanPathTableView = new TableView<>(scanPathListTableModel);
        scanPathTableView.setStriped(true);
        scanPathTableView.setMinRowHeight(25);
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(scanPathTableView, null);

        decorator.setAddAction(button -> {
            VirtualFile selectedFile =
                    FileChooser.chooseFile(getFileChooserDescriptor(GoORMHelperBundle.message("setting.decorator.title")), project, null);

            if (selectedFile != null) {
                scanPathListTableModel.insertRow(0, selectedFile.getUrl());
            }
        });

        JPanel scanPathPanel = decorator.createPanel();

        UIUtil.addBorder(scanPathPanel, IdeBorderFactory.createTitledBorder(GoORMHelperBundle.message("setting.decorator.title"), false));

        return scanPathPanel;
    }

    @Override
    public void reset(@NotNull GoORMHelperProjectSettings settings) {
        loadSettings(settings);
    }

    private void loadSettings(@NotNull GoORMHelperProjectSettings settings) {
        GoORMHelperProjectSettings.State state = Objects.requireNonNull(settings.getState());

        ormComboBox.setSelectedItem(state.defaultORM);
        databaseComboBox.setSelectedItem(state.defaultDatabase);
//        sqlPathTextField.setText(state.sqlPath);
        enableGlobalScanCheckBox.setSelected(state.enableGlobalScan);
        scanPathListTableModel.setItems(state.scanPathList);
        scanPathTableView.setEnabled(!state.enableGlobalScan);
    }


    @Override
    public boolean isModified(@NotNull GoORMHelperProjectSettings settings) {
        GoORMHelperProjectSettings.State state = Objects.requireNonNull(settings.getState());

        return !(ormComboBox.getSelectedItem() == state.defaultORM
                && databaseComboBox.getSelectedItem() == state.defaultDatabase
//                && sqlPathTextField.getText().equals(state.sqlPath)
                && enableGlobalScanCheckBox.isSelected() == state.enableGlobalScan
                && scanPathListTableModel.getItems().equals(state.scanPathList));
    }

    @Override
    public void apply(@NotNull GoORMHelperProjectSettings settings) {

        boolean oldEnableGlobalScan = Objects.requireNonNull(settings.getState()).enableGlobalScan;
        List<String> oldscanPathList = scanPathListTableModel.getItems();

        settings.setDefaultDatabase((Types.Database) databaseComboBox.getSelectedItem());
        settings.setDefaultORM((Types.ORM) ormComboBox.getSelectedItem());
        settings.setEnableGlobalScan(enableGlobalScanCheckBox.isSelected());
        settings.setScanPathList(scanPathListTableModel.getItems());
//        settings.setSQLPath(sqlPathTextField.getText());

        if (oldEnableGlobalScan != enableGlobalScanCheckBox.isSelected() || !oldscanPathList.equals(scanPathListTableModel.getItems())) {
            GoORMHelperManager goORMHelperManager = GoORMHelperManager.getInstance(project);
            goORMHelperManager.clear();
            goORMHelperManager.scan();
        }
    }

    @Override
    public @NotNull JComponent getComponent() {
        return panel;
    }

    private static FileChooserDescriptor getFileChooserDescriptor(@NlsContexts.DialogTitle String title) {
        FileChooserDescriptor descriptor = new FileChooserDescriptor(true, true, false, false, false, true)
                .withShowFileSystemRoots(true)
                .withShowHiddenFiles(true);

        if (title != null) {
            descriptor.setTitle(title);
        }

        return descriptor;
    }

}
