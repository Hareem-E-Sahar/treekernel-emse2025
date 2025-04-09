package ru.susu.algebra.ranks;

import static java.lang.Runtime.getRuntime;
import static java.lang.System.currentTimeMillis;
import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Array;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Программа для вычисления рангов групп центральных единиц целочисленных групповых колец,
 * основанная на теореме Ферраза о рангах
 */
public class RanksByFerrazTheorem {

    private static Logger _log = Logger.getLogger("RanksByFerrazTheorem");

    /**
	 * Стартовая степень An (с нее начинается вычисление рангов)
	 */
    public static int START_NUMBER = 1;

    /**
	 * Конечная степень An (до нее включительно будут вычислены ранги)
	 */
    public static int END_NUMBER = 1000;

    public static void main(String[] args) throws Exception {
        org.apache.log4j.BasicConfigurator.configure();
        for (int n = START_NUMBER; n <= END_NUMBER; ++n) {
            long startTimeMS = currentTimeMillis();
            long squares = SquaresPartitions.instance.getSquares(n);
            long rank = RMod4Partitions.instance.getPartitions(n) - squares;
            if (n % 10 == 0) System.gc();
            _log.info("rank[" + n + "] - " + rank);
            _log.info("squares[" + n + "] - " + squares);
            _log.info("total memory - " + getRuntime().totalMemory() / (1 << 20) + "m");
            _log.info("free memory - " + getRuntime().freeMemory() / (1 << 20) + "m");
            _log.info("used memory - " + (getRuntime().totalMemory() - getRuntime().freeMemory()) / (1 << 20) + "m");
            _log.info("time - " + (currentTimeMillis() - startTimeMS) + "ms");
            BruteForceRanks.instance.checkRank(n, rank);
        }
        _log.info("finished");
    }

    /**
	 * Класс вычисляет количество разбиений, удовлетворяющих условиям
	 * 1, 2, 3 теоремы ферраза, но не удовлетворяющих условию 4
	 */
    private enum SquaresPartitions {

        instance;

        @SuppressWarnings("unchecked")
        private Map<BitSet, Long>[][][] _squares = (Map<BitSet, Long>[][][]) Array.newInstance(Map.class, END_NUMBER + 1, END_NUMBER + 1, 4);

        private Map<BitSet, BitSet> _cache = Maps.newHashMap();

        private BitSet getCached(BitSet obj) {
            BitSet result = _cache.get(obj);
            if (result == null) {
                _cache.put(obj, obj);
                result = obj;
            }
            return result;
        }

        private long getSquaresCount(int n, int k, int shift, BitSet factors) {
            if (n < k) return (n == 0 && shift == 0 && factors.isEmpty()) ? 1 : 0;
            if (_squares[n][k][shift] == null) _squares[n][k][shift] = new SortedArrayMap<BitSet, Long>(new BitSetComparator());
            Long result = _squares[n][k][shift].get(factors);
            if (result != null) return result;
            if (!MinPrimeSumRestriction.instance.check(n, k, factors)) return 0;
            result = 0L;
            for (int newK = k; newK <= n; newK += 2) {
                if (RMod4Partitions.instance.isCanUseK(n, newK, shift) && MaxPrimeNumberRestriction.instance.check(n, newK)) {
                    BitSet tmp = (BitSet) factors.clone();
                    tmp.xor(PrimeNumbers.instance.getDecomposition(newK));
                    result += getSquaresCount(n - newK, newK + 2, RMod4Partitions.instance.getNewShift(newK, shift), tmp);
                }
            }
            _squares[n][k][shift].put(getCached(factors), result);
            return result;
        }

        /**
		 * @return количество разбиений, уловлетворяющих условиям 1, 2, 3, но не
		 * удовлетворяющих условию 4
		 */
        public long getSquares(int n) {
            return getSquaresCount(n, 1, 0, new BitSet(PrimeNumbers.instance.getPrimes().size()));
        }
    }

    /**
	 * Класс вычисляет количетсво разбиений, удовлетворяющих условиям 1, 2, 3
	 * теоремы ферраза
	 */
    private enum RMod4Partitions {

        instance;

        private Long[][][] _partitions = new Long[END_NUMBER + 1][END_NUMBER + 1][4];

