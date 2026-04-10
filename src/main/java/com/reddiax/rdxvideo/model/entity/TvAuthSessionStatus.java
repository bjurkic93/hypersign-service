package com.reddiax.rdxvideo.model.entity;

/**
 * Status of a TV authentication session.
 */
public enum TvAuthSessionStatus {
    /**
     * Session created, waiting for user approval.
     */
    PENDING,
    
    /**
     * User has approved the session, tokens are available.
     */
    APPROVED,
    
    /**
     * Session has expired without being approved.
     */
    EXPIRED,
    
    /**
     * TV has retrieved the tokens, session is complete.
     */
    USED
}
