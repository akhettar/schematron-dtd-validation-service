package com.akhettar.validation.core.services;

import java.io.InputStream;

import com.akhettar.validation.model.file.FileDetails;
import com.akhettar.validation.model.file.FileType;
import com.akhettar.validation.model.report.Report;
import com.akhettar.validation.model.report.Reports;

/**
 * ValidationService interface.
 * 
 * @author a.khettar
 * 
 */
public interface ValidationService {

    /**
     * Validates single input NLM XML.
     * 
     * @param nlm
     *            the given NLM XML to validate.
     * 
     * @return {@link Report}.
     */
    Report validate(final InputStream nlm);

    /**
     * Validates an {@link InputStream}. This inputStream
     * can be single NLM XML file or a Zip file containing
     * set of NLM XML files.
     * 
     * @param stream
     *            the {@link InputStream}.
     * @param fileDetails
     *            {@link FileDetails}.
     * 
     * @return Reports.
     */
    Reports validate(final InputStream stream, final String fileName);

    /**
     * Validates an {@link InputStream}. This inputStream
     * can be single NLM XML file or a Zip file containing
     * set of NLM XML files.
     * 
     * @param stream
     *            the {@link InputStream}.
     * @param fileDetails
     *            {@link FileDetails}.
     * 
     * @return Reports.
     */
    Reports validate(final InputStream stream, final FileType type);

}
