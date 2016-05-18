package net.theunifyproject.lethalskillzz.app;

import java.io.File;

/**
 * Created by Ibrahim on 16/10/2015.
 */
public class AppConfig {

    public static final int REG_STAGE_ZERO = 0;
    public static final int REG_STAGE_ONE = 1;
    public static final int REG_STAGE_TWO = 2;
    public static final int REG_STAGE_THREE = 3;

    // Server url
    public static String SERVER_URL = "http://192.168.56.1/TheUnifyProject/";
    //public static String SERVER_URL = "https://theunifyproject-1218.appspot.com/";

    public static String  URL_REGISTER_PROFILE_DETAIL = SERVER_URL+"auth/register_profile_detail.php";
    public static String  URL_REGISTER_ACCOUNT_DETAIL = SERVER_URL+"auth/register_account_detail.php";
    public static String  URL_USER_LOGIN = SERVER_URL+"auth/user_login.php";
    public static String  URL_VERIFY_OTP = SERVER_URL+"auth/verify_otp.php";
    public static String  URL_RESEND_OTP = SERVER_URL+"auth/resend_otp.php";
    public static String  URL_GCM_TOKEN = SERVER_URL+"auth/submit_gcm_token.php";
    public static String  URL_CHANGE_NUMBER = SERVER_URL+"auth/change_number.php";
    public static String  URL_CHANGE_PASSWORD = SERVER_URL+"auth/change_password.php";
    public static String  URL_CHANGE_NUMBER_VERIFY_OTP = SERVER_URL+"auth/change_number_verify_otp.php";
    public static String  URL_RECOVER_PASSWORD = SERVER_URL+"auth/recover_password.php";

    public static String  URL_LOAD_FEED = SERVER_URL+"feed/load_feed.php";
    public static String  URL_LOAD_HASH = SERVER_URL+"feed/load_hash.php";
    public static String  URL_LOAD_PROFILE_FEED = SERVER_URL+"feed/load_profile_feed.php";
    public static String  URL_DISPLAY_FEED = SERVER_URL+"feed/display_feed.php";
    public static String  URL_POST_FEED = SERVER_URL+"feed/post_feed.php";
    public static String  URL_EDIT_FEED = SERVER_URL+"feed/edit_feed.php";
    public static String  URL_LIKE_FEED = SERVER_URL+"feed/like_feed.php";
    public static String  URL_DELETE_FEED = SERVER_URL+"feed/delete_feed.php";
    public static String  URL_POST_COMMENT = SERVER_URL+"feed/post_comment.php";
    public static String  URL_LOAD_LIKE = SERVER_URL+"feed/load_like.php";
    public static String  URL_LOAD_COMMENT = SERVER_URL+"feed/load_comment.php";

    public static String  URL_LOAD_NOTIFICATION = SERVER_URL+"notification/load_notification.php";
    public static String  URL_SEEN_NOTIFICATION = SERVER_URL+"notification/seen_notification.php";

    public static String  URL_LOAD_DISCOVER = SERVER_URL+"discover/load_discover.php";
    public static String  URL_LOAD_INTRO_DISCOVER = SERVER_URL+"discover/load_intro_discover.php";
    public static String  URL_FOLLOW_INTRO_DISCOVER = SERVER_URL+"discover/follow_intro_discover.php";

    public static String  URL_LOAD_SEARCH = SERVER_URL+"search/load_search.php";
    public static String  URL_LOAD_CONTACT_SEARCH = SERVER_URL+"search/load_contact_search.php";
    public static String  URL_LOAD_SHOP_SEARCH = SERVER_URL+"search/load_shop_search.php";
    public static String  URL_LOAD_STORE_SEARCH = SERVER_URL+"search/load_store_search.php";
    public static String  URL_LOAD_REPO_SEARCH = SERVER_URL+"search/load_repo_search.php";
    public static String  URL_LOAD_DIGEST_SEARCH = SERVER_URL+"search/load_digest_search.php";
    public static String  URL_LOAD_TAG_POPUP = SERVER_URL+"search/load_tag_popup.php";

    public static String  URL_LOAD_PROFILE_DETAIL = SERVER_URL+"profile/load_profile_detail.php";
    public static String  URL_EDIT_PROFILE = SERVER_URL+"profile/edit_profile.php";
    public static String  URL_CHANGE_PROFILE_PIC = SERVER_URL+"profile/change_profile_pic.php";
    public static String  URL_PROFILE_PIC = SERVER_URL+"profile/pic/";

    public static String  URL_LOAD_FOLLOW = SERVER_URL+"follow/load_follow.php";
    public static String  URL_FOLLOW_USER = SERVER_URL+"follow/follow_user.php";


