import numpy as np


class RougeL(object):
    def __init__(self, gamma=1.2):
        self.gamma = gamma  # gamma 为常量
        self.inst_scores = []

    def lcs(self, string: str, sub: str) -> int:
        """计算最长公共子序列

        Arguments:
            string {str} -- 字符串
            sub {str} -- 字符串

        Returns:
            int -- 最长公共子序列的长度
        """

        str_length = len(string)
        sub_length = len(sub)

        lengths = np.zeros(((str_length + 1), (sub_length + 1)), dtype=np.int)
        for i in range(1, str_length + 1):
            for j in range(1, sub_length + 1):
                if string[i - 1] == sub[j - 1]:
                    lengths[i][j] = lengths[i - 1][j - 1] + 1
                else:
                    lengths[i][j] = max(lengths[i - 1][j], lengths[i][j - 1])
        return lengths[str_length, sub_length]

    def add_inst(self, cand: str, ref: str):
        """根据参考答案分析出预测答案的分数

        Arguments:
            cand {str} -- 预测答案
            ref {str} -- 参考答案
        """

        basic_lcs = self.lcs(cand, ref)
        p_denom = len(cand)
        r_denom = len(ref)
        prec = basic_lcs / p_denom if p_denom > 0. else 0.
        rec = basic_lcs / r_denom if r_denom > 0. else 0.
        if prec != 0 and rec != 0:
            score = ((1 + self.gamma ** 2) * prec * rec) / \
                float(rec + self.gamma**2 * prec)
        else:
            score = 0
        self.inst_scores.append(score)

    def get_score(self) -> float:
        """计算cand预测数据的RougeL分数

        Returns:
            float -- RougeL分数
        """
        return 1. * sum(self.inst_scores) / len(self.inst_scores)


if __name__ == '__main__':
    print('Hello World')
