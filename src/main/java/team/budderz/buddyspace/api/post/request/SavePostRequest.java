package team.budderz.buddyspace.api.post.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SavePostRequest {

    @NotBlank(message = "내용은 비울 수 없습니다.")
    private String content;

    private LocalDateTime reserveAt;
    private Boolean isNotice;

}
