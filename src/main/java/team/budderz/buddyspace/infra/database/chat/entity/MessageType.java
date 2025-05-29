package team.budderz.buddyspace.infra.database.chat.entity;

// 메시지가 어떤 형식으로 구성되었는지 구분
// TEXT: 일반 문자 메시지
// IMAGE: 이미지 메시지
// FILE: 일반 파일 첨부
// SYSTEM: 시스템 메시지 (ex. 누가 채팅방에 입장함)
public enum MessageType {
    TEXT, IMAGE, VIDEO, FILE, SYSTEM
}
