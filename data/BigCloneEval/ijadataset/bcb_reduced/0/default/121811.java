import java.util.ArrayList;

public class Bishop extends Chessman implements Cloneable {

    public int yLimitLittle;

    public int yLimitBig;

    public Object clone() throws CloneNotSupportedException {
        Bishop result = new Bishop();
        result.setName(this.name);
        result.setFlag(this.flag);
        result.setPosition(this.pos);
        if (this.underAttack) {
            result.setUnderAttack();
        }
        if (this.isDead) {
            result.setDead();
        }
        if (this.beProtected) {
            result.setBeProtected();
        }
        result.setSelfValue(this.selfValue);
        result.setUnderAttackValue(this.underAttackValue);
        result.setCanGoPositionValue(this.canGoPositionValue);
        result.setProtectedValue(this.protectedValue);
        return result;
    }

    public Bishop() {
        super();
    }

    public Bishop(ChessBoard cb, String flag, CPosition p) {
        super(cb, "��", flag, p);
        this.setFlag(flag);
    }

    public boolean specialLegalMove(Message msg) {
        int i, ox, oy, nx, ny, xcheck, ycheck, xpass, ypass;
        ox = msg.oldPos.x;
        oy = msg.oldPos.y;
        nx = msg.newPos.x;
        ny = msg.newPos.y;
        xpass = nx - ox;
        ypass = ny - oy;
        xcheck = (nx + ox) / 2;
        ycheck = (ny + oy) / 2;
        if (ny > yLimitBig || ny < yLimitLittle) {
            return false;
        }
        if ((xpass != 2 && xpass != -2) || (ypass != 2 && ypass != -2)) {
            return false;
        }
        if (this.cb.hasChessman(xcheck, ycheck)) {
            return false;
        }
        return true;
    }

    public ArrayList canMove() {
        whereCanMove.clear();
        Message[] temp = new Message[4];
        temp[0] = new Message(this.flag, this.pos.x, this.pos.y, this.pos.x + 2, this.pos.y + 2);
        temp[1] = new Message(this.flag, this.pos.x, this.pos.y, this.pos.x - 2, this.pos.y + 2);
        temp[2] = new Message(this.flag, this.pos.x, this.pos.y, this.pos.x + 2, this.pos.y - 2);
        temp[3] = new Message(this.flag, this.pos.x, this.pos.y, this.pos.x - 2, this.pos.y - 2);
        for (int i = 0; i < 4; ++i) {
            if (legalMove(temp[i])) {
                whereCanMove.add(temp[i]);
            }
        }
        return super.canMove();
    }

    public void setFlag(String flag) {
        super.setFlag(flag);
        yLimitBig = yLimitLittle = -1;
        if (flag.equals("red")) {
            this.yLimitLittle = 0;
            this.yLimitBig = 4;
        }
        if (flag.equals("blue")) {
            this.yLimitLittle = 5;
            this.yLimitBig = 9;
        }
    }
}
