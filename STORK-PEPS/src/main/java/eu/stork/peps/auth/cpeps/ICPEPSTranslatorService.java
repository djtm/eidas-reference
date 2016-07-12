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

import eu.stork.peps.auth.commons.IPersonalAttributeList;
import eu.stork.peps.auth.commons.IStorkSession;
import eu.stork.peps.auth.commons.STORKAuthnRequest;

/**
 * Interface for normalizing the {@link IPersonalAttributeList}.
 * 
 * @author ricardo.ferreira@multicert.com, renato.portela@multicert.com,
 *         luis.felix@multicert.com, hugo.magalhaes@multicert.com,
 *         paulo.ribeiro@multicert.com
 * @version $Revision: 1.23 $, $Date: 2010-11-18 23:17:50 $
 */
public interface ICPEPSTranslatorService {
  
  /**
   * Normalizes the attributes' name from a given {@link IPersonalAttributeList}
   * to a common format.
   * 
   * @param pal The personal attribute list to normalize.
   * 
   * @return The normalized personal attribute list.
   * 
   * @see IPersonalAttributeList
   */
  IPersonalAttributeList normaliseAttributeNamesToStork(
    IPersonalAttributeList pal);
  
  /**
   * Normalizes the attributes' name from a given {@link IPersonalAttributeList}
   * to a specific format.
   * 
   * @param pal The personal attribute list to normalize.
   * 
   * @return The normalized personal attribute list.
   * 
   * @see IPersonalAttributeList
   */
  IPersonalAttributeList normaliseAttributeNamesFromStork(
    IPersonalAttributeList pal);
  
  /**
   * Normalizes the attributes' values from a given
   * {@link IPersonalAttributeList} to the common format.
   * 
   * @param samlService The SAML Service.
   * @param authData The authentication request.
   * @param ipUserAddress The citizen's IP address.
   * 
   * @return The normalized personal attribute list's values.
   * 
   * @see ICPEPSSAMLService
   * @see STORKAuthnRequest
   */
  IPersonalAttributeList normaliseAttributeValuesToStork(
    ICPEPSSAMLService samlService, STORKAuthnRequest authData,
    String ipUserAddress);
  
  /**
   * Derives the attributes' name to a common format. Updates the original
   * Personal Attribute List, stored in the session, based on the values of
   * attrList.
   * 
   * @param samlService The SAML Service.
   * @param session The session containing the original attribute list to
   *          update.
   * @param authData The authentication request.
   * @param ipUserAddress The citizen's IP address.
   * 
   * @return The new personal attribute list with the derived attributes.
   * 
   * @see ICPEPSSAMLService
   * @see IStorkSession
   */
  IPersonalAttributeList deriveAttributesToStork(ICPEPSSAMLService samlService,
    IStorkSession session, STORKAuthnRequest authData, String ipUserAddress);
  
  /**
   * Derives the attributes' name to a specific format.
   * 
   * @param pal Personal attribute list with the attributes to derive.
   * 
   * @return The new personal attribute list with the derived attributes.
   * 
   * @see IPersonalAttributeList
   */
  IPersonalAttributeList deriveAttributesFromStork(IPersonalAttributeList pal);
}
