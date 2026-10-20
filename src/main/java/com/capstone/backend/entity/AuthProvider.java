package com.capstone.backend.entity;

public enum AuthProvider {
    LOCAL,
    GOOGLE,
    // Kakao·Naver 로그인은 제거했지만(TS-21) 값은 남겨요.
    // provider 컬럼은 문자열(EnumType.STRING)이라 기존 행에 KAKAO·NAVER가 있으면, 값을 지우는 순간 그 사용자를 읽을 때 예외가 나요.
    @Deprecated
    KAKAO,
    @Deprecated
    NAVER
}
