package com.bright.proxy.http;

/**
 * @author Bright Xu
 */
public class HostAndPort {
    /**
     * Magic value indicating the absence of a port number.
     */
    private static final int NO_PORT = -1;

    /**
     * Hostname, IPv4/IPv6 literal, or unvalidated nonsense.
     */
    private final String host;

    /**
     * Validated port number in the range [0..65535], or NO_PORT
     */
    private final int port;

    private HostAndPort(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Split a freeform string into a host and port, without strict validation.
     *
     * <p>Note that the host-only formats will leave the port field undefined.
     *
     * @param hostPortString the input string to parse.
     * @return if parsing was successful, a populated HostAndPort object.
     * @throws IllegalArgumentException if nothing meaningful could be parsed.
     */
    public static HostAndPort fromString(String hostPortString) {
        String host;
        String portString = null;

        if (hostPortString.startsWith("[")) {
            String[] hostAndPort = getHostAndPortFromBracketedHost(hostPortString);
            host = hostAndPort[0];
            portString = hostAndPort[1];
        } else {
            int colonPos = hostPortString.indexOf(':');
            if (colonPos >= 0 && hostPortString.indexOf(':', colonPos + 1) == -1) {
                // Exactly 1 colon. Split into host:port.
                host = hostPortString.substring(0, colonPos);
                portString = hostPortString.substring(colonPos + 1);
            } else {
                // 0 or 2+ colons. Bare hostname or IPv6 literal.
                host = hostPortString;
            }
        }

        int port = NO_PORT;
        if (portString != null && !"".equals(portString)) {
            // Try to parse the whole port string as a number.
            // JDK7 accepts leading plus signs. We don't want to.
            try {
                port = Integer.parseInt(portString);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("UnParseable port number: " + hostPortString);
            }
        }

        return new HostAndPort(host, port);
    }

    /**
     * Parses a bracketed host-port string, throwing IllegalArgumentException if parsing fails.
     *
     * @param hostPortString the full bracketed host-port specification. Post might not be specified.
     * @return an array with 2 strings: host and port, in that order.
     * @throws IllegalArgumentException if parsing the bracketed host-port string fails.
     */
    private static String[] getHostAndPortFromBracketedHost(String hostPortString) {
        int closeBracketIndex = hostPortString.lastIndexOf(']');

        String host = hostPortString.substring(1, closeBracketIndex);
        if (closeBracketIndex + 1 == hostPortString.length()) {
            return new String[]{host, ""};
        } else {
            return new String[]{host, hostPortString.substring(closeBracketIndex + 2)};
        }
    }

    /**
     * Returns the portion of this {@code HostAndPort} instance that should represent the hostname or
     * IPv4/IPv6 literal.
     *
     * <p>A successful parse does not imply any degree of sanity in this field.
     */
    public String getHost() {
        return host;
    }

    /**
     * Return true if this instance has a defined port.
     */
    public boolean hasPort() {
        return port >= 0;
    }

    /**
     * Get the current port number, failing if no port is defined.
     *
     * @return a validated port number, in the range [0..65535]
     */
    public int getPort() {
        return port;
    }

    /**
     * Returns the current port number, with a default if no port is defined.
     */
    public int getPortOrDefault(int defaultPort) {
        return hasPort() ? port : defaultPort;
    }

}
