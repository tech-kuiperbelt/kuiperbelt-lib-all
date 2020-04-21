package tech.kuiperbelt.lib.common.web;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class HttpErrorResponse {
    public static final String END_USER_EXCEPTION = "END_USER_EXCEPTION";

    private boolean temporary;
    private String code;
    private String message;
    private Map<String, String> detail;
}
