import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;

public class SetMissions extends DBLoader {

    int _missionID = 0;

    Random _randMemId;

    Random _randDateDay;

    Random _randDateMonth;

    ArrayList<String> _cols;

    ArrayList<String> _vals;

    HashSet<Integer> _alreadySelected;

    private int _totNumOfTeams;

    public SetMissions() {
        _cols = new ArrayList<String>();
        _vals = new ArrayList<String>();
        _alreadySelected = new HashSet<Integer>();
    }

    /** Loads all the names from 'fileName' into db.table name (Users)
	 * @param fileName - input file - for the team members names
	 * Check in the config file : dbNamesSrc = names.txt
	 */
    public void loadAllMembers(String fileName) {
        initCounters();
        readDataSrc(fileName);
    }

    private void initCounters() {
        _totNumOfTeams = (int) (LoadTeamMembers._lastUsrID / 30) + 1;
        _randMemId = new Random(_totNumOfTeams);
        _randDateDay = new Random(26);
        _randDateMonth = new Random(10);
    }

    /**
	 * take a name, split it to first + last name and randomize a position for this pearson
	 * then it insert it to the DB
	 */
    @Override
    void parseLine(String strLine) {
        String dest = strLine;
        int groupID = _randMemId.nextInt(_totNumOfTeams + 1);
        _missionID++;
        int month = _randDateMonth.nextInt(10);
        int day = _randDateDay.nextInt(26);
        String startDate = gerateDate(month, day);
        String endDate = gerateDate(month + 1, day + 1);
        _cols.clear();
        _vals.clear();
        _cols.add("MissionID");
        _cols.add("GroupID");
        _cols.add("StartDate");
        _cols.add("EndDate");
        _cols.add("Location");
        _cols.add("State");
        _vals.add("'" + _missionID + "'");
        _vals.add("'" + groupID + "'");
        _vals.add("'" + startDate + "'");
        _vals.add("'" + endDate + "'");
        _vals.add("'" + dest + "'");
        _vals.add("'" + "NA" + "'");
        genSQLInsertForTable("missions", _cols, _vals);
    }

    private final String gerateDate(int month, int day) {
        Calendar cdr = Calendar.getInstance();
        cdr.set(Calendar.MONTH, month);
        cdr.set(Calendar.HOUR_OF_DAY, day);
        cdr.set(Calendar.MINUTE, 0);
        cdr.set(Calendar.SECOND, 0);
        long val1 = cdr.getTimeInMillis();
        cdr.set(Calendar.MONTH, 12);
        cdr.set(Calendar.HOUR_OF_DAY, 28);
        cdr.set(Calendar.MINUTE, 0);
        cdr.set(Calendar.SECOND, 0);
        long val2 = cdr.getTimeInMillis();
        Random r = new Random();
        long randomTS = (long) (r.nextDouble() * (val2 - val1)) + val1;
        Date date = new Date(randomTS);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentTime = sdf.format(date);
        return currentTime;
    }
}
