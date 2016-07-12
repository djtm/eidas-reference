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
package eu.stork.peps.auth.engine.metadata;

import org.apache.commons.lang.StringUtils;

/**
 * a contact in a piece of metadata
 */
public class Contact {
    private String email="";
    private String company="";
    private String givenName="";
    private String surName="";
    private String phone="";

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if(!StringUtils.isEmpty(email)) {
            this.email = email;
        }
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        if(!StringUtils.isEmpty(company)) {
            this.company = company;
        }
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        if(!StringUtils.isEmpty(givenName)) {
            this.givenName = givenName;
        }
    }

    public String getSurName() {
        return surName;
    }

    public void setSurName(String surName) {
        if(!StringUtils.isEmpty(surName)) {
            this.surName = surName;
        }
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        if(!StringUtils.isEmpty(phone)) {
            this.phone = phone;
        }
    }
}
