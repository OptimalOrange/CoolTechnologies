package com.optimalorange.cooltechnologies.network;

import com.optimalorange.cooltechnologies.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;

/**
 * 网络连接检查器。<br />可以用它判断能否建立网络连接，{@link ConnectivityManager#TYPE_WIFI WiFi}等网络是否可用。
 * 不能建立网络连接时，可以借助它询问用户是否打开系统设置中的网络设置界面。
 */
public class NetworkChecker {

    private final ConnectivityManager mConnectivityManager;

    /**
     * {@link #openNoConnectionDialog(FragmentManager)}打开的{@link DialogFragment}的tag
     */
    public static final String FRAGMENT_TAG_NO_CONNECTION_DIALOG =
            NetworkChecker.class.getName() + ".fragment_tag_no_connection_dialog";

    /**
     * 用{@link #newInstance(Context context)}实例化
     */
    private NetworkChecker(Context context) {
        mConnectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    /**
     * 获取新{@link NetworkChecker}实例
     *
     * @param context 用于获取{@link ConnectivityManager}的{@link Context}。
     * @return 新创建的NetworkChecker实例
     * @see Context#getSystemService(String)
     */
    public static NetworkChecker newInstance(Context context) {
        return new NetworkChecker(context);
    }

    /**
     * 根据{@link Context}取得{@link ConnectivityManager}
     *
     * @return {@code context.getSystemService(Context.CONNECTIVITY_SERVICE)}
     */
    public ConnectivityManager getConnectivityManager() {
        return mConnectivityManager;
    }

    /**
     * 取得当前可用的默认数据网络的类型（the type of currently active default data network）
     *
     * @return 如果没有可用的默认数据网络，或者还不能用它建立网络连接，则返回null；<br/>其他情况下，返回
     * {@link ConnectivityManager#TYPE_MOBILE       TYPE_MOBILE}、
     * {@link ConnectivityManager#TYPE_WIFI         TYPE_WIFI}、
     * {@link ConnectivityManager#TYPE_WIMAX        TYPE_WIMAX}、
     * {@link ConnectivityManager#TYPE_ETHERNET     TYPE_ETHERNET}、
     * {@link ConnectivityManager#TYPE_BLUETOOTH    TYPE_BLUETOOTH}
     * 或者其它{@link ConnectivityManager}定义的网络类型。
     * @see android.net.ConnectivityManager#getActiveNetworkInfo()
     */
    public Integer getActiveNetworkType() {
        NetworkInfo networkInfo = getConnectivityManager().getActiveNetworkInfo();
        return networkInfo == null || !networkInfo.isConnected() ? null : networkInfo.getType();
    }

    /**
     * 是否可以建立Internet连接。
     *
     * @return 可以返回true；不可能建立返回false
     */
    public boolean isConnected() {
        return getActiveNetworkType() != null;
    }

    /**
     * 是否正在通过{@link ConnectivityManager#TYPE_MOBILE 移动网络}联网。
     *
     * @return 是返回true；不是返回false
     */
    public boolean isMobileConnected() {
        Integer type = getActiveNetworkType();
        return type != null && type == ConnectivityManager.TYPE_MOBILE;
    }

    /**
     * 是否正在通过{@link ConnectivityManager#TYPE_WIFI WiFi}联网。
     *
     * @return 是返回true；不是返回false
     */
    public boolean isWifiConnected() {
        Integer type = getActiveNetworkType();
        return type != null && type == ConnectivityManager.TYPE_WIFI;
    }

    /**
     * 打开网络设置Activity。
     *
     * @return 成功打开返回true，失败返回false
     * @see android.provider.Settings#ACTION_WIRELESS_SETTINGS
     */
    public static boolean openWirelessSettings(Context context) {
        Intent intent = new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
        // 验证此intent可被响应
        if (context.getPackageManager().queryIntentActivities(intent, 0).size() > 0) {
            context.startActivity(intent);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 打开“无网络连接”对话框，提问是否打开网络设置Activity。
     */
    public static void openNoConnectionDialog(FragmentManager manager) {
        NoConnectionDialogFragment newDialog = new NoConnectionDialogFragment();
        newDialog.show(manager, FRAGMENT_TAG_NO_CONNECTION_DIALOG);
    }

    /**
     * “无网络连接”对话框。提问是否打开网络设置Activity。
     */
    public static class NoConnectionDialogFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.no_connection_title)
                    .setMessage(R.string.no_connection_message)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            openWirelessSettings(getActivity());
                        }
                    })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });
            return builder.create();
        }
    }

}
