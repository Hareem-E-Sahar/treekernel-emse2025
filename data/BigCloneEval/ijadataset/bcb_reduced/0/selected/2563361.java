package utils.video.filters.rearingdetection;

import utils.PManager.ProgramState;
import utils.video.filters.FilterConfigs;
import utils.video.filters.Link;
import utils.video.filters.VideoFilter;

/**
 * Detects whether the rat is rearing or not.
 * 
 * @author Creative
 */
public class RearingDetector extends VideoFilter<RearingFilterConfigs, RearingFilterData> {

    /**
     * Initializes the filter.
     * 
     * @param name
     *            filter's name
     * @param linkIn
     *            input Link for the filter
     * @param linkOut
     *            output Link from the filter
     */
    public RearingDetector(final String name, final Link linkIn, final Link linkOut) {
        super(name, linkIn, linkOut);
        configs = configs;
        filterData = new RearingFilterData("Rearing Data");
        gui = new RearingDetectorGUI(this);
        filterData.setRearing(false);
    }

    @Override
    public boolean configure(final FilterConfigs configs) {
        this.configs = (RearingFilterConfigs) configs;
        return super.configure(configs);
    }

    private boolean rearing_now;

    private int normal_rat_area;

    private boolean is_rearing;

    private int current_rat_area;

    /**
     * Used to train the filter of the white area of the rat when
     * (walking/rearing).
     * 
     * @param rearing
     *            whether the rat is rearing now or not
     */
    public void rearingNow(final boolean rearing) {
        if (rearing) {
            rearing_now = true;
            configs.rearing_thresh = (normal_rat_area + current_rat_area) / 2;
            System.out.print("Rearing threshold: " + configs.rearing_thresh + "\n");
        } else {
            rearing_now = false;
            final Thread th_rearing = new Thread(new NormalRatAreaThread());
            th_rearing.start();
            System.out.print("Rearing Training Started" + "\n");
        }
    }

    /**
     * Runnable to calculate the mean rat area.
     * 
     * @author Creative
     */
    private class NormalRatAreaThread implements Runnable {

        private static final long rat_area_training_time = 3;

        @Override
        public void run() {
            final long timer_start = (System.currentTimeMillis() / 1000);
            int tmp_rat_area_sum = 0, num_samples = 0;
            while (!rearing_now && (System.currentTimeMillis() / 1000) - timer_start < rat_area_training_time) {
                num_samples++;
                tmp_rat_area_sum += current_rat_area;
                try {
                    Thread.sleep(400);
                } catch (final InterruptedException e) {
                }
            }
            rearing_now = false;
            normal_rat_area = tmp_rat_area_sum / num_samples;
            System.out.print("normal rat area: " + normal_rat_area + "\n");
        }
    }

    @Override
    public void process() {
        final int[] imageData = link_in.getData();
        if (configs.enabled) if (imageData != null) {
            int white_area = 0;
            for (int x = configs.ref_center_point.x - (configs.margin_x / 2); x < configs.ref_center_point.x + (configs.margin_x / 2); x++) for (int y = configs.ref_center_point.y - (configs.margin_y / 2); y < configs.ref_center_point.y + (configs.margin_y / 2); y++) if (x < configs.common_configs.width & x >= 0 & y < configs.common_configs.height & y >= 0) if (imageData[x + y * configs.common_configs.width] == 0x00FFFFFF) white_area++;
            current_rat_area = white_area;
            if (current_rat_area < configs.rearing_thresh) is_rearing = true; else is_rearing = false;
        }
        filterData.setRearing(is_rearing);
    }

    @Override
    public void updateProgramState(final ProgramState state) {
    }
}
