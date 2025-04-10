public class Test {    private void addNextItem(ScheduleItem schItem, Element item, Document doc) {
        Element nextItem = doc.createElement("item");
        Element name = null;
        Text text = null;
        name = doc.createElement("name");
        text = doc.createTextNode(schItem.getName());
        name.appendChild(text);
        nextItem.appendChild(name);
        Calendar start = Calendar.getInstance();
        start.setTime(schItem.getStart());
        name = doc.createElement("start");
        int hour = start.get(Calendar.HOUR_OF_DAY);
        if (hour > 12) hour = hour - 12; else if (hour == 0) hour = 12;
        name.setAttribute("hour_12", intToXchar(hour, 2));
        name.setAttribute("hour_24", intToXchar(start.get(Calendar.HOUR_OF_DAY), 2));
        name.setAttribute("minute", intToXchar(start.get(Calendar.MINUTE), 2));
        if (start.get(Calendar.AM_PM) == Calendar.AM) name.setAttribute("am_pm", "am"); else name.setAttribute("am_pm", "pm");
        nextItem.appendChild(name);
        name = doc.createElement("id");
        text = doc.createTextNode(schItem.toString());
        name.appendChild(text);
        nextItem.appendChild(name);
        name = doc.createElement("duration");
        text = doc.createTextNode(new Integer(schItem.getDuration()).toString());
        name.appendChild(text);
        nextItem.appendChild(name);
        name = doc.createElement("channel");
        text = doc.createTextNode(schItem.getChannel());
        name.appendChild(text);
        nextItem.appendChild(name);
        name = doc.createElement("status");
        text = doc.createTextNode(schItem.getStatus());
        name.appendChild(text);
        nextItem.appendChild(name);
        Date now = new Date();
        long timeLeft = schItem.getStart().getTime() - now.getTime();
        long days = timeLeft / (1000 * 60 * 60 * 24);
        long hours = (timeLeft - (days * 1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
        long min = (timeLeft - (days * 1000 * 60 * 60 * 24) - (hours * 1000 * 60 * 60)) / (1000 * 60);
        long seconds = (timeLeft - (days * 1000 * 60 * 60 * 24) - (hours * 1000 * 60 * 60) - (min * 1000 * 60)) / 1000;
        name = doc.createElement("time_to_action");
        name.setAttribute("days", new Long(days).toString());
        name.setAttribute("hours", new Long(hours).toString());
        name.setAttribute("minutes", new Long(min).toString());
        name.setAttribute("seconds", new Long(seconds).toString());
        nextItem.appendChild(name);
        item.appendChild(nextItem);
    }
}