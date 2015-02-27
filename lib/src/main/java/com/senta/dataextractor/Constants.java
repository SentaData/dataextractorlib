package com.senta.dataextractor;

/**
 * Constants
 */
public interface Constants {
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String URL_PROPERTY = "url";

    // preferences
    String PREF_KEY_REFRESH_INTERVAL = "refresh_interval"; //$NON-NLS-1$

    int REFRESH_HIGH = 0;
    int REFRESH_NORMAL = 1;
    int REFRESH_LOW = 2;
    int REFRESH_PAUSED = 3;

    String PREF_KEY_SORT_ORDER_TYPE = "sort_order_type"; //$NON-NLS-1$
    String PREF_KEY_SORT_DIRECTION = "sort_direction"; //$NON-NLS-1$

    int ORDER_ASC = 1;
    int ORDER_DESC = -1;

    String PREF_KEY_SHOW_SIZE = "show_size"; //$NON-NLS-1$
    String PREF_KEY_SHOW_DATE = "show_date"; //$NON-NLS-1$
    String PREF_KEY_SHOW_ICON = "show_icon"; //$NON-NLS-1$
    String PREF_KEY_DEFAULT_TAP_ACTION = "default_tap_action"; //$NON-NLS-1$

    // context menu
    int MI_DELETE = 1;
    int MI_LAUNCH = 2;
    int MI_SEARCH = 3;
    int MI_DISPLAY = 4;
    int MI_ENDTASK = 5;
    int MI_IGNORE = 6;
    int MI_DETAILS = 7;
    int MI_END_OTHERS = 8;
    int MI_ARCHIVE = 9;
    int MI_MANAGE = 10;

    // option menu
    int MI_REVERT = 101;
    int MI_SHARE = 102;
    int MI_REFRESH = 103;
    int MI_CLEAR_CACHE = 104;
    int MI_SYNC_APP = 105;
    int MI_USAGE_STATS = 106;
    int MI_ABOUT = 107;
    int MI_HELP = 108;
    //int MI_UNLOCK = 109;
    int MI_PREFERENCE = 110;
    int MI_EXIT = 111;

    // message
    int MSG_INIT_OK = 1;
    int MSG_DISMISS_PROGRESS = 2;
    int MSG_CONTENT_READY = 3;
    int MSG_CHECK_FORCE_COMPRESSION = 4;
    int MSG_TOAST = 5;

    int MSG_PRIVATE = 200;

    // notification
    int NOTIFY_ERROR_REPORT = 1;
    int NOTIFY_EXPORT_FINISHED = 2;
    int NOTIFY_INFO_UPDATE = 3;
    int NOTIFY_TASK_UPDATE = 4;

    // format
    int PLAINTEXT = 0;
    int HTML = 1;
    int CSV = 2;
}
