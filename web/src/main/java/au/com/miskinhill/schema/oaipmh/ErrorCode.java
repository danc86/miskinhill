package au.com.miskinhill.schema.oaipmh;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

@XmlEnum
public enum ErrorCode {

    @XmlEnumValue("cannotDisseminateFormat")
    CANNOT_DISSEMINATE_FORMAT {
        @Override
        public boolean verbApplies(Verb verb) {
            return verb == Verb.GET_RECORD || verb == Verb.LIST_IDENTIFIERS || verb == Verb.LIST_RECORDS;
        }
    },
    @XmlEnumValue("idDoesNotExist")
    ID_DOES_NOT_EXIST {
        @Override
        public boolean verbApplies(Verb verb) {
            return verb == Verb.GET_RECORD || verb == Verb.LIST_METADATA_FORMATS;
        }
    },
    @XmlEnumValue("badArgument")
    BAD_ARGUMENT {
        @Override
        public boolean verbApplies(Verb verb) {
            return true;
        }
    },
    @XmlEnumValue("badVerb")
    BAD_VERB {
        @Override
        public boolean verbApplies(Verb verb) {
            return false;
        }
    },
    @XmlEnumValue("noMetadataFormats")
    NO_METADATA_FORMATS {
        @Override
        public boolean verbApplies(Verb verb) {
            return verb == Verb.LIST_METADATA_FORMATS;
        }
    },
    @XmlEnumValue("noRecordsMatch")
    NO_RECORDS_MATCH {
        @Override
        public boolean verbApplies(Verb verb) {
            return verb == Verb.LIST_IDENTIFIERS || verb == Verb.LIST_RECORDS;
        }
    },
    @XmlEnumValue("badResumptionToken")
    BAD_RESUMPTION_TOKEN {
        @Override
        public boolean verbApplies(Verb verb) {
            return verb == Verb.LIST_IDENTIFIERS || verb == Verb.LIST_RECORDS || verb == Verb.LIST_SETS;
        }
    },
    @XmlEnumValue("noSetHierarchy")
    NO_SET_HIERARCHY {
        @Override
        public boolean verbApplies(Verb verb) {
            return verb == Verb.LIST_IDENTIFIERS || verb == Verb.LIST_RECORDS || verb == Verb.LIST_SETS;
        }
    };
    
    public abstract boolean verbApplies(Verb verb);

}
