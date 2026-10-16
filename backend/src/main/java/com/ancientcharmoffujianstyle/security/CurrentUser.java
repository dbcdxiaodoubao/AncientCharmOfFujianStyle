package com.ancientcharmoffujianstyle.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

/** User identities come from the verified session, never from a client-supplied id. */
public final class CurrentUser {
    private CurrentUser() { }

    public static Long optionalId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof Long
                ? (Long) authentication.getPrincipal() : null;
    }

    public static Long requireId() {
        Long id = optionalId();
        if (id == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "请先登录");
        return id;
    }

    public static Long requireSelf(Long claimedId) {
        Long id = requireId();
        if (claimedId != null && !id.equals(claimedId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权操作其他用户的数据");
        }
        return id;
    }

    public static Long optionalSelf(Long claimedId) {
        return claimedId == null ? optionalId() : requireSelf(claimedId);
    }
}
