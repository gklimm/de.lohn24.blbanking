package de.lohn24.blbanking;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {
    private static final String BUNDLE_NAME = "de.lohn24.blbanking.Messages"; //$NON-NLS-1$
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    // Read the Messages for the dialog from the properties
    public static final String DIALOG_TITLE = Messages.getString("LoginDialog.title");
    public static final String DIALOG_MESSAGE = Messages.getString("LoginDialog.message");
    public static final String LOGIN_LABEL = Messages.getString("LoginDialog.loginLabel");
    public static final String PASSWORD_LABEL = Messages.getString("LoginDialog.passwordLabel");
    public static final String REMEMBER_PASSWORD_LABEL = Messages.getString("LoginDialog.rememberPasswordLabel");
    public static final String LOGIN_FAILED_ERROR_MSG = Messages.getString("LoginDialog.loginFailed");
    public static final String LOGIN_MISSING = Messages.getString("LoginDialog.loginRequired");
    public static final String PASSWORD_MISSING = Messages.getString("LoginDialog.passwordRequired");

    public static final String MANDANT_TITLE = Messages.getString("Mandant.title");
    public static final String MANDANT_MESSAGE = Messages.getString("Mandant.message");

    public static final String JOB_TITLE = Messages.getString("DownloadJob.title");
    public static final String TASK_NAME = Messages.getString("DownloadJob.task");
    public static final String ERROR_IN_JOB = Messages.getString("DownloadJob.error");
    public static final String NO_FILES_TITLE = Messages.getString("DownloadJob.noFiles.title");
    public static final String NO_FILES_MESSAGE = Messages.getString("DownloadJob.noFiles.message");

    private Messages() {
    }

    public static String getString(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }
}