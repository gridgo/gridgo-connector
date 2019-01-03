package io.gridgo.connector.jetty.server;

public enum JettyServletContextHandlerOption {

    NO_SESSIONS(0), NO_SECURITY(0), SESSIONS(1), SECURITY(2), GZIP(4);

    public static final JettyServletContextHandlerOption fromCode(int code) {
        for (JettyServletContextHandlerOption option : values()) {
            if (option.getCode() == code) {
                return option;
            }
        }
        return null;
    }

    public static final JettyServletContextHandlerOption fromName(String name) {
        for (JettyServletContextHandlerOption option : values()) {
            if (option.name().toLowerCase().equalsIgnoreCase(name)) {
                return option;
            }
        }
        return null;
    }

    private int code;

    private JettyServletContextHandlerOption(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }
}
