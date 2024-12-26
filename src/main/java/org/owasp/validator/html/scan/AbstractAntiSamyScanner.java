/*
 * Copyright (c) 2007-2023, Arshan Dabirsiaghi, Jason Li
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. Neither the name of OWASP nor the names of its
 * contributors may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.owasp.validator.html.scan;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import org.apache.xml.serializer.Serializer;
import org.apache.xml.serializer.SerializerFactory;
import org.owasp.validator.html.CleanResults;
import org.owasp.validator.html.InternalPolicy;
import org.owasp.validator.html.Policy;
import org.owasp.validator.html.PolicyException;
import org.owasp.validator.html.ScanException;
import org.owasp.validator.html.util.ErrorMessageUtil;

/**
 * This class defines the basic structure for each type of AntiSamy scanner. All the
 * scanning/filtration logic is to reside in each implementation of this class, but the
 * implementations should not be called directly. All scanning should be done through an <code>
 * AntiSamy.scan()</code> method invocation.
 *
 * @author Arshan Dabirsiaghi
 */
public abstract class AbstractAntiSamyScanner {

  protected final InternalPolicy policy;
  protected final List<String> errorMessages = new ArrayList<String>();

  protected static final ResourceBundle messages = getResourceBundle();
  protected final Locale locale = Locale.getDefault();

  protected boolean isNofollowAnchors = false;
  protected boolean isNoopenerAndNoreferrerAnchors = false;
  protected boolean isValidateParamAsEmbed = false;

  public abstract CleanResults scan(String html) throws ScanException;

  public abstract CleanResults getResults();

  /**
   * Construct an AntiSamy Scanner instance that uses the specified AntiSamy policy.
   *
   * @param policy The policy to use.
   */
  public AbstractAntiSamyScanner(Policy policy) {
    assert policy instanceof InternalPolicy : policy.getClass();
    this.policy = (InternalPolicy) policy;
  }

  /**
   * Construct an AntiSamy Scanner instance that uses the default AntiSamy policy file.
   *
   * @throws PolicyException thrown when there is a problem validating or parsing the policy file.
   *     Any validation errors not caught by the XML validation will be thrown with this exception.
   */
  public AbstractAntiSamyScanner() throws PolicyException {
    policy = (InternalPolicy) Policy.getInstance();
  }

  private static ResourceBundle getResourceBundle() {
    try {
      return ResourceBundle.getBundle("AntiSamy", Locale.getDefault());
    } catch (MissingResourceException mre) {
      return ResourceBundle.getBundle(
          "AntiSamy", new Locale(Constants.DEFAULT_LOCALE_LANG, Constants.DEFAULT_LOCALE_LOC));
    }
  }

  protected void addError(String errorKey, Object[] objs) {
    errorMessages.add(ErrorMessageUtil.getMessage(messages, errorKey, objs));
  }

  protected Serializer getOutputFormat() {

    Properties properties = new Properties();
    properties.setProperty("omitXMLDeclaration", Boolean.toString(policy.isOmitXmlDeclaration()));
    properties.setProperty("omitDocumentType", Boolean.toString(policy.isOmitDoctypeDeclaration()));
    properties.setProperty("preserveEmptyAttributes", Boolean.TRUE.toString());
    properties.setProperty("preserveSpace", Boolean.toString(policy.isPreserveSpace()));

    if (policy.isFormatOutput()) {
      properties.setProperty("lineWidth", "80");
      properties.setProperty("indenting", Boolean.TRUE.toString());
      properties.setProperty("indent", "2");
    }

    return SerializerFactory.getSerializer(properties);
  }

  protected Serializer getHTMLSerializer(
      Writer w, Serializer format) {
    return new ASHTMLSerializer(w, format, policy);
  }

  protected String trim(String original, String cleaned) {
    if (cleaned.endsWith("\n")) {
      if (!original.endsWith("\n")) {
        if (cleaned.endsWith("\r\n")) {
          cleaned = cleaned.substring(0, cleaned.length() - 2);
        } else if (cleaned.endsWith("\n")) {
          cleaned = cleaned.substring(0, cleaned.length() - 1);
        }
      }
    }

    return cleaned;
  }
}
