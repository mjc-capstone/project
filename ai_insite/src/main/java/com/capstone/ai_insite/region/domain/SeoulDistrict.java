package com.capstone.ai_insite.region.domain;

import java.util.Arrays;

public enum SeoulDistrict {
    JONGNO("11110", "종로구"),
    JUNG("11140", "중구"),
    YONGSAN("11170", "용산구"),
    SEONGDONG("11200", "성동구"),
    GWANGJIN("11215", "광진구"),
    DONGDAEMUN("11230", "동대문구"),
    JUNGNANG("11260", "중랑구"),
    SEONGBUK("11290", "성북구"),
    GANGBUK("11305", "강북구"),
    DOBONG("11320", "도봉구"),
    NOWON("11350", "노원구"),
    EUNPYEONG("11380", "은평구"),
    SEODAEMUN("11410", "서대문구"),
    MAPO("11440", "마포구"),
    YANGCHEON("11470", "양천구"),
    GANGSEO("11500", "강서구"),
    GURO("11530", "구로구"),
    GEUMCHEON("11545", "금천구"),
    YEONGDEUNGPO("11560", "영등포구"),
    DONGJAK("11590", "동작구"),
    GWANAK("11620", "관악구"),
    SEOCHO("11650", "서초구"),
    GANGNAM("11680", "강남구"),
    SONGPA("11710", "송파구"),
    GANGDONG("11740", "강동구");

    private final String code;
    private final String districtName;

    SeoulDistrict(String code, String districtName) {
        this.code = code;
        this.districtName = districtName;
    }

    public String code() {
        return code;
    }

    public String districtName() {
        return districtName;
    }

    public static SeoulDistrict fromAdministrativeDongCode(String administrativeDongCode) {
        if (administrativeDongCode == null || administrativeDongCode.length() < 5) {
            throw new IllegalArgumentException("유효하지 않은 서울 행정동 코드입니다.");
        }
        String districtCode = administrativeDongCode.substring(0, 5);
        return Arrays.stream(values())
            .filter(district -> district.code.equals(districtCode))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                "서울 자치구를 찾을 수 없습니다: " + districtCode
            ));
    }
}
