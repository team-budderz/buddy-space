package team.budderz.buddyspace.infra.database.chat.entity;

// 메시지에 첨부된 실제 파일의 형식을 구분
// IMAGE: .jpg, .png 등 이미지 파일
// VIDEO: .mp4, .avi 등 동영상 파일
// FILE: PDF, ZIP 등 기타 일반 파일
public enum FileType {
    IMAGE, VIDEO, FILE
}

