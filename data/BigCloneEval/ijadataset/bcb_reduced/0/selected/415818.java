package org.swiftgantt.core.layout;

import java.util.Collection;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.swiftgantt.core.Config;
import org.swiftgantt.core.GanttContext;
import org.swiftgantt.core.Time;
import org.swiftgantt.core.TimeUnit;
import org.swiftgantt.core.adapter.CanvasAdapter;
import org.swiftgantt.core.adapter.GanttColor;
import org.swiftgantt.core.adapter.GanttPoint;
import org.swiftgantt.core.adapter.GanttPolygon;
import org.swiftgantt.core.adapter.GanttRectangle;
import org.swiftgantt.core.adapter.ShapeAdapter;
import org.swiftgantt.core.common.PaintLogger;
import org.swiftgantt.core.layout.strategy.LayoutStrategy;
import org.swiftgantt.core.model.Task;
import org.swiftgantt.core.model.TaskModel.TraverseCallback;
import org.swiftgantt.core.timeaxis.Paintable;

/**
 * The non-accurate renderer for tasks. Rendering task's shape inaccurately in Gantt Chart.<br/>
 * <code>createDiamondShape</code> method for painting diamond shape
 * 
 * @author Yuxing Wang
 * @version 1.0
 */
public class TaskRenderer implements Paintable {

    private static final int DEBUG_PAUSE_TIME = 1;

    protected PaintLogger log;

    protected boolean isDebug = false;

    protected int step_length = 0;

    protected int row_height = 0;

    protected int task_bar_height = 0;

    protected int progress_bar_height = 0;

    protected int padding_v = 0;

    protected CanvasAdapter canvas = null;

    protected GanttRectangle bigRectangle = null;

    protected GanttContext context;

    protected int connectorStyle = 1;

    protected List<LayoutTask> allTasksByDFS = null;

    protected LayoutStrategy layoutStrategy;

    public TaskRenderer(GanttContext context) {
        log = new PaintLogger(this.getClass());
        this.context = context;
        this.layoutStrategy = context.getLayoutStrategy();
    }

    public void paint(CanvasAdapter canvas, Object paintedObject, GanttRectangle rec) {
        this.canvas = canvas;
        step_length = this.context.getConfig().getTimeUnitWidth();
        row_height = this.context.getConfig().getGanttChartRowHeight();
        task_bar_height = this.context.getConfig().getTaskBarHeight();
        progress_bar_height = this.context.getConfig().getProgressBarHeight();
        padding_v = (row_height - task_bar_height) / 2;
        LayoutModel model = context.getGanttModel();
        if (model == null) {
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug(StringUtils.center("Start to render all " + model.getTasksCount() + " tasks.", 100, "-"));
            model.logModelStructure();
            log.debug(StringUtils.center("End render all tasks.", 100, "-"));
        }
        context.getTaskLocationManager().clear();
        allTasksByDFS = model.getTasksByDFS();
        bigRectangle = new GanttRectangle(0, 0, rec.width, rec.height);
        final int[] selectedTaskIndices = model.getSelectedIds();
        LayoutTask curTask;
        model.traverseByDFS(new TraverseCallback() {

            @Override
            public boolean onEachTask(Task task, int i) {
                LayoutTask curTask = (LayoutTask) task;
                int x = calcTaskStartPointX(curTask, bigRectangle);
                int y = calcTaskPointY(bigRectangle, i, padding_v);
                boolean isMilestone = isTaskMilestone(context.getTimeUnit(), curTask);
                if (isMilestone) {
                    log.debug("Paint  as milestone.");
                    drawMilestone(x, y, curTask);
                } else {
                    if (curTask.isLeaf() == true) {
                        int taskSteps = layoutStrategy.calcTaskSteps(curTask);
                        int pgsSteps = layoutStrategy.calcProgressSteps(curTask);
                        int taskLen = taskSteps * step_length;
                        int pgsLen = pgsSteps * step_length;
                        if (log.isDebugEnabled()) {
                            log.debug("Paint " + taskSteps + "steps task: " + curTask);
                        }
                        drawLeafTask(x, y, taskLen, pgsLen, curTask);
                    } else {
                        log.debug("Paint as task group.");
                        LayoutTask earlistTaskInGroup = curTask.getEarliestTask();
                        LayoutTask latestTaskInGroup = curTask.getLatestTask();
                        int steps = layoutStrategy.getTimeInterval(earlistTaskInGroup.getActualStart(), latestTaskInGroup.getActualEnd());
                        int radius = context.getConfig().getTaskBarHeight() / 2;
                        float barLength = (steps + 1) * step_length;
                        int actualLen = (int) barLength + radius * 2;
                        drawTaskGroup(x, y, actualLen, curTask);
                    }
                }
                if (!curTask.getPredecessors().isEmpty()) {
                    drawConnectToPredecesor(curTask);
                }
                if (ArrayUtils.contains(selectedTaskIndices, curTask.getId())) {
                    drawSelectedRow(i, bigRectangle.width);
                }
                return true;
            }
        });
    }

