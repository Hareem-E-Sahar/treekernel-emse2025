public class Test {    protected void doNewTrain() {
        Train newTrain = new Train();
        newTrain.trainNameFull = "XXXX/YYYY";
        newTrain.trainNameDown = "DDDD";
        newTrain.trainNameUp = "UUUU";
        newTrain.stopNum = 3;
        newTrain.stops[0] = new Stop(_("Departure"), "00:00", "00:00", false);
        newTrain.stops[1] = new Stop(_("Middle"), "00:00", "00:00", false);
        newTrain.stops[2] = new Stop(_("Terminal"), "00:00", "00:00", false);
        TrainDialog dialog = new TrainDialog(mainFrame, newTrain);
        dialog.editTrain();
        if (!dialog.isCanceled) {
            Train addingTrain = dialog.getTrain();
            if (chart.isLoaded(addingTrain)) {
                if (new YesNoBox(mainFrame, String.format(_("%s is already in the graph. Overwrite?"), addingTrain.getTrainName())).askForYes()) chart.updateTrain(addingTrain);
            } else {
                chart.addTrain(addingTrain);
            }
            table.revalidate();
            mainFrame.chartView.repaint();
            mainFrame.runView.refresh();
        }
    }
}