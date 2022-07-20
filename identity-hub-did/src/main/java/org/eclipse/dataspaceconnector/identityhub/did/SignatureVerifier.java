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

package org.eclipse.dataspaceconnector.identityhub.did;

import com.nimbusds.jwt.SignedJWT;
import org.eclipse.dataspaceconnector.iam.did.crypto.credentials.VerifiableCredentialFactory;
import org.eclipse.dataspaceconnector.iam.did.spi.resolution.DidPublicKeyResolver;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.result.Result;

import java.text.ParseException;

/**
 * Class responsible for verifying JWT signatures.
 */
class SignatureVerifier {

    private final DidPublicKeyResolver didPublicKeyResolver;
    private final Monitor monitor;

    private static final String VC_AUDIENCE = "identity-hub";

    SignatureVerifier(DidPublicKeyResolver didPublicKeyResolver, Monitor monitor) {
        this.didPublicKeyResolver = didPublicKeyResolver;
        this.monitor = monitor;
    }

    /**
     * Verifies if a JWT is really signed by the claimed issuer (iss field).
     *
     * @param jwt to be verified.
     * @return if the JWT is signed by the claimed issuer.
     */
    boolean isSignedByIssuer(SignedJWT jwt) {
        var issuer = getIssuer(jwt);
        if (issuer.failed()) {
            monitor.warning("Failed finding JWT issuer");
            return false;
        }
        var issuerPublicKey = didPublicKeyResolver.resolvePublicKey(issuer.getContent());
        if (issuerPublicKey.failed()) {
            monitor.warning(String.format("Failed finding publicKey of issuer: %s", issuer.getContent()));
            return false;
        }
        var verificationResult = VerifiableCredentialFactory.verify(jwt, issuerPublicKey.getContent(), VC_AUDIENCE);
        return verificationResult.succeeded();
    }

    private Result<String> getIssuer(SignedJWT jwt) {
        try {
            var issuer = jwt.getJWTClaimsSet().getIssuer();
            return issuer == null ? Result.failure("Issuer missing from JWT") : Result.success(issuer);
        } catch (ParseException e) {
            monitor.warning("Error parsing issuer from JWT", e);
            return Result.failure(e.getMessage());
        }
    }
}
