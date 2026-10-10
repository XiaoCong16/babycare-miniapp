package com.example.babycare.infrastructure.wechat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class WeChatClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${wechat.appid}")
    private String appid;

    @Value("${wechat.secret}")
    private String secret;

    @Value("${wechat.template-id}")
    private String templateId;

    public String getOpenidByCode(String code) {
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid={appid}&secret={secret}&js_code={code}&grant_type=authorization_code";
        String response = restTemplate.getForObject(url, String.class, appid, secret, code);
        try {
            JsonNode json = objectMapper.readTree(response);
            String openid = json.path("openid").asText();
            if (openid == null || openid.isBlank()) {
                throw new IllegalStateException("获取 openid 失败: " + response);
            }
            return openid;
        } catch (Exception e) {
            throw new IllegalStateException("解析微信登录返回数据失败", e);
        }
    }

    public void sendSubscribeMessage(String openid, String title, String content) {
        String accessToken = getAccessToken();
        String url = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + accessToken;

        Map<String, Object> payload = new HashMap<>();
        payload.put("touser", openid);
        payload.put("template_id", templateId);
        payload.put("page", "pages/index/index");

        Map<String, Object> data = new HashMap<>();
        data.put("thing1", Map.of("value", title));
        data.put("thing2", Map.of("value", content));
        payload.put("data", data);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        String response = restTemplate.postForObject(url, entity, String.class);
        System.out.println("微信提醒返回: " + response);
    }

    private String getAccessToken() {
        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid={appid}&secret={secret}";
        String response = restTemplate.getForObject(url, String.class, appid, secret);
        try {
            JsonNode json = objectMapper.readTree(response);
            String token = json.path("access_token").asText();
            if (token == null || token.isBlank()) {
                throw new IllegalStateException("获取访问令牌失败: " + response);
            }
            return token;
        } catch (Exception e) {
            throw new IllegalStateException("解析微信 access token 失败", e);
        }
    }
}
