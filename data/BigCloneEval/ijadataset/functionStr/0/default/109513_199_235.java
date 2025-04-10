public class Test {    private void addNowRunningItem(ScheduleItem schItem, Element item, Document doc) {
        Element runnintItem = null;
        Element name = null;
        Text text = null;
        runnintItem = doc.createElement("item");
        name = doc.createElement("name");
        text = doc.createTextNode(schItem.getName());
        name.appendChild(text);
        runnintItem.appendChild(name);
        Calendar start = Calendar.getInstance();
        start.setTime(schItem.getStart());
        name = doc.createElement("start");
        int hour = start.get(Calendar.HOUR_OF_DAY);
        if (hour > 12) hour = hour - 12; else if (hour == 0) hour = 12;
        name.setAttribute("hour_12", intToXchar(hour, 2));
        name.setAttribute("hour_24", intToXchar(start.get(Calendar.HOUR_OF_DAY), 2));
        name.setAttribute("minute", intToXchar(start.get(Calendar.MINUTE), 2));
        if (start.get(Calendar.AM_PM) == Calendar.AM) name.setAttribute("am_pm", "am"); else name.setAttribute("am_pm", "pm");
        runnintItem.appendChild(name);
        name = doc.createElement("id");
        text = doc.createTextNode(schItem.toString());
        name.appendChild(text);
        runnintItem.appendChild(name);
        name = doc.createElement("channel");
        text = doc.createTextNode(schItem.getChannel());
        name.appendChild(text);
        runnintItem.appendChild(name);
        name = doc.createElement("duration");
        text = doc.createTextNode(new Integer(schItem.getDuration()).toString());
        name.appendChild(text);
        runnintItem.appendChild(name);
        name = doc.createElement("status");
        text = doc.createTextNode(schItem.getStatus());
        name.appendChild(text);
        runnintItem.appendChild(name);
        item.appendChild(runnintItem);
    }
}