    protected boolean isTaskMilestone(TimeUnit tu, LayoutTask curTask) {
        boolean isMilestone;
        if (log.isDebugEnabled()) {
            log.debug("actualTimeInterval:" + layoutStrategy.getActualTimeInterval(curTask));
            try {
                Thread.sleep(DEBUG_PAUSE_TIME);
            } catch (InterruptedException ex) {
                log.error(ex.getLocalizedMessage());
            }
        }
        isMilestone = (layoutStrategy.getActualTimeInterval(curTask) <= 0);
        return isMilestone;
    }

    protected void drawConnectToPredecesor(LayoutTask curTask) {
        if (log.isDebugEnabled()) {
            log.debug("Paint connector lines from task: " + curTask + " to all " + curTask.getPredecessors().size() + " predecessors");
            try {
                Thread.sleep(DEBUG_PAUSE_TIME);
            } catch (InterruptedException ex) {
                log.error(ex.getLocalizedMessage());
            }
        }
        List<LayoutTask> preds = curTask.getPredecessors();
        for (int i = 0; i < preds.size(); i++) {
            LayoutTask pred = preds.get(i);
            float distance = layoutStrategy.getTimeInterval(pred) - 1;
            if (log.isDebugEnabled()) {
                log.debug("Distance from predecessor to task is " + distance);
            }
            int startX = calcTaskStartPointX(pred, bigRectangle);
            int endX = calcTaskEndPointX(pred, startX);
            if (distance > 0) {
                this.drawDistantConnectToPredecessor(endX, curTask, pred, distance);
            } else {
                this.drawCloseConnectToPredecessor(endX, curTask, pred);
            }
        }
    }

    protected void drawCloseConnectToPredecessor(int predEndX, LayoutTask tw, LayoutTask pred) {
        int offset = 10;
        int thisIndex = allTasksByDFS.indexOf(tw);
        int predIndex = allTasksByDFS.indexOf(pred);
        int y = calcTaskPointY(bigRectangle, predIndex, padding_v);
        if (log.isDebugEnabled()) {
            log.debug("" + bigRectangle + " " + predIndex + " " + padding_v);
        }
        GanttPoint ps = new GanttPoint(predEndX, y + task_bar_height / 2);
        GanttPoint p1 = new GanttPoint(predEndX + offset, ps.y);
        GanttPoint p2 = new GanttPoint(p1.x, ps.y + row_height / 2);
        GanttPoint p3 = new GanttPoint(predEndX - offset, p2.y);
        GanttPoint p4 = new GanttPoint(p3.x, y + task_bar_height / 2 + (thisIndex - predIndex) * row_height);
        GanttPoint pe = new GanttPoint(predEndX, p4.y);
        canvas.drawLine(ps.x, ps.y, p1.x, p1.y);
        canvas.drawLine(p1.x, p1.y, p2.x, p2.y);
        canvas.drawLine(p2.x, p2.y, p3.x, p3.y);
        canvas.drawLine(p3.x, p3.y, p4.x, p4.y);
        canvas.drawLine(p4.x, p4.y, pe.x, pe.y);
        canvas.drawLine(pe.x - 5, pe.y + 3, pe.x, pe.y);
        canvas.drawLine(pe.x - 5, pe.y - 3, pe.x, pe.y);
    }

    protected void drawDistantConnectToPredecessor(int predEndX, LayoutTask t, LayoutTask pred, float distance) {
        int offset = 10;
        int thisIndex = allTasksByDFS.indexOf(t);
        int predIndex = allTasksByDFS.indexOf(pred);
        int y = calcTaskPointY(bigRectangle, predIndex, padding_v);
        GanttPoint ps = new GanttPoint(predEndX, y + task_bar_height / 2);
        GanttPoint pe = new GanttPoint(predEndX + (int) (distance * step_length), y + task_bar_height / 2 + (thisIndex - predIndex) * row_height);
        GanttPoint p1 = null;
        GanttPoint p2 = null;
        if (this.connectorStyle == 1) {
            p1 = new GanttPoint(ps.x + offset, ps.y);
            p2 = new GanttPoint(p1.x, pe.y);
        } else if (this.connectorStyle == 2) {
            p1 = new GanttPoint(pe.x - offset, ps.y);
            p2 = new GanttPoint(p1.x, pe.y);
        }
        canvas.drawLine(ps.x, ps.y, p1.x, p1.y);
        canvas.drawLine(p1.x, p1.y, p2.x, p2.y);
        canvas.drawLine(p2.x, p2.y, pe.x, pe.y);
        canvas.drawLine(pe.x - 5, pe.y + 3, pe.x, pe.y);
        canvas.drawLine(pe.x - 5, pe.y - 3, pe.x, pe.y);
    }

