package de.lohn24.blbanking;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * This class is the dialog that we open. It is used to ask the user for the
 * username and password used for Lohn24.de.
 * 
 * Username and password can be stored in a configuration file in the workspace
 * folder.
 */
public class LoginDialog extends TitleAreaDialog {

    private Exception exception = null;
    private String session = null;

    private Text valUsername;
    private Text valPassword;
    private Button rememberPasswordBtn;

    private JSONObject mandants;
    private int selected = 0; // the selected mandant, if more than one.

    // Is the user logged in
    private boolean loggedIn = false;

    public boolean isLoggedIn() {
        return loggedIn;
    }

    // To store the user properties
    private UsersProperties.UserData userData;

    public LoginDialog(Shell parent) {
        super(parent);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        if (getShell() != null) {
            getShell().setText(Messages.DIALOG_TITLE);
        }
        setTitle(Messages.DIALOG_TITLE);
        setMessage(Messages.DIALOG_MESSAGE, IMessageProvider.INFORMATION);

        // Load the user properties and get the saved values for login, password
        // and remember password
        try {
            userData = UsersProperties.load();
        } catch (IOException io) {
            setErrorMessage(io.toString());
            userData = null;
        }

        // Create the area for login, password and remember password
        Composite compositeLogin = new Composite(parent, SWT.NONE);
        compositeLogin.setLayout(new GridLayout(2, false));
        compositeLogin.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                true));

        // Create the login label and add it to the compositeLogin area
        Label label = new Label(compositeLogin, SWT.NONE);
        label.setText(Messages.LOGIN_LABEL);

        // Create the login input field
        valUsername = new Text(compositeLogin, SWT.BORDER);
        valUsername.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false,
                1, 1));
        // If we have userData, we fill in the user-name.
        if (userData != null) {
            // Load the saved login to the login input field
            valUsername.setText(userData.name);
        }

        // Create the password label in the loginComposite
        label = new Label(compositeLogin, SWT.NONE);
        label.setText(Messages.PASSWORD_LABEL);

        // Create the password input field
        valPassword = new Text(compositeLogin, SWT.PASSWORD | SWT.BORDER);
        valPassword.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false,
                1, 1));
        // If we have userData, we fill in the password.
        if (userData != null) {
            // but only if we should do so (otherwise the password should not be
            // stored).
            if (userData.remember) {
                // Set the value of password to the saved value
                valPassword.setText(userData.password);
            }
        }

        // Create the label for remember password in the compositLogin
        label = new Label(compositeLogin, SWT.NONE);
        label.setLayoutData(new GridData());

        // Create the check box button in the compositeLogin
        rememberPasswordBtn = new Button(compositeLogin, SWT.CHECK);
        rememberPasswordBtn.setText(Messages.REMEMBER_PASSWORD_LABEL);
        rememberPasswordBtn.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
                true, false, 1, 1));

        /*
         * If the user whishes not to save the values, we are doing so.
         * Otherwise (the default) is to remember the password.
         */
        if (userData != null) {
            rememberPasswordBtn.setSelection(userData.remember);
        } else {
            // Default here.
            rememberPasswordBtn.setSelection(true);
        }
        // Setting the focus. If we have a username, we set focus on password.
        if ((userData != null) && (userData.name != null)
                && (userData.name.length() > 0)) {
            // If password is also set, we focus on OK.
            if ((userData != null) && (userData.password != null)
                    && (userData.password.length() > 0)) {
                if (getButton(OK) != null) {
                    getButton(OK).setFocus();
                }
            } else {
                valPassword.setFocus();
            }
        } else {
            valUsername.setFocus();
        }
        return compositeLogin;
    }

    /**
     * On okPressed, we send the login-request to the server to check login and
     * password. If that does not work, we show an error. If it works, we close
     * this dialog and run the {@link MenuAction}.
     * 
     * This method also saves the userData.
     */
    protected void okPressed() {
        if (userData == null) {
            userData = new UsersProperties.UserData();
        }
        userData.name = valUsername.getText().trim();
        userData.remember = rememberPasswordBtn.getSelection();
        userData.password = valPassword.getText();

        // Username and password are required. Show an error if not filled.
        if ((userData.name == null) || (userData.name.length() == 0)) {
            setErrorMessage(Messages.LOGIN_MISSING);
            valUsername.setFocus();
            return;
        }
        if ((userData.password == null) || (userData.password.length() == 0)) {
            setErrorMessage(Messages.PASSWORD_MISSING);
            valPassword.setFocus();
            return;
        }

        exception = null;
        session = null;

        // As this might take longer, we use the busyIndicator here.
        BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
            public void run() {
                // // Now try the URL.
                try {
                    URL url = new URL(Constants.COMPANY_BASE_LOCATION
                            + Constants.COMPANY_SUB_LOGIN_LOCATION.trim()
                            + LoginDialog.this.userData.name + "/"
                            + LoginDialog.this.userData.password);
                    LoginDialog.this.session = getAllAsString(url.openStream());

                    // And we check for the list of mandants.
                    url = new URL(Constants.COMPANY_BASE_LOCATION + session
                            + Constants.COMPANY_LIST_MANDANT);
                    mandants = new JSONObject(getAllAsString(url.openStream()));

                } catch (IOException io) {
                    // TODO: Are all IO Exceptions are due to HTTP errors?
                    Activator.getDefault().getLog().log(
                            new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                                    "IOException for login at Lohn24: {0}",
                                    exception));
                } catch (Exception e) {
                    LoginDialog.this.exception = e;
                }
            }
        });

        if ((mandants != null) && (mandants.length() > 0)) {
            JSONArray names = mandants.getJSONArray("name");
            final String[] p = new String[names.length()];
            for (int i = 0; i < names.length(); i++) {
                p[i] = names.getString(i);
            }
            // If there are only a few mandants, we save one click.
            if (p.length <= 0) {
                MessageDialog d = new MessageDialog(getParentShell(),
                        Messages.MANDANT_TITLE, null, Messages.MANDANT_MESSAGE,
                        MessageDialog.QUESTION, p, 0);
                selected = d.open();
            } else {
                // More than 6 mandants. Use a checkbox.
                MessageDialog d = new MessageDialog(getParentShell(),
                        Messages.MANDANT_TITLE, null, Messages.MANDANT_MESSAGE,
                        MessageDialog.QUESTION, new String[] {
                                IDialogConstants.OK_LABEL,
                                IDialogConstants.CANCEL_LABEL }, 0) {
                    protected Control createCustomArea(Composite parent) {
                        final Combo c = new Combo(parent, SWT.READ_ONLY);
                        c.setItems(p);
                        c.select(0);
                        c.addSelectionListener(new SelectionAdapter() {
                            public void widgetSelected(SelectionEvent e) {
                                LoginDialog.this.selected = c.getSelectionIndex();
                            }
                        });
                        c.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
                                true, true));
                        return c;
                    }
                };
                int btn = d.open();
                if (btn != MessageDialog.OK) {
                    return;
                }
            }
            if (selected >= 0) {
                // Select that mandant id.
                BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
                    public void run() {
                        try {
                            URL url = new URL(
                                    Constants.COMPANY_BASE_LOCATION
                                            + session
                                            + Constants.COMPANY_SELECT_MANDANT
                                            + mandants.getJSONArray("mandant").getString(
                                                    selected));
                            String s = getAllAsString(url.openStream());
                            if (!"OK".equalsIgnoreCase(s)) {
                                LoginDialog.this.exception = new Exception(s);
                            }
                        } catch (Exception e) {
                            LoginDialog.this.exception = e;
                        }
                    }
                });
            }
        }

        // If we got an error, show that.
        if (exception != null) {
            Activator.getDefault().getLog().log(
                    new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                            "Error for login at Lohn24: {0}", exception));
            setErrorMessage(exception.toString());
            return;
        }
        if (session == null) {
            setErrorMessage(Messages.LOGIN_FAILED_ERROR_MSG);
            valPassword.setFocus();
            return;
        }

        try {
            if (!userData.remember) {
                userData.password = "";
            }
            UsersProperties.save(userData);
        } catch (IOException io) {
            Activator.getDefault().getLog().log(
                    new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                            "Error saving userData: {0}", exception));
        }
        super.okPressed();
    }

    public String getSessionId() {
        return session;
    }

    public static String getAllAsString(InputStream in) {
        Scanner scanner = new Scanner(in, "UTF-8");
        scanner.useDelimiter("\\A");
        try {
            return scanner.hasNext() ? scanner.next() : "";
        } finally {
            scanner.close();
        }
    }

    public boolean isValidMD5(String s) {
        return s.matches("[a-fA-F0-9]{32}");
    }
}