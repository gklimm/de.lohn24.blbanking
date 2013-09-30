package de.lohn24.blbanking;

/**
 * Constant definitions for plug-in preferences
 */
public class Constants {

    // Base-URL for all requests.
    public static final String COMPANY_BASE_LOCATION = "https://www.lohn24.de/";
    // Sub-URL for the login
    public static final String COMPANY_SUB_LOGIN_LOCATION = "online-test/d.php/login/";

    public static final String COMPANY_LIST_MANDANT="/online-test/d.php/listMandant/";
    public static final String COMPANY_SELECT_MANDANT="/online-test/d.php/selectMandant/";
    public static final String COMPANY_SUB_LOGOUT_LOCATION = "/online-test/d.php/logout";
    public static final String COMPANY_SUB_FILE_LOCATION = "/online-test/d.php/getDTAUS/";
    public static final String COMPANY_SUB_FILES_LIST_LOCATION = "/online-test/d.php/getDTAUSlist/false";
    public static final String COMPANY_SUB_COMMIT_FILE = "/online-test/d.php/commitDTAUS/";

    // Sub-Directory in the workspace location.
    public static final String COMPANY_DIRECTORY_NAME="lohn24";
}
