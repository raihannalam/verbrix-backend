package com.verbrix.security.websocket;

import com.verbrix.exception.ResourceNotFoundException;
import com.verbrix.model.business.booking.Relationship;
import com.verbrix.repository.RelationshipRepository;
import com.verbrix.security.jwt.JwtUtils;
import com.verbrix.security.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
@Slf4j
public class WebSocketSecurityInterceptor implements ChannelInterceptor {

    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;
    private final RelationshipRepository relationshipRepository;
    private final AntPathMatcher matcher = new AntPathMatcher();

    @Override
    public Message<?> preSend(@NotNull Message<?> message, @NotNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && accessor.getCommand() != null) {

            // 1. AUTHENTICATION (The Handshake)
            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                String authHeader = accessor.getFirstNativeHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);
                    if (jwtUtils.validateJwtToken(token)) {
                        String email = jwtUtils.getUsernameFromJwtToken(token);
                        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        accessor.setUser(auth);
                        log.info("WS Connect: {}", email);
                    } else {
                        // 🟢 FIX: Reject the connection immediately if token is expired
                        log.warn("WS Connect failed: Invalid or expired token");
                        throw new AccessDeniedException("WebSocket connection rejected: Token expired or invalid.");
                    }
                } else {
                    // 🟢 FIX: Reject if no token is provided
                    throw new AccessDeniedException("WebSocket connection rejected: Missing Authorization header.");
                }
            }
            // 2. AUTHORIZATION (The Subscription)
            if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                String dest = accessor.getDestination();
                var user = accessor.getUser();

                if (user == null) throw new AccessDeniedException("Unauthenticated subscription");

                // Check if they are trying to spy on a chat: /topic/chat/{id}
                if (matcher.match("/topic/chat/{relationshipId}", dest)) {
                    String idStr = matcher.extractUriTemplateVariables("/topic/chat/{relationshipId}", dest).get("relationshipId");
                    Long relationshipId = Long.valueOf(idStr);

                    // 🔴 FIX IS HERE: Use 'findByIdWithUsers' to load Client/Interpreter eagerly
                    // The old 'findById' caused the LazyInitializationException crash
                    Relationship rel = relationshipRepository.findRelationshipWithUsers(relationshipId)
                            .orElseThrow(() -> new ResourceNotFoundException("Relationship not found"));

                    String email = user.getName();

                    // Now this check is safe because .getClient() and .getInterpreter() are fully loaded
                    boolean isParticipant = rel.getClient().getEmail().equals(email) ||
                            rel.getInterpreter().getEmail().equals(email);

                    if (!isParticipant) {
                        log.warn("Security Alert: User {} tried to spy on Relationship {}", email, relationshipId);
                        throw new AccessDeniedException("Not authorized for this chat");
                    }
                }
            }
        }
        return message;
    }
}