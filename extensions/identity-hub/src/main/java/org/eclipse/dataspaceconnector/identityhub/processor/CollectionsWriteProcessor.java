/*
 *  Copyright (c) 2022 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - initial API and implementation
 *
 */

package org.eclipse.dataspaceconnector.identityhub.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.dataspaceconnector.identityhub.dtos.MessageResponseObject;
import org.eclipse.dataspaceconnector.identityhub.dtos.MessageStatus;
import org.eclipse.dataspaceconnector.identityhub.store.IdentityHubStore;

import java.io.IOException;
import java.util.Base64;

import static org.eclipse.dataspaceconnector.identityhub.dtos.MessageResponseObject.MESSAGE_ID_VALUE;

/**
 * Processor of "CollectionsWrite" messages, in order to write objects into the {@link IdentityHubStore}.
 */
public class CollectionsWriteProcessor implements MessageProcessor {

    private final IdentityHubStore<Object> identityHubStore;
    private final ObjectMapper objectMapper;

    public CollectionsWriteProcessor(IdentityHubStore<Object> identityHubStore, ObjectMapper objectMapper) {
        this.identityHubStore = identityHubStore;
        this.objectMapper = objectMapper;
    }

    @Override
    public MessageResponseObject process(byte[] data) {
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(data);
            var hubObject = objectMapper.readValue(decoded, Object.class);
            identityHubStore.add(hubObject);
            return MessageResponseObject.Builder.newInstance().messageId(MESSAGE_ID_VALUE).status(MessageStatus.OK).build();
        } catch (IllegalArgumentException | IOException e) {
            return MessageResponseObject.Builder.newInstance().messageId(MESSAGE_ID_VALUE).status(MessageStatus.MALFORMED_MESSAGE).build();
        }
    }
}
