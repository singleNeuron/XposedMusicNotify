package cn.nexus6p.QQMusicNotify.Utils;

import android.app.AndroidAppHelper;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.core.view.ViewCompat;
import androidx.preference.Preference;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import cn.nexus6p.QQMusicNotify.BuildConfig;
import cn.nexus6p.QQMusicNotify.MainActivity;
import cn.nexus6p.QQMusicNotify.R;
import de.robv.android.xposed.XposedBridge;

final public class GeneralUtils {

    private static boolean isCheckingUpdate = false;
    private static boolean isDownloading = false;

    public static Context getContext() {
        return AndroidAppHelper.currentApplication().getApplicationContext();
    }

    public static Context getModuleContext(Context context) {
        Context moduleContext = null;
        try {
            moduleContext = context.createPackageContext(BuildConfig.APPLICATION_ID, Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
        } catch (PackageManager.NameNotFoundException e) {
            XposedBridge.log(e);
        }
        return moduleContext;
    }

    public static Context getModuleContext() {
        return getModuleContext(getContext());
    }

    public static JSONArray getSupportPackages(Context context) {
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(getAssetsString("packages.json", context));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonArray;
    }

    public static boolean isStringInJSONArray(String string, JSONArray jsonArray) {
        if (jsonArray == null) return false;
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                if (jsonArray.getJSONObject(i).getString("app").equals(string)) return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static String getAssetsString(String fileName, Context context) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            File file = new File(context.getExternalFilesDir(null) + File.separator + fileName);
            //File file = new File(Environment.getExternalStorageDirectory().getPath()+File.separator+"Android/data/cn.nexus6p.QQMusicNotify/files/" +fileName);
            BufferedReader bf = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (Exception e) {
            return null;
        }
        return stringBuilder.toString();
    }

    public static void editFile(File file, Context activity) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID + ".fileProvider", file);
        intent.setDataAndType(uri, "text/*");
        try {
            activity.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //抄的https://www.jianshu.com/p/4e12da9866a0
    public static void getJsonFromInternet(MainActivity activity, boolean shouldShowToast) {
        if (isCheckingUpdate) return;
        final String url = "https://xposedmusicnotify.singleneuron.me/config/version.json";
        if (!getSharedPreferenceOnUI(activity).getBoolean("network", false)) {
            if (shouldShowToast)
                Toast.makeText(activity, "联网已禁用，无法检查新版本", Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog alertDialog = null;
        if (shouldShowToast) {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity);
            builder.setTitle("检查更新中...");
            builder.setCancelable(false);
            ProgressBar progressBar = new ProgressBar(activity);
            builder.setView(progressBar);
            alertDialog = builder.create();
            alertDialog.show();

        }
        AlertDialog finalAlertDialog = alertDialog;
        Thread thread = new Thread(() -> {
            try {
                isCheckingUpdate = true;
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setConnectTimeout(5000);
                conn.setRequestMethod("GET");
                if (conn.getResponseCode() == 200) {
                    InputStream inputStream = conn.getInputStream();
                    byte[] jsonBytes = convertIsToByteArray(inputStream);
                    if (Thread.currentThread().isInterrupted()) {
                        activity.runOnUiThread(() -> Toast.makeText(activity, "操作被取消", Toast.LENGTH_SHORT).show());
                        return;
                    }
                    String json = new String(jsonBytes);
                    if (json.length() > 0) {
                        activity.runOnUiThread(() -> {
                            try {
                                JSONObject jsonObject = new JSONObject(json);
                                int versionCode = jsonObject.optInt("code");
                                if (versionCode > BuildConfig.VERSION_CODE) {
                                    if (shouldShowToast) finalAlertDialog.dismiss();
                                    Snackbar snackBar = Snackbar.make(activity.findViewById(R.id.content), "发现新版本: " + jsonObject.optString("name"), Snackbar.LENGTH_LONG)
                                            .setAction("下载", v -> {
                                                Intent localIntent = new Intent("android.intent.action.VIEW");
                                                localIntent.setData(Uri.parse(PreferenceUtil.isGooglePlay ? "https://play.google.com/store/apps/details?id=cn.nexus6p.QQMusicNotify" : "https://github.com/singleNeuron/XposedMusicNotify/releases"));
                                                activity.startActivity(localIntent);
                                            });
                                    View snackBarView = snackBar.getView();
                                    snackBarView.setFitsSystemWindows(false);
                                    ViewCompat.setOnApplyWindowInsetsListener(snackBarView, null);
                                    snackBar.show();
                                    /*new MaterialAlertDialogBuilder(activity)
                                            .setTitle("发现新版本")
                                            .setMessage(jsonObject.optString("name"))
                                            .setNegativeButton("取消", null)
                                            .setPositiveButton("下载", (dialogInterface, i) -> {
                                                Intent localIntent = new Intent("android.intent.action.VIEW");
                                                localIntent.setData(Uri.parse(PreferenceUtil.isGooglePlay ? "https://play.google.com/store/apps/details?id=cn.nexus6p.QQMusicNotify" : "https://github.com/singleNeuron/XposedMusicNotify/releases"));
                                                activity.startActivity(localIntent);
                                            })
                                            .create()
                                            .show();*/
                                } else {
                                    if (shouldShowToast) finalAlertDialog.dismiss();
                                    activity.runOnUiThread(() -> Toast.makeText(activity, "检查更新成功，当前已是最新版本", Toast.LENGTH_SHORT).show());
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                        return;
                    }
                }
                if (shouldShowToast) finalAlertDialog.dismiss();
                activity.runOnUiThread(() -> {
                    try {
                        Toast.makeText(activity, "网络连接错误：" + conn.getResponseMessage(), Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                if (shouldShowToast) finalAlertDialog.dismiss();
                activity.runOnUiThread(() -> Toast.makeText(activity, "检查更新时出错：" + e.getMessage(), Toast.LENGTH_LONG).show());
            } finally {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                isCheckingUpdate = false;
            }
        });
        if (shouldShowToast) {
            alertDialog.setButton(Dialog.BUTTON_NEGATIVE, "取消", (dialog, which) -> thread.interrupt());
            alertDialog.show();
        }
        thread.start();
    }

    private static byte[] convertIsToByteArray(InputStream inputStream) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        try {
            while ((length = inputStream.read(buffer)) != -1) {
                if (Thread.currentThread().isInterrupted()) return null;
                baos.write(buffer, 0, length);
            }
            inputStream.close();
            baos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }

    public static void downloadFileFromInternet(String locate, MainActivity context) {
        SharedPreferences sharedPreferences = getSharedPreferenceOnUI(context);
        String address = sharedPreferences.getString("onlineGit", "https://xposedmusicnotify.singleneuron.me/config/");
        downloadFileFromInternet(locate, address, context, context.getExternalFilesDir(null));
    }

    public static void downloadFileFromInternet(String locate, String address, MainActivity context, File dir) {
        if (isDownloading) return;
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle("下载中..." + locate);
        builder.setCancelable(false);
        ProgressBar progressBar = new ProgressBar(context);
        builder.setView(progressBar);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        new Thread(() -> {
            try {
                isDownloading = true;
                URL url = new URL(address + (address.endsWith("/") ? "" : "/") + locate);
                //Log.d("Download",url.toString());
                String fileName = locate.substring(locate.lastIndexOf("/") + 1);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setReadTimeout(5000);
                connection.setConnectTimeout(5000);
                connection.setRequestProperty("Charset", "UTF-8");
                connection.setRequestMethod("GET");
                if (connection.getResponseCode() == 200) {
                    InputStream inputStream = connection.getInputStream();
                    FileOutputStream fileOutputStream = null;
                    if (inputStream != null) {
                        File file = new File(dir + File.separator + fileName);
                        if (file.exists()) file.delete();
                        fileOutputStream = new FileOutputStream(file);
                        byte[] buf = new byte[1024];
                        int ch;
                        while ((ch = inputStream.read(buf)) != -1) {
                            fileOutputStream.write(buf, 0, ch);
                        }
                        context.runOnUiThread(() -> {
                            alertDialog.dismiss();
                            Toast.makeText(context, "成功", Toast.LENGTH_LONG).show();
                            try {
                                context.reload();
                            } catch (Exception e) {
                                e.printStackTrace();
                                context.finish();
                            }
                        });
                    }
                    if (fileOutputStream != null) {
                        fileOutputStream.flush();
                        fileOutputStream.close();
                    }
                } else context.runOnUiThread(() -> {
                    try {
                        Toast.makeText(context, connection.getResponseMessage(), Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                context.runOnUiThread(() -> Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show());
            } finally {
                try {
                    context.runOnUiThread(alertDialog::dismiss);
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                isDownloading = false;
            }
        }).start();
    }

    public static SharedPreferences getSharedPreferenceOnUI(Context context) {
        return context.getSharedPreferences(BuildConfig.APPLICATION_ID + "_preferences", Context.MODE_PRIVATE);
    }

    public static void deviceContextPreferenceChangeListener(Preference preference, Object newValue, Context strongeContext) {
        Context deviceContext = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            deviceContext = strongeContext.createDeviceProtectedStorageContext();
        } else {
            deviceContext = strongeContext;
        }
        SharedPreferences deviceProtectedSharedPreferences = deviceContext.getSharedPreferences("deviceProtected", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = deviceProtectedSharedPreferences.edit();
        editor.putBoolean(preference.getKey(), (Boolean) newValue);
        editor.apply();
    }

}
