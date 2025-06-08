package team.budderz.buddyspace.domain.group.util;

import java.security.SecureRandom;

public class InviteCodeGenerator {

    private static final String CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int CODE_LENGTH = 16;
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generate() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            int idx = RANDOM.nextInt(CHAR_POOL.length());
            sb.append(CHAR_POOL.charAt(idx));
        }
        return sb.toString();
    }
}
