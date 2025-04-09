package cross.reputation.model;

import java.util.List;

public class CommunityMetricToImport {

    private Community community;

    private Community destinationCommunity;

    private Metric metric;

    private Metric destinationMetric;

    private Double trust;

    public CommunityMetricToImport(Community community, Community destinationCommunity, Metric metric, Metric destinationMetric, Double categoryMatching, Double fixedValue) {
        this.community = community;
        this.destinationCommunity = destinationCommunity;
        this.metric = metric;
        this.destinationMetric = destinationMetric;
        calculateTrust(categoryMatching, fixedValue, null, null);
    }

    public CommunityMetricToImport(Community community, Community destinationCommunity, Metric metric, Metric destinationMetric, Double categoryMatching, Double fixedValue, Double categoryMatchWieght, Double fixedValueWight) {
        this.community = community;
        this.destinationCommunity = destinationCommunity;
        this.metric = metric;
        this.destinationMetric = destinationMetric;
        calculateTrust(categoryMatching, fixedValue, categoryMatchWieght, fixedValueWight);
    }

    public boolean isContained(List<CommunityMetricToImport> communityMetricsToImport) {
        for (CommunityMetricToImport comMetToImp : communityMetricsToImport) {
            if (comMetToImp.getCommunity() == community && comMetToImp.getDestinationCommunity() == destinationCommunity && comMetToImp.getMetric() == metric && comMetToImp.getDestinationMetric() == destinationMetric) {
                return true;
            }
        }
        return false;
    }

    public double calculateTrust(Double categoryMatching, Double fixedValue, Double categoryMatchWieght, Double fixedValueWight) {
        if (categoryMatching != null) {
            if (fixedValue != null) {
                if (categoryMatchWieght != null && fixedValueWight != null) {
                    trust = (categoryMatching * categoryMatchWieght + fixedValue * fixedValueWight) / (categoryMatchWieght + fixedValueWight);
                } else {
                    trust = (categoryMatching + fixedValue) / 2;
                }
            } else {
                trust = categoryMatching;
            }
        } else if (fixedValue != null) {
            trust = fixedValue;
        } else {
            trust = 0.0;
        }
        return trust;
    }

    public Double categoryCommunityMatching(Community source, Community destination) {
        return 1.0;
    }

    public Community getDestinationCommunity() {
        return destinationCommunity;
    }

    public void setDestinationCommunity(Community destinationCommunity) {
        this.destinationCommunity = destinationCommunity;
    }

    public Community getCommunity() {
        return community;
    }

    public void setCommunity(Community community) {
        this.community = community;
    }

    public Metric getMetric() {
        return metric;
    }

    public void setMetric(Metric metric) {
        this.metric = metric;
    }

    public Double getTrust() {
        return trust;
    }

    public void setTrust(Double trust) {
        this.trust = trust;
    }

    public Metric getDestinationMetric() {
        return destinationMetric;
    }

    public void setDestinationMetric(Metric destinationMetric) {
        this.destinationMetric = destinationMetric;
    }
}
