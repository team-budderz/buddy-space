package team.budderz.buddyspace.infra.database.attachment.entity;

public enum AttachmentType {
    IMAGE,
    VIDEO,
    FILE;

    public static AttachmentType fromContentType(String contentType) {
        if (contentType == null) return FILE;
        if (contentType.startsWith("image/")) return IMAGE;
        if (contentType.startsWith("video/")) return VIDEO;
        return FILE;
    }
}
