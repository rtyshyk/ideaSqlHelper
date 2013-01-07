package com.pivasyk.ideaPlugin.actions;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.command.UndoConfirmationPolicy;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.project.Project;
import com.pivasyk.ideaPlugin.ClipboardHelper;

public class ExecuteImmediateAction extends AnAction {

    static String strTrailer = " || CHR(10) ||\n";
    static String strEmpty1 = "  CHR(10)";
    static String strEmpty2 = "            ||\n";
    static int nMinLineLen = 30;

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
                            String sqlString = clipboardHelper.getClipboard(); //end of each line
                            int sqlStringLines = sqlString.length() - sqlString.replaceAll("\n", "").length();
                            if (sqlStringLines == 0) {
                                sqlStringLines = 1;
                            }


                            int offset = document.getLineStartOffset(visualLineStart.getLine());

                            String strConverted = getConvertedBlock(sqlString, 0, sqlStringLines);
                            document.replaceString(offset, offset, strConverted);
                        }
                    }
                });
            }
        }, "ExecuteImmediate", UndoConfirmationPolicy.DO_NOT_REQUEST_CONFIRMATION);
    }

    /**
     * Author: Frank De Prins (frank.deprins@skynet.be)
     */
    public String getConvertedBlock(String sqlString, int p_nStartLine, int p_nStopLine) {
        boolean bIsCodeBlock = false;
        int nLineLen = nMinLineLen;
        StringBuffer sb = new StringBuffer(sqlString.length() * 2);
        String strExtraIndent = "";
        String[] sqlStringByLine = sqlString.split("\n");

        for (int i = p_nStartLine; i < p_nStopLine; ++i) {
            String line = sqlStringByLine[i];

            if (i == p_nStartLine) // check first line to see if we have a code block
            {
                // addd indentation of the first line to the whole block
                int j = 0;
                while (j < line.length() && line.charAt(j) == ' ') {
                    strExtraIndent += ' ';
                    ++j;
                }

                sb.append(strExtraIndent + "EXECUTE IMMEDIATE\n");

                bIsCodeBlock = line.toUpperCase().matches(
                        "^[ ]*CREATE([ ]+OR[ ]+REPLACE)?[ ]+(TRIGGER|PROCEDURE|FUNCTION|PACKAGE).+$");
                // remove the owner name which TOAD stuburnly prepends to object names,
                // even when you switch this off in the options dialog
                if (bIsCodeBlock)
                    line = line.replaceAll(" ([\"]?[a-zA-Z0-9_]+[\"]?)\\.([a-zA-Z0-9_]+)", " $2");
            } else {
                // make sure none of the lines, starting from the second one, have a
                // smaller indent than the first one
                if (strExtraIndent != "" && !line.startsWith(strExtraIndent)) {
                    // drop any leading whitespace (which must be less than the
                    // extra indent; otherways we would not be in this block) and
                    // prepend the extra indent
                    line = strExtraIndent + line.replaceAll("^[ ]+", "");
                }
            }
            // for the conversion; only pass the part after the initial indent length.
            line = ExecuteImmediateAction.doLineConversions(line.substring(strExtraIndent.length()),
                    i == (p_nStopLine -1), bIsCodeBlock, nLineLen);
            nLineLen = line.length();
            sb.append(strExtraIndent + line);
        }

        return sb.toString();
    }

    /**
     * Author: Frank De Prins (frank.deprins@skynet.be)
     */
    public static String doLineConversions(String p_strLine, boolean p_bIsLastLine, boolean p_bIsCodeLine, int p_nLineLen) {
        String strResult = "";
        // this is a replacement of rtrim (which Java's String lacks)
        String strLine = p_strLine.replaceAll("[ ]+$", "");

        for (int j = 0; j < strLine.length(); ++j) {
            strResult += strLine.charAt(j);
            if ('\'' == strLine.charAt(j)) {
                // repeat ' characters to double them
                strResult += strLine.charAt(j);
            }
        }
        if (p_bIsLastLine) {
            if (strResult.trim().length() > 0) {
                if (strResult.charAt(strResult.length() - 1) == ';') {
                    if (!p_bIsCodeLine)
                        strResult = strResult.substring(0, strResult.length() - 1);
                } else {
                    if (p_bIsCodeLine)
                        strResult += ';';
                }
                strResult = "  '" + strResult + "';\n";
            }
        } else // actions for not-last lines
        {
            if (strResult.trim().length() > 0) {
                strResult = "  '" + strResult + "'";
                while (strResult.length() + strTrailer.length() < p_nLineLen)
                    strResult += ' ';

                strResult += strTrailer;
            } else {
                // avoid concatenation with empty string because empty strings are
                // regarded as NULL
                strResult += strEmpty1;
                while (strResult.length() + strTrailer.length() < p_nLineLen)
                    strResult += ' ';
                strResult += strEmpty2;
            }
        }
        return strResult;
    }


}
