package com.ntdoc.notangdoccore.service;

import lombok.RequiredArgsConstructor;
//import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WeatherService {

//    private final ChatClient chatClient;

    /**
     * 调用 OpenAI (LLM) 生成一个当前城市天气的简要描述。
     * 说明：此为演示用的 LLM 生成信息，不保证真实实时天气。
     */
    public String getWeather(String city) {
        String prompt = "你是一个天气简报助手。请用简短中文回答" +
                "当前城市: '" + city + "' 的今日天气概况。格式: 天气现象, 气温范围(摄氏度), 是否需要携带雨具。" +
                " 如果不确定真实数据，请基于典型季节常见情况给出合理估计并标注'示例'。";
/*        return chatClient.prompt(prompt)
                .call()
                .content();*/
        return prompt;
    }
}

