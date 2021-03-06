package cn.nexus6p.QQMusicNotify.SharedPreferences

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import cn.nexus6p.QQMusicNotify.BackgroundActivity
import cn.nexus6p.QQMusicNotify.BuildConfig
import cn.nexus6p.QQMusicNotify.ContentProvider
import org.json.JSONObject

class ContentProviderPreference private constructor() : BasePreference() {

    constructor(@ContentProvider.ContentProviderParams position: String, key: String?, context: Context) : this() {
        var bundle: Bundle? = null
        if (position == ContentProvider.CONTENT_PROVIDER_COMMIT) {
            getBundle(position, key, context)
            return
        }
        if (position == ContentProvider.CONTENT_PROVIDER_JSON) {
            bundle = getBundle(position, key, context)
        } else if (position == ContentProvider.CONTENT_PROVIDER_PREFERENCE || position == ContentProvider.CONTENT_PROVIDER_DEVICE_PROTECTED_PREFERENCE) {
            bundle = getBundle(position, null, context)
        }
        if (bundle == null) {
            this.jsonObject = JSONObject()
        } else {
            //Log.d("XposedMusicNotify", "getBundle: $bundle")
            originalJsonString = bundle.getString(ContentProvider.BUNDLE_KEY_JSON_STRING)
            try {
                if (!originalJsonString!!.startsWith('['))
                    this.jsonObject = JSONObject(originalJsonString!!)
            } catch (e: Exception) {
                e.printStackTrace()
                jsonObject = JSONObject()
            }
        }
    }

    lateinit var jsonObject: JSONObject
    var originalJsonString: String? = null

    override fun getBoolean(key: String?, defValue: Boolean): Boolean {
        return jsonObject.optBoolean(key, defValue)
    }

    override fun getInt(key: String?, defValue: Int): Int {
        return jsonObject.optString(key, defValue.toString()).toInt(16)
    }

    override fun getString(key: String?, defValue: String?): String? {
        return jsonObject.optString(key, defValue ?: "")
    }

    fun getJSONString(): String {
        return jsonObject.toString(4)
    }

    companion object {

        fun getBundle(@ContentProvider.ContentProviderParams position: String, key: String?, context: Context): Bundle? {
            return getBundle(position,key,context,null)
        }

        fun getBundle(@ContentProvider.ContentProviderParams position: String, key: String?, context: Context, extras: Bundle?): Bundle? {
            try {
                val contentResolver: ContentResolver = context.contentResolver
                val uri = Uri.parse("content://cn.nexus6p.QQMusicNotify.provider/")
                var result: Bundle? = null
                try {
                    result = contentResolver.call(uri, position, key, extras)
                } catch (e: RuntimeException) {
                    try {
                        val intent = Intent()
                        intent.setClassName(BuildConfig.APPLICATION_ID, BackgroundActivity::class.java.name)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    } catch (e1: Throwable) {
                        return null
                    }
                }
                if (result == null) {
                    result = contentResolver.call(uri, position, key, extras)
                }
                if (result == null) {
                    return null
                }
                return result
            } catch (ignored: Throwable) {
                return null
            }
        }
    }

}