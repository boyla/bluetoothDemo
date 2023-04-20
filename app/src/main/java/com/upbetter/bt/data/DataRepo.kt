package com.upbetter.bt.data

import com.google.gson.Gson

public class DataRepo {
    fun getData(): DataBean {
        val rawStr = "{" +
                "\"msg\": \"success\"," +
                "\"res\": {" +
                "\"vertical\": [{" +
                "\"preview\": \"http://img5.adesk.com/63bb9390e7bce714522da14c?sign=123aa41fe90135ebf6161681bd2e752a&t=643cc789\"," +
                "\"thumb\": \"http://img5.adesk.com/63bb9390e7bce714522da14c?imageMogr2/thumbnail/!350x540r/gravity/Center/crop/350x540&sign=123aa41fe90135ebf6161681bd2e752a&t=643cc789\"," +
                "\"img\": \"http://img5.adesk.com/63bb9390e7bce714522da14c?imageMogr2/thumbnail/!1080x1920r/gravity/Center/crop/1080x1920&sign=123aa41fe90135ebf6161681bd2e752a&t=643cc789\"," +
                "\"views\": 0," +
                "\"cid\": [" +
                "\"4fb47a195ba1c60ca5000222\"" +
                "]," +
                "\"rule\": \"&imageMogr2/thumbnail/!\$<Width>x\$<Height>r/gravity/Center/crop/\$<Width>x\$<Height>\"," +
                "\"ncos\": 0," +
                "\"rank\": 22581," +
                "\"source_type\": \"vertical\"," +
                "\"tag\": [" +
                "\"兔子\"," +
                "\"新年\"," +
                "\"2023\"," +
                "\"红色\"," +
                "\"喜庆\"," +
                "\"兔年\"" +
                "]," +
                "\"url\": []," +
                "\"wp\": \"http://img5.adesk.com/63bb9390e7bce714522da14c?sign=123aa41fe90135ebf6161681bd2e752a&t=643cc789\"," +
                "\"xr\": false," +
                "\"cr\": false," +
                "\"favs\": 56," +
                "\"atime\": 1674309916," +
                "\"id\": \"63bb9390e7bce714522da14c\"," +
                "\"store\": \"qiniu\"," +
                "\"desc\": \"\"" +
                "}," +
                "{" +
                "\"preview\": \"http://img5.adesk.com/629c19b87e978a1ff135c3fb?sign=c6cc293bb63ca9a9423b695e46d0efed&t=643cc789\"," +
                "\"thumb\": \"http://img5.adesk.com/629c19b87e978a1ff135c3fb?imageMogr2/thumbnail/!350x540r/gravity/Center/crop/350x540&sign=c6cc293bb63ca9a9423b695e46d0efed&t=643cc789\"," +
                "\"img\": \"http://img5.adesk.com/629c19b87e978a1ff135c3fb?imageMogr2/thumbnail/!1080x1920r/gravity/Center/crop/1080x1920&sign=c6cc293bb63ca9a9423b695e46d0efed&t=643cc789\"," +
                "\"views\": 0," +
                "\"cid\": [" +
                "\"4e4d610cdf714d2966000007\"" +
                "]," +
                "\"rule\": \"&imageMogr2/thumbnail/!\$<Width>x\$<Height>r/gravity/Center/crop/\$<Width>x\$<Height>\"," +
                "\"ncos\": 4," +
                "\"rank\": 21618," +
                "\"source_type\": \"vertical\"," +
                "\"tag\": [" +
                "\"游戏\"," +
                "\"动漫\"," +
                "\"萌妹\"," +
                "\"\"" +
                "]," +
                "\"url\": []," +
                "\"wp\": \"http://img5.adesk.com/629c19b87e978a1ff135c3fb?sign=c6cc293bb63ca9a9423b695e46d0efed&t=643cc789\"," +
                "\"xr\": false," +
                "\"cr\": false," +
                "\"favs\": 59," +
                "\"atime\": 1673964305," +
                "\"id\": \"629c19b87e978a1ff135c3fb\"," +
                "\"store\": \"qiniu\"," +
                "\"desc\": \"\"" +
                "}]" +
                "}," +
                "\"code\": 0" +
                "}"
        val gson = Gson()
        var ret = gson.fromJson(rawStr, DataBean::class.java)
        return ret
    }
}