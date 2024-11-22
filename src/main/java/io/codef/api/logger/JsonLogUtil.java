package io.codef.api.logger;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonLogUtil {

    private static final Logger log = LoggerFactory.getLogger(JsonLogUtil.class);

    public static String toPrettyJson(Object obj) {
        return JSON.toJSONString(obj,
            JSONWriter.Feature.PrettyFormat,
            JSONWriter.Feature.WriteMapNullValue,
            JSONWriter.Feature.WriteNullListAsEmpty
        );
    }
}