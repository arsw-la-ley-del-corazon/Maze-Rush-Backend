package org.arsw.maze_rush.auth.dto;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class AuthResponseDTOTest {


    @Test
    void testAuthResponseDTO_BuilderAndDefaults() {
        AuthResponseDTO defaultDto = AuthResponseDTO.builder().build();

        assertEquals("Bearer", defaultDto.getTokenType());

        assertNull(defaultDto.getAccessToken());

        Instant now = Instant.now();
        AuthResponseDTO.UserInfo userInfo = new AuthResponseDTO.UserInfo();
        
        AuthResponseDTO customDto = AuthResponseDTO.builder()
                .accessToken("token123")
                .refreshToken("refresh123")
                .tokenType("CustomType")
                .expiresIn(3600L)
                .expiresAt(now)
                .user(userInfo)
                .build();

        assertEquals("token123", customDto.getAccessToken());
        assertEquals("refresh123", customDto.getRefreshToken());
        assertEquals("CustomType", customDto.getTokenType());
        assertEquals(3600L, customDto.getExpiresIn());
        assertEquals(now, customDto.getExpiresAt());
        assertEquals(userInfo, customDto.getUser());
    }

    @Test
    void testAuthResponseDTO_NoArgsAndSetters() {
        AuthResponseDTO dto = new AuthResponseDTO();
        dto.setAccessToken("acc");
        dto.setRefreshToken("ref");
        dto.setTokenType("Basic");
        dto.setExpiresIn(100L);
        
        assertEquals("acc", dto.getAccessToken());
        assertEquals("Basic", dto.getTokenType());
    }

    @Test
    void testAuthResponseDTO_AllArgsConstructor() {
        Instant now = Instant.now();
        AuthResponseDTO.UserInfo u = new AuthResponseDTO.UserInfo();
        
        AuthResponseDTO dto = new AuthResponseDTO(
            "acc", "ref", "Bearer", 100L, now, u
        );
        
        assertEquals("acc", dto.getAccessToken());
        assertEquals(u, dto.getUser());
    }

    @Test
    void testAuthResponseDTO_EqualsHashCodeToString() {
        AuthResponseDTO dto1 = AuthResponseDTO.builder().accessToken("A").build();
        AuthResponseDTO dto2 = AuthResponseDTO.builder().accessToken("A").build();
        AuthResponseDTO dto3 = AuthResponseDTO.builder().accessToken("B").build();

        assertEquals(dto1, dto2);
        assertEquals(dto1, dto1);
        assertNotEquals(dto1, dto3);
        assertNotEquals(null, dto1);
        assertNotEquals(dto1, new Object());

        assertEquals(dto1.hashCode(), dto2.hashCode());

        assertNotNull(dto1.toString());
    }


    @Test
    void testUserInfo_BuilderAndDefaults() {
        AuthResponseDTO.UserInfo defaultUser = AuthResponseDTO.UserInfo.builder().build();

        assertEquals(0, defaultUser.getScore()); 
        assertEquals(1, defaultUser.getLevel());
        assertNull(defaultUser.getUsername());

        AuthResponseDTO.UserInfo customUser = AuthResponseDTO.UserInfo.builder()
                .id("1")
                .username("juan")
                .email("juan@mail.com")
                .score(100) 
                .level(10)  
                .build();

        assertEquals("juan", customUser.getUsername());
        assertEquals(100, customUser.getScore());
        assertEquals(10, customUser.getLevel());
    }

    @Test
    void testUserInfo_NoArgsAndSetters() {
        AuthResponseDTO.UserInfo user = new AuthResponseDTO.UserInfo();
        user.setId("u1");
        user.setUsername("name");
        
        assertEquals("u1", user.getId());
        assertEquals("name", user.getUsername());
    }

    @Test
    void testUserInfo_AllArgsConstructor() {
        AuthResponseDTO.UserInfo user = new AuthResponseDTO.UserInfo(
            "id1", "user", "email", 50, 2
        );
        assertEquals("id1", user.getId());
        assertEquals(50, user.getScore());
    }

    @Test
    void testUserInfo_EqualsHashCodeToString() {
        AuthResponseDTO.UserInfo u1 = AuthResponseDTO.UserInfo.builder().id("1").build();
        AuthResponseDTO.UserInfo u2 = AuthResponseDTO.UserInfo.builder().id("1").build();
        AuthResponseDTO.UserInfo u3 = AuthResponseDTO.UserInfo.builder().id("2").build();

        assertEquals(u1, u2);
        assertEquals(u1.hashCode(), u2.hashCode());
        assertNotEquals(u1, u3);
        assertNotNull(u1.toString());
    }
}