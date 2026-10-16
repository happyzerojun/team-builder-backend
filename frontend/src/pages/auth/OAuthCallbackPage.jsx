import { useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import api from "../../services/api";

/**
 * 백엔드 OAuth2 로그인(/oauth2/authorization/{provider}) 성공 후 돌아오는 화면.
 * OAuth2AuthenticationSuccessHandler가 ?accessToken=...&email=... 로 리다이렉트한다.
 *
 * 이전에는 프론트가 Google/Kakao 토큰을 직접 교환해(클라이언트 시크릿이 번들에 포함됨) 우리 서버의 JWT 없이
 * 로그인된 것처럼 보이기만 했다. user_id에는 Google/Kakao의 ID가 들어가 서버 API가 모두 실패했다 (TS-15)
 */
const OAuthCallbackPage = ({ onLoginSuccess }) => {
    const navigate = useNavigate();
    // StrictMode(개발 모드)의 effect 이중 실행·부모 리렌더로 두 번 처리되지 않게 한 번만 실행
    const handled = useRef(false);

    useEffect(() => {
        if (handled.current) return;
        handled.current = true;

        const params = new URLSearchParams(window.location.search);
        const accessToken = params.get("accessToken");

        // 토큰이 주소창·히스토리에 남지 않게 바로 지운다
        window.history.replaceState({}, document.title, "/oauth/callback");

        if (!accessToken) {
            alert("소셜 로그인에 실패했습니다.");
            navigate("/login", { replace: true });
            return;
        }

        const finishLogin = async () => {
            localStorage.setItem("token", accessToken);
            try {
                const { data } = await api.get("/api/auth/me");
                localStorage.setItem("user", JSON.stringify({
                    user_id: data.userId,
                    email: data.email,
                    name: data.name
                }));
                localStorage.setItem("isLoggedIn", "true");
                if (onLoginSuccess) onLoginSuccess();
                navigate("/", { replace: true });
            } catch (error) {
                console.error("소셜 로그인 사용자 조회 실패:", error);
                localStorage.removeItem("token");
                alert("소셜 로그인에 실패했습니다.");
                navigate("/login", { replace: true });
            }
        };

        finishLogin();
    }, [navigate, onLoginSuccess]);

    return <div style={{ textAlign: "center", marginTop: "100px" }}>로그인 처리 중...</div>;
};

export default OAuthCallbackPage;
