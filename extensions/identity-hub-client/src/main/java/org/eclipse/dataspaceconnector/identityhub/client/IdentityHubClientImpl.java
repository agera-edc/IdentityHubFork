package org.eclipse.dataspaceconnector.identityhub.client;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.core.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.eclipse.dataspaceconnector.identityhub.client.credentials.VerifiableCredential;
import org.eclipse.dataspaceconnector.identityhub.models.Descriptor;
import org.eclipse.dataspaceconnector.identityhub.models.MessageRequestObject;
import org.eclipse.dataspaceconnector.identityhub.models.RequestObject;
import org.eclipse.dataspaceconnector.identityhub.models.ResponseObject;
import org.eclipse.dataspaceconnector.spi.EdcException;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.eclipse.dataspaceconnector.identityhub.models.WebNodeInterfaceMethod.COLLECTIONS_QUERY;
import static org.eclipse.dataspaceconnector.identityhub.models.WebNodeInterfaceMethod.COLLECTIONS_WRITE;

public class IdentityHubClientImpl implements IdentityHubClient {

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public IdentityHubClientImpl(OkHttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public Collection<VerifiableCredential> getVerifiableCredentials(String hubBaseUrl) {
        ResponseObject responseObject;
        try {
            Response response = httpClient.newCall(
                        new Request.Builder()
                                .url(hubBaseUrl)
                                .post(buildRequestBody(COLLECTIONS_QUERY.getName()))
                                .build())
                .execute();

            responseObject = objectMapper.readValue(response.body().byteStream(), ResponseObject.class);

        } catch (IOException e) {
            throw new EdcException(e);
        }

        Collection<VerifiableCredential> entries = responseObject.getReplies().stream()
                .findFirst()
                .orElseThrow(() -> new EdcException("Invalid response"))
                .getEntries().stream()
                    .map(e -> objectMapper.convertValue(e, VerifiableCredential.class)).collect(Collectors.toList());

        return entries;
    }

    @Override
    public void pushVerifiableCredential(String hubBaseUrl, VerifiableCredential verifiableCredential) {
        try {
            var payload = objectMapper.writeValueAsString(verifiableCredential);
            httpClient.newCall(
                            new Request.Builder()
                                    .url(hubBaseUrl)
                                    .post(buildRequestBody(COLLECTIONS_WRITE.getName(), payload.getBytes(UTF_8)))
                                    .build())
                    .execute();
        } catch (IOException e) {
            throw new EdcException(e);
        }
    }

    private RequestBody buildRequestBody(String method) {
        return buildRequestBody(method, null);
    }

    private RequestBody buildRequestBody(String method, byte[] data) {
        String requestId = UUID.randomUUID().toString();
        RequestObject requestObject = RequestObject.Builder.newInstance()
                .requestId(requestId)
                .target("target")
                .messages(List.of(MessageRequestObject.Builder.newInstance()
                        .descriptor(Descriptor.Builder.newInstance()
                                .nonce("nonce")
                                .method(method)
                                .build())
                        .data(data)
                        .build())
                )
                .build();
        try {
            var payload = objectMapper.writeValueAsString(requestObject);
            return RequestBody.create(payload, okhttp3.MediaType.get(MediaType.APPLICATION_JSON));
        } catch (JsonProcessingException e) {
            throw new EdcException(e);
        }
    }
}
