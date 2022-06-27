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

package org.eclipse.dataspaceconnector.identityhub.client;

import org.eclipse.dataspaceconnector.identityhub.models.credentials.VerifiableCredential;

import java.io.IOException;
import java.util.Collection;

/**
 * IdentityHub Client
 * This client is used to call the IdentityHub endpoints in order query and write VerifiableCredentials.
 */
public interface IdentityHubClient {

    /**
     * Get VerifiableCredentials provided by an Identity Hub instance.
     * @param hubBaseUrl Base URL of the IdentityHub instance
     * @return VerifiableCredentials
     * @throws IOException
     */
    Collection<VerifiableCredential> getVerifiableCredentials(String hubBaseUrl) throws IOException;

    /**
     * Write a VerifiableCredential.
     * @param hubBaseUrl Base URL of the IdentityHub instance
     * @param verifiableCredential
     * @throws IOException
     */
    void addVerifiableCredential(String hubBaseUrl, VerifiableCredential verifiableCredential) throws IOException;

}
