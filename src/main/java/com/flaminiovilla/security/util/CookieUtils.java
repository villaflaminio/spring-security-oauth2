package com.flaminiovilla.security.util;

import org.springframework.util.SerializationUtils;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Base64;
import java.util.Optional;

/**
 * Utils class to handle cookies.
 */
public class CookieUtils {

    /**
     * Get a cookie from the request.
     * @param request The request.
     * @param name The name of the cookie.
     * @return The cookie, if present.
     */
    public static Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return Optional.of(cookie);
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Add a cookie to the response.
     * @param response The response.
     * @param name The name of the cookie.
     * @param value The value of the cookie.
     * @param maxAge The max age of the cookie.
     */
    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    /**
     * Delete a cookie from the response.
     * @param request The request.
     * @param response The response.
     * @param name The name of the cookie to be deleted.
     */
    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie: cookies) {
                if (cookie.getName().equals(name)) {
                    cookie.setValue("");
                    cookie.setPath("/");
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                }
            }
        }
    }

    /**
     * Serialize an object to a Base64 string.
     * @param object The object to be serialized.
     * @return The Base64 string.
     */
    public static String serialize(Object object) {
        return Base64.getUrlEncoder()
                .encodeToString(SerializationUtils.serialize(object));
    }

    /**
     * Deserialize an object from a Base64 string.
     * @param cookie The Base64 string.
     * @param cls The class of the object to be deserialized.
     * @return The deserialized object.
     */
    public static <T> T deserialize(Cookie cookie, Class<T> cls) {
        return cls.cast(SerializationUtils.deserialize(
                        Base64.getUrlDecoder().decode(cookie.getValue())));
    }


}