    protected void drawDebugLocations() {
        if (log.isDebugEnabled()) {
            canvas.setColor(GanttColor.black);
            Collection<ShapeAdapter> ss = context.getTaskLocationManager().getAllBoundLocations();
            for (ShapeAdapter t : ss) {
                if (t instanceof GanttRectangle) {
                    canvas.drawRect((GanttRectangle) t);
                } else if (t instanceof GanttPolygon) {
                }
            }
        }
    }

    /**
	 * Leaf task is the lowest level task in the task tree.
	 * @param x
	 * @param y
	 * @param tasklen
	 * @param pgsLen
	 * @param task
	 */
    protected void drawLeafTask(int x, int y, int taskLen, int pgsLen, LayoutTask curTask) {
        if (log.isInfoEnabled()) {
            log.info("Paint task " + curTask + " at [" + x + "," + y + "], length is " + taskLen);
            try {
                Thread.sleep(DEBUG_PAUSE_TIME);
            } catch (InterruptedException ex) {
                log.error(ex.getLocalizedMessage());
            }
        }
        GanttColor taskBarBackcolor = curTask.getBackcolor() != null ? curTask.getBackcolor() : context.getConfig().getTaskBarBackColor();
        GanttRectangle taskRect = new GanttRectangle(x, y, (int) taskLen, task_bar_height);
        canvas.setColor(taskBarBackcolor, 255);
        canvas.fillRect(taskRect);
        if (pgsLen > 0) {
            GanttColor pgsBarColor = context.getConfig().getProgressBarBackColor();
            int pgsY = y + (task_bar_height - progress_bar_height) / 2;
            GanttRectangle pgsRect = new GanttRectangle(x, pgsY, (int) pgsLen, progress_bar_height);
            if (log.isDebugEnabled()) {
                log.debug(pgsRect, "Paint progress with progress " + pgsLen);
            }
            canvas.setColor(pgsBarColor);
            canvas.fillRect(pgsRect);
        }
        if (ArrayUtils.contains(this.context.getGanttModel().getSelectedIds(), curTask.getId())) {
            drawSelection(taskRect);
        }
        if (context.getConfig().isShowTaskInfoBehindTaskBar()) {
            String taskInfo = this.isDebug ? curTask.toSimpleString() : curTask.getName();
            int startX = x + (int) taskLen + 16;
            int actualY = y + 12;
            canvas.setColor(GanttColor.black);
            canvas.drawChars(taskInfo.toCharArray(), 0, taskInfo.length(), startX, actualY);
        }
        context.getTaskLocationManager().bindTaskAndShape(curTask, taskRect);
    }

    protected void drawMilestone(int x, int y, LayoutTask curTask) {
        if (log.isInfoEnabled()) {
            log.info("Paint milestone task " + curTask + " at [" + x + "," + y + "]");
            try {
                Thread.sleep(DEBUG_PAUSE_TIME);
            } catch (InterruptedException ex) {
                log.error(ex.getLocalizedMessage());
            }
        }
        int width = step_length;
        int height = context.getConfig().getTaskBarHeight();
        GanttPolygon poly = createDiamondShape(x + width / 2, y - padding_v, width, height);
        canvas.setColor(context.getConfig().getTaskGroupBarBackColor());
        canvas.fillPolygon(poly);
        if (context.getConfig().isShowTaskInfoBehindTaskBar()) {
            String taskInfo = this.isDebug ? curTask.toSimpleString() : curTask.getName();
            int startX = x + width + 16;
            int actualY = y + 12;
            canvas.setColor(GanttColor.black);
            canvas.drawChars(taskInfo.toCharArray(), 0, taskInfo.length(), startX, actualY);
        }
    }

    /**
	 * Draw non-leaf task, which is the task group. Adjusted the end time by the last sub task
	 */
    protected void drawTaskGroup(int x, int y, int totalLen, LayoutTask task) {
        String taskInfo = this.isDebug ? task.toSimpleString() : task.getName();
        if (log.isInfoEnabled()) {
            log.info("Paint task group " + taskInfo + " at [" + x + "," + y + "], length is " + totalLen);
            try {
                Thread.sleep(DEBUG_PAUSE_TIME);
            } catch (InterruptedException ex) {
                log.error(ex.getLocalizedMessage());
            }
        }
        int radius = context.getConfig().getTaskBarHeight() / 2;
        canvas.setColor(context.getConfig().getTaskGroupBarBackColor());
        GanttPolygon leftPoly = createDiamondShape(x, y, radius);
        canvas.fillPolygon(leftPoly);
        GanttRectangle barRect = new GanttRectangle(x - radius, y, totalLen, radius);
        canvas.fillRect(barRect);
        GanttPolygon rightPoly = createDiamondShape(x + totalLen - radius * 2, y, radius);
        canvas.fillPolygon(rightPoly);
        canvas.setColor(GanttColor.black);
        if (context.getConfig().isShowTaskInfoBehindTaskBar()) {
            canvas.drawChars(taskInfo.toCharArray(), 0, taskInfo.length(), x + totalLen, y + 8);
        }
        context.getTaskLocationManager().bindTaskAndShape(task, barRect);
    }

