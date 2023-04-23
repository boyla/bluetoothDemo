package com.upbetter.bt.util

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast

@SuppressLint("StaticFieldLeak")
object ToastUtil {
    private var toast: Toast? = null
    var ctx:Context? = null
    fun showToast(mContext: Context, text: CharSequence?) {
        //这么写防止其在其他页面的时候使用这个方法时，还是显示上一个的提示。
        if (toast == null) {
            toast = Toast.makeText(mContext, text, Toast.LENGTH_LONG)
        } else {
            toast!!.setText(text)
            toast!!.duration = Toast.LENGTH_SHORT
        }
        toast!!.show()
    }

}