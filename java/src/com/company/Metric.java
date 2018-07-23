package com.company;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class Metric {
    private static List<Article> refList = null;
    private static byte[] refCached = new byte[1];

    private static final String ARTICLE_ID = "article_id";
    private static final String QUESTIONS = "questions";
    private static final String QUESTION_ID = "questions_id";
    private static final String ANSWER = "answer";
    public static final String BLEU_SCORE = "bleu_score";
    public static final String ROUGE_SCORE = "rouge_score";

    /**
     * 分别计算RougeL分数和Bleu分数，以ConcurrentHashMap返回
     *
     * @param candFile 预测答案文件
     * @return ConcurrentHashMap返回存储方式{"bleu_log": score1, "rouge_log": score2}
     */
    public static ConcurrentHashMap<String, Double> getScore(File refFile, File candFile) throws RuntimeException {

        if (candFile == null) {
            throw new RuntimeException("提交的答案不能为空");
        }

        if (refFile == null && refList == null) {
            throw new RuntimeException("没有初始化参考答案");
        }
        while (true) {
            if (refFile != null && refList == null) {
                setRefList(refFile);
            }
            if (refList != null) {
                break;
            }
        }

        return scoreCandFile(candFile);
    }

    public static ConcurrentHashMap<String, Double> scoreCandFile(File candFile) {
        // 开始进行统计
        JsonFactory jsonFactory = new JsonFactory();

        // 初始化计分器
        Bleu bleuEval = new Bleu();
        RougeL rougeLEval = new RougeL();
        ConcurrentHashMap<String, Double> scoreMap = new ConcurrentHashMap<>();

        int NEXT_BLOCK_STEP = 3;
        int NEXT_ARTICLE_STEP = 2;
        int NEXT_TOKEN_STEP = 1;

        try {
            JsonParser jsonParser = jsonFactory.createParser(candFile);
            for (Article article :
                    refList) {
                String refArticleID = article.getArticleID();
                // 读取答卷键值
                jsonParser = jump(jsonParser, NEXT_BLOCK_STEP);
                String curName = jsonParser.getCurrentName();
                if (!ARTICLE_ID.equals(curName)) {
                    throw new RuntimeException("无法识别article_id键值，JSON文件格式出错");
                }
                // 读取article_id
                jsonParser = jump(jsonParser, NEXT_TOKEN_STEP);
                String candArticleID = jsonParser.getText();
                if (!refArticleID.equals(candArticleID)) {
                    throw new RuntimeException("article_id键值不吻合，请检查JSON文件");
                }
                jsonParser = jump(jsonParser, NEXT_TOKEN_STEP);
                if (!QUESTIONS.equals(jsonParser.getCurrentName())) {
                    throw new RuntimeException("无法识别questions键值，JSON文件格式出错");
                }
                List<Question> refQuestions = article.getQuestions();
                for (Question refQuestion: refQuestions) {
                    String refQuestionID = refQuestion.getQuestionID();
                    String refAnswer = refQuestion.getAnswer();

                    // 获取candidate question id
                    // 读取答卷键值
                    jsonParser = jump(jsonParser, NEXT_BLOCK_STEP);
                    String candQuestionKey = jsonParser.getCurrentName();
                    if (!QUESTION_ID.equals(candQuestionKey)) {
                        throw new RuntimeException("questions_id键值不吻合，请检查JSON文件");
                    }
                    jsonParser = jump(jsonParser, NEXT_TOKEN_STEP);
                    String candQuestionID = jsonParser.getText();
                    if (!refQuestionID.equals(candQuestionID)) {
                        throw new RuntimeException("答卷question_id不吻合");
                    }
                    jsonParser = jump(jsonParser, NEXT_TOKEN_STEP);
                    String candAnswerKey = jsonParser.getCurrentName();
                    if (!ANSWER.equals(candAnswerKey)) {
                        throw new RuntimeException("answer键值不吻合，请检查JSON文件");
                    }
                    jsonParser = jump(jsonParser, NEXT_TOKEN_STEP);
                    String candAnswer = jsonParser.getText();

                    candAnswer = processString(candAnswer);
                    bleuEval.addInstance(candAnswer, refAnswer);
                    rougeLEval.addInstance(candAnswer, refAnswer);
                }
                jsonParser = jump(jsonParser, NEXT_ARTICLE_STEP);
            }
        } catch (IOException e) {
            throw new RuntimeException("无法正确解析答卷JSON文件");
        }

        // 开始进行统计
        scoreMap.put(BLEU_SCORE, bleuEval.getScore());
        scoreMap.put(ROUGE_SCORE, rougeLEval.getScore());
        return scoreMap;
    }

    public static JsonParser jump(JsonParser jsonParser, int jumpStep) {

        int curIDX = 0;
        try {
            while ( curIDX < jumpStep && jsonParser.nextToken() != null) {
                curIDX++;
            }
            if (curIDX != jumpStep) {
                throw new RuntimeException("JSON文件格式出错");
            }
        } catch (IOException e) {
            throw new RuntimeException("无法正确解析答卷JSON文件");
        }
        return jsonParser;
    }

    /**
     * 读取参考答案文件，将参考答案加载到内存中去
     *
     * @param refFile 参考答案文件
     * @throws RuntimeException 读取文件时出错抛出错误
     */
    public synchronized static void setRefList(File refFile) throws RuntimeException {
        // 初始化对象
        if (refList != null) {
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        refList = new ArrayList<>();

        // 读取参考答案数据
        try {
            // 初始化JSON解析方法
            JsonNode rootNode = mapper.readTree(refFile);

            // 初始化数组
            int size = rootNode.size();
            refList = new ArrayList<>(size);

            for (int i = 0; i < size; i++) {
                JsonNode articleJSON = rootNode.get(i);
                String articleID = articleJSON.path(ARTICLE_ID).asText();
                if (articleID == null) {
                    throw new RuntimeException("参考答案文件不包含article_id键值，请检查该文件");
                }
                JsonNode questionsJson = articleJSON.path(QUESTIONS);
                if (questionsJson == null) {
                    throw new RuntimeException("参考答案文件不包含questions键值");
                }
                int questionSize = questionsJson.size();

                List<Question> questionList = new ArrayList<>(questionSize);
                for (int j = 0; j < questionSize; j++) {
                    JsonNode questionNode = questionsJson.get(j);
                    String questionID = questionNode.path(QUESTION_ID).asText();
                    if (questionID == null) {
                        throw new RuntimeException("参考答案不包含questions_id键值，请检查文件");
                    }
                    String answer = questionNode.path(ANSWER).asText();
                    answer = processString(answer);

                    Question question = new Question(questionID, answer);
                    questionList.add(question);
                }
                Article article = new Article(articleID, questionList);
                refList.add(article);
            }
        } catch (IOException e) {
            throw new RuntimeException("参考答案JSON文件格式有问题，请检查！");
        }

//        System.out.println("加载参考答案完毕！！！");
    }

    /**
     * 将短语中的英文单词转化为单个字母，
     * 请去掉换行符等无用字符
     *
     * @param str 字符串
     * @return 处理后的字符串
     */
    public static String processString(String str) {
        if (str == null || str.equals("")) {
            return str;
        }
        String sub = str.replaceAll("\\s+", "");
        return sub;
    }

    public static void main(String args[]) {
        File refFile = new File("src/data/my_ref.json");
        File candFile = new File("src/data/my_cand.json");
        ConcurrentHashMap<String, Double> scores = null;
        try {
            scores = Metric.getScore(refFile, candFile);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return;
        }
        if (scores != null) {
            System.out.println("bleu score: " + scores.get(Metric.BLEU_SCORE));
            System.out.println("rouge score: " + scores.get(Metric.ROUGE_SCORE));
        }
//        String str = Metric.processString("123445   ");
//        System.out.println(str);
    }
}

/**
 * Bleu 算法用于评价及其翻译的准确度
 */
class Bleu {
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
        if (candSent == null || candSent.equals("")) {
            return;
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

class RougeL {

    private static double GAMMA;
    private double scoreSum;
    private int scoreSize;

    static {
        Properties prop = new Properties();
        try {
            //读取属性文件
            InputStream in = new BufferedInputStream(new FileInputStream("src/args.properties"));
            prop.load(in);  //加载属性列表
            GAMMA = Double.parseDouble(prop.getProperty("GAMMA"));
//            System.out.println("GAMMA: " + GAMMA);
            in.close();
        } catch (IOException e) {
            e.getStackTrace();
        }
    }

    /**
     * RougeL算法默认赋值gamma=1.2
     */
    public RougeL() {
        this.scoreSum = 0.0;
        this.scoreSize = 0;
    }

    /**
     * 计算str和sub的最长公共子串的大小
     *
     * @param str 字符串1
     * @param sub 字符串2
     * @return 最长公共子串的size
     */
    public int getLCS(String str, String sub) {
        // receive the length of tow string
        int strLength = str.length();
        int subLength = sub.length();
        // initialize length to sort midterm info
        int[][] lengths = new int[strLength + 1][subLength + 1];
        // compute the longest size store in lengths[strLength + 1][subLength + 1]
        for (int i = 1; i < strLength + 1; i++) {
            for (int j = 1; j < subLength + 1; j++) {
                char strChar = str.charAt(i - 1);
                char subChar = sub.charAt(j - 1);
                if (strChar == subChar) {
                    lengths[i][j] = lengths[i - 1][j - 1] + 1;
                } else {
                    lengths[i][j] = Math.max(lengths[i - 1][j], lengths[i][j - 1]);
                }
            }
        }

        return lengths[strLength][subLength];
    }

    /**
     * 计算分数并添加到InstanceScore中去
     * @param candSent 预测答案句子
     * @param refSent 参考答案句子
     */
    public void addInstance(String candSent, String refSent) {
        // 判断是否为空
        if (refSent == null) {
            return;
        } else if (candSent == null) {
            candSent = "";
        }

        // 获取字符串长度
        int candLength = candSent.length();
        int refsLength = refSent.length();

        double score = 0;
        if (candLength > 0 && refsLength > 0) {
//            System.out.println("reference: " + refSent);
            // 计算最大公共子序列表
            int lcs = getLCS(candSent, refSent);
//            System.out.println("LCS: " + lcs);
            // 计算准确率
            double precs = lcs * 1.0 / candLength;
//            System.out.println("P_LCS: " + lcs + " / " + candLength + " = " + precs);
            // 计算召回率
            double recall = lcs * 1.0 / refsLength;
//            System.out.println("R_LCS: " + lcs + " / " + refsLength + " = " + recall);

            // 计算RougeL分数
            score = (1 + Math.pow(GAMMA, 2)) * recall * precs;
//            System.out.println("score = (1 + GAMMA^2)R_lcs*P_lcs");
//            System.out.println("score = (1 + " + GAMMA +"^2) * " + recall + " * " + precs);
//            System.out.println("score = " + score);
            score /= recall + Math.pow(GAMMA, 2) * precs;
//            System.out.println("score /= R_lcs + GAMMA^2 * P_lcs");
//            System.out.println("score /= " + recall + " " + GAMMA + "^2" + " * " + precs);
//            System.out.println("score: " + score);
        } else if (refsLength == 0) {
            return;
        }

        this.scoreSum += score;
        this.scoreSize++;
    }

    /**
     * 计算预测答案的RougeL分数，并返回
     *
     * @return RougeL分数, 如果句子中存在空的字符串返回null
     */
    public double getScore() {
        return this.scoreSum / this.scoreSize;
    }

    public static void main(String[] args) {
        String candidate = "我是孙维松";
        String refrences = "孙维松是我";

        RougeL rougeLScorer = new RougeL();
        int lcs = rougeLScorer.getLCS(candidate, refrences);
        System.out.println("LCS: " + lcs);

        double score = rougeLScorer.getScore();
        System.out.println("RougeL score: " + score);
    }
}

/**
 * 文章对象保存了文章相关信息以及问题列表(使用ArrayList初始化)
 */
class Article {
    private String articleID;
    private String articleType;
    private String articleTitle;
    private String articleContent;
    private List<Question> questions;

    /**
     * 当数据包含于预测结果中使用该构造函数
     *
     * @param articleID    文章ID号码
     * @param questions 问题列表对象
     */
    public Article(String articleID, List<Question> questions) {
        this.articleID = articleID;
        this.questions = questions;
    }

    /**
     * 当数据保存于参考答案中时使用该构造函数
     *
     * @param articleID      文章ID号码
     * @param articleType    文章类型
     * @param articleTitle   文章标题
     * @param articleContent 文章内容
     */
    public Article(String articleID, String articleType, String articleTitle,
                   String articleContent, List<Question> questions) {
        this.articleID = articleID;
        this.articleType = articleType;
        this.articleTitle = articleTitle;
        this.articleContent = articleContent;
        this.questions = questions;
    }

    public String getArticleID() {
        return articleID;
    }

    public String getArticleType() {
        return articleType;
    }

    public String getArticleTitle() {
        return articleTitle;
    }

    public void setArticleID(String articleID) {
        this.articleID = articleID;
    }

    public void setArticleType(String articleType) {
        this.articleType = articleType;
    }

    public void setArticleTitle(String articleTitle) {
        this.articleTitle = articleTitle;
    }

    public void setArticleContent(String articleContent) {
        this.articleContent = articleContent;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public String getArticleContent() {
        return articleContent;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Article article = (Article) o;
        return articleID.equals(article.articleID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(articleID);
    }
}

class Question {
    private String questionID;
    private String questionContent;
    private String answer;
    private String questionType;

    /**
     * 当输入的数据为指标数据的时候时候这个构造函数
     *
     * @param questionID      问题ID号码
     * @param questionContent 问题的具体内容
     * @param answer          问题答案
     * @param questionType    问题类型
     */
    public Question(String questionID, String questionContent, String answer, String questionType) {
        this.questionID = questionID;
        this.questionContent = questionContent;
        this.answer = answer;
        this.questionType = questionType;
    }

    /**
     * 当输入的数据为预测数据时，使用该构造函数
     *
     * @param questionID 问题Id号码
     * @param answer     问题候选答案
     */
    public Question(String questionID, String answer) {
        this.questionID = questionID;
        this.answer = answer;
    }

    public String getQuestionID() {
        return questionID;
    }

    public String getAnswer() {
        return answer;
    }

    public String getQuestionContent() {
        return questionContent;
    }

    public String getQuestionType() {
        return questionType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Question question = (Question) o;
        return questionID.equals(question.questionID);
    }

    @Override
    public int hashCode() {

        return Objects.hash(questionID);
    }
}
