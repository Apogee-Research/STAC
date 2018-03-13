/*
 * Copyright (c) 2009-2016 Matthew R. Harrah
 *
 * MIT License
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package infotrader.parser.model;

/**
 * Information about the version of the GEDCOM spec used
 * 
 * @author frizbog1
 */
public class InfoTraderVersion extends AbstractElement {

    /**
     * Serial Version UID
     */
    private static final long serialVersionUID = -8766863038155122803L;

    /**
     * The form
     */
    private StringWithCustomTags InfoTraderForm = new StringWithCustomTags("LINEAGE-LINKED");

    /**
     * The version number for this GEDCOM
     */
    private SupportedVersion versionNumber = SupportedVersion.V5_5_1;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        InfoTraderVersion other = (InfoTraderVersion) obj;
        if (InfoTraderForm == null) {
            if (other.InfoTraderForm != null) {
                return false;
            }
        } else if (!InfoTraderForm.equals(other.InfoTraderForm)) {
            return false;
        }
        if (versionNumber == null) {
            if (other.versionNumber != null) {
                return false;
            }
        } else if (!versionNumber.equals(other.versionNumber)) {
            return false;
        }
        return true;
    }

    /**
     * Gets the gedcom form.
     *
     * @return the gedcom form
     */
    public StringWithCustomTags getInfoTraderForm() {
        return InfoTraderForm;
    }

    /**
     * Gets the version number.
     *
     * @return the version number
     */
    public SupportedVersion getVersionNumber() {
        return versionNumber;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (InfoTraderForm == null ? 0 : InfoTraderForm.hashCode());
        result = prime * result + (versionNumber == null ? 0 : versionNumber.hashCode());
        return result;
    }


    public void setInfoTraderForm(StringWithCustomTags InfoTraderForm) {
        this.InfoTraderForm = InfoTraderForm;
    }


    public void setVersionNumber(SupportedVersion versionNumber) {
        this.versionNumber = versionNumber;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("InfoTraderVersion [");
        if (InfoTraderForm != null) {
            builder.append("InfoTraderForm=");
            builder.append(InfoTraderForm);
            builder.append(", ");
        }
        if (versionNumber != null) {
            builder.append("versionNumber=");
            builder.append(versionNumber);
            builder.append(", ");
        }
        if (customTags != null) {
            builder.append("customTags=");
            builder.append(customTags);
        }
        builder.append("]");
        return builder.toString();
    }
}
