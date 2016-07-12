/*
 * This work is Open Source and licensed by the European Commission under the
 * conditions of the European Public License v1.1 
 *  
 * (http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1); 
 * 
 * any use of this file implies acceptance of the conditions of this license. 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS,  WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
 * License for the specific language governing permissions and limitations 
 * under the License.
 */
package eu.stork.peps.auth.cpeps;

import eu.stork.peps.auth.commons.*;
import eu.stork.peps.auth.commons.exceptions.CPEPSException;
import eu.stork.peps.logging.LoggingMarkerMDC;
import eu.stork.peps.utils.EidasAttributesUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * This class is a service used by {@link AUCPEPS} to get, process citizen
 * consent and to update attribute the attribute list on session or citizen
 * consent based.
 * 
 * @author ricardo.ferreira@multicert.com, renato.portela@multicert.com,
 *         luis.felix@multicert.com, hugo.magalhaes@multicert.com,
 *         paulo.ribeiro@multicert.com
 * @version $Revision: 1.7 $, $Date: 2010-11-18 23:17:50 $
 * 
 * @see ICPEPSTranslatorService
 */
public final class AUCPEPSCitizen implements ICPEPSCitizenService {

  /**
   * Logger object.
   */
  private static final Logger LOG = LoggerFactory.getLogger(AUCPEPSCitizen.class
    .getName());
    protected AUCPEPSUtil cpepsUtil;

  /**
   * {@inheritDoc}
   */
  public CitizenConsent getCitizenConsent(final Map<String, String> parameters,
    final IPersonalAttributeList pal) {
    
    final CitizenConsent consent = new CitizenConsent();
    
    LOG.debug("[getCitizenConsent] Constructing consent...");
    for (final PersonalAttribute pa : pal) {
      LOG.debug("[getCitizenConsent] checking " + pa.getName());
      if (parameters.get(pa.getName()) != null) {
        if (pa.isRequired()) {
          LOG.trace("[getCitizenConsent] adding " + pa.getName()
            + " to mandatory attributes");
          consent.setMandatoryAttribute(pa.getName());
        } else {
          LOG.trace("[getCitizenConsent] adding " + pa.getName()
            + " to optional attributes");
          consent.setOptionalAttribute(pa.getName());
        }
        
      }
    }
    return consent;
  }
  
