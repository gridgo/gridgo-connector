package io.gridgo.connector.httpcommon;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum HttpStatus {

    CONTINUE_100(100, "continue"), SWITCHING_PROTOCOLS_101(101, "switching protocols"), PROCESSING_102(102, "processing"), OK_200(200, "OK"),
    CREATED_201(201, "created"), ACCEPTED_202(202, "accepted"), NON_AUTHORITATIVE_INFORMATION_203(203, "non authoritative information"),
    NO_CONTENT_204(204, "no content"), RESET_CONTENT_205(205, "reset content"), PARTIAL_CONTENT_206(206, "partial content"),
    MULTI_STATUS_207(207, "multi status"), MULTIPLE_CHOICES_300(300, "multiple choices"), MOVED_PERMANENTLY_301(301, "moved permanently"),
    MOVED_TEMPORARILY_302(302, "moved_temporarily"), FOUND_302(302, "found"), SEE_OTHER_303(303, "see other"), NOT_MODIFIED_304(304, "not modified"),
    USE_PROXY_305(305, "use proxy"), TEMPORARY_REDIRECT_307(307, "temporary redirect"), PERMANENT_REDIRECT_308(308, "permanent redirect"),
    BAD_REQUEST_400(400, "bad request"), UNAUTHORIZED_401(401, "unauthorized"), PAYMENT_REQUIRED_402(402, "payment required"), FORBIDDEN_403(403, "forbidden"),
    NOT_FOUND_404(404, "not_found"), METHOD_NOT_ALLOWED_405(405, "method not allowed"), NOT_ACCEPTABLE_406(406, "not acceptable"),
    PROXY_AUTHENTICATION_REQUIRED_407(407, "proxy authentication required"), REQUEST_TIMEOUT_408(408, "request timeout"), CONFLICT_409(409, "conflict"),
    GONE_410(410, "gone"), LENGTH_REQUIRED_411(411, "length required"), PRECONDITION_FAILED_412(412, "precondition failed"),

    /**
     * Use PAYLOAD_TOO_LARGE_413 instead
     */
    @Deprecated
    REQUEST_ENTITY_TOO_LARGE_413(413, "request entity too large"),

    PAYLOAD_TOO_LARGE_413(413, "payload too large"),

    /**
     * use URI_TOO_LONG_414 instead
     */
    @Deprecated
    REQUEST_URI_TOO_LONG_414(414, "request uri too long"),

    URI_TOO_LONG_414(414, "uri too long"), UNSUPPORTED_MEDIA_TYPE_415(415, "unsupported media type"),

    /**
     * use RANGE_NOT_SATISFIABLE_416 instead
     */
    @Deprecated
    REQUESTED_RANGE_NOT_SATISFIABLE_416(416, "requested range not satisfiable"),

    RANGE_NOT_SATISFIABLE_416(416, "range not satisfiable"), EXPECTATION_FAILED_417(417, "expectation failed"), IM_A_TEAPOT_418(418, "im a teapot"),
    ENHANCE_YOUR_CALM_420(420, "enhance your calm"), MISDIRECTED_REQUEST_421(421, "misdirected request"), UNPROCESSABLE_ENTITY_422(422, "unprocessable entity"),
    LOCKED_423(423, "locked"), FAILED_DEPENDENCY_424(424, "failed dependency"), UPGRADE_REQUIRED_426(426, "upgrade required"),
    PRECONDITION_REQUIRED_428(428, "precondition required"), TOO_MANY_REQUESTS_429(429, "too many requests"),
    REQUEST_HEADER_FIELDS_TOO_LARGE_431(431, "request header fields too large"), UNAVAILABLE_FOR_LEGAL_REASONS_451(451, "unavailable for legal reasons"),
    INTERNAL_SERVER_ERROR_500(500, "internal server error"), NOT_IMPLEMENTED_501(501, "not implemented"), BAD_GATEWAY_502(502, "bad gateway"),
    SERVICE_UNAVAILABLE_503(503, "service unavailable"), GATEWAY_TIMEOUT_504(504, "gateway timeout"),
    HTTP_VERSION_NOT_SUPPORTED_505(505, "http version not supported"), INSUFFICIENT_STORAGE_507(507, "insufficient storage"),
    LOOP_DETECTED_508(508, "loop detected"), NOT_EXTENDED_510(510, "not extended"), NETWORK_AUTHENTICATION_REQUIRED_511(511, "network authentication required"),

    MAX_CODE(511, "max code");
    ;

    private static final Map<Integer, HttpStatus> CACHE = new HashMap<>();
    static {
        for (HttpStatus status : values()) {
            CACHE.put(status.getCode(), status);
        }
    }

    public static final HttpStatus lookUp(int code) {
        return CACHE.get(code);
    }

    public static final HttpStatus lookUpOrDefault(int code, HttpStatus defaultStatus) {
        var result = lookUp(code);
        return result == null ? defaultStatus : result;
    }

    private final int code;
    private final String defaultMessage;
}
