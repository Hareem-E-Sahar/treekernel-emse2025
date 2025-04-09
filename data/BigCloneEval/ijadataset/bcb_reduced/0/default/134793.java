import java.util.ArrayList;

public class Knight extends Chessman implements Cloneable {

    public Object clone() throws CloneNotSupportedException {
        Knight result = new Knight();
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

    public Knight() {
        super();
    }

    public Knight(ChessBoard cb, String flag, CPosition p) {
        super(cb, "��", flag, p);
    }

    public boolean specialLegalMove(Message msg) {
        int i, ox, oy, nx, ny, xcheck, ycheck, xpass, ypass;
        ox = msg.oldPos.x;
        oy = msg.oldPos.y;
        nx = msg.newPos.x;
        ny = msg.newPos.y;
        xpass = nx - ox;
        ypass = ny - oy;
        xcheck = ox;
        ycheck = oy;
        if (xpass == 1 || xpass == -1) {
            xcheck = ox;
            if (ypass == 2 || ypass == -2) {
                ycheck = (oy + ny) / 2;
            } else {
                return false;
            }
        }
        if (ypass == 1 || ypass == -1) {
            ycheck = oy;
            if (xpass == 2 || xpass == -2) {
                xcheck = (ox + nx) / 2;
            } else {
                return false;
            }
        }
        if (this.cb.hasChessman(xcheck, ycheck)) {
            return false;
        }
        return true;
    }

    public ArrayList canMove() {
        whereCanMove.clear();
        Message[] temp = new Message[8];
        temp[0] = new Message(this.flag, this.pos.x, this.pos.y, this.pos.x + 2, this.pos.y + 1);
        temp[1] = new Message(this.flag, this.pos.x, this.pos.y, this.pos.x - 2, this.pos.y + 1);
        temp[2] = new Message(this.flag, this.pos.x, this.pos.y, this.pos.x + 2, this.pos.y - 1);
        temp[3] = new Message(this.flag, this.pos.x, this.pos.y, this.pos.x - 2, this.pos.y - 1);
        temp[4] = new Message(this.flag, this.pos.x, this.pos.y, this.pos.x + 1, this.pos.y + 2);
        temp[5] = new Message(this.flag, this.pos.x, this.pos.y, this.pos.x - 1, this.pos.y + 2);
        temp[6] = new Message(this.flag, this.pos.x, this.pos.y, this.pos.x + 1, this.pos.y - 2);
        temp[7] = new Message(this.flag, this.pos.x, this.pos.y, this.pos.x - 1, this.pos.y - 2);
        for (int i = 0; i < 8; ++i) {
            if (legalMove(temp[i])) {
                whereCanMove.add(temp[i]);
            }
        }
        return super.canMove();
    }
}
