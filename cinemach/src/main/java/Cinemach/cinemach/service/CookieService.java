package Cinemach.cinemach.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class CookieService {
    public String getCookie(HttpServletRequest request, String nome) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(nome)) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
