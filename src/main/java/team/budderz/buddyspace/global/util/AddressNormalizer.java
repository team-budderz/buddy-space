package team.budderz.buddyspace.global.util;

/**
 * 사용자의 전체 지번 주소 문자열에서 시/구/동(또는 읍/면/리) 단위까지만 추출하여 정제된 주소를 반환
 */
public class AddressNormalizer {

    public static String normalizeAddress(String address) {
        if (address == null || address.isBlank()) return address;

        String[] parts = address.trim().split("\\s+");
        if (parts.length < 3) return address;

        String part1 = parts[0]; // 시 or 도
        String part2 = parts[1]; // 구 or 시/군
        String part3 = parts[2]; // 동/읍/면/리 등

        String normalized = part1 + " " + part2 + " " + normalizeLastPart(part3);

        return normalized.trim();
    }

    private static String normalizeLastPart(String name) {
        // 1. 숫자+가 로 끝나는 경우 → 제거
        if (name.matches(".*\\d+가$")) {
            return name.replaceFirst("\\d+가$", "");
        }

        // 2. 숫자+동으로 끝나는 경우 → 숫자 제거, '동' 유지
        if (name.matches(".*\\d+동$")) {
            return name.replaceFirst("(\\d+)동$", "동");
        }

        // 3. 그 외는 그대로
        return name;
    }
}