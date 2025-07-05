import kr.ai.nemo.domain.auth.security.JwtProvider;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

public class TokenGenerator {
    public static void main(String[] args) {
        JwtProvider jwtProvider = new JwtProvider();
        
        // 실제 시크릿 키 설정
        ReflectionTestUtils.setField(jwtProvider, "secretKeyString", "djk@dklsa2$$:SADLGSd!#vfdskdfsfsa!058@dlsj@dasd:#45fjsdjkS");
        ReflectionTestUtils.setField(jwtProvider, "accessTokenValidity", 3600000L); // 1시간
        ReflectionTestUtils.setField(jwtProvider, "refreshTokenValidity", 604800000L); // 1주일
        jwtProvider.init();
        
        System.out.println("=== 23개의 유효한 Refresh Token 생성 ===");
        
        // 활성 토큰들 (15개)
        Long[] activeUserIds = {1L, 2L, 3L, 5L, 6L, 8L, 9L, 11L, 12L, 13L, 15L, 16L, 17L, 18L, 20L};
        String[] providers = {"KAKAO", "GOOGLE", "NAVER", "GOOGLE", "NAVER", "GOOGLE", "NAVER", "GOOGLE", "NAVER", "KAKAO", "NAVER", "KAKAO", "GOOGLE", "NAVER", "GOOGLE"};
        
        System.out.println("-- 활성 토큰들 (15개)");
        for (int i = 0; i < activeUserIds.length; i++) {
            String refreshToken = jwtProvider.createRefreshToken(activeUserIds[i]);
            System.out.println("User " + activeUserIds[i] + " (" + providers[i] + "): " + refreshToken);
        }
        
        // 만료된 토큰들 (8개) - 실제로는 과거 시점에 생성된 것처럼 시뮬레이션
        Long[] expiredUserIds = {1L, 2L, 4L, 7L, 10L, 14L, 19L, 4L};
        String[] expiredProviders = {"KAKAO", "GOOGLE", "KAKAO", "KAKAO", "KAKAO", "GOOGLE", "KAKAO", "NAVER"};
        
        System.out.println("\n-- 만료된 토큰들 (8개)");
        for (int i = 0; i < expiredUserIds.length; i++) {
            String refreshToken = jwtProvider.createRefreshToken(expiredUserIds[i]);
            System.out.println("User " + expiredUserIds[i] + " (" + expiredProviders[i] + "): " + refreshToken);
        }
        
        System.out.println("\n총 23개의 토큰 생성 완료!");
    }
}
