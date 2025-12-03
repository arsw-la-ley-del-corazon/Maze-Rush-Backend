package org.arsw.maze_rush.auth.util;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CookieUtilTest {

    private CookieUtil createDevCookieUtil() {
        return new CookieUtil("dev", "");
    }

    private CookieUtil createProdCookieUtil() {
        return new CookieUtil("prod", "example.com");
    }

    // TEST: createAccessTokenCookie en DEV
    @Test
    void testCreateAccessTokenCookie_dev() {

        CookieUtil util = createDevCookieUtil();

        Cookie cookie = util.createAccessTokenCookie("abc123", 3600);

        assertEquals("access_token", cookie.getName());
        assertEquals("abc123", cookie.getValue());
        assertEquals("/", cookie.getPath());
        assertEquals(3600, cookie.getMaxAge());
        assertFalse(cookie.getSecure());
        assertEquals("Lax", cookie.getAttribute("SameSite"));
        assertTrue(cookie.isHttpOnly());
    }


    // TEST: createAccessTokenCookie en PROD
    @Test
    void testCreateAccessTokenCookie_prod() {

        CookieUtil util = createProdCookieUtil();
        Cookie cookie = util.createAccessTokenCookie("xyz999", 7200);

        assertEquals("access_token", cookie.getName());
        assertEquals("xyz999", cookie.getValue());
        assertTrue(cookie.getSecure());    // PROD
        assertEquals("None", cookie.getAttribute("SameSite"));
        assertEquals("example.com", cookie.getDomain());
    }

    // TEST: getCookieValue devuelve valor correcto
    @Test
    void testGetCookieValue_found() {

        CookieUtil util = createDevCookieUtil();

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setCookies(
                new Cookie("other", "123"),
                new Cookie("access_token", "tokValue")
        );

        Optional<String> result = util.getCookieValue(req, "access_token");

        assertTrue(result.isPresent());
        assertEquals("tokValue", result.get());
    }


    // TEST: getCookieValue vacío
    @Test
    void testGetCookieValue_notFound() {

        CookieUtil util = createDevCookieUtil();
        MockHttpServletRequest req = new MockHttpServletRequest();

        Optional<String> result = util.getCookieValue(req, "missing");

        assertTrue(result.isEmpty());
    }

    // TEST: deleteCookie agrega cookie que expira
    @Test
    void testDeleteCookie() {

        CookieUtil util = createDevCookieUtil();
        MockHttpServletResponse res = new MockHttpServletResponse();

        util.deleteCookie(res, CookieUtil.ACCESS_TOKEN_COOKIE);

        Cookie result = res.getCookies()[0];

        assertEquals(CookieUtil.ACCESS_TOKEN_COOKIE, result.getName());
        assertEquals(0, result.getMaxAge());  // Expirada
        assertTrue(result.isHttpOnly());
        assertEquals("/", result.getPath());
    }

    // TEST: deleteAuthCookies envía 2 cookies
    @Test
    void testDeleteAuthCookies() {

        CookieUtil util = createDevCookieUtil();
        MockHttpServletResponse res = new MockHttpServletResponse();

        util.deleteAuthCookies(res);

        Cookie c1 = res.getCookies()[0];
        Cookie c2 = res.getCookies()[1];

        assertEquals("access_token", c1.getName());
        assertEquals("refresh_token", c2.getName());
    }

    // TEST: setAuthCookies agrega ambas cookies
    @Test
    void testSetAuthCookies() {

        CookieUtil util = createDevCookieUtil();
        MockHttpServletResponse res = new MockHttpServletResponse();

        util.setAuthCookies(res, "a1", "r1", 100, 200);

        Cookie c1 = res.getCookies()[0];
        Cookie c2 = res.getCookies()[1];

        assertEquals("access_token", c1.getName());
        assertEquals("a1", c1.getValue());

        assertEquals("refresh_token", c2.getName());
        assertEquals("r1", c2.getValue());
    }
}
