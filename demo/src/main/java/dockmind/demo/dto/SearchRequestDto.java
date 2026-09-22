package dockmind.demo.dto;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchRequestDto {
    private String query;

    private UUID documentId;

    private  Integer topK;

    private  Double similaritySearch;
}
