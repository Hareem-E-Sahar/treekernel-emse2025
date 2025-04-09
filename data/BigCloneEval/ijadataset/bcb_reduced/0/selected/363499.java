package gridsim;

import gridsim.scheduler.QuotaScheduler;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

/**
 *
 * @author Rafael Silva
 */
public class Workload {

    private final int MIN_TASKS = 1;

    private final int MAX_TASKS = 5;

    private final int MIN_DATA_SIZE = 10;

    private final int MAX_DATA_SIZE = 512;

    private final int MIN_RUNTIME = 300;

    private final int MAX_RUNTIME = 3000;

    private final int SUBMISSION_DELAY = 200;

    private File file;

    private Connection conn;

    private PreparedStatement stat;

    private Random random;

    private ResultSet rs;

    private int compareCode;

    public Workload(String fileName) {
        this.file = new File(fileName);
    }

    public void prepareToRead(int compareCode) throws Exception {
        this.compareCode = compareCode;
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Missing SQLite JDBC library.");
        }
        conn = DriverManager.getConnection("jdbc:sqlite:" + file.getPath());
        Statement stat = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.CLOSE_CURSORS_AT_COMMIT);
        conn.setAutoCommit(true);
        rs = stat.executeQuery("SELECT JobID, SubmitTime, RunTime, DataId, DataSize, UserId FROM Jobs ORDER BY SubmitTime;");
    }

    public Job getJob() throws Exception {
        if (!rs.next()) {
            return null;
        }
        return new Job(rs.getInt("JobID"), rs.getInt("SubmitTime"), rs.getInt("RunTime"), rs.getInt("DataId"), rs.getInt("DataSize"), this.compareCode, rs.getInt("UserId"));
    }

    public void generate(int numOfJobs, double reuse) throws Exception {
        this.createDatabase();
        int submitTime = 0;
        int threshold = numOfJobs - (int) (numOfJobs * (reuse / 100));
        int reuseLimit = numOfJobs - threshold;
        System.out.println("-- LIMIAR: " + threshold);
        System.out.println("-- REUSE LIMIT: " + reuseLimit);
        int[] datas = new int[threshold];
        for (int i = 0; i < threshold; i++) {
            random = new Random(System.nanoTime());
            datas[i] = MIN_DATA_SIZE + random.nextInt(MAX_DATA_SIZE - MIN_DATA_SIZE);
        }
        int id = 1;
        int count = 0;
        int dataId = 1;
        for (int i = 0; i < threshold; i++) {
            try {
                random = new Random(System.nanoTime());
                int userId = 1 + random.nextInt(QuotaScheduler.NUMBER_OF_USERS);
                this.addStat(id++, submitTime, dataId, datas[dataId - 1], userId);
                if (count++ == 1024) {
                    stat.executeBatch();
                    conn.commit();
                    count = 0;
                }
                submitTime += SUBMISSION_DELAY;
                dataId++;
            } catch (SQLException sqlException) {
                sqlException.printStackTrace();
            }
        }
        for (int i = 0; i < reuseLimit; ) {
            random = new Random(System.nanoTime());
            dataId = 1 + random.nextInt(threshold);
            int randomSubmitTime = random.nextInt(submitTime);
            int tasks = MIN_TASKS + random.nextInt(MAX_TASKS - MIN_TASKS);
            int userId = 1 + random.nextInt(QuotaScheduler.NUMBER_OF_USERS);
            for (int j = 0; j < tasks && i < reuseLimit; j++, i++) {
                this.addStat(id++, randomSubmitTime, dataId, datas[dataId - 1], userId);
                if (count++ == 1024) {
                    stat.executeBatch();
                    conn.commit();
                    count = 0;
                }
            }
        }
        this.close();
    }

    public void generate(int SEsize, int type) throws Exception {
        this.createDatabase();
        int[] quotas = new int[QuotaScheduler.NUMBER_OF_USERS];
        int quota = SEsize / QuotaScheduler.NUMBER_OF_USERS;
        int newQuota = quota + (quota * 70) / 100;
        int diffQuota = quota - (quota * 30) / 100;
        int id = 1;
        int count = 0;
        int dataId = 1;
        try {
            while (true) {
                random = new Random(System.nanoTime());
                int userId = 1 + random.nextInt(QuotaScheduler.NUMBER_OF_USERS);
                int tasks = MIN_TASKS + random.nextInt(MAX_TASKS - MIN_TASKS);
                int submitTime = 0 + random.nextInt(90000);
                int dataSize = MIN_DATA_SIZE + random.nextInt(MAX_DATA_SIZE - MIN_DATA_SIZE);
                taskfor: for (int i = 0; i < tasks; i++) {
                    int userQuota = quotas[userId - 1];
                    switch(type) {
                        case 1:
                            if (userQuota < quota && quota - userQuota >= dataSize) {
                                this.addStat(id++, submitTime, dataId, dataSize, userId);
                            } else {
                                break taskfor;
                            }
                            break;
                        case 2:
                            if (userQuota < newQuota && newQuota - userQuota >= dataSize) {
                                this.addStat(id++, submitTime, dataId, dataSize, userId);
                            } else {
                                break taskfor;
                            }
                            break;
                        case 3:
                            if (userId == 1) {
                                if (userQuota < newQuota && newQuota - userQuota >= dataSize) {
                                    this.addStat(id++, submitTime, dataId, dataSize, userId);
                                } else {
                                    break taskfor;
                                }
                            } else if (userQuota < diffQuota && diffQuota - userQuota >= dataSize) {
                                this.addStat(id++, submitTime, dataId, dataSize, userId);
                            } else {
                                break taskfor;
                            }
                            break;
                    }
                }
                if (count++ >= 1024) {
                    stat.executeBatch();
                    conn.commit();
                    count = 0;
                }
                quotas[userId - 1] += dataSize;
                dataId++;
                boolean finished = true;
                finishFor: for (int i = 0; i < QuotaScheduler.NUMBER_OF_USERS; i++) {
                    switch(type) {
                        case 1:
                            if (quotas[i] < quota - MIN_DATA_SIZE) {
                                finished = false;
                                break finishFor;
                            }
                            break;
                        case 2:
                            if (quotas[i] < newQuota - MIN_DATA_SIZE) {
                                finished = false;
                                break finishFor;
                            }
                            break;
                        case 3:
                            if (i == 0 && quotas[0] < newQuota - MAX_DATA_SIZE) {
                                finished = false;
                                break finishFor;
                            } else if (quotas[i] < diffQuota - MAX_DATA_SIZE) {
                                finished = false;
                                break finishFor;
                            }
                            break;
                    }
                }
                if (finished) {
                    break;
                }
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        this.close();
    }

    private void addStat(int id, int submitTime, int dataId, int dataSize, int userId) throws SQLException {
        int runTime = MIN_RUNTIME + random.nextInt(MAX_RUNTIME - MIN_RUNTIME);
        stat.setInt(1, id);
        stat.setInt(2, submitTime);
        stat.setInt(3, runTime);
        stat.setInt(4, dataId);
        stat.setInt(5, dataSize);
        stat.setInt(6, userId);
        stat.addBatch();
    }

    private void createDatabase() throws SQLException {
        if (file.exists()) {
            file.delete();
        }
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Missing SQLite JDBC library.");
        }
        conn = DriverManager.getConnection("jdbc:sqlite:" + file.getPath());
        Statement stat = conn.createStatement();
        conn.setAutoCommit(false);
        try {
            stat.execute("DROP TABLE Jobs;");
            conn.commit();
        } catch (Exception e) {
        }
        try {
            stat.execute("CREATE TABLE Jobs (JobID INTEGER, SubmitTime INTEGER, RunTime INTEGER, DataID INTEGER, DataSize INTEGER, UserId INTEGER);");
            conn.commit();
        } catch (Exception e) {
        }
        this.stat = conn.prepareStatement("INSERT INTO Jobs VALUES(?, ?, ?, ?, ?, ?)");
    }

    private void close() throws SQLException {
        stat.executeBatch();
        conn.commit();
        conn.close();
    }
}
