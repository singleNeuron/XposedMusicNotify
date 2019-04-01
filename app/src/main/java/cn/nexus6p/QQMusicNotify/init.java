package cn.nexus6p.QQMusicNotify;

import android.app.Application;
import android.content.Context;
import android.content.res.XModuleResources;
import android.util.Log;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class init implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals("cn.nexus6p.QQMusicNotify")) {
            findAndHookMethod(HookStatue.class, "isEnabled", XC_MethodReplacement.returnConstant(true));
            return;
        }
        if (lpparam.packageName.equals("com.tencent.qqmusiclocalplayer")) {
            new QingtingHook(lpparam.classLoader).init();
        }
        if (lpparam.packageName.equals("com.tencent.karaoke")) {
            XposedHelpers.findAndHookMethod(Application.class.getName(), lpparam.classLoader, "attach", Context.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    final ClassLoader classLoader = ((Context) param.args[0]).getClassLoader();
                    if (classLoader == null) {
                        Log.e("KaraokeHook","Can't get ClassLoader!");
                        return;
                    }
                    new KaraokeHook(classLoader).init();
                }
            });
        }
    }
}
