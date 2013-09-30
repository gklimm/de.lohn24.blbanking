package de.lohn24.blbanking;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * This action is called when either the toolbar-icon or the menu-entry is
 * selected. It opens the dialog to ask for the user's name and password. Once
 * the dialog is closed with OK, we run a job to download the files.
 */
public class MenuAction implements IWorkbenchWindowActionDelegate {

    /**
     * The action has been activated. The argument of the method represents the
     * 'real' action sitting in the workbench UI.
     * 
     * @see IWorkbenchWindowActionDelegate#run
     */
    public void run(IAction action) {
        LoginDialog d = new LoginDialog(null);
        if (d.open() == IDialogConstants.OK_ID) {
            // Do something.
            DownloadJob job = new DownloadJob(d.getSessionId());
            job.setUser(true);
            job.schedule();
        }
    }

    public void selectionChanged(IAction action, ISelection selection) {
    }

    public void dispose() {
    }

    public void init(IWorkbenchWindow window) {
    }
}