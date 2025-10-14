package Entity;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Posts {
    private int postId;
    private int userId;
    private String content;
    private String image_url;
    private String video_url;
    private String created_at;
    private String updated_at;



}
