package cn.nexus6p.QQMusicNotify.Fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;

import cn.nexus6p.QQMusicNotify.Utils.GeneralUtils;
import cn.nexus6p.QQMusicNotify.Utils.HookStatue;
import cn.nexus6p.QQMusicNotify.R;

import static android.content.Context.MODE_WORLD_READABLE;
import static cn.nexus6p.QQMusicNotify.Utils.GeneralUtils.jumpToAlipay;
import static cn.nexus6p.QQMusicNotify.Utils.GeneralUtils.setWorldReadable;

public class MusicNotificationFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.music_notification);
        setWorldReadable(getActivity());
        //GeneralUtils.bindPreference(this,"styleModify","always_show");

        GeneralUtils.jumpToLink(this,"magiskMoudle","feed/11103560",true);
        GeneralUtils.jumpToLink(this,"magiskMoudle2","feed/11103560",true);
        GeneralUtils.jumpToLink(this,"github2","https://github.com/Qiwu2542284182/MusicNotification",false);
        if (HookStatue.isExpModuleActive(getActivity())) {
            findPreference("miuiModify").setSummary("仅太极·阳有效，请将SystemUI加入太极");
        }

        jumpToAlipay(this,"alipay","fkx00337aktbgg6hgq64ae2?t=1542355035868");

        setWorldReadable(getActivity());

    }
}
