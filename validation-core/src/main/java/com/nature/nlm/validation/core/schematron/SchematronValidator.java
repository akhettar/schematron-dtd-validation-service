package com.nature.nlm.validation.core.schematron;

import java.io.InputStream;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.nature.nlm.validation.core.Constants;
import com.nature.nlm.validation.core.Util;
import com.nature.nlm.validation.core.Validator;
import com.nature.nlm.validation.core.exception.ValidationException;
import com.nature.nlm.validation.model.report.Report;

/**
 * Schematron validator. The schamatron rules (Nature-NLM.sch) get converted
 * into to multiple formats using three different XSLT templates provided by
 * schematron.org.
 * The end result is that we assert our input xml against an XSLT templates
 * which contains
 * all our schematron rules as XSLT templates.
 * 
 * @author a.khettar
 * 
 */
public class SchematronValidator implements Validator {

    private final Logger LOG = LoggerFactory.getLogger(this.getClass());
    private final XMLReader reader;
    private final Unmarshaller unmarshaller;
    private final Templates reports;
    private final Templates schematrons;
    private final URIResolver resolver;

    @Inject
    public SchematronValidator(@Named("schematron") Templates schematrons, Unmarshaller unmarshaller,
            @Named("report") Templates reports, XMLReader reader, URIResolver resolver) {

        this.reader = reader;
        this.unmarshaller = unmarshaller;
        this.reports = reports;
        this.schematrons = schematrons;
        this.resolver = resolver;

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.nature.nlm.service.validator.core.Validator#validate(java.lang.String
     * )
     */
    @Override
    public Report validate(InputStream nlm) {

        try {

            final Source source = new SAXSource(reader, new InputSource(nlm));
            final DOMResult result = new DOMResult();
            final Transformer transformer = schematrons.newTransformer();
            transformer.setURIResolver(resolver);
            transformer.transform(source, result);
            return generateReport(result);
        } catch (TransformerException e) {
            LOG.error(e.getMessage());
            throw new ValidationException("Failed to apply schematron validation transform", e);

        }

    }

    /**
     * 
     * Generates report from {@link DOMResult}.
     * 
     * @param result
     *            the given report in {@link DOMResult}.
     * @return {@link Report}.
     */
    private Report generateReport(DOMResult result) {

        try {
            Util.toString(result.getNode());
            final DOMResult reportResult = new DOMResult();
            final Transformer transformer = reports.newTransformer();
            transformer.transform(new DOMSource(result.getNode()), reportResult);
            final Node reportNode = reportResult.getNode();
            return this.unmarshall(reportNode);

        } catch (JAXBException e) {
            LOG.error(e.getMessage());
            throw new ValidationException("Failed to unmarshall report generated by the transform", e);
        } catch (TransformerException e) {
            LOG.error(e.getMessage());
            throw new ValidationException("Failed apply schematron validation transform", e);
        }

    }

    /**
     * Unmarshal report xml into Report
     * 
     * @param node
     * @return
     * @throws JAXBException
     */
    private Report unmarshall(Node node) throws JAXBException {

        Report report = (Report) this.unmarshaller.unmarshal(node);
        if (report == null || report.getMessage().size() == 0) {
            report = Util.buildSuccessfulReport();
            report.setStatus(Constants.SUCCESS);
            return report;
        }
        report.setStatus(Constants.FAILURE);
        return report;

    }

}