        private long getPartitionsCount(int n, int k, int shift) {
            if (n < k) return (n == 0 && shift == 0) ? 1 : 0;
            if (_partitions[n][k][shift] != null) return _partitions[n][k][shift];
            return _partitions[n][k][shift] = getPartitionsCount(n - k, k + 2, getNewShift(k, shift)) + getPartitionsCount(n, k + 2, shift);
        }

        /**
		 * @return количество разбиений для n
		 */
        public long getPartitions(int n) {
            return getPartitionsCount(n, 1, 0);
        }

        public int getNewShift(int k, int shift) {
            return (shift + 1 + 3 * k) & 3;
        }

        /**
		 * @return true, если при выборе k, можно получить какое-либо разбиение
		 */
        public boolean isCanUseK(int n, int k, int shift) {
            return getPartitionsCount(n - k, k + 2, getNewShift(k, shift)) > 0;
        }
    }

    /**
	 * Максимальное простое в нечетной степени должно быть не больше, 
	 * чем END_NUMBER / 4, иначе не получится элемента MAX_PRIME + 3 * MAX_PRIME
	 */
    public static int MAX_PRIME_NUMBER = END_NUMBER / 4 + 1;

    private enum MaxPrimeNumberRestriction {

        instance;

        private Boolean[][] _cache = new Boolean[END_NUMBER + 1][END_NUMBER + 1];

        public boolean check(int n, int k) {
            if (_cache[n][k] != null) return _cache[n][k];
            int maxPrime = PrimeNumbers.instance.getMaxPrimeNumberOddPower(k);
            return _cache[n][k] = maxPrime <= MAX_PRIME_NUMBER;
        }
    }

    /**
	 * Размер кэша для MinPrimeSumRestriction
	 */
    private static int MIN_SUM_PRIMES_CACHE_SIZE = 1 << 20;

    /**
	 * Класс выполняет проверку возможности получения суммы чисел, которая будет не больше n,
	 * в числа будут входить все нужные простые числа
	 * (благодаря этому можно получить factors = 0, сл-но произведение станет квадратом)
	 * и каждое число должно быть не меньше k
	 *
	 * Результаты вызовоз кэшируются в _cache с ключом из пары (k, factors)
	 * размером MIN_SUM_PRIMES_CACHE_SIZE
	 *
	 * Результатом является минимальная возможная сумма,
	 * для конкретного n она просто с ним сравнивается
	 */
    private enum MinPrimeSumRestriction {

        instance;

        private static int DP_LENGTH = 20;

        private static int DP_SIZE = 1 << DP_LENGTH;

        private static int[] _dp = new int[DP_SIZE];

        private static int INF = 1 << 20;

        private Map<String, Integer> _cache = new LRUHashMap<String, Integer>(MIN_SUM_PRIMES_CACHE_SIZE);

        /**
		 * @return true, если минимальная сумма getMinPrimeSumK(factors, k) <= n
		 */
        public boolean check(int n, int k, BitSet factors) {
            return getMinPrimeSumK(factors, k) <= n;
        }

        /**
		 * @return минимульную сумму, которую можно получить из сгруппированных простых чисел из factors,
		 * каждая группа >= k
		 */
        private int getMinPrimeSumK(BitSet factors, int k) {
            String key = factors.toString() + "_" + k;
            Integer result = _cache.get(key);
            if (result != null) return result;
            ArrayList<Integer> primes = PrimeNumbers.instance.getPrimes(factors);
            assert (primes.size() <= DP_LENGTH);
            Arrays.fill(_dp, 0, 1 << primes.size(), -1);
            result = subSetMinPrimeSum(primes, (1 << primes.size()) - 1, k);
            _cache.put(key, result);
            return result;
        }

        /**
		 * @param primes список простых чисел
		 * @param mask оставшиеся простые числа, доступные для выбора (1ые биты)
		 * @return решение подзадачи динамического программирования
		 */
        private int subSetMinPrimeSum(ArrayList<Integer> primes, int mask, int k) {
            if (_dp[mask] != -1) return _dp[mask]; else if (mask == 0) return 0;
            int result = INF;
            int highestOneBit = Integer.highestOneBit(mask);
            for (int subSet = mask; (subSet & highestOneBit) > 0; subSet = (subSet - 1) & mask) {
                int product = getProduct(primes, subSet, result);
                product = ensureProductGreaterEqualK(product, k);
                if (product < result) result = Math.min(product + subSetMinPrimeSum(primes, mask ^ subSet, k), result);
            }
            return _dp[mask] = result;
        }

