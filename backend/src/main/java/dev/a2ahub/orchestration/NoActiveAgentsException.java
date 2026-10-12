package dev.a2ahub.orchestration;

/**
 * Thrown when no active (HEALTHY or DEGRADED) agents are available to handle orchestration requests.
 */
public class NoActiveAgentsException extends RuntimeException {
    public NoActiveAgentsException(String message) {
        super(message);
    }
}
