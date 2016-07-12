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
package eu.stork.samlengineconfig.impl;

import eu.stork.samlengineconfig.BinaryParameter;
import eu.stork.samlengineconfig.ConfigurationParameter;
import eu.stork.samlengineconfig.PropsParameter;
import eu.stork.samlengineconfig.StringParameter;
import eu.stork.samlengineconfig.impl.tools.StorkConfigManagerUtil;

import java.util.Properties;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * marshaller adapter for ConfigurationParameter 
 */
public class ConfigurationParameterAdapter extends XmlAdapter<JAXBConfigurationParameter,ConfigurationParameter> {
    private static final String FILE_PREFIX="file";
    private static final String KEYSTORE_PATH="keystorePath";
    private static final String ENCRYPTION_ACTIVATION="encryptionActivation";
    private static final String[] BINARY_PARAMETERS={KEYSTORE_PATH, ENCRYPTION_ACTIVATION};

    @Override
    public ConfigurationParameter unmarshal(JAXBConfigurationParameter v) throws Exception {
        if(isPropsParameter(v)){
            return buildPropsParameter(v);
        }else {
            StringParameter sp = new StringParameter();
            sp.setName(v.getName());
            sp.setValue(v.getValue());
            return sp;
        }
    }
    private boolean isPropsParameter(JAXBConfigurationParameter parameter){
        return parameter.getName()!=null && parameter.getName().startsWith(FILE_PREFIX) &&
                StorkConfigManagerUtil.getInstance().existsFile(parameter.getValue());
    }

    private boolean isBinaryParameter(Object parameter){
        return parameter!=null &&
                StorkConfigManagerUtil.getInstance().existsFile(parameter.toString());
    }

    private PropsParameter buildPropsParameter(JAXBConfigurationParameter parameter){
        PropsParameter p=new PropsParameter();
        p.setName(parameter.getName());
        p.setFileName(parameter.getValue());
        Properties props=StorkConfigManagerUtil.getInstance().loadProps(parameter.getValue());
        p.setValue(props);
        for(String key:BINARY_PARAMETERS) {
            Object keystorePath = props.get(key);
            if (isBinaryParameter(keystorePath)) {
                BinaryParameter bp = new BinaryParameter();
                bp.setValue(loadBinaryFile(keystorePath.toString()));
                bp.setName(key);
                bp.setUrl(keystorePath.toString());
                props.put(key, bp);
            }
        }
        return p;
    }

    @Override
    public JAXBConfigurationParameter marshal(ConfigurationParameter v) throws Exception {
        JAXBConfigurationParameter sp= new JAXBConfigurationParameter();
        sp.setName(v.getName());
        if(v instanceof StringParameter) {
            sp.setValue(getStringValue(v));
        }else if(v instanceof PropsParameter){
            PropsParameter propsParameter=(PropsParameter)v;
            sp.setValue(propsParameter.getFileName());
        }
        return sp;
    }

	private String getStringValue(ConfigurationParameter v) {
		if(v!=null && v.getValue()!=null){
			return v.getValue().toString();
		}
		return "";
	}

    private byte[] loadBinaryFile(String fileName){
        return StorkConfigManagerUtil.getInstance().loadBinaryFile(fileName);
    }
}
