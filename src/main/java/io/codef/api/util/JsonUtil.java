package io.codef.api.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;

public class JsonUtil {

    public static String toPrettyJson(Object object) {
        return JSON.toJSONString(
                object,
                JSONWriter.Feature.PrettyFormat,
                JSONWriter.Feature.WriteMapNullValue,
                JSONWriter.Feature.WriteNullListAsEmpty
        );
    }
}