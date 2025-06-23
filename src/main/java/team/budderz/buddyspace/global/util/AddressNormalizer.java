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

        String normalized = part1 + " " + part2 + " ";

        if (part3.endsWith("동") || part3.endsWith("가")) {
            normalized += normalizeDong(part3);
        } else if (part3.endsWith("읍") || part3.endsWith("면") || part3.endsWith("리")) {
            // 읍/면/리는 그대로 사용
            normalized += part3;
        } else {
            // 끝이 명확하지 않으면 그대로
            normalized += part3;
        }

        return normalized.trim();
    }

    private static String normalizeDong(String dong) {
        dong = dong.replaceAll("(\\d+가|\\d+동|가|동)$", "");
        dong = dong.replaceAll("(\\d+)$", "");
        if (!dong.endsWith("동")) {
            dong += "동";
        }
        return dong;
    }
}
