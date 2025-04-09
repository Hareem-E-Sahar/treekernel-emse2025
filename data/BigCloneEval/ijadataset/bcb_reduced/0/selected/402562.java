package factor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FunctionUtils {

    public static class Facteur {

        private int npr;

        private int ordre;

        private Facteur(int npr, int ordre) {
            this.npr = npr;
            this.ordre = ordre;
        }

        @Override
        public String toString() {
            super.toString();
            return npr + "^" + ordre;
        }
    }

    public static final IFunction<List<Facteur>, Long> fDecompose = new IFunction<List<Facteur>, Long>() {

        @Override
        public List<Facteur> exec(Long L) {
            List<Facteur> ret = new ArrayList<Facteur>();
            int ordre = 0;
            for (int npr = 2; npr <= L / npr; npr++) {
                while (L % npr == 0) {
                    ordre++;
                    L = L / npr;
                }
                ret.add(new Facteur(npr, ordre));
                ordre = 0;
            }
            if (L > 1) {
                ret.add(new Facteur(L.intValue(), 1));
            }
            return ret;
        }
    };

    public static final IFunction<Integer, Long> IndEuler = new IFunction<Integer, Long>() {

        @Override
        public Integer exec(Long L) {
            List<Facteur> res = fDecompose.exec(L);
            int pi;
            Integer ret = 1;
            Iterator<Facteur> it = res.iterator();
            while (it.hasNext()) {
                Facteur f = it.next();
                int npr = f.npr;
                int ordre = f.ordre;
                ret *= (npr - 1) * (puissance(npr, ordre - 1));
            }
            return ret;
        }

        private int puissance(int x, int n) {
            int ret = 1;
            for (int i = 1; i <= n; i++) {
                ret *= x;
            }
            return ret;
        }
    };
}
