package com.cyberpointllc.stac.snapservice.model;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import org.apache.commons.lang3.StringUtils;

public class Filter {

    private final String identity;

    private final String name;

    private final BufferedImageOp filter;

    public Filter(String identity, String name, BufferedImageOp filter) {
        if (StringUtils.isBlank(identity)) {
            throw new  IllegalArgumentException("Filter identity may not be null or empty");
        }
        if (StringUtils.isBlank(name)) {
            throw new  IllegalArgumentException("Filter name may not be null or empty");
        }
        if (filter == null) {
            throw new  IllegalArgumentException("Filter filter may not be null");
        }
        this.identity = identity;
        this.name = name;
        this.filter = filter;
    }

    /**
     * Returns the identity for this Filter. The identity may not be modified.
     *
     * @return String representing the identity; guaranteed to not be empty or
     *         <code>null</code>
     */
    public String getIdentity() {
        return identity;
    }

    /**
     * Returns the displayable name of this filter. This name is used to help a
     * user identify this filter.
     *
     * @return String representing the filter name; guaranteed to not be empty
     *         or <code>null</code>
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the BufferedImageOp that can be applied to images.
     * 
     * @return BufferedImageOp the filter
     */
    public BufferedImageOp getFilter() {
        ClassgetFilter replacementClass = new  ClassgetFilter();
        ;
        return replacementClass.doIt0();
    }

    public BufferedImage filter(BufferedImage img) {
        return filter.filter(img, null);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Filter filter = (Filter) obj;
        return identity.equals(filter.identity);
    }

    @Override
    public int hashCode() {
        return identity.hashCode();
    }

    public class ClassgetFilter {

        public ClassgetFilter() {
        }

        public BufferedImageOp doIt0() {
            return filter;
        }
    }
}
