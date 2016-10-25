/*
 * Copyright (c) 2016 by European Commission
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This product combines work with different licenses. See the "NOTICE" text
 * file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution. Any derivative works
 * that you distribute must include a readable copy of the "NOTICE" text file.
 *
 */

package eu.eidas.sp;

import java.io.IOException;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import eu.eidas.config.impl.EnvironmentVariableSubstitutor;
import org.opensaml.saml2.core.Response;
import org.opensaml.xml.XMLObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import eu.eidas.auth.commons.xml.DocumentBuilderFactoryUtil;
import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.encryption.exception.UnmarshallException;
import eu.eidas.samlengineconfig.CertificateConfigurationManager;

public class SPUtil {

    private static final Logger LOG = LoggerFactory.getLogger(SPUtil.class);

    private static final String SAML_ENGINE_LOCATION_VAR = "SP_CONF_LOCATION";

    private static CertificateConfigurationManager spSamlEngineConfig = null;

    private static final String[] PATH_PREFIXES = {"file://", "file:/", "file:"};

    private static Properties loadConfigs(String path) throws IOException {
        Properties properties = new Properties();
        properties.load(SPUtil.class.getClassLoader().getResourceAsStream(path));
        return properties;
    }

    public static Properties loadSPConfigs() throws ApplicationSpecificServiceException {
        try {
            return new EnvironmentVariableSubstitutor().mutatePropertiesReplaceValues(SPUtil.loadConfigs(Constants.SP_PROPERTIES));
        } catch (IOException e) {
            LOG.error(e.getMessage());
            throw new ApplicationSpecificServiceException("Could not load configuration file", e.getMessage());
        }
    }

    /**
     * @return true when the metadata support should be active
     */
    public static boolean isMetadataEnabled() {
        Properties properties = SPUtil.loadSPConfigs();
        return properties.getProperty(Constants.SP_METADATA_ACTIVATE) == null || Boolean.parseBoolean(
                properties.getProperty(Constants.SP_METADATA_ACTIVATE));
    }

/*    public static ProtocolEngine createCompatibleSAMLEngine(String samlEngineName, EidasExtensionProcessor extProc) throws EIDASSAMLEngineException {
        return createEidasSAMLEngine(samlEngineName, extProc);
    }

    public static ProtocolEngine createEidasSAMLEngine(String samlEngineName) throws EIDASSAMLEngineException {
        return createEidasSAMLEngine(samlEngineName, null);
    }*/

    private static String getLocation(String location) {
        if (location != null) {
            for (String prefix : PATH_PREFIXES) {
                if (location.startsWith(prefix)) {
                    return location.substring(prefix.length());
                }
            }
        }
        return location;
    }

    private static final String NO_ASSERTION = "no assertion found";

    private static final String ASSERTION_XPATH = "//*[local-name()='Assertion']";

    /**
     * Returns true when the input contains an encrypted SAML Response
     *
     * @param tokenSaml
     * @return
     * @throws EIDASSAMLEngineException
     */
    public static boolean isEncryptedSamlResponse(byte[] tokenSaml) throws UnmarshallException {
        XMLObject samlObject = OpenSamlHelper.unmarshall(tokenSaml);
        if (samlObject instanceof Response) {
            Response response = (Response) samlObject;
            return response.getEncryptedAssertions() != null && !response.getEncryptedAssertions().isEmpty();
        }
        return false;

    }

    /**
     * @param samlMsg the saml response as a string
     * @return a string representing the Assertion
     */
    public static String extractAssertionAsString(String samlMsg) {
        String assertion = NO_ASSERTION;
        try {
            Document doc = DocumentBuilderFactoryUtil.parse(samlMsg);

            XPath xPath = XPathFactory.newInstance().newXPath();
            Node node = (Node) xPath.evaluate(ASSERTION_XPATH, doc, XPathConstants.NODE);
            if (node != null) {
                assertion = DocumentBuilderFactoryUtil.toString(node);
            }
        } catch (ParserConfigurationException pce) {
            LOG.error("cannot parse response {}", pce);
        } catch (SAXException saxe) {
            LOG.error("cannot parse response {}", saxe);

        } catch (IOException ioe) {
            LOG.error("cannot parse response {}", ioe);

        } catch (XPathExpressionException xpathe) {
            LOG.error("cannot find the assertion {}", xpathe);

        } catch (TransformerException trfe) {
            LOG.error("cannot output the assertion {}", trfe);

        }

        return assertion;
    }
}
