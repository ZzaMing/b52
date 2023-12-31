package org.zerock.b52.security;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.zerock.b52.dto.MemberDTO;
import org.zerock.b52.dto.MemberReadDTO;
import org.zerock.b52.mappers.MemberMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService{
    
    private final MemberMapper memberMapper;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        
        log.info("---------loadUser-----------");

        log.info(userRequest);

        log.info("============================");

        ClientRegistration clientRegistration = userRequest.getClientRegistration();
        String clientName = clientRegistration.getClientName();

        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> paramMap = oAuth2User.getAttributes();

        String email = null;

        // 소셜 분기
        switch (clientName){
            case "kakao":
                email = getKakaoEmail(paramMap);
                break;
        }

        log.info("===============================");
        log.info(email);
        log.info("===============================");

        // DB에 해당 이메일 사용자가 있다면
        MemberReadDTO readDTO = memberMapper.selectOne(email);
    
        if (readDTO != null){
            MemberDTO memberDTO = new MemberDTO(
                email,
                readDTO.getMpw(),
                readDTO.getMname(),
                readDTO.getRolenames()
            );     
            return memberDTO;   
        }else{
            // 아니라면
            // 소셜사용자랑 일반사용자랑 화면에서 동일한 방법으로 처리하기 위해 같이 MemberDTO를 사용한다.
            // 인증이 이미 끝난곳에서 패드워드는 의미가 없기때문에 신경쓰지 않아도 된다.
            MemberDTO memberDTO = new MemberDTO(email, "", "카카오사용자", List.of("USER"));
            return memberDTO;
        }
  
    }

    private String getKakaoEmail(Map<String, Object> paramMap){

        log.info("KAKAO-----------------------------------------");

        Object value = paramMap.get("kakao_account");

        log.info(value);

        LinkedHashMap accountMap = (LinkedHashMap) value;

        String email = (String)accountMap.get("email");

        log.info("email..." + email);

        return email;
    }
    
}
