@XmlSchema(
        location = "http://www.openarchives.org/OAI/2.0/oai_dc.xsd",
        namespace = "http://www.openarchives.org/OAI/2.0/oai_dc/",
        elementFormDefault = javax.xml.bind.annotation.XmlNsForm.QUALIFIED)
@XmlJavaTypeAdapters({
    @XmlJavaTypeAdapter(value = DateTimeAdapter.class, type = DateTime.class)
})
package au.com.miskinhill.schema.oaidc;

import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;

import org.joda.time.DateTime;

import au.com.miskinhill.schema.xmladapter.DateTimeAdapter;

