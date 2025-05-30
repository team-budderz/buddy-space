package team.budderz.buddyspace.api.post.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePostRequest {

    @NotBlank(message = "내용은 비울 수 없습니다.")
    private String content;

    private Boolean isNotice;

}
