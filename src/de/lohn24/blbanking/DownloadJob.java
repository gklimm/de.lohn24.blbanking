package de.lohn24.blbanking;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.json.JSONArray;

import de.businesslogics.banking.transfer.core.Api;

/**
 * This class does most of the job. Only the login is already done.
 */
public class DownloadJob extends Job {

    private String session;

    public DownloadJob(String sessionId) {
        super(Messages.JOB_TITLE);
        this.session = sessionId;
    }

    private String[] names;
    private HashMap<String, String> idMapping;

    @Override
    protected IStatus run(IProgressMonitor p) {
        SubMonitor sub = SubMonitor.convert(p, Messages.TASK_NAME, 102);

        try {
            // Downloading the files data
            sub.newChild(1);
            // Send next request to get dtaus files
            URL url = new URL(Constants.COMPANY_BASE_LOCATION + session
                    + Constants.COMPANY_SUB_FILES_LIST_LOCATION);

            JSONArray dtaus = new JSONArray(
                    LoginDialog.getAllAsString(url.openStream()));
            sub.newChild(1);

            if (dtaus.length() < 1) {
                Display.getDefault().asyncExec(new Runnable() {
                    public void run() {
                        // Show that there are not files and exit.
                        MessageDialog.openInformation(
                                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                                Messages.NO_FILES_TITLE,
                                Messages.NO_FILES_MESSAGE);
                    }
                });
                return Status.OK_STATUS;
            }
            sub.setWorkRemaining(dtaus.length());
            File dir = new File(
                    Platform.getInstanceLocation().getURL().getFile()
                            + File.separator + Constants.COMPANY_DIRECTORY_NAME
                            + File.separator + "download");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            HashSet<File> files = new HashSet<File>();
            idMapping = new HashMap<String, String>();
            for (int i = 0; i < dtaus.length(); i++) {
                JSONArray dta = dtaus.getJSONArray(i);
                File f = new File(dir, dta.getString(0) + "_"
                        + dta.getString(1) + ".DTAUS");
                files.add(f);
                idMapping.put(f.getAbsolutePath(), dta.getString(0));

                URL fileURL = new URL(Constants.COMPANY_BASE_LOCATION + session
                        + Constants.COMPANY_SUB_FILE_LOCATION
                        + dta.getString(0));
                InputStream is = fileURL.openStream();
                // Stream to the destination file
                FileOutputStream fos = new FileOutputStream(f);
                byte[] buffer = new byte[4096];
                int bytesRead = 0;
                while ((bytesRead = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
                // Close destination stream
                fos.close();
                // Close URL stream
                is.close();

                sub.newChild(1);
            }

            names = new String[files.size()];
            int i = 0;
            for (File f : files) {
                names[i] = f.getAbsolutePath();
                i++;
            }
            Arrays.sort(names);
            // Now open the send-file dialog with those files.
            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    Api.openSendFileDialog(names, new MyListener(idMapping));
                }
            });
            return Status.OK_STATUS;
        } catch (Exception e) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    Messages.ERROR_IN_JOB, e);
        }
    }

    public class MyListener extends JobChangeAdapter {

        HashMap<String, String> filesToId = null;

        public MyListener(HashMap<String, String> filesToId) {
            this.filesToId = filesToId;
        }

        @Override
        public void done(IJobChangeEvent arg0) {
            Job job = arg0.getJob();
            if (job != null) {
                if (arg0.getResult().isOK()) {
                    try {
                        String id = filesToId.get(job.getName());
                        URL fileURL = new URL(Constants.COMPANY_BASE_LOCATION
                                + session + Constants.COMPANY_SUB_COMMIT_FILE
                                + id);
                        String ok = LoginDialog.getAllAsString(fileURL.openStream());
                        Activator.getDefault().getLog().log(
                                new Status(IStatus.INFO, Activator.PLUGIN_ID,
                                        "commit for " + id + " returned: " + ok));
                    } catch (IOException io) {
                        // TODO
                    }
                }
            }
        }
    }
}
