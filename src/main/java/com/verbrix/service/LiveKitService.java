package com.verbrix.service;

import io.livekit.server.AccessToken;
import io.livekit.server.RoomJoin;
import io.livekit.server.RoomName;
import io.livekit.server.CanPublish;
import io.livekit.server.CanSubscribe;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LiveKitService {


    @Value("${livekit.api-key}")
    private String apiKey;

    @Value("${livekit.api-secret}")
    private String apiSecret;

    public String generateCallToken(Long relationshipId, String identity) {


        // 1️⃣ UNIQUE room per call (NOT per relationship)
        String roomName = "call-" + relationshipId;

        AccessToken token = new AccessToken(apiKey, apiSecret);

        // 2️⃣ Stable identity (good)
        token.setIdentity(identity);
        token.setName(identity);

        // 3️⃣ Metadata is fine
        token.setMetadata("vcall");

        // 4️⃣ SHORT TTL (VERY IMPORTANT)
        token.setTtl(3600);

        // 5️⃣ CALL-SAFE grants
        token.addGrants(
                new RoomJoin(true),
                new RoomName(roomName),
                new CanPublish(true),
                new CanSubscribe(true)
        );
        return token.toJwt();
    }
}
