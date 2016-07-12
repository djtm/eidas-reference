/*
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence. You may
 * obtain a copy of the Licence at:
 *
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * Licence for the specific language governing permissions and limitations under
 * the Licence.
 */

package eu.stork.peps.auth.engine.core;

import eu.stork.peps.exceptions.SAMLEngineException;

import java.security.cert.X509Certificate;

/**
 * common behavior for modules of a saml engine
 */
public interface SAMLEngineModuleI {
    String CHECK_VALIDITY_PERIOD_PROPERTY="check_certificate_validity_period";
    String SELF_SIGNED_PROPERTY="disallow_self_signed_certificate";
    void checkCertificateIssuer(X509Certificate certificate) throws SAMLEngineException;
    void checkCertificateValidityPeriod(X509Certificate certificate) throws SAMLEngineException;

}
