package org.com.vnp.caothubongbong.views;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.com.cnc.common.adnroid16.CommonDeviceId;
import org.com.vnp.caothubongbong.R;
import org.com.vnp.caothubongbong.config.ConfigMediaplayer;
import org.com.vnp.caothubongbong.data.Bullet;
import org.com.vnp.caothubongbong.data.GroupBalloons;
import org.com.vnp.caothubongbong.data.Score;
import org.com.vnp.caothubongbong.linner.PlayTouchLinner;
import org.com.vnp.caothubongbong.linner.ReloadLinner;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PlayView extends LinearLayout {

    private int number_bullet = 7;

    private Paint paint = new Paint();

    private Bitmap bmBullet;

    private Button btnReload;

    private List<Bullet> lBullets = new ArrayList<Bullet>();

    private List<GroupBalloons> lGroupBalloons = new ArrayList<GroupBalloons>();

    private List<Score> lScore = new ArrayList<Score>();

    private int time = 61;

    private int score = 0;

    private TextView tvTime;

    private TextView tvScore;

    public PlayView(Context context) {
        super(context);
        config();
    }

    public PlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        config();
    }

    private void config() {
        LayoutInflater li;
        String service = Context.LAYOUT_INFLATER_SERVICE;
        li = (LayoutInflater) getContext().getSystemService(service);
        li.inflate(R.layout.bullet, this);
        tvTime = (TextView) findViewById(R.id.textView2);
        updateTime();
        tvScore = (TextView) findViewById(R.id.textView1);
        updateScore(0);
        setWillNotDraw(false);
        btnReload = (Button) findViewById(R.id.button1);
        showBtnReload(false);
        btnReload.setOnClickListener(new ReloadLinner(this));
        setOnTouchListener(new PlayTouchLinner(this));
        Resources rs = getResources();
        int res = R.drawable.bullet;
        bmBullet = BitmapFactory.decodeResource(rs, res);
        paint.setColor(Color.RED);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < lGroupBalloons.size(); i++) {
            GroupBalloons balloons = lGroupBalloons.get(i);
            balloons.onDraw(canvas, paint);
            if (balloons.isDie()) {
                balloons.update();
            }
        }
        for (int i = 0; i < lBullets.size(); i++) {
            Bullet bullet = lBullets.get(i);
            bullet.onDraw(canvas, paint);
        }
        for (int i = 0; i < lBullets.size(); i++) {
            Bullet bullet = lBullets.get(i);
            if (bullet.isDie()) {
                lBullets.remove(bullet);
            }
        }
        for (int i = 0; i < number_bullet; i++) {
            int x = (i) * bmBullet.getWidth() + 5;
            int y = getHeight() - 10 - bmBullet.getHeight();
            canvas.drawBitmap(bmBullet, x, y, paint);
        }
        for (int i = 0; i < lScore.size(); i++) {
            Score score = lScore.get(i);
            Point point = score.getPoint();
            canvas.drawText(score.getScore() + "", point.x, point.y, paint);
        }
        for (int i = 0; i < lScore.size(); i++) {
            Score score = lScore.get(i);
            if (score.isDie()) {
                lScore.remove(score);
            }
        }
        if (lGroupBalloons.size() == 0) {
            int number = 5;
            if (CommonDeviceId.isTablet((Activity) getContext())) {
                number = 8;
            }
            GroupBalloons groupBalloons = new GroupBalloons(getContext(), 1, 100, 200);
            int y = getHeight() - 10 - bmBullet.getHeight() - groupBalloons.getMinHeight() / 2;
            int x = groupBalloons.getMaxWidth() / 2 + 10;
            int space = groupBalloons.getMaxWidth() + 10;
            int spaceRight = getWidth() - (x + space * (number - 1));
            x = x + (spaceRight - x) / 2;
            for (int i = 0; i < number; i++) {
                lGroupBalloons.add(createGroupBalloons(x + space * (i), y));
            }
        }
        invalidate();
    }

    private GroupBalloons createGroupBalloons(int x, int y) {
        if (CommonDeviceId.isTablet((Activity) getContext())) {
            y = y - new Random().nextInt(200);
        } else {
            y = y - new Random().nextInt(50);
        }
        return new GroupBalloons(getContext(), new Random().nextInt(3), x, y);
    }

    public int getNumber_bullet() {
        return number_bullet;
    }

    public void setNumber_bullet(int number_bullet) {
        this.number_bullet = number_bullet;
    }

    public void showBtnReload(boolean b) {
        btnReload.setVisibility(b ? VISIBLE : GONE);
    }

    public void update(int x, int y) {
        ConfigMediaplayer.startKill(getContext());
        Bullet bullet = new Bullet(getContext(), x, y);
        lBullets.add(bullet);
        for (int i = 0; i < lGroupBalloons.size(); i++) {
            GroupBalloons balloons = lGroupBalloons.get(i);
            if (balloons.typeShoot(x, y) == 1) {
                if (!balloons.isDie()) {
                    balloons.setDie(true);
                    Score score = new Score();
                    score.setScore(balloons.getScore());
                    score.getPoint().x = x;
                    score.getPoint().y = y;
                    lScore.add(score);
                    updateScore(score.getScore());
                }
            }
        }
    }

    public void updateTime() {
        time--;
        if (time < 0) {
            time = 0;
        }
        tvTime.post(new Runnable() {

            public void run() {
                String txtTime = "Time : " + (time < 10 ? "0" : "") + time;
                tvTime.setText(txtTime);
            }
        });
    }

    public void updateScore(int score1) {
        this.score += score1;
        tvScore.post(new Runnable() {

            public void run() {
                String txtTime = "Score : " + score;
                tvScore.setText(txtTime);
            }
        });
    }

    public boolean isClose() {
        return time <= 0;
    }

    public int getScore() {
        return score;
    }
}
