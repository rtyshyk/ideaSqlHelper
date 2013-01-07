package com.pivasyk.ideaPlugin.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.command.UndoConfirmationPolicy;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.project.Project;
import com.pivasyk.ideaPlugin.ClipboardHelper;
import com.pivasyk.ideaPlugin.StringHelper;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class StringToSqlAction extends AnAction {

    public void update(AnActionEvent event) {
        event.getPresentation().setEnabled(event.getData(PlatformDataKeys.EDITOR) != null);
    }

    public void actionPerformed(final AnActionEvent event) {

        final Project currentProject = event.getData(PlatformDataKeys.PROJECT);

        CommandProcessor.getInstance().executeCommand(currentProject, new Runnable() {
            public void run() {

                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    public void run() {
                        Editor editor = event.getData(PlatformDataKeys.EDITOR);

                        if (editor != null) {
                            //remove block, if something selected
                            ClipboardHelper clipboardHelper = new ClipboardHelper();
                            SelectionModel selectionModel = editor.getSelectionModel();
                            String selectedText = selectionModel.getSelectedText();

                            selectedText = StringHelper.removeStringOffset(selectedText);

                            String sqlString = selectedText
                                    .trim()
                                    .replaceAll("\\s*\\\\n\\s*\"\\s*.", "")//end each line
                                    .replaceAll("\\/\\/.+\\n", "\n") //clear php comments
                                    .replaceAll("\"\\..+\\$([a-zA-Z_1-9]+).\\.\"", ":\1")
                                    .replaceAll(";\"$", "");

                            clipboardHelper.setClipboard(sqlString);
                        }
                    }
                });
            }
        }, "SqlToString", UndoConfirmationPolicy.DO_NOT_REQUEST_CONFIRMATION);
    }
}
