package com.game.dataservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.game.dataservice.entity.User;
import com.game.dataservice.repository.UserRepository;
import com.game.dataservice.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WechatAuthService {

    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;

    @Value("${wechat.miniapp.appid}")
    private String appId;

    @Value("${wechat.miniapp.secret}")
    private String appSecret;

    private static final String WECHAT_LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session?appid={appid}&secret={secret}&js_code={code}&grant_type=authorization_code";

    /**
     * 微信小程序一键登录
     * @param code wx.login() 获取的临时登录凭证
     * @return 包含 token 和用户信息的 Map
     */
    public Map<String, Object> login(String code) {
        // 1. 调用微信接口获取 openid
        String url = WECHAT_LOGIN_URL.replace("{appid}", appId)
                .replace("{secret}", appSecret)
                .replace("{code}", code);

        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode jsonNode = objectMapper.readTree(response);
            
            if (jsonNode.has("errcode") && jsonNode.get("errcode").asInt() != 0) {
                log.error("Wechat login failed: {}", response);
                throw new RuntimeException("Wechat login failed: " + jsonNode.get("errmsg").asText());
            }

            String openid = jsonNode.get("openid").asText();
            String unionid = jsonNode.has("unionid") ? jsonNode.get("unionid").asText() : null;

            // 2. 查询用户是否已存在，不存在则自动注册
            Optional<User> userOpt = userRepository.findByOpenid(openid);
            User user;
            if (userOpt.isPresent()) {
                user = userOpt.get();
                user.setLastLoginAt(LocalDateTime.now());
                if (unionid != null && user.getUnionid() == null) {
                    user.setUnionid(unionid);
                }
                userRepository.save(user);
            } else {
                user = User.builder()
                        .openid(openid)
                        .unionid(unionid)
                        .build();
                userRepository.save(user);
            }

            // 3. 生成 JWT Token
            String token = jwtUtils.generateToken(user.getId().toString(), "USER");

            return Map.of(
                    "token", token,
                    "userId", user.getId(),
                    "openid", user.getOpenid()
            );
        } catch (Exception e) {
            log.error("Error during wechat login", e);
            throw new RuntimeException("Error during wechat login", e);
        }
    }
    
    /**
     * 更新用户信息
     */
    public User updateUserInfo(Long userId, String nickname, String avatarUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (nickname != null) user.setNickname(nickname);
        if (avatarUrl != null) user.setAvatarUrl(avatarUrl);
        
        return userRepository.save(user);
    }
}
