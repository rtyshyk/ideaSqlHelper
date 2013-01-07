package com.pivasyk.ideaPlugin.actions;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.command.UndoConfirmationPolicy;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.project.Project;
import com.pivasyk.ideaPlugin.*;

public class SqlToStringAction extends AnAction {

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
                            EditorModificationUtil.deleteSelectedText(editor);

                            CaretModel caretModel = editor.getCaretModel();
                            VisualPosition visualLineStart = caretModel.getVisualPosition();
                            final Document document = editor.getDocument();


                            ClipboardHelper clipboardHelper = new ClipboardHelper();
                            String sqlString = clipboardHelper.getClipboard();

                            String formattedString = sqlString
                                    .trim()
                                    .replaceAll("^", "\"")//start string
                                    .replaceAll("\n", " \\\\n\".\n\"") //end of each line
                                    .replaceAll("$", "\";"); //end string

                            int visualLineStartColumn = visualLineStart.getColumn();
                            formattedString = StringHelper.padLeftStringByLine(formattedString, visualLineStartColumn);

                            int offset = document.getLineStartOffset(visualLineStart.getLine());
                            document.replaceString(offset, offset, formattedString);
                        }
                    }
                });
            }
        }, "SqlToString", UndoConfirmationPolicy.DO_NOT_REQUEST_CONFIRMATION);
    }
}
