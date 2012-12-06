package org.jasig.portlet.emailpreview;

import java.math.BigDecimal;

public class EmailQuota {
    public static final String QUOTA_UNIT_MB = "MB";
    public static final String QUOTA_UNIT_GB = "GB";
    public static final long QUOTA_COEF_MB = 1024;
    public static final long QUOTA_COEF_GB = 1048576; //(1024*1024);

    private long usage;
    private long limit;

    public EmailQuota() {
        this(0,0);
    }

    public EmailQuota(long limit,long usage) {
        this.limit = limit;
        this.usage = usage;
    }

    public long getLimit() {
        return limit;
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }

    public long getUsage() {
        return usage;
    }

    public void setUsage(long usage) {
        this.usage = usage;
    }

    public String getLimitAsString() {
        return getValueAsString(limit);
    }

    public String getUsageAsString() {
        return getValueAsString(usage);
    }

    public String getUsageAsPercentage() {
        return new BigDecimal(String.valueOf(((double)(usage)/(double)(limit)))).setScale(2, BigDecimal.ROUND_DOWN).toString().concat("%");
    }

    private String getValueAsString(long value) {
        String unit = QUOTA_UNIT_MB;
        long coef = QUOTA_COEF_MB;

        if(value > 999999) {
            unit = QUOTA_UNIT_GB;
            coef = QUOTA_COEF_GB;
        }

        return new BigDecimal(String.valueOf(((double)(value))/coef)).setScale(2, BigDecimal.ROUND_DOWN).toString().concat(unit);
    }
}
