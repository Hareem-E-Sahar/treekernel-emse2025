public class Test {            public void actionPerformed(ActionEvent ae) {
                TimeRange timeRange = RBNBHelper.getChannelsTimeRange();
                double time = DateTimeDialog.showDialog(ControlPanel.this, rbnbController.getLocation(), timeRange.start, timeRange.end);
                if (time >= 0) {
                    rbnbController.setLocation(time);
                }
            }
}