        /**
		 * @return произведение чисел, соответствующих маске mask,
		 * если произведение >= limit, то результат будет некоторым
		 * делителем произведения, который тоже >= limit
		 */
        private int getProduct(ArrayList<Integer> primes, int mask, int limit) {
            int result = 1;
            for (int index = 0; index < primes.size() && result < limit; ++index) if ((mask & (1 << index)) != 0) result *= primes.get(index);
            return result;
        }

        /**
		 * Если product < k, то вернет минимальное нечетное кратное product >= k
		 */
        private int ensureProductGreaterEqualK(int product, int k) {
            if (product < k) {
                if (k % product == 0) return k;
                int result = (k / product + 1) * product;
                return ((result & 1) == 1) ? result : result + product;
            }
            return product;
        }
    }

    /**
	 * Класс хранит в себе простые числа до числа MAX_PRIME_NUMBER
	 * и показатели степеней этих простых по модулю 2 в разложении в
	 * произведение простых чисел до END_NUMBER (с помощью BitSet)
	 */
    private enum PrimeNumbers {

        instance;

        private ArrayList<Integer> _primes;

        private BitSet[] _decompositions;

        private PrimeNumbers() {
            _primes = generatePrimes(MAX_PRIME_NUMBER);
            _decompositions = generateDecompositions(END_NUMBER, _primes);
        }

        public int getMaxPrimeNumberOddPower(int number) {
            int result = 0;
            for (int prime = 2; prime <= number; ++prime) {
                int count = 0;
                while (number % prime == 0) {
                    count ^= 1;
                    number /= prime;
                }
                if (count == 1) result = prime;
            }
            return result;
        }

        /**
	     * @return список простых чисел <= maxNumber,
	     * полученных с помощью решета Эратосфена
	     */
        private ArrayList<Integer> generatePrimes(int maxNumber) {
            ArrayList<Integer> primes = Lists.newArrayList();
            boolean isPrime[] = new boolean[maxNumber + 1];
            Arrays.fill(isPrime, true);
            for (int index = 2; index <= maxNumber; ++index) {
                if (isPrime[index]) {
                    for (int clearIndex = index + index; clearIndex <= maxNumber; clearIndex += index) isPrime[clearIndex] = false;
                    primes.add(index);
                }
            }
            return primes;
        }

        /**
		 * @return массив показателей степеней простых чисел по модулю 2 в разложении
		 * натуральных чисел до END_NUMBER в произведение простых primes
		 */
        private BitSet[] generateDecompositions(int maxNumber, ArrayList<Integer> primes) {
            BitSet[] decompositions = new BitSet[maxNumber + 1];
            Arrays.fill(decompositions, new BitSet(primes.size()));
            for (int number = 2; number <= maxNumber; ++number) {
                int tmpNumber = number;
                for (int primeIndex = 0; primeIndex < primes.size(); ++primeIndex) {
                    while (tmpNumber % primes.get(primeIndex) == 0) {
                        tmpNumber /= primes.get(primeIndex);
                        decompositions[number] = ((BitSet) decompositions[number].clone());
                        decompositions[number].set(primeIndex, !decompositions[number].get(primeIndex));
                    }
                }
            }
            return decompositions;
        }

        public BitSet getDecomposition(int number) {
            return _decompositions[number];
        }

        public ArrayList<Integer> getPrimes() {
            return _primes;
        }

        public ArrayList<Integer> getPrimes(BitSet mask) {
            ArrayList<Integer> primes = new ArrayList<Integer>(mask.cardinality());
            for (int index = 0; index < mask.length(); ++index) if (mask.get(index)) primes.add(_primes.get(index));
            return primes;
        }
    }

    /**
	 * Класс обеспечивает сравнение с ранее посчитанными рангами,
	 * которые сохранены в файле ranks800.txt
	 */
    private enum BruteForceRanks {

        instance;

