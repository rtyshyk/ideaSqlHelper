package com.pivasyk.ideaPlugin;

import java.awt.*;
import java.awt.datatransfer.*;
import java.io.IOException;

public class ClipboardHelper {

    public String getClipboard() {
        // get the system clipboard
        Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable clipboardContents = systemClipboard.getContents(null);

        if (clipboardContents == null) {
            return null;
        }
            try {
                if (clipboardContents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                    String returnText = (String) clipboardContents.getTransferData(DataFlavor.stringFlavor);
                    return returnText;
                }
            } catch (UnsupportedFlavorException ufe) {
                ufe.printStackTrace();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }

        return null;
    }

    public void setClipboard(String string){
        StringSelection stringSelection = new StringSelection(string);
        Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        systemClipboard.setContents(stringSelection, stringSelection);
    }

}