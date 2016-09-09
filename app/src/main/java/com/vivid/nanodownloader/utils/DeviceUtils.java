package com.vivid.nanodownloader.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;


import com.vivid.nanodownloader.BuildConfig;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.util.Locale;
import java.util.UUID;

public class DeviceUtils {

    public enum ConnectionType {
        UNKNOWN("Unknown", 0),
        ETHERNET("Ethernet", 1),
        WIFI("WIFI", 2),
        TYPE_2G("2G", 3),
        TYPE_3G("3G", 4),
        TYPE_4G("4G", 5);

        private final String name;
        private final int value;

        ConnectionType(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public int getValue() {
            return value;
        }
    }

    public static ConnectionType getNetworkMainType(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            if (info.getType() == ConnectivityManager.TYPE_ETHERNET) {
                return ConnectionType.ETHERNET;
            } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                return ConnectionType.WIFI;
            } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                switch (info.getSubtype()) {
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_IDEN:
                        return ConnectionType.TYPE_2G;
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EHRPD:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B:
                    case TelephonyManager.NETWORK_TYPE_HSPAP:
                        return ConnectionType.TYPE_3G;
                    case TelephonyManager.NETWORK_TYPE_LTE:
                        return ConnectionType.TYPE_4G;
                    case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                    default:
                        return ConnectionType.UNKNOWN;
                }
            }
        }
        return ConnectionType.UNKNOWN;
    }

    public static String getNetworkMainTypeName(Context context) {
        return getNetworkMainType(context).getName();
    }

    private static final String TAG = "DeviceInfoUtils";
    private static final String DEFAULT_VALUE = "NONE";
    private static final int MCC_END_INDEX = 3;
    private static String sUID;
    private static String sWUID;
    private static String PRODUCT_SIGN = "01";
    private static final String UID_DIRECTORY = "." + BuildConfig.APPLICATION_ID;
    private static final String DEVICE_SETTINGS_PREFS = "device_settings";

    private enum Id {
        UID(0),
        WUID(1);

        static final String[][] sNames = {
                {
                        "uid",
                        BuildConfig.APPLICATION_ID + ".userid",
                        BuildConfig.APPLICATION_ID + ".checksum",
                        "uuid",
                        "checkSum"
                },
                {
                        getHash(".wuid"),
                        getHash(BuildConfig.APPLICATION_ID + ".wuid"),
                        getHash(BuildConfig.APPLICATION_ID + ".wuid.checksum"),
                        getHash("wuid"),
                        getHash("wuid.checksum")
                }
        };

        private int mIndex;

        Id(int index) {
            mIndex = index;
        }

        String getFileName() {
            return sNames[mIndex][0];
        }

        String getSystemSettingKey(boolean checksum) {
            return sNames[mIndex][checksum ? 1 : 2];
        }

        String getUserSettingKey(boolean checksum) {
            return sNames[mIndex][checksum ? 3 : 4];
        }

        String generateNewId(Context context) {
            return mIndex == 0 ? getUUID() : generateWUID(context);
        }
    }

    private static String getHash(String str) {
        long hash = str.hashCode();
        hash <<= 32;

        final int p = 16777619;
        int hashlow = (int) 2166136261L;

        for (int i = 0; i < str.length(); i++) {
            hashlow = (hashlow ^ str.charAt(i)) * p;
        }

        hashlow += hashlow << 13;
        hashlow ^= hashlow >> 7;
        hashlow += hashlow << 3;
        hashlow ^= hashlow >> 17;
        hashlow += hashlow << 5;

        hash |= hashlow;
        return String.valueOf(hash > 0 ? hash : -hash);
    }

    private static String getUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    private static String generateWUID(Context context) {
        final String imei = getIMEI(context);
        final String androidId = getAndroidID(context);
        final String uuid = getUUID();
        StringBuilder sb = new StringBuilder();
        if (!DEFAULT_VALUE.equals(imei)) {
            sb.append(imei);
        }
        if (!DEFAULT_VALUE.equals(androidId)) {
            sb.append(androidId);
        }
        sb.append(uuid);

        final String deviceId = MD5Utils.getMD5(sb.toString());
        if (deviceId != null && deviceId.length() > 25) {
            StringBuilder wuidBuilder = new StringBuilder();
            wuidBuilder.append(deviceId.substring(0, 16))
                    .append(PRODUCT_SIGN)
                    .append(deviceId.charAt(1))
                    .append(deviceId.charAt(8))
                    .append(deviceId.charAt(20))
                    .append(deviceId.charAt(22))
                    .append(deviceId.substring(16));
            return wuidBuilder.toString();
        }
        return DEFAULT_VALUE;
    }

    private static TelephonyManager getTelephonyManager(Context context) {
        return (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    }

    public static String getMCC(Context context) {
        String simOperator = getTelephonyManager(context).getSimOperator();
        return !TextUtils.isEmpty(simOperator) && simOperator.length() >= MCC_END_INDEX ?
                simOperator.substring(0, MCC_END_INDEX) : "";
    }

    public static String getMNC(Context context) {
        String simOperator = getTelephonyManager(context).getSimOperator();
        return !TextUtils.isEmpty(simOperator) && simOperator.length() > MCC_END_INDEX ?
                simOperator.substring(MCC_END_INDEX) : "";
    }

    private static ConnectivityManager getConnectivityManager(Context context) {
        return (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public static boolean isNetworkAvailable(Context context) {
        NetworkInfo networkInfo = getConnectivityManager(context).getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isAvailable()) {
            return true;
        }

        return false;
    }

    public static String getNetworkExtraInfo(Context context) {
        NetworkInfo networkInfo = getConnectivityManager(context).getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isAvailable() && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            return networkInfo.getExtraInfo();
        }

        return null;
    }

    public static int getNetworkType(Context context) {
        return getTelephonyManager(context).getNetworkType();
    }

    private static String defaultValue(String val) {
        return TextUtils.isEmpty(val) ? DEFAULT_VALUE : val;
    }

    private static WifiManager getWifiManager(Context context) {
        return (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }


    public static String getIMEI(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return DEFAULT_VALUE;
        }
        String deviceId = DEFAULT_VALUE;
        try {
            deviceId = getTelephonyManager(context).getDeviceId();
        } catch (Exception ex) {
            return DEFAULT_VALUE;
        }
        return defaultValue(deviceId);
    }

    public static String getIMSI(Context context) {
        String subscriberId = getTelephonyManager(context).getSubscriberId();
        return defaultValue(subscriberId);
    }

    public static String getNetworkOperatorName(Context context) {
        return defaultValue(getTelephonyManager(context).getNetworkOperatorName());
    }

    public static String getDeviceSerialID() {
        String serialId = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                serialId = GingerBreadCompatLayer.getAndroidSerial();
            }
        } catch (Throwable t) {}
        return defaultValue(serialId);
    }

    public static String getUserID(Context context) {
        if (sUID == null) {
            sUID = getId(context, Id.UID);
        }

        return sUID;
    }

    private static String getId(Context context, Id idType) {
        String id = null;
        id = readIdfromSystemSetting(context, idType);
        if (id == null) {
            id = readIdFromUserSetting(context, idType);
            if (id == null) {
                id = readIdfromFile(context, idType);
                if (id == null) {
                    id = idType.generateNewId(context);
                }
            }
        }

        writeIdToFile(context, id, idType);
        writeIdToUserSetting(context, id, idType);
        writeIdToSystemSetting(context, id, idType);

        return id;
    }

    private static String readIdfromSystemSetting(Context context, Id id) {
        String userID = null;
        String tmpUID = Settings.System.getString(context.getContentResolver(),
                id.getSystemSettingKey(false));
        String checkSum = Settings.System.getString(context.getContentResolver(),
                id.getSystemSettingKey(true));
        if (isUserIDValid(tmpUID, checkSum)) {
            userID = tmpUID;
        }

        return userID;
    }

    private static boolean isUserIDValid(String userID, String checkSum) {
        return userID != null && checkSum != null
                && getHash(userID).equals(checkSum);
    }

    private static String readIdFromUserSetting(Context context, Id id) {
        String userID = null;
        try {
            // StrictModeHelper.getInstance().allowThreadDiskReads(true);
            SharedPreferences userPref = context.getSharedPreferences(DEVICE_SETTINGS_PREFS,
                    Context.MODE_MULTI_PROCESS);
            String tmpUID = userPref.getString(id.getUserSettingKey(false), null);
            String checkSum = userPref.getString(id.getUserSettingKey(true), null);
            if (isUserIDValid(tmpUID, checkSum)) {
                userID = tmpUID;
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "Error on reading user id", e);
        } finally {
            // StrictModeHelper.getInstance().allowThreadDiskReads(false);
        }
        return userID;
    }

    private static String readIdfromFile(Context context, Id id) {
        String userID = null;
        String tmpUID = null;
        String checkSum = null;
        BufferedReader reader = null;

        try {
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                return null;
            }

            File d = new File(Environment.getExternalStorageDirectory(), UID_DIRECTORY);
            if (!d.exists()) {
                return null;
            }

            reader = new BufferedReader(new FileReader(new File(d, id.getFileName())));
            tmpUID = reader.readLine();
            checkSum = reader.readLine();
        } catch (Exception e) {
            LogUtils.e(TAG, "Reading user id from file failed", e);
        } finally {
            IOUtils.close(reader);
        }

        if (isUserIDValid(tmpUID, checkSum)) {
            userID = tmpUID;
        }

        return userID;
    }

    private static void writeIdToFile(Context context, String userID, Id id) {
        BufferedWriter writer = null;

        try {
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                return;
            }

            File d = new File(Environment.getExternalStorageDirectory(), UID_DIRECTORY);
            if (!d.exists()) {
                d.mkdir();
            }

            writer = new BufferedWriter(new FileWriter(new File(d, id.getFileName())));
            writer.write(userID);
            writer.newLine();
            writer.write(getHash(userID));
        } catch (Exception e) {
            LogUtils.e(TAG, "Could not write UserID into file", e);
        } finally {
            IOUtils.close(writer);
        }
    }

    private static void writeIdToUserSetting(Context context, String userID, Id id) {
        String checkSum = getHash(userID);
        SharedPreferences userPref = context.getSharedPreferences(DEVICE_SETTINGS_PREFS, Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = userPref.edit();
        editor.putString(id.getUserSettingKey(false), userID);
        editor.putString(id.getUserSettingKey(true), checkSum);
        editor.apply();
    }

    private static void writeIdToSystemSetting(Context context, String userID, Id id) {
        try {
            String checkSum = getHash(userID);
            Settings.System.putString(context.getContentResolver(), id.getSystemSettingKey(false),
                    userID);
            Settings.System.putString(context.getContentResolver(), id.getSystemSettingKey(true),
                    checkSum);
        } catch (Exception e) {
            LogUtils.e(TAG, "Could not write UserID into Settings", e);
        }
    }

    public static String getWUID(Context context) {
        if (sWUID == null) {
            sWUID = getId(context, Id.WUID);
        }
        return sWUID;
    }

    public static String getMacAddress(Context context) {
        WifiManager wifiManager = getWifiManager(context);
        if (wifiManager == null || wifiManager.getConnectionInfo() == null
                || wifiManager.getConnectionInfo().getMacAddress() == null) {
            return DEFAULT_VALUE;
        }
        return wifiManager.getConnectionInfo().getMacAddress();
    }

    public static String getPlatform() {
        return "ad_" + Build.VERSION.RELEASE;
    }

    public static String getDeviceModel() {
        return Build.MODEL;
    }

    public static String getManufacturer() {
        return defaultValue(Build.MANUFACTURER);
    }

    public static String getBuildBoard() {
        return defaultValue(Build.BOARD);
    }

    public static String getBuildBootLoader() {
        return defaultValue(Build.BOOTLOADER);
    }

    public static String getBuildBrand() {
        return defaultValue(Build.BRAND);
    }

    public static String getBuildDevice() {
        return defaultValue(Build.DEVICE);
    }

    public static String getBuildDisplay() {
        return defaultValue(Build.DISPLAY);
    }

    public static String getBuildFingerPrint() {
        return defaultValue(Build.FINGERPRINT);
    }

    public static String getBuildHardware() {
        return defaultValue(Build.HARDWARE);
    }

    public static String getBuildId() {
        return defaultValue(Build.ID);
    }

    public static String getBuildProduct() {
        return defaultValue(Build.PRODUCT);
    }

    public static String getBuildRadio() {
        String radioString = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            radioString = IceCreamSandwichCompatLayer.getBuildRadio();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            radioString = FroyoCompatLayer.getBuildRadio();
        }
        return defaultValue(radioString);
    }

    public static String getAndroidID(Context context) {
        String androidID = DEFAULT_VALUE;
        Class<?> cls = Secure.class;
        try {
            Field fld = cls.getDeclaredField("ANDROID_ID");
            if (fld == null) {
                return androidID;
            }
        } catch (NoSuchFieldException e) {
            return androidID;
        }
        return getAndroidIDNormal(context);
    }

    public static int getScreenWidth(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.widthPixels;
    }

    public static int getScreenHeight(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.heightPixels;
    }

    public static String getUserCountry(Context context) {
        try {
            final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            final String simCountry = tm.getSimCountryIso();
            if (simCountry != null && simCountry.length() == 2) {
                // SIM country code is available
                return simCountry.toUpperCase(Locale.US);
            } else if (tm.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) {
                // device is not 3G (would be unreliable)
                String networkCountry = tm.getNetworkCountryIso();
                if (networkCountry != null && networkCountry.length() == 2) {
                    // network country code is available
                    return networkCountry.toUpperCase(Locale.US);
                }
            }
        }
        catch (Exception e) {}
        // fall back to locale configure
        return context.getResources().getConfiguration().locale.getCountry().toUpperCase(Locale.US);
    }
    private static String getAndroidIDNormal(Context context) {
        try {
            String id = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
            return defaultValue(id);
        } catch (Throwable t) {
            return DEFAULT_VALUE;
        }
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private static class GingerBreadCompatLayer {
        public static String getAndroidSerial() {
            return Build.SERIAL;
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private static class IceCreamSandwichCompatLayer {
        public static String getBuildRadio() {
            return Build.getRadioVersion();
        }
    }

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private static class FroyoCompatLayer {
        public static String getBuildRadio() {
            return Build.RADIO;
        }
    }
}
