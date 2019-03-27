package io.gridgo.connector.httpcommon;

import java.io.File;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public enum HttpContentType {

    /* -------------- TEXT ---------------- */
    APPLICATION_ATOM_XML("application/atom+xml", true, false, false, false), //
    APPLICATION_SVG_XML("application/svg+xml", true, false, false, false), //
    APPLICATION_XHTML_XML("application/xhtml+xml", true, false, false, false), //
    APPLICATION_XML("application/xml", true, false, false, false), //
    TEXT_HTML("text/html", true, false, false, false), //
    TEXT_PLAIN("text/plain", true, false, false, false), //
    TEXT_CSS("text/css", true, false, false, false), //
    TEXT_XML("text/xml", true, false, false, false), //
    WILDCARD("*/*", true, false, false, false), //

    /* -------------- BINARY ---------------- */
    APPLICATION_OCTET_STREAM("application/octet-stream", false, true, false, false), //
    APPLICATION_JAVASCRIPT("application/javascript", false, true, false, false), //
    APPLICATION_PKCS12("application/pkcs12", false, true, false, false), //
    APPLICATION_POWERPOINT("application/vnd.mspowerpoint", false, true, false, false), //
    APPLICATION_PDF("application/pdf", false, true, false, false),

    /**
     * Images
     */
    IMAGE_BMP("image/bmp", false, true, false, false), //
    IMAGE_GIF("image/gif", false, true, false, false), //
    IMAGE_JPEG("image/jpeg", false, true, false, false), //
    IMAGE_PNG("image/png", false, true, false, false), //
    IMAGE_SVG("image/svg+xml", false, true, false, false), //
    IMAGE_TIFF("image/tiff", false, true, false, false), //
    IMAGE_WEBP("image/webp", false, true, false, false),

    /**
     * Audio
     * 
     */
    AUDIO_BASIC("audio/basic", false, true, false, false), //
    AUDIO_MPEG("audio/mpeg", false, true, false, false), //
    AUDIO_MP4("audio/mp4", false, true, false, false), //
    AUDIO_AIF("audio/x-aiff", false, true, false, false), //
    AUDIO_MID("audio/mid", false, true, false, false), //
    AUDIO_MPEGURL("audio/x-mpegurl", false, true, false, false), //
    AUDIO_REAL("audio/vnd.rn-realaudio", false, true, false, false), //
    AUDIO_OGG("audio/ogg", false, true, false, false), //
    AUDIO_WAV("audio/vnd.wav", false, true, false, false),

    /**
     * Video
     * 
     */
    VIDEO_OGG("video/ogg", false, true, false, false), //
    VIDEO_WEBM("video/webm", false, true, false, false),

    /* -------------- MULTYPART ---------------- */
    APPLICATION_FORM_URLENCODED("application/x-www-form-urlencoded", false, false, true, false),
    MULTIPART_FORM_DATA("multipart/form-data", false, false, true, false),

    /* -------------- JSON ---------------- */
    APPLICATION_JSON("application/json", false, false, false, true), //
    APPLICATION_JSONP("application/jsonp", false, false, false, true), //

    ;

    public static final String APPLICATION_TYPE_PREFIX = "application/";
    public static final HttpContentType DEFAULT_TEXT = TEXT_PLAIN;
    public static final HttpContentType DEFAULT_MULTIPART = MULTIPART_FORM_DATA;
    public static final HttpContentType DEFAULT_JSON = APPLICATION_JSON;
    public static final HttpContentType DEFAULT_BINARY = APPLICATION_OCTET_STREAM;

    public static final HttpContentType forFile(@NonNull File file) {
        String fileName = file.getName();
        return forFileName(fileName);
    }

    public static final HttpContentType forFileName(String fileName) {
        String[] splittedFileName = fileName.split("\\.");

        if (splittedFileName.length > 1) {
            String ext = splittedFileName[splittedFileName.length - 1].trim().toLowerCase();
            switch (ext) {
            /**
             * Application
             */
            case "js":
                return APPLICATION_JAVASCRIPT;
            case "pdf":
                return APPLICATION_PDF;
            case "pkcs12":
                return APPLICATION_PKCS12;
            case "ppt":
            case "pptx":
                return APPLICATION_POWERPOINT;

            /**
             * Image
             */
            case "gif":
                return IMAGE_GIF;
            case "jpg":
            case "jpeg":
                return IMAGE_JPEG;
            case "bmp":
                return IMAGE_BMP;
            case "png":
                return IMAGE_PNG;
            case "tiff":
                return IMAGE_TIFF;
            case "svg":
                return IMAGE_SVG;
            case "webp":
                return IMAGE_WEBP;

            /**
             * Audio
             */
            case "au":
            case "snd":
                return AUDIO_BASIC;
            case "mp3":
                return AUDIO_MPEG;
            case "mp4":
            case "m4a":
                return AUDIO_MP4;
            case "m3u":
                return AUDIO_MPEGURL;
            case "aiff":
            case "aif":
            case "aifc":
                return AUDIO_AIF;
            case "mid":
            case "rmi":
                return AUDIO_MID;
            case "ra":
            case "ram":
                return AUDIO_REAL;
            case "ogg":
                return AUDIO_OGG;
            case "wav":
                return AUDIO_WAV;

            /**
             * Video
             */
            case "webm":
                return VIDEO_WEBM;

            /**
             * common
             */
            case "txt":
                return TEXT_PLAIN;
            case "html":
            case "htm":
                return TEXT_HTML;
            case "css":
                return TEXT_CSS;
            default:
            }
        }
        return DEFAULT_BINARY;
    }

    public static final HttpContentType forValue(String mime) {
        if (mime != null) {
            for (HttpContentType contentType : values()) {
                if (mime.trim().toLowerCase().startsWith(contentType.getMime().toLowerCase())) {
                    return contentType;
                }
            }
        }
        return null;
    }

    public static final HttpContentType forValueOrDefault(String value, HttpContentType defaultValue) {
        var result = forValue(value);
        return result == null ? defaultValue : result;
    }

    public static final boolean isBinaryType(@NonNull String mime) {
        var contentType = forValue(mime);
        if (contentType != null) {
            return contentType.isBinaryFormat();
        }
        return mime.toLowerCase().startsWith(APPLICATION_TYPE_PREFIX);
    }

    public static final boolean isSupported(String value) {
        return forValue(value) != null;
    }

    private final String mime;

    private final boolean textFormat;

    private final boolean binaryFormat;

    private final boolean multipartFormat;

    private final boolean jsonFormat;
}
