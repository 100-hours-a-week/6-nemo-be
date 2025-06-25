package kr.ai.nemo.global.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CacheKeyUtil 테스트")
class CacheKeyUtilTest {

    @Test
    @DisplayName("[성공] 기본 키 생성")
    void key_BasicKey_Success() {
        // given
        String namespace = "user";
        Long userId = 123L;

        // when
        String result = CacheKeyUtil.key(namespace, userId);

        // then
        assertThat(result).isEqualTo("cache::user::123");
    }

    @Test
    @DisplayName("[성공] 여러 파라미터로 키 생성")
    void key_MultipleParams_Success() {
        // given
        String namespace = "group";
        Long groupId = 456L;
        String action = "members";
        Integer page = 1;

        // when
        String result = CacheKeyUtil.key(namespace, groupId, action, page);

        // then
        assertThat(result).isEqualTo("cache::group::456::members::1");
    }

    @Test
    @DisplayName("[성공] null 파라미터 포함 키 생성")
    void key_WithNullParam_Success() {
        // given
        String namespace = "session";
        String sessionId = "abc123";
        Object nullParam = null;
        String status = "active";

        // when
        String result = CacheKeyUtil.key(namespace, sessionId, nullParam, status);

        // then
        assertThat(result).isEqualTo("cache::session::abc123::null::active");
    }

    @Test
    @DisplayName("[성공] 파라미터 없이 키 생성")
    void key_NoParams_Success() {
        // given
        String namespace = "global";

        // when
        String result = CacheKeyUtil.key(namespace);

        // then
        assertThat(result).isEqualTo("cache::global");
    }

    @Test
    @DisplayName("[성공] 빈 문자열 파라미터 포함 키 생성")
    void key_WithEmptyStringParam_Success() {
        // given
        String namespace = "test";
        String emptyParam = "";
        String normalParam = "value";

        // when
        String result = CacheKeyUtil.key(namespace, emptyParam, normalParam);

        // then
        assertThat(result).isEqualTo("cache::test::::value");
    }

    @Test
    @DisplayName("[성공] 다양한 타입 파라미터로 키 생성")
    void key_VariousTypes_Success() {
        // given
        String namespace = "mixed";
        Integer intValue = 42;
        Long longValue = 999L;
        Boolean boolValue = true;
        Double doubleValue = 3.14;

        // when
        String result = CacheKeyUtil.key(namespace, intValue, longValue, boolValue, doubleValue);

        // then
        assertThat(result).isEqualTo("cache::mixed::42::999::true::3.14");
    }

    @Test
    @DisplayName("[실패] null namespace로 키 생성")
    void key_NullNamespace_ThrowException() {
        // given
        String namespace = null;
        String param = "test";

        // when & then
        assertThatThrownBy(() -> CacheKeyUtil.key(namespace, param))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("namespace is null or empty");
    }

    @Test
    @DisplayName("[실패] 빈 namespace로 키 생성")
    void key_EmptyNamespace_ThrowException() {
        // given
        String namespace = "";
        String param = "test";

        // when & then
        assertThatThrownBy(() -> CacheKeyUtil.key(namespace, param))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("namespace is null or empty");
    }

    @Test
    @DisplayName("[실패] 유틸리티 클래스 인스턴스 생성 시도")
    void constructor_ThrowException() {
        // when & then
        assertThatThrownBy(() -> {
            var constructor = CacheKeyUtil.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        })
        .getCause()
        .isInstanceOf(UnsupportedOperationException.class)
        .hasMessage("Utility class");
    }

    @Test
    @DisplayName("[성공] 긴 키 생성 - 성능 테스트")
    void key_LongKey_Success() {
        // given
        String namespace = "performance";
        Object[] manyParams = new Object[100];
        for (int i = 0; i < 100; i++) {
            manyParams[i] = "param" + i;
        }

        // when
        String result = CacheKeyUtil.key(namespace, manyParams);

        // then
        assertThat(result).startsWith("cache::performance");
        assertThat(result).contains("param0");
        assertThat(result).contains("param99");
        assertThat(result.split("::")).hasSize(102); // PREFIX + namespace + 100 params
    }

    @Test
    @DisplayName("[성공] 특수 문자 포함 파라미터로 키 생성")
    void key_SpecialCharacters_Success() {
        // given
        String namespace = "special";
        String specialParam = "test@email.com";
        String symbolParam = "user#123$";

        // when
        String result = CacheKeyUtil.key(namespace, specialParam, symbolParam);

        // then
        assertThat(result).isEqualTo("cache::special::test@email.com::user#123$");
    }
}
