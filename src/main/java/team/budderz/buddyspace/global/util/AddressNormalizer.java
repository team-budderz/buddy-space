package team.budderz.buddyspace.global.util;

/**
 * 사용자의 전체 지번 주소 문자열에서 시/구/동(또는 읍/면/리) 단위까지만 추출하여 정제된 주소를 반환
 */
public class AddressNormalizer {


    public static String normalizeAddress(String address) {
        if (address == null || address.isBlank()) return address;

        String[] parts = address.trim().split("\\s+");
        if (parts.length < 3) return address;

        // 맨 뒤에서부터 동/읍/면/리 찾기
        int lastIndex = -1;
        for (int i = parts.length - 1; i >= 0; i--) {
            if (isLastUnit(parts[i])) {
                lastIndex = i;
                break;
            }
        }

        // 못 찾았으면 원본 반환
        if (lastIndex == -1 || lastIndex < 2) return address;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= lastIndex; i++) {
            if (i == lastIndex) {
                sb.append(normalizeLastPart(parts[i]));
            } else {
                sb.append(parts[i]).append(" ");
            }
        }

        return sb.toString().trim();
    }

    private static boolean isLastUnit(String word) {
        return word.endsWith("동") || word.endsWith("읍") || word.endsWith("면") || word.endsWith("리");
    }

    public static String normalizeLastPart(String name) {
        // 공백이 있으면 앞 단어만 사용
        if (name.contains(" ")) {
            name = name.substring(0, name.indexOf(" "));
        }

        // "숫자+가" 제거
        if (name.matches(".*\\d+가$")) {
            return name.replaceFirst("\\d+가$", "");
        }

        // "숫자+동"이면 숫자 제거
        if (name.matches(".*\\d+동$")) {
            return name.replaceFirst("\\d+동$", "동");
        }

        return name;
    }
}