    public static String  URL_LOAD_REPOSITORY = SERVER_URL+"extra/load_repository.php";
    public static String  URL_LOAD_DIGEST = SERVER_URL+"extra/load_digest.php";
    public static String  URL_LOAD_SHOPPING = SERVER_URL+"extra/load_shopping.php";
    public static String  URL_DISPLAY_SHOP = SERVER_URL+"extra/display_shopping.php";
    public static String  URL_POST_SHOP = SERVER_URL+"extra/post_shopping.php";
    public static String  URL_EDIT_SHOP = SERVER_URL+"extra/edit_shopping.php";
    public static String  URL_DELETE_SHOP = SERVER_URL+"extra/delete_shopping.php";
    public static String  URL_LOAD_STORE = SERVER_URL+"extra/load_store.php";
    public static String  URL_LOAD_STORE_DETAIL = SERVER_URL+"extra/load_store_detail.php";
    public static String  URL_UPDATE_STORE = SERVER_URL+"extra/update_store.php";
    public static String  URL_RATE_STORE = SERVER_URL+"extra/rate_store.php";
    public static String  URL_LOAD_STORE_SETTING = SERVER_URL+"extra/load_store_setting.php";
    public static String  URL_POST_TRANSIT = SERVER_URL+"extra/post_transit.php";
    public static String  URL_LOAD_BUS_TRANSIT = SERVER_URL+"extra/load_bus_transit.php";
    public static String  URL_LOAD_TAXI_TRANSIT = SERVER_URL+"extra/load_taxi_transit.php";


    public static String  URL_REPORT_USER = SERVER_URL+"policy/report_user.php";
    public static String  URL_REPORT_FEED = SERVER_URL+"policy/report_feed.php";
    public static String  URL_REPORT_SHOP = SERVER_URL+"policy/report_shop.php";


    //Application file directories
    public static String  DIR_APPLICATION = File.separator+"TheUnifyProject"+File.separator;

    public static String  DIR_FEED_IMAGE = DIR_APPLICATION+"Feed"+File.separator;
    public static String  DIR_PROFILE_IMAGE = DIR_APPLICATION+"Profile"+File.separator;
    public static String  DIR_SHOP_IMAGE = DIR_APPLICATION+"Shop"+File.separator;
    public static String  DIR_DOWNLOAD_PDF = DIR_APPLICATION+"Pdf"+File.separator;

    // GCM project number
    public static String GCM_SENDER_ID = "595002836068";

    // SMS provider identification
    public static final String SMS_ORIGIN = "theunifyproject.net";

    //special character to prefix the otp. Make sure this character appears only once in the sms
    public static final String OTP_DELIMITER = ":";

    public static final String AUTO_REF_HACK () {

        return "";//"?ref="+System.currentTimeMillis();
    }



    //Push notification IDs
    public static final int NOTIFICATION_FOLLOW = 0;
    public static final int NOTIFICATION_MENTION = 1;
    public static final int NOTIFICATION_COMMENT = 2;
    public static final int NOTIFICATION_LIKE = 3;
    public static final int NOTIFICATION_REPO = 4;
    public static final int NOTIFICATION_SHOP = 5;
    public static final int NOTIFICATION_DIGEST = 6;
    public static final int NOTIFICATION_TRANSIT = 7;


    //HttpService intent types
    public static final String httpIntentOtp = "http_otp";
    public static final String httpIntentPostFeed = "http_post_feed";
    public static final String httpIntentPostComment = "http_post_comment";
    public static final String httpIntentLikeFeed = "http_like_feed";
    public static final String httpIntentFollowUser = "http_follow_user";
    public static final String httpIntentDeleteFeed = "http_delete_feed";
    public static final String httpIntentReportFeed = "http_report_feed";
    public static final String httpIntentEditFeed = "http_edit_feed";
    public static final String httpIntentPostShop = "http_post_shop";
    public static final String httpIntentChangeProfilePic = "http_change_profile_pic";
    public static final String httpIntentSeenNotification = "http_seen_notify";
    public static final String httpIntentEditShop = "http_edit_shop";
    public static final String httpIntentDeleteShop = "http_delete_shop";
    public static final String httpIntentPostTransit = "http_post_transit";
    public static final String httpIntentReportShop = "http_report_shop";
    public static final String httpIntentUpdateStore = "http_update_store";
    public static final String httpIntentRateStore = "http_rate_store";
    public static final String httpIntentReportUser = "http_report_user";


    //HttpService messageHandler IDs
    public static final int httpHandlerOtp = 0;
    public static final int httpHandlerPostFeed = 1;
    public static final int httpHandlerPostComment = 2;
    public static final int httpHandlerLikeFeed = 3;
    public static final int httpHandlerDeleteFeed = 4;
    public static final int httpHandlerReportFeed = 5;
    public static final int httpHandlerFollowUser = 6;
    public static final int httpHandlerPostShop = 7;
    public static final int httpHandlerChangeProfilePic = 8;
    public static final int httpHandlerSeenNotification = 9;
    public static final int httpHandlerEditShop = 10;
    public static final int httpHandlerDeleteShop = 11;
    public static final int httpHandlerEditFeed = 12;
    public static final int httpHandlerPostTransit = 13;
    public static final int httpHandlerReportShop = 14;
    public static final int httpHandlerUpdateStore = 15;
    public static final int httpHandlerRateStore = 16;
    public static final int httpHandlerReportUser = 17;

    public static final int smsHandlerOtp = 18;

    //UserListActivity intent types
    public static final String listFollowing = "list_following";
    public static final String listFollowers = "list_followers";
    public static final String listLike = "list_like";


}
