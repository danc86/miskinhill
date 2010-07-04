@XmlSchema(
        location = "http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd",
        namespace = "http://www.openarchives.org/OAI/2.0/",
        elementFormDefault = javax.xml.bind.annotation.XmlNsForm.QUALIFIED)
@XmlJavaTypeAdapters({
    @XmlJavaTypeAdapter(value = LocalDateAdapter.class, type = LocalDate.class),
    @XmlJavaTypeAdapter(value = DateTimeAdapter.class, type = DateTime.class)
})
package au.com.miskinhill.schema.oaipmh;

import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import au.com.miskinhill.schema.xmladapter.DateTimeAdapter;
import au.com.miskinhill.schema.xmladapter.LocalDateAdapter;

