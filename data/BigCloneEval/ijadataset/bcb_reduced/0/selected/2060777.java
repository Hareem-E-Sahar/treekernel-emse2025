package com.gusto.engine.recommend.prediction;

import org.apache.log4j.Logger;
import com.gusto.engine.colfil.Prediction;
import com.gusto.engine.recommend.PredictionService;
import com.gusto.engine.recommend.prediction.base.BaseImpl;

/**
 * <p>Mean algorithm, returns whether the userMean, itemMean, or both user and item mean.</p>
 * 
 * @author amokrane.belloui@gmail.com
 *
 */
public class MeanImpl extends BaseImpl implements PredictionService {

    private Logger log = Logger.getLogger(getClass());

    private boolean userMean;

    public void setUserMean(boolean userMean) {
        this.userMean = userMean;
    }

    private boolean itemMean;

    public void setItemMean(boolean itemMean) {
        this.itemMean = itemMean;
    }

    public Prediction predict(long userId, long itemId, boolean includePrediction) {
        long start = System.currentTimeMillis();
        Double user_mean = (this.userMean ? collaborativeService.getUserMeanRating(userId) : null);
        Double item_mean = (this.itemMean ? collaborativeService.getItemMeanRating(itemId) : null);
        Double val = null;
        if (userMean) {
            if (itemMean) {
                val = (user_mean + item_mean) / 2;
                log.debug("Mean user/item prediction " + val);
            } else {
                val = user_mean;
                log.debug("Mean user prediction " + val);
            }
        } else {
            if (itemMean) {
                val = item_mean;
                log.debug("Mean item prediction " + val);
            }
        }
        logPrediction(0, 0, 0, (System.currentTimeMillis() - start), "PPPP");
        return returnPrediction(userId, itemId, val);
    }
}
