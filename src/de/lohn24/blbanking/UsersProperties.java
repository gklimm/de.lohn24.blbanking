package de.lohn24.blbanking;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import de.businesslogics.banking.transfer.core.Api;

/**
 * This class is managing the <code>users</code> file in the <code>key</code>
 * directory of lohn24. In that file we have a list of all existing users and we
 * store the following information per user:
 * 
 * <dl>
 * <dt>User<code>username</code></dt>
 * <dd>contains the username for the lohn24 that should be preset in the login
 * dialog.</dd>
 * <dt>Password<code>username</code></dt>
 * <dd>contains the encrypted password for the lohn24 that should be preset in
 * the login dialog.</dd>
 * <dt>RememberPassword<code>username</code></dt>
 * <dd>contains the remember password flag of the login dialog</dd>
 * </dl>
 * 
 * As there are multiple instances running at the same time, we don't keep the
 * file open. Each modification is done by reading, updating and saving in one
 * call.
 */
public class UsersProperties {
    private static final char[] PWD = "HP6DorNBCf".toCharArray(); //$NON-NLS-1$

    private static final String REMEMBER_COMPANY_PASSWORD = "RememberPassword"; //$NON-NLS-1$
    private static final String PREFIX_COMPANY_USERNAME = "User"; //$NON-NLS-1$
    private static final String PREFIX_COMPANY_PASSWORD = "Password"; //$NON-NLS-1$

    public static class UserData {
        String name;
        String password;
        boolean remember;
    }

    /**
     * Gets the data object for the current user from the rememberd file.
     * 
     * @return the data-object found, or <code>null</code> if nothing is stored.
     */
    public static UserData load() throws IOException {
        File dir = new File(Platform.getInstanceLocation().getURL().getFile()
                + File.separator + Constants.COMPANY_DIRECTORY_NAME);
        dir.mkdirs();
        File file = new File(dir, "keys"); //$NON-NLS-1$
        if (file.canRead()) {
            Properties p = new Properties();
            FileInputStream fin = new FileInputStream(file);
            p.load(fin);
            fin.close();

            String userName = System.getProperty("user.name");
            UserData d = new UserData();
            String tmp = p.getProperty(REMEMBER_COMPANY_PASSWORD + userName);
            if (tmp == null) {
                // No entry, no object.
                return null;
            } else {
                d.remember = Boolean.valueOf(tmp);
            }
            tmp = p.getProperty(PREFIX_COMPANY_USERNAME + userName);
            if (tmp != null) {
                d.name = tmp;
            }
            if (d.remember) {
                tmp = p.getProperty(PREFIX_COMPANY_PASSWORD + userName);
                if (tmp != null) {
                    try {
                        d.password = Api.decryptData(tmp, PWD);
                    } catch (Exception e) {
                        Activator.getDefault().getLog().log(
                                new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                                        "Error decrypting the password", e));
                        d.password = "";
                    }
                }
            }
            return d;
        }
        // else, no file or it can not be read.
        return null;
    }

    public static void save(UserData data) throws IOException {
        File file = new File(Platform.getInstanceLocation().getURL().getFile()
                + File.separator + Constants.COMPANY_DIRECTORY_NAME
                + File.separator + "keys"); //$NON-NLS-1$
        Properties p = new Properties();
        if (file.canRead()) {
            FileInputStream fin = new FileInputStream(file);
            p.load(fin);
            fin.close();
        }
        String userName = System.getProperty("user.name");
        p.setProperty(PREFIX_COMPANY_USERNAME + userName, data.name);
        p.setProperty(REMEMBER_COMPANY_PASSWORD + userName,
                String.valueOf(data.remember));
        if (data.remember) {
            try {
            p.setProperty(PREFIX_COMPANY_PASSWORD + userName, Api.encryptData(data.password, PWD));
            } catch(Exception e) {
                Activator.getDefault().getLog().log(
                        new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                                "Error encrypting the password", e));
            }
        } else {
            p.remove(PREFIX_COMPANY_PASSWORD+userName);
        }
        FileOutputStream fout = new FileOutputStream(file);
        p.store(fout, null);
        fout.close();
    }

}
