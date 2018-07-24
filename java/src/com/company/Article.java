/**
 * 文章对象保存了文章相关信息以及问题列表(使用ArrayList初始化)
 */
public class Article {
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