    protected void drawSelectedRow(int row, int width) {
        GanttColor selectionColor = context.getConfig().getSelectionColor();
        canvas.setColor(selectionColor, 80);
        canvas.fillRect(0, row_height * row, width, row_height);
        canvas.setColor(GanttColor.DARK_GRAY);
        canvas.drawRect(0, row_height * row, width, row_height);
    }

    protected void drawSelection(ShapeAdapter shape) {
        if (true) {
            return;
        }
        GanttColor selectionColor = context.getConfig().getSelectionColor();
        canvas.setColor(selectionColor, 130);
        if (shape instanceof GanttRectangle) {
            GanttRectangle r = (GanttRectangle) shape;
            int x = r.x;
            int y = r.y;
            canvas.fillRect(x, y + bigRectangle.y, r.width, r.height);
            canvas.setColor(GanttColor.DARK_GRAY);
            canvas.drawRect(x - 1, y + this.bigRectangle.y - 1, r.width + 1, r.height + 1);
        } else if (shape instanceof GanttPolygon) {
        }
    }

    /**
	 * Calculate the start point's X of task.
	 * @param task
	 * @param rect The big rectangle of Gantt chart.
	 * @return
	 */
    public int calcTaskStartPointX(LayoutTask task, GanttRectangle rect) {
        if (log.isDebugEnabled()) {
            log.debug("    Calculate X for Start time of task: " + this);
        }
        Config config = context.getConfig();
        Time startTime = null;
        if (task.isLeaf() == true) {
            startTime = task.getActualStart();
        } else {
            startTime = task.getEarliestSubTask().getActualStart();
            if (startTime.after(task.getActualStart())) {
                startTime = task.getActualStart();
            }
        }
        int intervalToKickoff = layoutStrategy.getTimeInterval(context.getGanttModel().getKickoffTime(), startTime);
        int x = (intervalToKickoff + config.getBlankStepsToKickoffTime()) * config.getTimeUnitWidth();
        if (log.isDebugEnabled()) {
            log.debug("      = " + intervalToKickoff + "*" + config.getTimeUnitWidth() + " + (kickoff point x) = " + x);
        }
        return x + rect.x;
    }

    /**
	 * Calculate X of the end point of task
	 *
	 * @param task
	 * @param startPX
	 * @return
	 */
    public int calcTaskEndPointX(LayoutTask task, int startPX) {
        if (log.isDebugEnabled()) {
            log.debug("    Calculate X for End time of task: " + this);
        }
        int ret = 0;
        ret = startPX + layoutStrategy.calcTaskSteps(task) * context.getConfig().getTimeUnitWidth();
        if (log.isDebugEnabled()) {
            log.debug("      = " + ret);
        }
        return ret;
    }

    /**
	 *
	 * @param rect
	 * @param rowNum
	 * @param spaceToBar
	 * @return
	 */
    public int calcTaskPointY(GanttRectangle rect, int rowNum, int spaceToBar) {
        if (log.isDebugEnabled()) {
            log.debug("    Calculate Y of task: " + this + " for row: " + rowNum);
        }
        int y = rect.y + context.getConfig().getGanttChartRowHeight() * rowNum + spaceToBar;
        if (log.isDebugEnabled()) {
            log.debug("      = " + y);
        }
        return y;
    }

    /**
	 * Create diamond shape for milestone.
	 * @param topX
	 * @param topY
	 * @param width
	 * @param height
	 * @return
	 */
    protected GanttPolygon createDiamondShape(int topX, int topY, int width, int height) {
        topY += padding_v;
        int[] xPoints = new int[] { topX, topX + width / 2, topX, topX - width / 2 };
        int[] yPoints = new int[] { topY, topY + height / 2, topY + height, topY + height / 2 };
        return new GanttPolygon(xPoints, yPoints, 4);
    }

    /**
	 * Create diamond shape for task group
	 * @param topX
	 * @param topY
	 * @param radius
	 * @return
	 */
    protected GanttPolygon createDiamondShape(int topX, int topY, int radius) {
        int[] xPoints = new int[] { topX, topX + radius, topX, topX - radius };
        int[] yPoints = new int[] { topY, topY + radius, topY + radius * 2, topY + radius };
        return new GanttPolygon(xPoints, yPoints, 4);
    }
}
