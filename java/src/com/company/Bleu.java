/**
 * Bleu 算法用于评价及其翻译的准确度
 */
public class Bleu {
    private static int BLEU_N;
    private long[] matchNGrams;
    private long[] candiNGrams;
    private long BPR;
    private long BPC;

    static {
        Properties prop = new Properties();
        try {
            //读取属性文件
            InputStream in = new BufferedInputStream(new FileInputStream("src/args.properties"));
            prop.load(in);  //加载属性列表
            String num = prop.getProperty("ROUGE_N");
            BLEU_N = Integer.parseInt(num);
//            System.out.println("BLEU_N: " + BLEU_N);
            in.close();
        } catch (IOException e) {
            e.getStackTrace();
        }
    }

    /**
     * 默认使用BLEU-4计算分数
     */
    public Bleu() {
        this.matchNGrams = new long[BLEU_N + 1];
        this.candiNGrams = new long[BLEU_N + 1];
        this.BPC = 0;
        this.BPR = 0;
    }

    /**
     * 根据预测答案和参考答案更新BP, matchList, candiList，注意
     * @param candSent 预测答案
     * @param refSent 参考答案
     */
    public void addInstance(String candSent, String refSent) {
        // 如果参考答案不存在不需要计算
        if (refSent == null || refSent.equals("")) {
            return;
        }
        // 如果答卷答案为null初始化为""
        if (candSent == null) {
            candSent = "";
        }

        int refSentSize = refSent.length();
        int candSentSize = candSent.length();
        int size = Math.min(BLEU_N, refSentSize);
        size = Math.min(size, candSentSize);

        for (int i = 0; i < size; i++) {
//            System.out.println("candidate sentence: " + candSent);
//            System.out.println("reference sentence: " + refSent);
            countNGram(candSent, refSent, i + 1);
        }
        countBP(candSent, refSent);
    }

    /**
     * 更新BP相关的参数r和c
     * @param candSent 预测答案
     * @param refSent 参考答案
     */
    public void countBP(String candSent, String refSent) {
//        System.out.println("old   BPC: " + this.BPC);
        this.BPC += candSent.length();
//        System.out.println("candidate size: " + candSent.length());
//        System.out.println("current   BPC: " + this.BPC);

//        System.out.println("old   BPR:  " + this.BPR);
        this.BPR += refSent.length();
//        System.out.println("reference size: " + refSent.length());
//        System.out.println("current   BPR:  " + this.BPR);
    }

    /**
     * 计算numSize的条件下计算匹配个数以及参考答案子序列的长度，更鞋雷暴
     * @param candSent 预测答案
     * @param refSent 参考答案
     * @param numSize 子序列大小 > 0
     */
    public void countNGram(String candSent, String refSent, int numSize) {
        assert numSize > 0;
        ConcurrentHashMap<String, Integer> candGramsMap = getNGramMap(candSent, numSize);
        ConcurrentHashMap<String, Integer> refsGramsMap = getNGramMap(refSent, numSize);
        int matchGramSize = getMatchSize(candGramsMap, refsGramsMap);
        int candGramSize = candSent.length() - numSize + 1;
//        System.out.println("current ngram size: " + numSize);
//        System.out.println("match size: " + matchGramSize);
//        System.out.println("candidate gram size: " + candGramSize);
//        if (matchGramSize > candGramSize) {
//            System.out.println( "candidate sentence:" + candSent);
//            System.out.println("reference sentence: " + refSent);
//        }
//        System.out.println("old match ngram " + numSize + " sum: " + this.matchNGrams[numSize]);
//        System.out.println("old candidate ngram " + numSize + " sum: " + this.candiNGrams[numSize]);

        this.matchNGrams[numSize] += matchGramSize;
        this.candiNGrams[numSize] += candGramSize;

//        System.out.println("now match ngram " + numSize + " sum: " + this.matchNGrams[numSize]);
//        System.out.println("now candidate ngram " + numSize + " sum: " + this.candiNGrams[numSize]);
    }

    /**
     * 计算n-gram子序列表
     *
     * @param sentence 句子
     * @param numSize  子序列的大小
     * @return 子序列字符串数组, 如果句子的长度小于要求子序列的长度，则返回已经实例化的空的HashMap
     */
    public ConcurrentHashMap<String, Integer> getNGramMap(String sentence, int numSize) {
        // 计算nGram列表的长度
        int nGramSize = sentence.length() - numSize + 1;
//        System.out.println("numSize: " + numSize);
        // 初始化nGramMap
        ConcurrentHashMap<String, Integer> gramMap = new ConcurrentHashMap<>();
        if (nGramSize > 0) {
            for (int i = 0; i < nGramSize; i++) {
                String curGram = sentence.substring(i, i + numSize);
                if (gramMap.containsKey(curGram)) {
                    int countGram = gramMap.get(curGram);
                    countGram++;
                    gramMap.replace(curGram, countGram);
                } else {
                    gramMap.put(curGram, 1);
                }
            }
        }
        return gramMap;
    }

    /**
     * 计算备选答案中ngrams在参考答案中的匹配个数
     *
     * @param candGramsMap 备选答案的n-grams HashMap
     * @param refsGramMap  参考答案的n-grams
     * @return 匹配的个数, 如果candGrams活着refsGram为null返回0
     */
    public int getMatchSize(ConcurrentHashMap<String, Integer> candGramsMap, ConcurrentHashMap<String, Integer> refsGramMap) {
        int matchSize = 0;

        for (String key : candGramsMap.keySet()) {
            if (refsGramMap.containsKey(key)) {
                matchSize += Math.min(candGramsMap.get(key), refsGramMap.get(key));
            }
        }

        return matchSize;
    }

    /**
     * 计算预测答案的Bleu的分数
     *
     * @return Bleu分数
     */
    public double getScore() {
//        if (candidate == null || refrence == null) {
//            return 0;
//        }
        // 预处理
//        refrence = PreProcess.processString(refrence);
//        candidate = PreProcess.processString(candidate);
        // 计算长度
//        int refLength = refrence.length();
//        int candLength = candidate.length();

//        if (refLength == 0 || candLength == 0) {
//            return 0;
//        }
        // numSize 取三个长度的最小值，避免getNGram返回null
        double[] probArray = new double[BLEU_N];

        for (int i = 0; i < probArray.length; i++) {
            if (this.candiNGrams[i + 1] != 0) {

                double curP = this.matchNGrams[i + 1] * 1.0 / this.candiNGrams[i + 1];
                probArray[i] = curP;
//                System.out.println("size: " + (i + 1) +
//                        " match ngrams: " + this.matchNGrams[i + 1] +
//                        " candidate ngrams: " + this.candiNGrams[i + 1] +
//                        "P_" + (i + 1) + " = " + curP);
            } else {
                probArray[i] = 0.0;
            }
        }
        // 计算BLEU_n的得分
        double score = probArray[0];
//        System.out.println("score size 1: " + score);
        for (int i = 1; i < BLEU_N; i++) {
            score *= probArray[i];
//            System.out.println("score size " + (i + 1) + " : " + score);
        }
        if (score != 0) {
            score = Math.pow(score, 1.0 / BLEU_N);
            // 计算BP的质
            double preValue = 1 - this.BPR * 1.0 / this.BPC;
            double BP = Math.exp(Math.min(preValue, 0.0));
//            System.out.println("BP = e^min(1 - " + this.BPR + " / " + this.BPC + ", 0) = " + BP);
            score *= BP;
        }
        return score;
    }

}