  /**
   * {@inheritDoc}
   */
  public void processCitizenConsent(final CitizenConsent consent,
    final STORKAuthnRequest authData, final String ipUserAddress,
    final ICPEPSSAMLService cpepsSAMLService) {
    
    for (final PersonalAttribute attr : authData.getPersonalAttributeList()) {
      LOG.trace("Searching for " + attr.getName());
      if (attr.isRequired()
        && !consent.getMandatoryList().contains(attr.getName())) {
        LOG.trace("Attribute " + attr.getName() + " not found");
        LOG.info("BUSINESS EXCEPTION : Required attribute is missing!");
        
        final String errorMessage =
          PEPSUtil.getConfig(PEPSErrors.CITIZEN_RESPONSE_MANDATORY
            .errorMessage());
        
        final String errorCode =
          PEPSUtil.getConfig(PEPSErrors.CITIZEN_RESPONSE_MANDATORY.errorCode());
        
        final byte[] error =
          cpepsSAMLService.generateErrorAuthenticationResponse(authData,
            errorCode, STORKSubStatusCode.REQUEST_DENIED_URI.toString(),
            errorMessage, ipUserAddress, true);
        throw new CPEPSException(PEPSUtil.encodeSAMLToken(error), errorCode,
          errorMessage);
      }
      LOG.debug(attr.getName() + " found");
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public IPersonalAttributeList updateAttributeList(
    final CitizenConsent citizenConsent,
    final IPersonalAttributeList personalList) {

      IPersonalAttributeList pal=null;
      try {
          pal = (IPersonalAttributeList) personalList.clone();
      } catch (CloneNotSupportedException e) {
          LOG.trace("[PersonalAttribute] Nothing to do.", e);
      }
      if (pal != null){
          for (final PersonalAttribute pa : pal) {
          LOG.trace("Searching for " + pa.getName());
          if (!pa.isRequired()
            && !citizenConsent.getOptionalList().contains(pa.getName())) {

            LOG.trace("Removing " + pa.getName());
            personalList.remove(pa.getName());
          }
      }
      
    }
    return personalList;
  }
  
  /**
   * {@inheritDoc}
   */
    public IPersonalAttributeList updateAttributeList(
        final IStorkSession session, final IPersonalAttributeList attributeList) {

        final STORKAuthnRequest authData = (STORKAuthnRequest) session.get(PEPSParameters.AUTH_REQUEST.toString());
        if(cpepsUtil!=null && !Boolean.parseBoolean(cpepsUtil.getConfigs().getProperty(PEPSValues.DISABLE_CHECK_MANDATORY_ATTRIBUTES.toString()))&&
            !EidasAttributesUtil.checkMandatoryAttributeSets(attributeList)){
              throw new CPEPSException(PEPSUtil.getConfig(PEPSErrors.EIDAS_MANDATORY_ATTRIBUTES.errorCode()), PEPSUtil.getConfig(PEPSErrors.EIDAS_MANDATORY_ATTRIBUTES.errorMessage()));
         }
        authData.setPersonalAttributeList(attributeList);
        session.put(PEPSParameters.AUTH_REQUEST.toString(), authData);
        LOG.trace(LoggingMarkerMDC.SESSION_CONTENT,"SESSION : updateAttributeList Called, size is " + session.size());
        return authData.getPersonalAttributeList();
    }

  /**
   * {@inheritDoc}
   */
  public IPersonalAttributeList updateAttributeListValues(
    final IStorkSession session, final IPersonalAttributeList attributeList) {
    
    final STORKAuthnRequest authData =
      (STORKAuthnRequest) session.get(PEPSParameters.AUTH_REQUEST.toString());
    LOG.trace("Updating Attribute values");
    final IPersonalAttributeList pal = authData.getPersonalAttributeList();
    for (final PersonalAttribute pa : pal) {
      if (pa.isEmptyValue()) {
        final PersonalAttribute paTemp = attributeList.get(pa.getName());
        // if the attribute is derived then paTemp is null
        if (paTemp != null) {
          if (!paTemp.isEmptyValue()) {
            pa.setValue(paTemp.getValue());
          }
          if (paTemp.getStatus() != null) {
            pa.setStatus(paTemp.getStatus());
          }
          if (!paTemp.isEmptyComplexValue()) {
            pa.setComplexValue(paTemp.getComplexValue());
          }
        }
      }else if(StringUtils.isEmpty(pa.getStatus()) &&  cpepsUtil!=null && cpepsUtil.isEIDAS10(authData.getMessageFormatName()) ){
        pa.setStatus(STORKStatusCode.STATUS_AVAILABLE.toString());
      }
    }
      if(cpepsUtil!=null && Boolean.parseBoolean(cpepsUtil.getConfigs().getProperty(PEPSValues.DISABLE_CHECK_MANDATORY_ATTRIBUTES.toString())) &&
          !EidasAttributesUtil.checkMandatoryAttributeSets(attributeList)){
            throw new CPEPSException(PEPSUtil.getConfig(PEPSErrors.EIDAS_MANDATORY_ATTRIBUTES.errorCode()), PEPSUtil.getConfig(PEPSErrors.EIDAS_MANDATORY_ATTRIBUTES.errorMessage()));
      }

    authData.setPersonalAttributeList(pal);
    session.put(PEPSParameters.AUTH_REQUEST.toString(), authData);
    return authData.getPersonalAttributeList();
  }

    public AUCPEPSUtil getCpepsUtil() {
        return cpepsUtil;
    }

    public void setCpepsUtil(AUCPEPSUtil cpepsUtil) {
        this.cpepsUtil = cpepsUtil;
    }
}