        /**
		 * На каждое число n NUM_LINES строк
		 */
        private final int NUM_LINES = 16;

        /**
		 * В OUR_LINE хранится нужный ранг
		 */
        private final int OUR_LINE = 9;

        private Map<Integer, Long> _ranks = Maps.newHashMap();

        private BruteForceRanks() {
            try {
                BufferedReader reader = new BufferedReader(new FileReader("Ranks/ranks800.txt"));
                for (int index = 0; index <= NUM_LINES; ++index) reader.readLine();
                int n = 1;
                int lineNumber = 0;
                while (true) {
                    String line = reader.readLine();
                    if (line == null) break;
                    if (lineNumber % NUM_LINES == OUR_LINE) _ranks.put(n++, Long.parseLong(line.substring(3).trim()));
                    ++lineNumber;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        /**
		 * Проверка ранга на совпадение, если для такого n есть ранее посчитанный ранг
		 * и новый отличается от него, то будет {@link AssertionError}
		 */
        public void checkRank(int n, long rank) {
            if (_ranks.containsKey(n)) assert _ranks.get(n) == rank;
        }
    }

    /**
	 * HashMap фиксированного размера, при первышении которого удаляется
	 * наименее используемый элемент (ранее добавленный)
	 */
    private static class LRUHashMap<K, V> extends LinkedHashMap<K, V> {

        private static final long serialVersionUID = 5806283927249211294L;

        private int _maxSize;

        public LRUHashMap(int maxSize) {
            _maxSize = maxSize;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > _maxSize;
        }
    }

    /**
	 * Реализация AbstractMap, ключи и значения хранятся в массивах,
	 * пары упорядочены по ключу. 
	 * Используется вместо HashMap, так как данная структура потребляет меньше 
	 * памяти и операция get выполняется с помощью binarySearch.
	 */
    public static class SortedArrayMap<K, V> extends AbstractMap<K, V> {

        private static int INITIAL_SIZE = 1;

        private static int INCREMENT = 5;

        private Object[] _keys = new Object[INITIAL_SIZE];

        private Object[] _values = new Object[INITIAL_SIZE];

        private int _size = 0;

        private Comparator _comparator;

        public SortedArrayMap(Comparator comparator) {
            _comparator = comparator;
        }

        @Override
        public int size() {
            return _size;
        }

        @Override
        public V put(K key, V value) {
            if (containsKey(key)) {
                int index = Arrays.binarySearch(_keys, 0, _size, key, _comparator);
                V oldValue = (V) _values[index];
                _values[index] = value;
                return oldValue;
            }
            addEntry(key, value);
            return null;
        }

        private void addEntry(K key, V value) {
            int index = (Arrays.binarySearch(_keys, 0, _size, key, _comparator) + 1) * (-1);
            if (_size == _keys.length) {
                resize(_size + INCREMENT);
            }
            for (int i = _size; i > index; --i) {
                _keys[i] = _keys[i - 1];
                _values[i] = _values[i - 1];
            }
            _keys[index] = key;
            _values[index] = value;
            ++_size;
        }

        private void resize(int newLength) {
            _keys = Arrays.copyOf(_keys, newLength);
            _values = Arrays.copyOf(_values, newLength);
        }

        @Override
        public boolean containsKey(Object key) {
            return null != get(key);
        }

        @Override
        public V get(Object key) {
            int index = Arrays.binarySearch(_keys, 0, _size, key, _comparator);
            if (index >= 0 && ObjectUtils.equals(_keys[index], key)) return (V) _values[index];
            return null;
        }

        @Override
        public Set<Map.Entry<K, V>> entrySet() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public V remove(Object key) {
            throw new UnsupportedOperationException();
        }
    }

    /**
	 * Класс, реализующий сравнение BitSetов
	 */
    private static class BitSetComparator implements Comparator<BitSet> {

        @Override
        public int compare(BitSet bitset1, BitSet bitset2) {
            if (bitset1.equals(bitset2)) return 0;
            if (bitset1.length() != bitset2.length()) return bitset1.length() - bitset2.length();
            for (int index = 0; index < bitset1.length(); ++index) if (bitset1.get(index) != bitset2.get(index)) return bitset1.get(index) ? 1 : -1;
            return 0;
        }
    }
}
