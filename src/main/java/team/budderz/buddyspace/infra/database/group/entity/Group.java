package team.budderz.buddyspace.infra.database.group.entity;

import io.micrometer.common.util.StringUtils;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team.budderz.buddyspace.api.group.request.SaveGroupRequest;
import team.budderz.buddyspace.api.group.request.UpdateGroupRequest;
import team.budderz.buddyspace.global.entity.BaseEntity;
import team.budderz.buddyspace.infra.database.attachment.entity.Attachment;
import team.budderz.buddyspace.infra.database.user.entity.User;

@Getter
@Entity
@Table(name = "groups")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Group extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cover_attachment_id")
    private Attachment coverAttachment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupAccess access;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupInterest interest;

    @Column(name = "invite_link", unique = true)
    private String inviteCode;

    private String address;

    @Column(name = "is_neighborhood_auth_required")
    private boolean isNeighborhoodAuthRequired;

    @ManyToOne
    @JoinColumn(name = "leader_id", nullable = false)
    private User leader;

    /**
     * 모임 생성용 생성자
     */
    @Builder
    public Group(SaveGroupRequest request, User leader) {
        this.name = request.name();
        this.access = request.access();
        this.type = request.type();
        this.interest = request.interest();
        this.leader = leader;
    }

    public void updateInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    public void updateLeader(User leader) {
        this.leader = leader;
    }

    public void updateAddress(String address) {
        this.address = address;
    }

    public void updateNeighborhoodAuthRequired(boolean isNeighborhoodAuthRequired) {
        this.isNeighborhoodAuthRequired = isNeighborhoodAuthRequired;
    }

    public void updateCoverAttachment(Attachment coverAttachment) {
        this.coverAttachment = coverAttachment;
    }

    public void updateGroupInfo(UpdateGroupRequest request) {
        if (!StringUtils.isBlank(request.name())) this.name = request.name();
        if (!StringUtils.isBlank(request.description())) this.description = request.description();
        if (request.access() != null) this.access = request.access();
        if (request.type() != null) this.type = request.type();
        if (request.interest() != null) this.interest = request.interest();
    }
}
