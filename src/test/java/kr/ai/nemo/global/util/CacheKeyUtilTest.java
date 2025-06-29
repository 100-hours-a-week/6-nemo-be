package kr.ai.nemo.global.util;

import static org.assertj.core.api.Assertions.assertThat;

import kr.ai.nemo.global.redis.CacheKeyUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CacheKeyUtil 테스트")
class CacheKeyUtilTest {

    @Test
    @DisplayName("[성공] 기본 키 생성")
    void key_BasicKey_Success() {
        // given
        String prefix = "user";
        
        // when
        String result = CacheKeyUtil.key(prefix);
        
        // then
        assertThat(result).isEqualTo("cache::user");
    }

    @Test
    @DisplayName("[성공] 키-값 쌍으로 키 생성")
    void key_WithKeyValuePairs_Success() {
        // given
        String prefix = "group-list";
        
        // when
        String result = CacheKeyUtil.key(prefix, "category", "스포츠", "page", 0, "size", 10);
        
        // then
        assertThat(result).isEqualTo("cache::group-list::category::스포츠::page::0::size::10");
    }

    @Test
    @DisplayName("[성공] null 값 처리")
    void key_WithNullValue_Success() {
        // given
        String prefix = "group-list";
        
        // when
        String result = CacheKeyUtil.key(prefix, "category", null, "page", 0);
        
        // then
        assertThat(result).isEqualTo("cache::group-list::category::null::page::0");
    }

    @Test
    @DisplayName("[성공] 홀수 개의 파라미터 처리")
    void key_WithOddParameters_Success() {
        // given
        String prefix = "test";
        
        // when
        String result = CacheKeyUtil.key(prefix, "key1", "value1", "key2");
        
        // then
        assertThat(result).isEqualTo("cache::test::key1::value1::key2");
    }

    @Test
    @DisplayName("[성공] 빈 파라미터 처리")
    void key_WithEmptyParameters_Success() {
        // given
        String prefix = "empty";
        
        // when
        String result = CacheKeyUtil.key(prefix);
        
        // then
        assertThat(result).isEqualTo("cache::empty");
    }

    @Test
    @DisplayName("[성공] 복잡한 객체 값 처리")
    void key_WithComplexObject_Success() {
        // given
        String prefix = "complex";
        Object complexObject = new TestObject("test", 123);
        
        // when
        String result = CacheKeyUtil.key(prefix, "object", complexObject);
        
        // then
        assertThat(result).contains("cache::complex::object::");
        assertThat(result).contains("TestObject");
    }

    // 테스트용 객체
    private static class TestObject {
        private String name;
        private int value;

        public TestObject(String name, int value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String toString() {
            return "TestObject{name='" + name + "', value=" + value + "}";
        }
    }
}
